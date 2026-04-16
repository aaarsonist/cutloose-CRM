import React, { useState, useEffect} from "react";
import axios from "axios";
import api from '../../../api/api';
import styles from './UserDashboard.module.css';
import { ToastContainer, toast } from 'react-toastify';

function UserDashboard() {
  // Стейты для услуг и мастеров
  const [services, setServices] = useState([]);
  const [masters, setMasters] = useState([]);
  
  // Стейты для формы бронирования
  const [selectedService, setSelectedService] = useState('');
  const [selectedMaster, setSelectedMaster] = useState('');
  const [selectedDate, setSelectedDate] = useState(''); 
  const [availableSlots, setAvailableSlots] = useState([]); 
  const [selectedSlot, setSelectedSlot] = useState(''); 
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [appointmentToEdit, setAppointmentToEdit] = useState(null);

  // Стейты для формы отзыва
  const [reviewText, setReviewText] = useState('');
  const [rating, setRating] = useState(0); 
  const [hoverRating, setHoverRating] = useState(0);
  const [appointmentToReview, setAppointmentToReview] = useState(null);

  // Стейты данных пользователя
  const [userName, setUserName] = useState('');
  const [completedAppointments, setCompletedAppointments] = useState([]);
  const [upcomingAppointments, setUpcomingAppointments] = useState([]);
  const [userReviews, setUserReviews] = useState([]);

  // Стейты интерфейса (Вкладки и Модалки)
  const [activeTab, setActiveTab] = useState('upcoming'); // 'upcoming' | 'past'
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);

  useEffect(() => {
    fetchServices();
    fetchMasters();
    fetchCompletedAppointments();
    fetchUpcomingAppointments();
    fetchUserReviews();

    const userString = localStorage.getItem('currentUser');
    if (userString) {
        const user = JSON.parse(userString);
        setUserName(user.name || user.username);
    }
  }, []);

  const getTime = (app) => app.appointmentTime || app.start;

  useEffect(() => {
    if (selectedService && selectedMaster && selectedDate) {
      fetchAvailableSlots();
    }
    setAvailableSlots([]);
    setSelectedSlot('');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedService, selectedMaster, selectedDate]);

  const fetchServices = async () => {
    try {
        const response = await axios.get('http://localhost:8080/services');
        setServices(response.data);
    } catch (error) {
        console.error('Ошибка при загрузке услуг:', error);
    }
  };

  const fetchMasters = async () => {
    try {
      const response = await api.get('/api/masters'); 
      setMasters(response.data);
    } catch (error) {
      console.error('Ошибка при загрузке мастеров:', error);
    }
  };

  const fetchUserReviews = async () => {
      try {
          const response = await api.get('/api/reviews');
          setUserReviews(response.data);
      } catch (error) {
          console.error('Ошибка при загрузке отзывов:', error);
      }
  };

  const fetchAvailableSlots = async () => {
    setIsLoadingSlots(true);
    try {
      const response = await api.get(`/api/masters/${selectedMaster}/availability`, {
        params: { serviceId: selectedService, date: selectedDate }
      });
      setAvailableSlots(response.data);
    } catch (error) {
      console.error('Ошибка при загрузке слотов:', error);
      toast.error("Не удалось загрузить доступные слоты.");
      setAvailableSlots([]);
    } finally {
      setIsLoadingSlots(false);
    }
  };

  const fetchCompletedAppointments = async () => {
    try {
        const response = await api.get('/api/timetable/user/completed');
        // Сортируем от самых новых к старым
        const sorted = response.data.sort((a, b) => new Date(b.appointmentTime) - new Date(a.appointmentTime));
        setCompletedAppointments(sorted);
    } catch (error) {
        console.error('Ошибка при загрузке прошедших записей:', error);
    }
  };

  const fetchUpcomingAppointments = async () => {
    try {
        const response = await api.get('/api/timetable/user/upcoming');
        const sorted = response.data.sort((a, b) => new Date(a.appointmentTime) - new Date(b.appointmentTime));
        setUpcomingAppointments(sorted);
    } catch (error) {
        console.error('Ошибка при загрузке предстоящих записей:', error);
    }
  };

  const handleBooking = () => {
    if (!selectedService || !selectedMaster || !selectedDate || !selectedSlot) {
      toast.error("Выберите услугу, мастера, дату и свободное время!");
      return;
    }
    
    const finalAppointmentTime = `${selectedDate}T${selectedSlot}`;
    const appointmentData = {
      service: { id: selectedService },
      master: { id: selectedMaster },
      appointmentTime: finalAppointmentTime, 
    };

    api.post('/api/timetable', appointmentData) 
      .then(() => {
        toast.success("Вы успешно забронировали услугу!");
        setSelectedService('');
        setSelectedMaster(''); 
        setSelectedDate('');
        setSelectedSlot('');
        setAvailableSlots([]);
        setIsBookingModalOpen(false); // Закрываем модалку
        fetchUpcomingAppointments();
      })
      .catch(error => {
        console.error("Ошибка при бронировании:", error);
        toast.error("Ошибка при бронировании. Возможно, этот слот только что заняли.");
        fetchAvailableSlots(); 
      });
  };

  const handleReviewSubmit = async () => {
    if (!appointmentToReview || rating < 1) { 
      toast.error("Пожалуйста, поставьте оценку!");
      return;
    }

    const reviewData = {
      reviewText: reviewText,
      rating: rating, 
      appointment: { id: appointmentToReview.id } 
    };

    try {
      await api.post('/api/reviews', reviewData);
      toast.success("Ваш отзыв успешно отправлен!");
      setIsReviewModalOpen(false);
      setAppointmentToReview(null);
      setReviewText('');
      setRating(0); 
      setHoverRating(0); 
      fetchCompletedAppointments();
      fetchUserReviews(); // Обновляем отзывы, чтобы скрыть кнопку
    } catch (error) {
      console.error("Ошибка при отправке отзыва:", error);
      toast.error("Ошибка при отправке отзыва. Возможно, вы уже оставляли отзыв.");
    }
  };

  const handleCancelAppointment = async (appointmentId) => {
    const ConfirmationToast = ({ closeToast }) => {
        const confirmAction = async () => {
            try {
                await api.delete(`/api/timetable/${appointmentId}`);
                toast.success("Запись успешно отменена!");
                fetchUpcomingAppointments();
            } catch (error) {
                toast.error(error.response?.data || "Не удалось отменить запись");
            }
            closeToast(); 
        };
        const cancelAction = () => closeToast();

        return (
            <div>
                <p style={{ margin: 0, marginBottom: '10px', fontSize: '1rem' }}>Вы уверены, что хотите отменить эту запись?</p>
                <button onClick={confirmAction} className={styles.toastConfirmButton}>Да, отменить</button>
                <button onClick={cancelAction} className={styles.toastCancelButton}>Нет</button>
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

  const formatAppointmentTime = (isoString) => {
      if (!isoString) return 'Неизвестное время';
      try {
          const date = new Date(isoString.replace('T', ' '));
          return new Date(date.getTime()).toLocaleString('ru-RU', {
              year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
          });
      } catch (e) {
          return 'Некорректное время';
      }
  };

  const formatServiceName = (serviceName) => {
    if (!serviceName) return "";
    return serviceName.replace("[НЕАКТИВНА] ", "");
  };

  // Проверка: есть ли уже отзыв на эту запись
  const getReviewForAppointment = (app) => {
      if (app.reviews && app.reviews.length > 0) return app.reviews[0];
      return userReviews.find(r => r.appointment?.id === app.id || r.appointmentId === app.id);
  };

  const hasNoAppointments = upcomingAppointments.length === 0 && completedAppointments.length === 0;
  const handleEditClick = (app) => {
    const fullTime = getTime(app);
    setAppointmentToEdit(app);
    
    // Предзаполняем поля текущими данными записи
    setSelectedService(app.service?.id || '');
    setSelectedMaster(app.master?.id || '');
    setSelectedDate(fullTime ? fullTime.split('T')[0] : '');
    setSelectedSlot(fullTime ? fullTime.split('T')[1].substring(0, 5) : '');
    
    setIsEditModalOpen(true);
  };
  const handleUpdateBooking = () => {
    if (!selectedService || !selectedMaster || !selectedDate || !selectedSlot) {
      toast.error("Все поля должны быть заполнены!");
      return;
    }

    const finalAppointmentTime = `${selectedDate}T${selectedSlot}`;
    const updateData = {
      service: { id: selectedService },
      master: { id: selectedMaster },
      appointmentTime: finalAppointmentTime,
    };

    api.put(`/api/timetable/${appointmentToEdit.id}`, updateData)
      .then(() => {
        toast.success("Запись успешно обновлена!");
        setIsEditModalOpen(false);
        setAppointmentToEdit(null);
        fetchUpcomingAppointments();
      })
      .catch(error => {
        console.error("Ошибка при обновлении:", error);
        toast.error("Не удалось обновить запись. Возможно, время уже занято.");
      });
  };

  return (
    <div className={styles.dashboardContainer}>
      <ToastContainer position="bottom-right" autoClose={3000} />
      
      {/* ОСНОВНОЙ КОНТЕНТ ТЕПЕРЬ СЛЕВА */}
      <main className={styles.mainContent}>
        <h2>{userName ? `${userName}. ` : ''}Личный кабинет</h2>

        {hasNoAppointments ? (
            <div className={styles.emptyState}>
                <p>У вас пока нет ни одной записи.</p>
                <button className={styles.button1} onClick={() => setIsBookingModalOpen(true)}>
                    Записаться сейчас
                </button>
            </div>
        ) : (
            <div className={styles.tabsSection}>
                <div className={styles.tabsHeader}>
                    <button 
                        className={activeTab === 'upcoming' ? styles.activeTab : styles.tab} 
                        onClick={() => setActiveTab('upcoming')}
                    >
                        Предстоящие записи
                    </button>
                    <button 
                        className={activeTab === 'past' ? styles.activeTab : styles.tab} 
                        onClick={() => setActiveTab('past')}
                    >
                        Прошедшие записи
                    </button>
                </div>

                <div className={styles.tabContent}>
                    {/* Вкладка ПРЕДСТОЯЩИХ записей */}
                    {activeTab === 'upcoming' && (
                        <ul className={styles.appointmentList}>
                            {upcomingAppointments.length > 0 ? upcomingAppointments.map((appointment) => (
                                <li key={appointment.id} className={styles.appointmentItem}>
                                    <div className={styles.appointmentDetails}>
                                        <span>{formatServiceName(appointment.service?.name) || 'Услуга'} у {appointment.master?.name || 'Мастер'}</span>
                                        <span>{formatAppointmentTime(appointment.appointmentTime)}</span>
                                    </div>
                                    <div className={styles.actionButtons}>
                                  <button className={styles.editLinkButton} onClick={() => handleEditClick(appointment)}>
                                    Изменить
                                  </button>
                                  <button className={styles.cancelButton} onClick={() => handleCancelAppointment(appointment.id)}>
                                    Отменить
                                  </button>
                                </div>
                                </li>
                            )) : (
                                <p className={styles.noDataText}>Нет предстоящих записей.</p>
                            )}
                        </ul>
                    )}

                    {/* Вкладка ПРОШЕДШИХ записей */}
                    {activeTab === 'past' && (
                        <ul className={styles.appointmentList}>
                            {completedAppointments.length > 0 ? completedAppointments.map((app) => {
                                const existingReview = getReviewForAppointment(app);
                                return (
                                    <li key={app.id} className={styles.appointmentItem} style={{ alignItems: 'flex-start' }}>
                                        <div className={styles.appointmentDetails}>
                                            <span>{formatServiceName(app.service?.name)} у {app.master?.name}</span>
                                            <span>{formatAppointmentTime(app.appointmentTime)}</span>
                                            
                                            {/* Если отзыв уже есть, показываем его текст */}
                                            {existingReview && (
                                                <div className={styles.existingReviewBox}>
                                                    <div className={styles.reviewStars}>
                                                        {'★'.repeat(existingReview.rating)}{'☆'.repeat(5 - existingReview.rating)}
                                                    </div>
                                                    <div className={styles.reviewTextDisplay}>«{existingReview.reviewText || existingReview.comment}»</div>
                                                </div>
                                            )}
                                        </div>

                                        {/* Если отзыва нет, показываем кнопку */}
                                        {!existingReview && (
                                            <button 
                                                className={styles.reviewButton} 
                                                onClick={() => {
                                                    setAppointmentToReview(app);
                                                    setIsReviewModalOpen(true);
                                                }}>
                                                Оставить отзыв
                                            </button>
                                        )}
                                    </li>
                                );
                            }) : (
                                <p className={styles.noDataText}>Нет прошедших записей.</p>
                            )}
                        </ul>
                    )}
                </div>
            </div>
        )}
      </main>

      {/* ПАНЕЛЬ С КНОПКОЙ ТЕПЕРЬ СПРАВА */}
      <aside className={styles.sidebar}>
        <button className={styles.bigBookButton} onClick={() => setIsBookingModalOpen(true)}>
          Запись на услугу
        </button>
      </aside>

      {/* ================= МОДАЛЬНЫЕ ОКНА (остаются без изменений) ================= */}

      {/* 1. Модальное окно ЗАПИСИ НА УСЛУГУ */}
      {isBookingModalOpen && (
          <div className={styles.modalOverlay} onClick={() => setIsBookingModalOpen(false)}>
              <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                  <div className={styles.modalHeader}>
                      <h3>Новая запись</h3>
                      <button className={styles.closeIcon} onClick={() => setIsBookingModalOpen(false)}>×</button>
                  </div>
                  
                  <div className={styles.bookingForm}>
                      <select value={selectedService} onChange={(e) => setSelectedService(e.target.value)} required>
                          <option value="" disabled>1. Выберите услугу</option>
                          {services.length > 0 ? services.map((service) => (
                              <option key={service.id} value={service.id}>
                                  {service.name} - {service.price} руб. ({service.type === 'MEN' ? 'Мужская' : 'Женская'})
                              </option>
                          )) : <option disabled>Загрузка услуг...</option>}
                      </select>
                      
                      <select value={selectedMaster} onChange={(e) => setSelectedMaster(e.target.value)} required disabled={!selectedService}>
                          <option value="" disabled>2. Выберите мастера</option>
                          {masters.length > 0 ? masters.map((master) => (
                              <option key={master.id} value={master.id}>{master.name}</option>
                          )) : <option disabled>Загрузка мастеров...</option>}
                      </select>

                      <input type="date" value={selectedDate} min={new Date().toISOString().split('T')[0]} 
                          onChange={(e) => setSelectedDate(e.target.value)} disabled={!selectedMaster} />

                      {selectedDate && (
                          <div className={styles.slotsContainer}>
                              {isLoadingSlots && <p>Загрузка свободных слотов...</p>}
                              {!isLoadingSlots && availableSlots.length > 0 && availableSlots.map(slot => (
                                  <button key={slot} className={`${styles.slotButton} ${selectedSlot === slot ? styles.selectedSlot : ''}`}
                                      onClick={() => setSelectedSlot(slot)}>
                                      {slot.substring(0, 5)}
                                  </button>
                              ))}
                              {!isLoadingSlots && availableSlots.length === 0 && <p>На выбранную дату слотов нет</p>}
                          </div>
                      )}

                      <div className={styles.modalActions}>
                          <button className={styles.button1} onClick={handleBooking} disabled={!selectedSlot}>
                              Забронировать
                          </button>
                          <button className={styles.cancelModalButton} onClick={() => setIsBookingModalOpen(false)}>
                              Отмена
                          </button>
                      </div>
                  </div>
              </div>
          </div>
      )}

      {/* 2. Модальное окно ОСТАВЛЕНИЯ ОТЗЫВА */}
      {isReviewModalOpen && appointmentToReview && (
          <div className={styles.modalOverlay} onClick={() => setIsReviewModalOpen(false)}>
              <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                  <div className={styles.modalHeader}>
                      <h3>Оцените визит</h3>
                      <button className={styles.closeIcon} onClick={() => setIsReviewModalOpen(false)}>×</button>
                  </div>
                  
                  <p style={{marginBottom: '20px', color: '#555'}}>
                      Услуга: <strong>{formatServiceName(appointmentToReview.service?.name)}</strong><br/>
                      Мастер: <strong>{appointmentToReview.master?.name}</strong>
                  </p>

                  <div className={styles.ratingInput}>
                      <label>Ваша оценка:</label>
                      <div className={styles.starRating}> 
                          {[...Array(5)].map((_, index) => {
                              const starValue = index + 1;
                              return (
                                  <span key={starValue} className={styles.star} 
                                      onClick={() => setRating(starValue)} 
                                      onMouseEnter={() => setHoverRating(starValue)} 
                                      onMouseLeave={() => setHoverRating(0)} 
                                      style={{ color: starValue <= (hoverRating || rating) ? 'gold' : 'lightgray' }}>
                                      ★ 
                                  </span>
                              );
                          })}
                      </div>
                  </div>

                  <textarea className={styles.reviewTextarea} placeholder="Напишите ваш отзыв (необязательно, но приятно)..."
                      value={reviewText} onChange={(e) => setReviewText(e.target.value)} />
                  
                  <div className={styles.modalActions}>
                      <button className={styles.button1} onClick={handleReviewSubmit} disabled={rating < 1}>
                          Отправить отзыв
                      </button>
                      <button className={styles.cancelModalButton} onClick={() => setIsReviewModalOpen(false)}>
                          Отмена
                      </button>
                  </div>
              </div>
          </div>
      )}
      {isEditModalOpen && (
          <div className={styles.modalOverlay} onClick={() => setIsEditModalOpen(false)}>
              <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                  <div className={styles.modalHeader}>
                      <h3>Изменение записи</h3>
                      <button className={styles.closeIcon} onClick={() => setIsEditModalOpen(false)}>×</button>
                  </div>
                  
                  <div className={styles.bookingForm}>
                      <label className={styles.fieldLabel}>Услуга:</label>
                      <select value={selectedService} onChange={(e) => setSelectedService(e.target.value)}>
                          {services.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                      </select>
                      
                      <label className={styles.fieldLabel}>Мастер:</label>
                      <select value={selectedMaster} onChange={(e) => setSelectedMaster(e.target.value)}>
                          {masters.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
                      </select>

                      <label className={styles.fieldLabel}>Дата:</label>
                      <input type="date" value={selectedDate} min={new Date().toISOString().split('T')[0]} 
                          onChange={(e) => setSelectedDate(e.target.value)} />

                      <label className={styles.fieldLabel}>Доступное время:</label>
                      <div className={styles.slotsContainer}>
                          {isLoadingSlots ? <p>Поиск окон...</p> : availableSlots.map(slot => (
                              <button key={slot} className={`${styles.slotButton} ${selectedSlot === slot ? styles.selectedSlot : ''}`}
                                  onClick={() => setSelectedSlot(slot)}>
                                  {slot.substring(0, 5)}
                              </button>
                          ))}
                          {!isLoadingSlots && availableSlots.length === 0 && <p>Нет свободных окон</p>}
                      </div>

                      <div className={styles.modalActions}>
                          <button className={styles.button1} onClick={handleUpdateBooking}>
                              Сохранить изменения
                          </button>
                          <button className={styles.cancelModalButton} onClick={() => setIsEditModalOpen(false)}>
                              Отмена
                          </button>
                      </div>
                  </div>
              </div>
          </div>
      )}

    </div>
  );
}

export default UserDashboard;