import React, { useEffect, useState, useCallback } from 'react'; 
import api from '../../../api/api';
import styles from './AdminDashboard.module.css'; 

import { toast } from 'react-toastify';

import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import 'moment/locale/ru'; 

import ScheduleEditModal from './ScheduleEditModal'; 
import AppointmentInfoModal from './AppointmentInfoModal'; 
import AdminBookingModal from './AdminBookingModal';

moment.locale('ru');
const localizer = momentLocalizer(moment);

const DAYS_OF_WEEK = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const DAY_NAMES_RU = {
    MONDAY: "Пн", TUESDAY: "Вт", WEDNESDAY: "Ср", THURSDAY: "Чт", FRIDAY: "Пт", SATURDAY: "Сб", SUNDAY: "Вс"
};
const formatTime = (timeString) => { 
    if (typeof timeString === 'string' && timeString.length >= 5) {
        return timeString.substring(0, 5);
    }
    return timeString;
};

function AdminSchedule() {
    const [masters, setMasters] = useState([]);
    const [schedules, setSchedules] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [editingSchedule, setEditingSchedule] = useState(null); 

    const [appointments, setAppointments] = useState([]); 
    const [isAppointmentModalOpen, setIsAppointmentModalOpen] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [isAdminBookingModalOpen, setIsAdminBookingModalOpen] = useState(false);

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const [mastersRes, schedulesRes, appointmentsRes] = await Promise.all([
                api.get('/api/masters'),
                api.get('/api/work-schedule/all'),
                api.get('/api/timetable') 
            ]);
            
            setMasters(mastersRes.data);
            setSchedules(schedulesRes.data);
            
            const calendarEvents = appointmentsRes.data.map(event => {
                const localStartStr = event.start.replace('T', ' ');
                const localEndStr = event.end.replace('T', ' ');

                return {
                    ...event,
                    start: new Date(localStartStr),
                    end: new Date(localEndStr),
                };
            });
            setAppointments(calendarEvents);
            
        } catch (error) {
            console.error("Ошибка при загрузке данных графика:", error);
            toast.error("Ошибка при загрузке данных графика.");
        } finally {
            setIsLoading(false);
        }
    }, []); 

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const findScheduleEntry = (masterId, dayOfWeek) => { 
        return schedules.find(s => s.master.id === masterId && s.dayOfWeek === dayOfWeek);
    };

    const handleCellClick = (master, dayOfWeek) => { 
        const entry = findScheduleEntry(master.id, dayOfWeek);
        if (entry) {
            setEditingSchedule(entry);
        } else {
            setEditingSchedule({
                master: master, dayOfWeek: dayOfWeek, startTime: null, endTime: null
            });
        }
        setIsScheduleModalOpen(true);
    };
    
    const handleSaveSchedule = async (updatedEntry) => { 
        try {
            const response = await api.post('/api/work-schedule', updatedEntry);
            const savedData = response.data; 

            setSchedules(prevSchedules => {
                const existingIndex = prevSchedules.findIndex(
                    s => s.master.id === updatedEntry.master.id && s.dayOfWeek === updatedEntry.dayOfWeek
                );
                
                if (savedData) { 
                    if (existingIndex > -1) {
                        const newSchedules = [...prevSchedules];
                        newSchedules[existingIndex] = savedData;
                        return newSchedules;
                    } else {
                        return [...prevSchedules, savedData];
                    }
                } else { 
                    if (existingIndex > -1) {
                        return prevSchedules.filter((_, index) => index !== existingIndex);
                    }
                    return prevSchedules; 
                }
            });
            toast.success('График успешно сохранен!'); 
            setIsScheduleModalOpen(false)
        } catch (error) {
            console.error("Ошибка при сохранении графика:", error);
            let errorMessage = "Не удалось сохранить график.";
            
            if (error.response && error.response.data) {
                if (error.response.data.message) {
                    errorMessage = error.response.data.message;
                } 
                else if (typeof error.response.data === 'string') {
                    errorMessage = error.response.data;
                }
            }

            toast.error(errorMessage); 
        }
    };

    const handleSelectEvent = (event) => {
        setSelectedAppointment(event); 
        setIsAppointmentModalOpen(true);
    };

    const handleDeleteAppointment = (id) => {
        const ConfirmationToast = ({ closeToast }) => {
            const confirmAction = async () => {
                try {
                    await api.delete(`/api/timetable/admin/${id}`);
                    
                    setAppointments(prev => prev.filter(app => app.id !== id));
                    setIsAppointmentModalOpen(false);
                    setSelectedAppointment(null);
                    
                    toast.success('Запись успешно отменена.'); 

                } catch (error) {
                    console.error("Ошибка при отмене записи:", error);
                    if (error.response && error.response.status === 400) {
                        toast.error("Не удалось отменить: эта запись уже в прошлом.");
                    } else {
                        toast.error("Не удалось отменить запись.");
                    }
                }
                closeToast();
            };
            const cancelAction = () => closeToast();

            return (
                <div>
                    <p style={{ margin: 0, marginBottom: '10px', fontSize: '1rem' }}>
                        Вы уверены, что хотите отменить эту запись?
                    </p>
                    <button onClick={confirmAction} className={styles.toastConfirmButton}>
                        Да, отменить
                    </button>
                    <button onClick={cancelAction} className={styles.toastCancelButton}>
                        Нет
                    </button>
                </div>
            );
        };
        
        toast.warn(<ConfirmationToast />, {
            position: "top-right", 
            autoClose: false, closeOnClick: false, draggable: false,
            closeButton: false, theme: "light",
            className: styles.confirmationToastBody
        });
    };

    const handleAdminBookingSave = async (bookingRequest) => {
        try {
            await api.post('/api/timetable/admin/book', bookingRequest);
            toast.success('Клиент успешно записан!');
            setIsAdminBookingModalOpen(false); 
            fetchData(); 
        } catch (error) {
            console.error("Ошибка при создании записи:", error);
            toast.error("Не удалось создать запись. Возможно, слот уже занят.");
        }
    };
    
    // --- АЛГОРИТМ РАСЧЕТА ТЕПЛОВОЙ КАРТЫ ДЛЯ КАЛЕНДАРЯ ---
    const getDayWorkloadStyle = (date) => {
        if (!schedules.length) return {};

        // 1. Определяем день недели для сопоставления с шаблонами графиков мастеров
        const jsDayToDayName = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
        const dayOfWeekStr = jsDayToDayName[date.getDay()];

        // 2. Считаем ПРЕДЛОЖЕНИЕ (сумма часов всех мастеров, работающих в этот день недели)
        const daySchedules = schedules.filter(s => s.dayOfWeek === dayOfWeekStr);
        let capacityHours = 0;
        daySchedules.forEach(ws => {
            if (ws.startTime && ws.endTime) {
                const start = parseInt(ws.startTime.split(':')[0], 10);
                const end = parseInt(ws.endTime.split(':')[0], 10);
                if (!isNaN(start) && !isNaN(end)) {
                    capacityHours += (end - start);
                }
            }
        });

        // 3. Считаем СПРОС (количество записей на эту конкретную дату)
        const dayAppointments = appointments.filter(app => {
            if (!app.start) return false;
            return app.start.getFullYear() === date.getFullYear() &&
                   app.start.getMonth() === date.getMonth() &&
                   app.start.getDate() === date.getDate();
        });

        const bookedHours = dayAppointments.length;

        // 4. РАСКРАСКА ДНЯ
        if (capacityHours === 0) {
            return {
                style: { backgroundColor: '#f9f9f9' },
                title: 'Выходной (нет мастеров)'
            };
        }

        const loadPercentage = (bookedHours / capacityHours) * 100;
        let bgColor = '';

        if (loadPercentage < 30) bgColor = '#e0f2fe';      // Голубой (Свободно)
        else if (loadPercentage < 60) bgColor = '#dcfce7'; // Зеленый (Оптимально)
        else if (loadPercentage < 85) bgColor = '#ffedd5'; // Оранжевый (Плотно)
        else bgColor = '#fee2e2';                          // Красный (Перегруз)

        return {
            style: { backgroundColor: bgColor },
            title: `Загрузка: ${Math.round(loadPercentage)}% (${bookedHours} из ${capacityHours} ч.)`
        };
    };

    const messages = {
        allDay: 'Весь день', previous: 'Назад', next: 'Вперед',
        today: 'Сегодня', month: 'Месяц', week: 'Неделя',
        day: 'День', agenda: 'Список', date: 'Дата',
        time: 'Время', event: 'Событие', noEventsInRange: 'На этот период нет записей.',
    };

    if (isLoading) {
        return <div className={styles.loader}>Загрузка графика...</div>;
    }

    return (
        <>
            <div className={styles.calendarContainer}>
                <div className={styles.calendarHeader}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flexWrap: 'wrap' }}>
                        <h3 style={{ margin: 0, border: 'none', paddingBottom: 0 }}>Календарь записей</h3>
                        
                        <div style={{ 
                            display: 'flex', alignItems: 'center', gap: '15px', 
                            fontSize: '0.85rem', color: '#4b5563', fontWeight: '500'
                        }}>
                            <span style={{ fontWeight: '600', color: '#333' }}>День по загрузке:</span>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <div style={{ width: '14px', height: '14px', backgroundColor: '#e0f2fe', border: '1px solid #bae6fd', borderRadius: '4px' }}></div>
                                <span>&lt;30% (Свободно)</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <div style={{ width: '14px', height: '14px', backgroundColor: '#dcfce7', border: '1px solid #bbf7d0', borderRadius: '4px' }}></div>
                                <span>30-60% (Норма)</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <div style={{ width: '14px', height: '14px', backgroundColor: '#ffedd5', border: '1px solid #fed7aa', borderRadius: '4px' }}></div>
                                <span>60-85% (Плотно)</span>
                            </div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <div style={{ width: '14px', height: '14px', backgroundColor: '#fee2e2', border: '1px solid #fecaca', borderRadius: '4px' }}></div>
                                <span>&gt;85% (Перегруз)</span>
                            </div>
                        </div>
                    </div>

                    <button 
                        className={styles.bookClientButton}
                        onClick={() => setIsAdminBookingModalOpen(true)}
                    >
                        + Записать клиента
                    </button>
                </div>
                
                <Calendar
                    localizer={localizer}
                    events={appointments}
                    startAccessor="start"
                    endAccessor="end"
                    style={{ height: 600 }} 
                    messages={messages} 
                    onSelectEvent={handleSelectEvent} 
                    defaultView="week" 
                    views={['month', 'week', 'day']} 
                    step={30} 
                    timeslots={2} 
                    min={new Date(0, 0, 0, 9, 0, 0)} 
                    max={new Date(0, 0, 0, 21, 0, 0)} 
                    dayPropGetter={getDayWorkloadStyle} 
                />
            </div>

            <div className={styles.scheduleContainer}>
                <h3>График работы мастеров</h3>
                <table className={styles.scheduleGrid}>
                    <thead>
                        <tr>
                            <th>Мастер</th>
                            {DAYS_OF_WEEK.map(day => (
                                <th key={day} className={(day === "SATURDAY" || day === "SUNDAY") ? styles.isWeekend : ""}>
                                    {DAY_NAMES_RU[day]}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {masters.map(master => (
                            <tr key={master.id}>
                                <td className={styles.masterNameCell}>{master.name}</td>
                                {DAYS_OF_WEEK.map(day => {
                                    const entry = findScheduleEntry(master.id, day);
                                    const isOff = !entry || !entry.startTime;
                                    return (
                                        <td 
                                            key={day} 
                                            className={`${styles.scheduleCell} ${isOff ? styles.isOff : ''} ${(day === "SATURDAY" || day === "SUNDAY") ? styles.isWeekend : ""}`}
                                            onClick={() => handleCellClick(master, day)}
                                        >
                                            {isOff ? "Выходной" : `${formatTime(entry.startTime)} - ${formatTime(entry.endTime)}`}
                                        </td>
                                    );
                                })}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            
            <ScheduleEditModal
                isOpen={isScheduleModalOpen}
                onClose={() => setIsScheduleModalOpen(false)}
                onSave={handleSaveSchedule}
                scheduleData={editingSchedule}
                masterName={editingSchedule?.master?.name}
            />
            
            <AppointmentInfoModal
                isOpen={isAppointmentModalOpen}
                onClose={() => setIsAppointmentModalOpen(false)}
                onDelete={handleDeleteAppointment}
                event={selectedAppointment}
            />

            <AdminBookingModal
                isOpen={isAdminBookingModalOpen}
                onClose={() => setIsAdminBookingModalOpen(false)}
                onSave={handleAdminBookingSave}
            />
        </>
    );
}

export default AdminSchedule;