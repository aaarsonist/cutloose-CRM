import React, { useState, useEffect } from 'react';
import styles from './AdminDashboard.module.css'; 
import api from '../../../api/api'; 
import { toast } from 'react-toastify';

function AdminBookingModal({ isOpen, onClose, onSave }) {
    const [clientName, setClientName] = useState('');
    const [clientEmail, setClientEmail] = useState('');
    const [selectedService, setSelectedService] = useState('');
    const [selectedMaster, setSelectedMaster] = useState('');
    const [selectedDate, setSelectedDate] = useState('');
    
    const [services, setServices] = useState([]);
    const [masters, setMasters] = useState([]);
    const [availableSlots, setAvailableSlots] = useState([]);
    const [isLoadingSlots, setIsLoadingSlots] = useState(false);
    
    const [selectedSlot, setSelectedSlot] = useState(null); 

    useEffect(() => {
        if (isOpen) {
            api.get('/services')
                .then(res => setServices(res.data))
                .catch(err => console.error("Ошибка загрузки услуг:", err));
            
            api.get('/api/masters')
                .then(res => setMasters(res.data))
                .catch(err => console.error("Ошибка загрузки мастеров:", err));
        }
    }, [isOpen]); 

    useEffect(() => {
        if (!selectedService || !selectedMaster || !selectedDate) {
            setAvailableSlots([]);
            setSelectedSlot(null); 
            return;
        }

        setIsLoadingSlots(true);
        setSelectedSlot(null); 

        const service = services.find(s => s.id === Number(selectedService));
        if (!service) return;

        api.get('/api/availability', {
            params: {
                masterId: selectedMaster,
                serviceId: service.id,
                date: selectedDate
            }
        })
        .then(res => {
            setAvailableSlots(res.data);
        })
        .catch(err => {
            console.error("Ошибка загрузки слотов:", err);
            setAvailableSlots([]);
        })
        .finally(() => {
            setIsLoadingSlots(false);
        });

    }, [selectedService, selectedMaster, selectedDate, services]);

    if (!isOpen) {
        return null; 
    }

    const handleSubmit = () => {
        if (!clientName || !clientEmail || !selectedSlot) {
            toast.error("Пожалуйста, заполните все поля и выберите время.");
            return;
        }

        const bookingRequest = {
            clientName: clientName,
            clientEmail: clientEmail,
            masterId: selectedMaster,
            serviceId: selectedService,
            appointmentTime: `${selectedDate}T${selectedSlot}` 
        };
        
        onSave(bookingRequest);
    };
    
    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h4>Записать клиента</h4>
                
                <div className={styles.editForm}>
                    <input 
                        type="text" 
                        placeholder="Имя клиента" 
                        value={clientName} 
                        onChange={e => setClientName(e.target.value)}
                    />
                    
                    <input 
                        type="email" 
                        placeholder="email клиента" 
                        value={clientEmail} 
                        onChange={e => setClientEmail(e.target.value)}
                    />
                    
                    <select value={selectedService} onChange={e => setSelectedService(e.target.value)}>
                        <option value="" disabled>-- Выберите услугу --</option>
                        {services.map(s => (
                            <option key={s.id} value={s.id}>{s.name} ({s.price} BYN)</option>
                        ))}
                    </select>

                    <select value={selectedMaster} onChange={e => setSelectedMaster(e.target.value)}>
                        <option value="" disabled>-- Выберите мастера --</option>
                        {masters.map(m => (
                            <option key={m.id} value={m.id}>{m.name}</option>
                        ))}
                    </select>
                    
                    <input 
                        type="date" 
                        value={selectedDate} 
                        onChange={e => setSelectedDate(e.target.value)}
                    />
                    
                    <div className={styles.timeSlotsContainer}> 
                    {isLoadingSlots ? (
                        <p>Загрузка слотов...</p>
                    ) : availableSlots.length > 0 ? (
                        availableSlots.map(slot => (
                            <button 
                                key={slot}
                                onClick={() => setSelectedSlot(slot)} 
                                className={`${styles.timeSlotButton} ${selectedSlot === slot ? styles.selected : ''}`}
                            >
                                {slot}
                            </button>
                        ))
                    ) : (
                        <p>(Нет доступных слотов на эту дату)</p>
                    )}
                </div>
                </div>

                <div className={styles.modalButtons}>
                    <button onClick={onClose} className={styles.cancelButton}>Отмена</button>
                    <button 
                        onClick={handleSubmit} 
                        className={styles.saveButton}
                        disabled={!selectedSlot || isLoadingSlots} 
                    >
                        Записать
                    </button>
                </div>
            </div>
        </div>
    );
}

export default AdminBookingModal;