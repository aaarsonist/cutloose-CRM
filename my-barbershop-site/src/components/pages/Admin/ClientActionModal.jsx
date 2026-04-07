import React, { useState, useEffect } from 'react';
import api from '../../../api/api';
import styles from './AdminDashboard.module.css';

const ClientActionModal = ({ clientId, clientData, onClose, onSuccess }) => {
    const [activeTab, setActiveTab] = useState('HISTORY'); 
    
    const [modalData, setModalData] = useState(null);
    const [historyData, setHistoryData] = useState([]);
    const [contactNotes, setContactNotes] = useState('');
    
    const [loading, setLoading] = useState(false);
    const [loadingHistory, setLoadingHistory] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!clientId) return;

        const fetchClientData = async () => {
            setLoading(true); setError(null);
            try {
                const response = await api.get(`/api/admin/customer-analytics/client-action/${clientId}`);
                setModalData(response.data);
            } catch (err) {
                setError("Не удалось загрузить карточку клиента");
            } finally { setLoading(false); }
        };

        const fetchHistory = async () => {
            setLoadingHistory(true);
            try {
                const response = await api.get(`/api/admin/customer-analytics/client-history/${clientId}`);
                setHistoryData(response.data);
            } catch (err) {
                console.error("Ошибка загрузки истории:", err);
            } finally { setLoadingHistory(false); }
        };

        fetchClientData();
        fetchHistory();
    }, [clientId]);

    const handleSaveResult = async (status) => {
        if (!modalData) return;
        try {
            await api.post(`/api/admin/customer-analytics/client-action/${modalData.clientId}/result`, {
                status: status, notes: contactNotes
            });
            onSuccess(); 
        } catch (err) {
            alert("Не удалось сохранить результат.");
        }
    };

    const isRecentlyContacted = clientData?.lastContactStatus === 'CONTACTED' && 
        clientData?.lastContactDate && 
        (new Date() - new Date(clientData.lastContactDate)) / (1000 * 3600 * 24) <= 7;

    return (
        <div className={styles.modalOverlay} onClick={onClose} style={{ zIndex: 1050 }}>
            {/* Окно жестко зафиксировано: 650px ширина, 600px высота */}
            <div className={styles.modalContent} onClick={(e) => e.stopPropagation()} style={{ maxWidth: '650px', width: '100%', height: '600px', display: 'flex', flexDirection: 'column' }}>
                
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '15px' }}>
                    <button 
                        onClick={onClose}
                        style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#555', padding: '0 15px 0 0', display: 'flex', alignItems: 'center', transition: 'color 0.2s' }}
                        title="Вернуться назад"
                        onMouseEnter={(e) => e.target.style.color = '#f0a500'}
                        onMouseLeave={(e) => e.target.style.color = '#555'}
                    >
                        &#8592;
                    </button>
                    <h4 style={{ margin: 0 }}>Профиль клиента</h4>
                </div>

                <div style={{ display: 'flex', borderBottom: '2px solid #e5e7eb', marginBottom: '15px', flexShrink: 0 }}>
                    <button 
                        style={{ flex: 1, padding: '10px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1rem',
                            borderBottom: activeTab === 'HISTORY' ? '3px solid #f0a500' : '3px solid transparent', 
                            color: activeTab === 'HISTORY' ? '#f0a500' : '#6b7280', fontWeight: activeTab === 'HISTORY' ? 'bold' : 'normal' 
                        }}
                        onClick={() => setActiveTab('HISTORY')}
                    >
                        История посещений
                    </button>
                    <button 
                        style={{ flex: 1, padding: '10px', background: 'none', border: 'none', cursor: 'pointer', fontSize: '1rem',
                            borderBottom: activeTab === 'ACTION' ? '3px solid #f0a500' : '3px solid transparent', 
                            color: activeTab === 'ACTION' ? '#f0a500' : '#6b7280', fontWeight: activeTab === 'ACTION' ? 'bold' : 'normal' 
                        }}
                        onClick={() => setActiveTab('ACTION')}
                    >
                        Действия 
                    </button>
                </div>

                {/* РОДИТЕЛЬСКИЙ КОНТЕЙНЕР (position: relative) */}
                <div style={{ flex: 1, position: 'relative' }}>
                    {loading ? <div style={{ textAlign: 'center', padding: '40px' }}>Загрузка...</div> : error ? <div style={{ color: 'red' }}>{error}</div> : modalData && (
                        <>
                            {/* ВКЛАДКА 1: ИСТОРИЯ ПОСЕЩЕНИЙ (ПРОКРУЧИВАЕТСЯ) */}
                            {activeTab === 'HISTORY' && (
                                <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, overflowY: 'auto', paddingRight: '5px' }}>
                                    {loadingHistory ? (
                                        <div style={{ textAlign: 'center', padding: '20px' }}>Загрузка истории...</div>
                                    ) : historyData.length > 0 ? (
                                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                                            <thead>
                                                <tr style={{ backgroundColor: '#f9f9f9', borderBottom: '2px solid #e0e0e0', position: 'sticky', top: 0, zIndex: 1 }}>
                                                    <th style={{ padding: '8px', textAlign: 'left' }}>Дата</th>
                                                    <th style={{ padding: '8px', textAlign: 'left' }}>Услуга</th>
                                                    <th style={{ padding: '8px', textAlign: 'left' }}>Мастер</th>
                                                    <th style={{ padding: '8px', textAlign: 'left' }}>Отзыв</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {historyData.map((visit, index) => (
                                                    <tr key={index} style={{ borderBottom: '1px solid #f0f0f0' }}>
                                                        <td style={{ padding: '8px' }}>{new Date(visit.date).toLocaleDateString('ru-RU')}</td>
                                                        <td style={{ padding: '8px' }}>{visit.serviceName} <br/><span style={{ color: '#10b981', fontWeight: 'bold' }}>{visit.price} BYN</span></td>
                                                        <td style={{ padding: '8px' }}>{visit.masterName}</td>
                                                        <td style={{ padding: '8px' }}>
                                                            {visit.reviewRating ? (
                                                                <div>
                                                                    <span style={{ color: '#f0a500' }}>{'★'.repeat(visit.reviewRating)}</span>
                                                                    {visit.reviewText && <div style={{ fontSize: '0.8rem', color: '#666', fontStyle: 'italic', marginTop: '4px' }}>«{visit.reviewText}»</div>}
                                                                </div>
                                                            ) : <span style={{ color: '#aaa', fontSize: '0.8rem' }}>Нет отзыва</span>}
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    ) : (
                                        <div style={{ textAlign: 'center', padding: '20px', color: '#888' }}>История посещений пуста.</div>
                                    )}
                                </div>
                            )}

                            {/* ВКЛАДКА 2: ДЕЙСТВИЯ (СТАБИЛЬНАЯ, БЕЗ ПРОКРУТКИ) */}
                            {activeTab === 'ACTION' && (
                                <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                                    <ul className={styles.appointmentDetails} style={{ marginBottom: '15px', marginTop: 0, flexShrink: 0 }}>
                                        <li style={{ padding: '6px 0' }}><span>Клиент:</span> <strong>{modalData.clientName}</strong></li>
                                        <li style={{ padding: '6px 0' }}><span>Контакты:</span> <strong>{modalData.contactInfo}</strong></li>
                                        <li style={{ padding: '6px 0' }}><span>Любимый мастер:</span> <strong>{modalData.favoriteMaster}</strong></li>
                                        <li style={{ padding: '6px 0' }}><span>Любимая услуга:</span> <strong>{modalData.favoriteService}</strong></li>
                                    </ul>

                                    <div style={{ backgroundColor: '#fff8e1', borderLeft: '4px solid #f0a500', padding: '12px', borderRadius: '4px', marginBottom: '15px', flexShrink: 0 }}>
                                        <h5 style={{ margin: '0 0 5px 0', color: '#d49200', fontSize: '0.9rem' }}>Рекомендация системы:</h5>
                                        <p style={{ margin: 0, fontSize: '0.95rem', color: '#333' }}>{modalData.recommendation}</p>
                                    </div>

                                    <div className={styles.editForm} style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                                        <label style={{ flexShrink: 0, marginBottom: '5px' }}>Результат контакта:</label>
                                        <textarea placeholder="Например: Предложила скидку, клиент записался..."
                                            value={contactNotes} onChange={(e) => setContactNotes(e.target.value)}
                                            style={{ flex: 1, width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc', resize: 'none', fontFamily: 'inherit' }}
                                        />
                                    </div>

                                    <div className={styles.modalButtons} style={{ marginTop: '20px', flexShrink: 0 }}>
                                        {isRecentlyContacted ? (
                                            <button className={styles.saveButton} disabled style={{ backgroundColor: '#9ca3af', borderColor: '#9ca3af', cursor: 'not-allowed' }}>
                                                Связались {new Date(clientData.lastContactDate).toLocaleDateString('ru-RU')}
                                            </button>
                                        ) : (
                                            <button className={styles.saveButton} onClick={() => handleSaveResult('CONTACTED')}>
                                                Контакт состоялся
                                            </button>
                                        )}
                                        <button className={styles.deleteButton} onClick={() => { if(window.confirm('Пометить клиента как окончательно ушедшего?')) handleSaveResult('CHURNED'); }}>
                                            Окончательный отток
                                        </button>
                                    </div>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ClientActionModal;