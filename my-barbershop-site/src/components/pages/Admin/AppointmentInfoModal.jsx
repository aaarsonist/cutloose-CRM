import React from 'react';
import styles from './AdminDashboard.module.css'; 

function AppointmentInfoModal({ isOpen, onClose, onDelete, event }) {
    if (!isOpen || !event) {
        return null; 
    }

    const formatEventDate = (date) => {
        return new Date(date).toLocaleString('ru-RU', {
            day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    };

    const isPast = event.start < new Date();
    
    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h4>Детали записи</h4>
                
                <ul className={styles.appointmentDetails}>
                    <li>
                        <span>Клиент:</span>
                        <strong>{event.clientName}</strong> ({event.clientEmail})
                    </li>
                    <li>
                        <span>Услуга:</span>
                        <strong>{event.serviceName}</strong>
                    </li>
                    <li>
                        <span>Мастер:</span>
                        <strong>{event.masterName}</strong>
                    </li>
                    <li>
                        <span>Время:</span>
                        <strong>{formatEventDate(event.start)}</strong>
                    </li>
                    {event.createdAt && (
                        <li>
                            <span>Запись создана:</span>
                            <strong style={{ color: '#777' }}>
                                {new Date(event.createdAt).toLocaleString('ru-RU')}
                            </strong>
                        </li>
                    )}
                </ul>

                <div className={styles.modalButtons}>
                    <button onClick={onClose} className={styles.cancelButton}>Закрыть</button>
                    
                    <button 
                        onClick={() => onDelete(event.id)} 
                        className={styles.deleteButton} 
                        disabled={isPast} 
                    >
                        {isPast ? "Запись прошла" : "Отменить запись"}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default AppointmentInfoModal;