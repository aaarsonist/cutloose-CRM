import React, { useState, useEffect } from 'react';
import api from '../../../api/api';
import styles from './AdminDashboard.module.css';

const ClientActionModal = ({ clientId, onClose, onSuccess }) => {
    const [modalData, setModalData] = useState(null);
    const [contactNotes, setContactNotes] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Загружаем данные при открытии модалки (при получении clientId)
    useEffect(() => {
        if (!clientId) return;

        const fetchClientData = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await api.get(`/api/admin/customer-analytics/client-action/${clientId}`);
                setModalData(response.data);
            } catch (err) {
                console.error("Ошибка загрузки данных клиента:", err);
                setError("Не удалось загрузить карточку клиента");
            } finally {
                setLoading(false);
            }
        };

        fetchClientData();
    }, [clientId]);

    const handleSaveResult = async (status) => {
        if (!modalData || !modalData.clientId) return;
        
        try {
            await api.post(`/api/admin/customer-analytics/client-action/${modalData.clientId}/result`, {
                status: status, // 'CONTACTED' или 'CHURNED'
                notes: contactNotes
            });
            alert(status === 'CHURNED' ? 'Клиент помечен как окончательный отток' : 'Результат контакта сохранен!');
            onSuccess(); // Закрываем окно и обновляем таблицу в родителе
        } catch (err) {
            console.error("Ошибка при сохранении результата:", err);
            alert("Не удалось сохранить результат.");
        }
    };

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()} style={{ maxWidth: '500px' }}>
                <h4>Карточка удержания</h4>
                
                {loading ? (
                    <div style={{ textAlign: 'center', padding: '20px' }}>Загрузка данных...</div>
                ) : error ? (
                    <div style={{ color: 'red' }}>{error}</div>
                ) : modalData && (
                    <>
                        <ul className={styles.appointmentDetails} style={{ marginBottom: '15px' }}>
                            <li><span>Клиент:</span> <strong>{modalData.clientName}</strong></li>
                            <li><span>Контакты:</span> <strong>{modalData.contactInfo}</strong></li>
                            <li><span>Любимый мастер:</span> <strong>{modalData.favoriteMaster}</strong></li>
                            <li><span>Любимая услуга:</span> <strong>{modalData.favoriteService}</strong></li>
                        </ul>

                        <div style={{ 
                            backgroundColor: '#fff8e1', 
                            borderLeft: '4px solid #f0a500', 
                            padding: '12px', 
                            borderRadius: '4px',
                            marginBottom: '20px' 
                        }}>
                            <h5 style={{ margin: '0 0 5px 0', color: '#d49200', fontSize: '0.9rem' }}>Рекомендация системы:</h5>
                            <p style={{ margin: 0, fontSize: '0.95rem', color: '#333' }}>
                                {modalData.recommendation}
                            </p>
                        </div>

                        <div className={styles.editForm}>
                            <label>Результат контакта:</label>
                            <textarea 
                                rows="3"
                                placeholder="Например: Предложила скидку, клиент записался на пятницу..."
                                value={contactNotes}
                                onChange={(e) => setContactNotes(e.target.value)}
                                style={{ 
                                    width: '100%', padding: '10px', borderRadius: '4px', 
                                    border: '1px solid #ccc', resize: 'vertical', fontFamily: 'inherit'
                                }}
                            />
                        </div>

                        <div className={styles.modalButtons} style={{ marginTop: '25px' }}>
                            <button 
                                className={styles.saveButton} 
                                onClick={() => handleSaveResult('CONTACTED')}
                            >
                                Контакт состоялся
                            </button>
                            <button 
                                className={styles.deleteButton} 
                                onClick={() => {
                                    if(window.confirm('Пометить клиента как окончательно ушедшего?')) {
                                        handleSaveResult('CHURNED');
                                    }
                                }}
                            >
                                Окончательный отток
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default ClientActionModal;