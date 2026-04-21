package com.barbershop.service;

import com.barbershop.dto.report.ClientActionDto;
import com.barbershop.dto.report.ContactResultDto;
import com.barbershop.dto.report.InteractionDto;
import com.barbershop.impl.CustomerAnalyticsServiceImpl;
import com.barbershop.model.ClientInteraction;
import com.barbershop.model.Master;
import com.barbershop.model.ServiceEntity;
import com.barbershop.model.Timetable;
import com.barbershop.model.User;
import com.barbershop.repository.ClientInteractionRepository;
import com.barbershop.repository.TimetableRepository;
import com.barbershop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAnalyticsServiceTest {

    @Mock
    private TimetableRepository timetableRepository;

    @Mock
    private ClientInteractionRepository interactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerAnalyticsServiceImpl analyticsService;

    private User testUser;
    private ServiceEntity testService;
    private Master testMaster;

    @BeforeEach
    void setUp() {
        // Подготавливаем базовые тестовые данные
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Иван Тестовый");
        testUser.setUsername("ivan@test.com");

        testService = new ServiceEntity(); // Используем правильный класс сущности
        testService.setName("Стрижка");
        testService.setPrice(150.0);

        testMaster = new Master();
        testMaster.setName("Алексей");
    }

    @Test
    void saveContactResult_ShouldSaveInteraction_WhenUserExists() {

        ContactResultDto dto = new ContactResultDto();
        dto.setStatus("CALLED");
        dto.setNotes("Клиент обещал прийти");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        analyticsService.saveContactResult(1L, dto);

        // Проверяем, что метод save был вызван с правильными параметрами
        ArgumentCaptor<ClientInteraction> captor = ArgumentCaptor.forClass(ClientInteraction.class);
        verify(interactionRepository, times(1)).save(captor.capture());

        ClientInteraction savedInteraction = captor.getValue();
        assertEquals(testUser, savedInteraction.getClient(), "Установлен неверный клиент");
        assertEquals("CALLED", savedInteraction.getStatus(), "Статус не совпадает");
        assertEquals("Клиент обещал прийти", savedInteraction.getNotes(), "Заметки не совпадают");
        assertNotNull(savedInteraction.getInteractionDate(), "Дата взаимодействия не должна быть null");
    }

    @Test
    void saveContactResult_ShouldThrowException_WhenUserNotFound() {

        ContactResultDto dto = new ContactResultDto();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analyticsService.saveContactResult(99L, dto);
        });

        assertEquals("Клиент не найден", exception.getMessage());
        // Убеждаемся, что сохранение в БД даже не пыталось выполниться
        verify(interactionRepository, never()).save(any());
    }

    @Test
    void getRecentInteractions_ShouldMapAndReturnDtos() {

        ClientInteraction interaction = new ClientInteraction();
        interaction.setId(10L);
        interaction.setClient(testUser);
        interaction.setInteractionDate(LocalDateTime.now());
        interaction.setStatus("EMAILED");
        interaction.setNotes("Отправлена скидка");

        when(interactionRepository.findAllByOrderByInteractionDateDesc())
                .thenReturn(Arrays.asList(interaction));

        List<InteractionDto> result = analyticsService.getRecentInteractions();

        assertEquals(1, result.size(), "Размер списка должен быть равен 1");
        InteractionDto dto = result.get(0);
        assertEquals(10L, dto.getId());
        assertEquals("Иван Тестовый", dto.getClientName());
        assertEquals("EMAILED", dto.getStatus());
    }

    @Test
    void getClientActionData_ShouldReturnCorrectRecommendation_ForSingleVisit() {

        Timetable visit = new Timetable();
        visit.setBookedBy(testUser);
        visit.setService(testService);
        visit.setMaster(testMaster);
        visit.setAppointmentTime(LocalDateTime.now().minusDays(10));

        when(timetableRepository.findAllCompletedInPeriod(any(), any()))
                .thenReturn(Arrays.asList(visit));

        ClientActionDto result = analyticsService.getClientActionData(1L);

        assertNotNull(result);
        assertEquals("Иван Тестовый", result.getClientName());
        assertEquals("Стрижка", result.getFavoriteService());
        assertEquals("Алексей", result.getFavoriteMaster());
        assertEquals(150.0, result.getLtv(), "LTV рассчитан неверно");

        // Проверяем, что для клиента с одним визитом выдается нужная базовая рекомендация
        assertEquals("Уточнить качество услуги после первого визита.", result.getRecommendation());
    }
}