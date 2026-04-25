package com.barbershop.impl;

import com.barbershop.dto.report.CustomerKpiDto;
import com.barbershop.model.BookingStatus;
import com.barbershop.model.ClientInteraction;
import com.barbershop.model.Timetable;
import com.barbershop.model.User;
import com.barbershop.repository.ClientInteractionRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.UserRepository;
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
    private final ClientInteractionRepository interactionRepository;
    private final UserRepository userRepository;

    public CustomerAnalyticsServiceImpl(TimetableRepository timetableRepository,
                                        ClientInteractionRepository interactionRepository,
                                        UserRepository userRepository) {
        this.timetableRepository = timetableRepository;
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
    }

    private Map<Long, ClientInteraction> getLatestInteractions() {
        List<ClientInteraction> allInteractions = interactionRepository.findAll();
        Map<Long, ClientInteraction> latest = new HashMap<>();
        for (ClientInteraction ci : allInteractions) {
            Long userId = ci.getClient().getId();
            if (!latest.containsKey(userId) || ci.getInteractionDate().isAfter(latest.get(userId).getInteractionDate())) {
                latest.put(userId, ci);
            }
        }
        return latest;
    }

    private Set<Long> getUsersWithUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        return timetableRepository.findAll().stream()
                .filter(t -> t.getAppointmentTime() != null && t.getAppointmentTime().isAfter(now))
                .filter(t -> t.getStatus() == BookingStatus.BOOKED)
                .filter(t -> t.getBookedBy() != null)
                .map(t -> t.getBookedBy().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public CustomerKpiDto getCustomerKpi(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDateTime prevStart = start.minusDays(daysBetween);
        LocalDateTime prevEnd = end.minusDays(daysBetween);

        List<Timetable> allVisits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), end);
        Map<Long, List<Timetable>> visitsByUser = allVisits.stream()
                .filter(v -> v.getBookedBy() != null && v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getBookedBy().getId()));

        Map<Long, ClientInteraction> latestInteractions = getLatestInteractions();

        Set<Long> upcomingUsers = getUsersWithUpcomingAppointments();

        double currentPeriodRevenue = 0.0, prevPeriodRevenue = 0.0;
        Set<Long> currentPeriodUsers = new HashSet<>(), prevPeriodUsers = new HashSet<>();
        int atRiskCurrent = 0, newClients = 0, activeClients = 0, atRiskSegment = 0, churned = 0;
        int activeAtStartOfPeriod = 0, churnedDuringPeriod = 0;

        for (Map.Entry<Long, List<Timetable>> entry : visitsByUser.entrySet()) {
            Long userId = entry.getKey();
            List<Timetable> userVisits = entry.getValue();
            userVisits.sort(Comparator.comparing(Timetable::getAppointmentTime));

            Timetable lastVisitBeforeEnd = null, lastVisitBeforeStart = null;
            int totalVisitsBeforeEnd = 0;

            for (Timetable v : userVisits) {
                if (v.getAppointmentTime().isBefore(end)) {
                    lastVisitBeforeEnd = v;
                    totalVisitsBeforeEnd++;
                    if (!v.getAppointmentTime().isBefore(start)) {
                        currentPeriodRevenue += v.getService().getPrice();
                        currentPeriodUsers.add(userId);
                    }
                    if (v.getAppointmentTime().isBefore(prevEnd) && !v.getAppointmentTime().isBefore(prevStart)) {
                        prevPeriodRevenue += v.getService().getPrice();
                        prevPeriodUsers.add(userId);
                    }
                }
                if (v.getAppointmentTime().isBefore(start)) lastVisitBeforeStart = v;
            }

            if (lastVisitBeforeStart != null) {
                long daysSinceLastBeforeStart = ChronoUnit.DAYS.between(lastVisitBeforeStart.getAppointmentTime(), start);
                if (daysSinceLastBeforeStart <= 60) {
                    activeAtStartOfPeriod++;
                    if (lastVisitBeforeEnd != null && ChronoUnit.DAYS.between(lastVisitBeforeEnd.getAppointmentTime(), end) > 60) {
                        if (!upcomingUsers.contains(userId)) {
                            churnedDuringPeriod++;
                        }
                    }
                }
            }

            if (lastVisitBeforeEnd != null) {
                ClientInteraction latestAction = latestInteractions.get(userId);
                long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisitBeforeEnd.getAppointmentTime(), end);

                boolean hasUpcoming = upcomingUsers.contains(userId);

                if (hasUpcoming) {
                    if (totalVisitsBeforeEnd == 1) newClients++;
                    else activeClients++;
                } else if (latestAction != null && "CHURNED".equals(latestAction.getStatus())) {
                    churned++;
                } else if (daysSinceLastVisit > 120) {
                    churned++;
                } else if (daysSinceLastVisit > 60) {
                    atRiskSegment++;
                    atRiskCurrent++;
                } else {
                    if (totalVisitsBeforeEnd == 1) newClients++;
                    else activeClients++;
                }
            }
        }

        CustomerKpiDto kpiDto = new CustomerKpiDto();
        double currentLtv = currentPeriodUsers.isEmpty() ? 0.0 : currentPeriodRevenue / currentPeriodUsers.size();
        double prevLtv = prevPeriodUsers.isEmpty() ? 0.0 : prevPeriodRevenue / prevPeriodUsers.size();
        kpiDto.setAverageLtv(Math.round(currentLtv * 100.0) / 100.0);
        kpiDto.setPreviousAverageLtv(Math.round(prevLtv * 100.0) / 100.0);
        kpiDto.setClientsAtRisk(atRiskCurrent);
        kpiDto.setChurnRate(activeAtStartOfPeriod > 0 ? Math.round((((double) churnedDuringPeriod / activeAtStartOfPeriod) * 100.0) * 100.0) / 100.0 : 0.0);

        kpiDto.setNewClients(newClients);
        kpiDto.setActiveClients(activeClients);
        kpiDto.setAtRiskClientsSegment(atRiskSegment);
        kpiDto.setChurnedClients(churned);

        List<String> dates = new ArrayList<>();
        List<Double> ltvDynamics = new ArrayList<>();
        List<Double> churnDynamics = new ArrayList<>();
        int stepDays = (int) Math.max(1, daysBetween / 30);

        for (LocalDate curr = startDate; !curr.isAfter(endDate); curr = curr.plusDays(stepDays)) {
            LocalDateTime currEnd = curr.atTime(23, 59, 59);
            double totalRevUpToDate = 0; int uniqueUsersUpToDate = 0; int riskUsersAtDate = 0;

            for (List<Timetable> userVisits : visitsByUser.values()) {
                Timetable lastV = null;
                double userSpent = 0;
                for (Timetable v : userVisits) {
                    if (v.getAppointmentTime().isBefore(currEnd)) {
                        lastV = v; userSpent += v.getService().getPrice();
                    }
                }
                if (lastV != null) {
                    uniqueUsersUpToDate++; totalRevUpToDate += userSpent;
                    if (ChronoUnit.DAYS.between(lastV.getAppointmentTime(), currEnd) > 60) riskUsersAtDate++;
                }
            }
            dates.add(curr.format(DateTimeFormatter.ISO_DATE));
            ltvDynamics.add(Math.round((uniqueUsersUpToDate > 0 ? totalRevUpToDate / uniqueUsersUpToDate : 0.0) * 100.0) / 100.0);
            churnDynamics.add(Math.round((uniqueUsersUpToDate > 0 ? ((double) riskUsersAtDate / uniqueUsersUpToDate) * 100.0 : 0.0) * 100.0) / 100.0);
        }
        kpiDto.setDynamicsDates(dates);
        kpiDto.setDynamicsLtv(ltvDynamics);
        kpiDto.setDynamicsChurnRate(churnDynamics);

        return kpiDto;
    }

    @Override
    public List<com.barbershop.dto.report.AtRiskClientDto> getAtRiskClients() {
        LocalDateTime now = LocalDateTime.now();
        List<Timetable> allVisits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), now);
        Map<Long, List<Timetable>> visitsByUser = allVisits.stream()
                .filter(v -> v.getBookedBy() != null && v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getBookedBy().getId()));

        Map<Long, ClientInteraction> latestInteractions = getLatestInteractions();
        Set<Long> upcomingUsers = getUsersWithUpcomingAppointments();

        List<com.barbershop.dto.report.AtRiskClientDto> allClientsTable = new ArrayList<>();

        for (Map.Entry<Long, List<Timetable>> entry : visitsByUser.entrySet()) {
            Long userId = entry.getKey();
            ClientInteraction latestAction = latestInteractions.get(userId);

            if (latestAction != null && "CHURNED".equals(latestAction.getStatus())) {
                continue;
            }

            List<Timetable> userVisits = entry.getValue();
            userVisits.sort(Comparator.comparing(Timetable::getAppointmentTime));
            Timetable lastVisit = userVisits.get(userVisits.size() - 1);
            long daysSinceLastVisit = ChronoUnit.DAYS.between(lastVisit.getAppointmentTime(), now);

            double ltv = userVisits.stream().mapToDouble(v -> v.getService().getPrice()).sum();

            int probability;
            if (upcomingUsers.contains(userId)) {
                probability = 0;
            } else if (daysSinceLastVisit > 60) {
                probability = (int) Math.min(99, 50 + (daysSinceLastVisit - 60) * 0.8);
            } else {
                probability = (int) Math.max(0, (daysSinceLastVisit / 60.0) * 49);
            }

            com.barbershop.dto.report.AtRiskClientDto dto = new com.barbershop.dto.report.AtRiskClientDto();
            dto.setClientId(userId);
            dto.setClientName(lastVisit.getBookedBy().getName() != null ? lastVisit.getBookedBy().getName() : "Клиент #" + userId);
            dto.setLastVisitDate(lastVisit.getAppointmentTime());
            dto.setLtv(Math.round(ltv * 100.0) / 100.0);
            dto.setChurnProbability(probability);

            if (latestAction != null) {
                dto.setLastContactDate(latestAction.getInteractionDate());
                dto.setLastContactStatus(latestAction.getStatus());
            }
            allClientsTable.add(dto);
        }

        allClientsTable.sort(Comparator.comparing(com.barbershop.dto.report.AtRiskClientDto::getChurnProbability).reversed()
                .thenComparing(Comparator.comparing(com.barbershop.dto.report.AtRiskClientDto::getLtv).reversed()));
        return allClientsTable;
    }

    @Override
    public List<com.barbershop.dto.report.VisitHistoryDto> getClientHistory(Long clientId) {
        List<Timetable> visits = timetableRepository.findAllCompletedInPeriod(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now())
                .stream().filter(t -> t.getBookedBy() != null && t.getBookedBy().getId().equals(clientId))
                .sorted(Comparator.comparing(Timetable::getAppointmentTime).reversed())
                .collect(Collectors.toList());

        return visits.stream().map(v -> {
            com.barbershop.dto.report.VisitHistoryDto dto = new com.barbershop.dto.report.VisitHistoryDto();
            dto.setDate(v.getAppointmentTime());
            dto.setServiceName(v.getService().getName());
            dto.setMasterName(v.getMaster().getName());
            dto.setPrice(v.getService().getPrice());

            if (v.getReviews() != null && !v.getReviews().isEmpty()) {
                com.barbershop.model.Review review = v.getReviews().get(0);
                dto.setReviewRating(review.getRating());
                dto.setReviewText(review.getReviewText());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void saveContactResult(Long clientId, com.barbershop.dto.report.ContactResultDto resultDto) {
        User client = userRepository.findById(clientId).orElseThrow(() -> new RuntimeException("Клиент не найден"));

        ClientInteraction interaction = new ClientInteraction();
        interaction.setClient(client);
        interaction.setInteractionDate(LocalDateTime.now());
        interaction.setStatus(resultDto.getStatus());
        interaction.setNotes(resultDto.getNotes());

        interactionRepository.save(interaction);
    }

    @Override
    public List<com.barbershop.dto.report.InteractionDto> getRecentInteractions() {
        return interactionRepository.findAllByOrderByInteractionDateDesc().stream()
                .limit(50)
                .map(ci -> {
                    com.barbershop.dto.report.InteractionDto dto = new com.barbershop.dto.report.InteractionDto();
                    dto.setId(ci.getId());
                    dto.setClientName(ci.getClient().getName() != null ? ci.getClient().getName() : "Клиент #" + ci.getClient().getId());
                    dto.setDate(ci.getInteractionDate());
                    dto.setStatus(ci.getStatus());
                    dto.setNotes(ci.getNotes());
                    return dto;
                }).collect(Collectors.toList());
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
        String contactInfo = client.getUsername() != null ? client.getUsername() : "Нет контактов";

        double ltv = clientVisits.stream().mapToDouble(v -> v.getService().getPrice()).sum();

        Map<String, Long> masterCounts = clientVisits.stream()
                .filter(v -> v.getMaster() != null)
                .collect(Collectors.groupingBy(v -> v.getMaster().getName(), Collectors.counting()));
        String favMaster = masterCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Любой мастер");

        Map<String, Long> serviceCounts = clientVisits.stream()
                .filter(v -> v.getService() != null)
                .collect(Collectors.groupingBy(v -> v.getService().getName(), Collectors.counting()));
        String favService = serviceCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("Любая услуга");

        Set<Long> upcomingUsers = getUsersWithUpcomingAppointments();
        boolean hasUpcoming = upcomingUsers.contains(clientId);

        String recommendation = "";
        Timetable lastVisit = clientVisits.get(clientVisits.size() - 1);
        long recency = ChronoUnit.DAYS.between(lastVisit.getAppointmentTime(), now);

        if (hasUpcoming) {
            recommendation = "У клиента уже есть предстоящая запись. Дополнительных действий не требуется.";
        } else if (clientVisits.size() == 1) {
            recommendation = "Уточнить качество услуги после первого визита.";
        } else {
            long totalDaysSinceFirstVisit = ChronoUnit.DAYS.between(clientVisits.get(0).getAppointmentTime(), lastVisit.getAppointmentTime());
            double n = totalDaysSinceFirstVisit > 0 ? (double) totalDaysSinceFirstVisit / (clientVisits.size() - 1) : 30.0;
            boolean isAtRisk = recency > (n * 1.5) || recency > 60;
            double HIGH_LTV_THRESHOLD = 200.0;

            if (isAtRisk) {
                if (ltv >= HIGH_LTV_THRESHOLD) {
                    recommendation = String.format("Связаться с клиентом. Предложить персональную скидку на %s у мастера %s.", favService, favMaster);
                } else {
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