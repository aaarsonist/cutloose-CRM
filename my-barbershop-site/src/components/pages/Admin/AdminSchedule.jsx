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
    
    const messages = {
        allDay: 'Весь день',
        previous: 'Назад',
        next: 'Вперед',
        today: 'Сегодня',
        month: 'Месяц',
        week: 'Неделя',
        day: 'День',
        agenda: 'Список',
        date: 'Дата',
        time: 'Время',
        event: 'Событие',
        noEventsInRange: 'На этот период нет записей.',
    };

    if (isLoading) {
        return <div className={styles.loader}>Загрузка графика...</div>;
    }

    return (
        <>
            <div className={styles.calendarContainer}>
                <div className={styles.calendarHeader}>
                    <h3>Календарь записей</h3>
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