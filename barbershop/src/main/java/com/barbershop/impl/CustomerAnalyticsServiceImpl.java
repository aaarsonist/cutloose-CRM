package com.barbershop.impl;

import com.barbershop.dto.report.CustomerKpiDto;
import com.barbershop.model.Timetable;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.service.CustomerAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerAnalyticsServiceImpl implements CustomerAnalyticsService {

    private final TimetableRepository timetableRepository;

    public CustomerAnalyticsServiceImpl(TimetableRepository timetableRepository) {
        this.timetableRepository = timetableRepository;
    }

    @Override
    public CustomerKpiDto getCustomerKpi(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStart = start.minusDays(daysBetween);
        LocalDateTime prevEnd = end.minusDays(daysBetween);

        // 1. Выгружаем всю историю визитов ДО конца периода (для точной истории каждого клиента)
        List<Timetable> allVisits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), end);

        // Группируем по клиентам (Используем getBookedBy())
        Map<Long, List<Timetable>> visitsByUser = allVisits.stream()
                .filter(v -> v.getBookedBy() != null && v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getBookedBy().getId()));

        double currentPeriodRevenue = 0.0;
        Set<Long> currentPeriodUsers = new HashSet<>();
        double prevPeriodRevenue = 0.0;
        Set<Long> prevPeriodUsers = new HashSet<>();

        double retainedRev = 0.0;
        int atRiskCurrent = 0;

        int newClients = 0, activeClients = 0, atRiskSegment = 0, churned = 0;

        for (List<Timetable> userVisits : visitsByUser.values()) {
            // Сортируем по getAppointmentTime()
            userVisits.sort(Comparator.comparing(Timetable::getAppointmentTime));

            Timetable lastVisitBeforeEnd = null;
            int totalVisitsBeforeEnd = 0;
            boolean userRetainedInPeriod = false;
            Timetable prevV = null;

            for (Timetable v : userVisits) {
                if (v.getAppointmentTime().isBefore(end)) {
                    lastVisitBeforeEnd = v;
                    totalVisitsBeforeEnd++;

                    // Сбор данных для текущего периода
                    if (!v.getAppointmentTime().isBefore(start)) {
                        currentPeriodRevenue += v.getService().getPrice();
                        currentPeriodUsers.add(v.getBookedBy().getId());

                        // Логика удержания: вернулся после паузы > 60 дней
                        if (prevV != null && ChronoUnit.DAYS.between(prevV.getAppointmentTime(), v.getAppointmentTime()) > 60) {
                            userRetainedInPeriod = true;
                        }
                        if (userRetainedInPeriod) {
                            retainedRev += v.getService().getPrice();
                        }
                    }

                    // Сбор данных для прошлого периода
                    if (v.getAppointmentTime().isBefore(prevEnd) && !v.getAppointmentTime().isBefore(prevStart)) {
                        prevPeriodRevenue += v.getService().getPrice();
                        prevPeriodUsers.add(v.getBookedBy().getId());
                    }
                }
                prevV = v;
            }

            // Сегментация базы (На момент endDate)
            if (lastVisitBeforeEnd != null) {
                long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisitBeforeEnd.getAppointmentTime(), end);

                if (daysSinceLastVisit > 120) {
                    churned++;
                } else if (daysSinceLastVisit > 60) {
                    atRiskSegment++;
                    atRiskCurrent++; // Для KPI
                } else {
                    if (totalVisitsBeforeEnd == 1) {
                        newClients++;
                    } else {
                        activeClients++;
                    }
                }
            }
        }

        CustomerKpiDto kpiDto = new CustomerKpiDto();
        double currentLtv = currentPeriodUsers.isEmpty() ? 0.0 : currentPeriodRevenue / currentPeriodUsers.size();
        double prevLtv = prevPeriodUsers.isEmpty() ? 0.0 : prevPeriodRevenue / prevPeriodUsers.size();

        kpiDto.setAverageLtv(Math.round(currentLtv * 100.0) / 100.0);
        kpiDto.setPreviousAverageLtv(Math.round(prevLtv * 100.0) / 100.0);
        kpiDto.setClientsAtRisk(atRiskCurrent);
        kpiDto.setRetainedRevenue(Math.round(retainedRev * 100.0) / 100.0);

        kpiDto.setNewClients(newClients);
        kpiDto.setActiveClients(activeClients);
        kpiDto.setAtRiskClientsSegment(atRiskSegment);
        kpiDto.setChurnedClients(churned);

        // 3. Формируем Динамику графиков (Шаг построения)
        List<String> dates = new ArrayList<>();
        List<Double> ltvDynamics = new ArrayList<>();
        List<Double> churnDynamics = new ArrayList<>();

        // Чтобы график не вис на больших дистанциях, берем макс 30 точек
        int stepDays = (int) Math.max(1, daysBetween / 30);

        for (LocalDate curr = startDate; !curr.isAfter(endDate); curr = curr.plusDays(stepDays)) {
            LocalDateTime currEnd = curr.atTime(23, 59, 59);
            double totalRevUpToDate = 0;
            int uniqueUsersUpToDate = 0;
            int riskUsersAtDate = 0;

            for (List<Timetable> userVisits : visitsByUser.values()) {
                Timetable lastV = null;
                double userSpent = 0;
                for (Timetable v : userVisits) {
                    if (v.getAppointmentTime().isBefore(currEnd)) {
                        lastV = v;
                        userSpent += v.getService().getPrice();
                    }
                }
                if (lastV != null) {
                    uniqueUsersUpToDate++;
                    totalRevUpToDate += userSpent;
                    if (ChronoUnit.DAYS.between(lastV.getAppointmentTime(), currEnd) > 60) {
                        riskUsersAtDate++;
                    }
                }
            }

            double dLtv = uniqueUsersUpToDate > 0 ? totalRevUpToDate / uniqueUsersUpToDate : 0.0;
            double dChurn = uniqueUsersUpToDate > 0 ? ((double) riskUsersAtDate / uniqueUsersUpToDate) * 100.0 : 0.0;

            dates.add(curr.format(DateTimeFormatter.ISO_DATE));
            ltvDynamics.add(Math.round(dLtv * 100.0) / 100.0);
            churnDynamics.add(Math.round(dChurn * 100.0) / 100.0);
        }

        kpiDto.setDynamicsDates(dates);
        kpiDto.setDynamicsLtv(ltvDynamics);
        kpiDto.setDynamicsChurnRate(churnDynamics);

        return kpiDto;
    }
    @Override
    public List<com.barbershop.dto.report.AtRiskClientDto> getAtRiskClients() {
        LocalDateTime now = LocalDateTime.now();
        // Забираем всю историю визитов
        List<Timetable> allVisits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), now);

        Map<Long, List<Timetable>> visitsByUser = allVisits.stream()
                .filter(v -> v.getBookedBy() != null && v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getBookedBy().getId()));

        List<com.barbershop.dto.report.AtRiskClientDto> atRiskClients = new ArrayList<>();

        for (Map.Entry<Long, List<Timetable>> entry : visitsByUser.entrySet()) {
            List<Timetable> userVisits = entry.getValue();
            userVisits.sort(Comparator.comparing(Timetable::getAppointmentTime));

            Timetable lastVisit = userVisits.get(userVisits.size() - 1);
            long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisit.getAppointmentTime(), now);

            // Попадают в зону риска только те, кто не был от 60 до 365 дней (исключаем "мертвые" души, которые не ходят годами)
            if (daysSinceLastVisit > 60 && daysSinceLastVisit <= 365) {
                double ltv = userVisits.stream().mapToDouble(v -> v.getService().getPrice()).sum();

                // Рассчитываем вероятность оттока (60 дней = 50%, 120+ дней = 99%)
                int probability = (int) Math.min(99, 50 + (daysSinceLastVisit - 60) * 0.8);

                com.barbershop.dto.report.AtRiskClientDto dto = new com.barbershop.dto.report.AtRiskClientDto();
                dto.setClientId(entry.getKey());

                // Если в User другое поле (например getFirstName), замени getName() на него
                String name = lastVisit.getBookedBy().getName() != null ? lastVisit.getBookedBy().getName() : "Клиент #" + entry.getKey();
                dto.setClientName(name);

                dto.setLastVisitDate(lastVisit.getAppointmentTime());
                dto.setLtv(Math.round(ltv * 100.0) / 100.0);
                dto.setChurnProbability(probability);

                atRiskClients.add(dto);
            }
        }

        // Сортировка: Сначала самые ценные (по LTV), затем по вероятности оттока
        atRiskClients.sort(Comparator.comparing(com.barbershop.dto.report.AtRiskClientDto::getLtv).reversed()
                .thenComparing(Comparator.comparing(com.barbershop.dto.report.AtRiskClientDto::getChurnProbability).reversed()));

        return atRiskClients;
    }
    @Override
    public com.barbershop.dto.report.ClientActionDto getClientActionData(Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        List<Timetable> allVisits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), now);

        List<Timetable> clientVisits = allVisits.stream()
                .filter(v -> v.getBookedBy() != null && v.getBookedBy().getId().equals(clientId))
                .sorted(Comparator.comparing(Timetable::getAppointmentTime))
                .collect(Collectors.toList());

        if (clientVisits.isEmpty()) {
            throw new RuntimeException("Клиент не найден или нет завершенных визитов");
        }

        com.barbershop.model.User client = clientVisits.get(0).getBookedBy();
        String name = client.getName() != null ? client.getName() : "Клиент #" + clientId;
        String contactInfo = client.getUsername() != null ? client.getUsername() :
                (client.getUsername() != null ? client.getUsername() : "Нет контактов");

        double ltv = clientVisits.stream().mapToDouble(v -> v.getService().getPrice()).sum();

        Map<String, Long> masterCounts = clientVisits.stream()
                .filter(v -> v.getMaster() != null)
                .collect(Collectors.groupingBy(v -> v.getMaster().getName(), Collectors.counting()));
        String favMaster = masterCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Любой мастер");

        Map<String, Long> serviceCounts = clientVisits.stream()
                .filter(v -> v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getService().getName(), Collectors.counting()));
        String favService = serviceCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Любая услуга");

        // --- ОБНОВЛЕННАЯ ЛОГИКА ТЕКСТА ---
        String recommendation = "";
        Timetable lastVisit = clientVisits.get(clientVisits.size() - 1);
        long recency = ChronoUnit.DAYS.between(lastVisit.getAppointmentTime(), now);

        if (clientVisits.size() == 1) {
            recommendation = "Уточнить качество услуги после первого визита.";
        } else {
            Timetable firstVisit = clientVisits.get(0);
            long totalDaysSinceFirstVisit = ChronoUnit.DAYS.between(firstVisit.getAppointmentTime(), lastVisit.getAppointmentTime());

            double n = totalDaysSinceFirstVisit > 0 ? (double) totalDaysSinceFirstVisit / (clientVisits.size() - 1) : 30.0;
            boolean isAtRisk = recency > (n * 1.5) || recency > 60;
            double HIGH_LTV_THRESHOLD = 200.0;

            if (isAtRisk) {
                if (ltv >= HIGH_LTV_THRESHOLD) {
                    // Убраны скобки, размер скидки и слова про VIP/звонок
                    recommendation = String.format("Связаться с клиентом. Предложить персональную скидку на %s у мастера %s.", favService, favMaster);
                } else {
                    // Убраны проценты и статус
                    recommendation = "Отправить сообщение в мессенджер с напоминанием о визите и небольшим бонусом.";
                }
            } else {
                recommendation = "Клиент пока не в зоне риска. Можно предложить запись на следующую процедуру.";
            }
        }

        com.barbershop.dto.report.ClientActionDto dto = new com.barbershop.dto.report.ClientActionDto();
        dto.setClientId(clientId);
        dto.setClientName(name);
        dto.setContactInfo(contactInfo);
        dto.setFavoriteMaster(favMaster);
        dto.setFavoriteService(favService);
        dto.setRecommendation(recommendation);
        dto.setLtv(Math.round(ltv * 100.0) / 100.0);

        return dto;
    }
}