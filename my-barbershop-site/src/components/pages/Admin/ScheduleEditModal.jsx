import React, { useState, useEffect } from 'react';
import styles from './AdminDashboard.module.css'; 

function ScheduleEditModal({ isOpen, onClose, onSave, scheduleData, masterName }) {
    const [isWorking, setIsWorking] = useState(true);
    const [startTime, setStartTime] = useState('09:00');
    const [endTime, setEndTime] = useState('18:00');

    const dayNames = {
        MONDAY: "Понедельник", TUESDAY: "Вторник", WEDNESDAY: "Среда",
        THURSDAY: "Четверг", FRIDAY: "Пятница", SATURDAY: "Суббота", SUNDAY: "Воскресенье"
    };

    useEffect(() => {
        if (scheduleData && scheduleData.startTime) {
            setIsWorking(true);
            setStartTime(scheduleData.startTime);
            setEndTime(scheduleData.endTime);
        } else {
            setIsWorking(false);
            setStartTime('09:00'); 
            setEndTime('15:00');
        }
    }, [scheduleData]);

    if (!isOpen) {
        return null; 
    }

    const handleSave = () => {
        const finalEntry = {
            ...scheduleData,
            startTime: isWorking ? startTime : null, 
            endTime: isWorking ? endTime : null,
        };
        onSave(finalEntry);
    };

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h4>{masterName}</h4>
                <h5>{dayNames[scheduleData.dayOfWeek]}</h5>

                <div className={styles.editForm}>
                    <div className={styles.checkboxGroup}>
                        <input
                            type="checkbox"
                            id="isWorkingCheckbox"
                            checked={isWorking}
                            onChange={(e) => setIsWorking(e.target.checked)}
                        />
                        <label htmlFor="isWorkingCheckbox">Рабочий день</label>
                    </div>

                    <label>Время начала:</label>
                    <input
                        type="time"
                        value={startTime}
                        onChange={(e) => setStartTime(e.target.value)}
                        disabled={!isWorking} 
                        step="1800" 
                    />
                    
                    <label>Время окончания:</label>
                    <input
                        type="time"
                        value={endTime}
                        onChange={(e) => setEndTime(e.target.value)}
                        disabled={!isWorking}
                        step="1800"
                    />

                    <div className={styles.modalButtons}>
                        <button onClick={onClose} className={styles.cancelButton}>Отмена</button>
                        <button onClick={handleSave} className={styles.saveButton}>Сохранить</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ScheduleEditModal;