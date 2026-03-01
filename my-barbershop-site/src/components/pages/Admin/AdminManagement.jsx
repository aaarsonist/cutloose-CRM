import React, { useEffect, useState } from 'react'; 
import api from '../../../api/api';
import styles from './AdminDashboard.module.css'; 
import { toast } from 'react-toastify';

function AdminManagement() {
    const [services, setServices] = useState([]);
    const [masters, setMasters] = useState([]);
    const [reviews, setReviews] = useState([]); 
    const [editingService, setEditingService] = useState(null);
    const [newService, setNewService] = useState({
        name: '', price: '', type: 'MEN', duration: '', category: 'HAIR'
    });
    const [newMasterName, setNewMasterName] = useState('');

    useEffect(() => {
        fetchServices();
        fetchReviews(); 
        fetchMasters(); 
    }, []);

    const fetchServices = async () => { 
        try {
            const response = await api.get('/services'); 
            setServices(response.data);
        } catch (error) {
            console.error('Ошибка при загрузке услуг:', error);
            toast.error('Ошибка при загрузке услуг.');
        }
    };
    const handleServiceFormChange = (e) => { 
        const { name, value } = e.target;
        if ((name === 'price' || name === 'duration') && parseFloat(value) < 0) {
            return;
        }
        setNewService(prevState => ({
            ...prevState,
            [name]: value
        }));
    };
    const handleEditFormChange = (e) => {
        const { name, value } = e.target;

        if ((name === 'price' || name === 'duration') && parseFloat(value) < 0) {
            return;
        }

        setEditingService(prevState => ({
            ...prevState,
            [name]: value
        }));
    };
    const handleAddService = async () => { 
        if (!newService.name || !newService.price || !newService.duration || !newService.category) {
            toast.error('Пожалуйста, заполните все поля (Название, Цена, Длительность).');
            return;
        }
        try {
            const serviceToAdd = {
                name: newService.name,
                price: parseFloat(newService.price),
                type: newService.type,
                duration: parseInt(newService.duration),
                category: newService.category
            };
            await api.post('/services', serviceToAdd);
            toast.success('Услуга успешно добавлена!');
            setNewService({ name: '', price: '', type: 'MEN', duration: '', category: 'HAIR' });
            fetchServices();
        } catch (error) {
            console.error('Ошибка при добавлении услуги:', error);
            toast.error('Не удалось добавить услугу.');
        }
    };
    const handleEditService = (service) => { 
        setEditingService(service);
    };
    const handleSaveEdit = async () => {  
        try {
            const serviceToSave = {
                ...editingService,
                price: parseFloat(editingService.price),
                duration: parseInt(editingService.duration)
            };
            await api.put(`/services/${editingService.id}`, serviceToSave);
            toast.success('Услуга успешно обновлена!');
            fetchServices();
            setEditingService(null);
        } catch (error) {
            console.error('Ошибка при обновлении услуги:', error);
            toast.error('Ошибка при обновлении услуги.');
        }
    };

    const handleDeleteService = (id) => { 
        
        const ConfirmationToast = ({ closeToast }) => {
            const confirmAction = async () => {
                try {
                    await api.delete(`/services/${id}`);
                    toast.success('Услуга успешно удалена.');
                    fetchServices();
                } catch (error) {
                    console.error('Ошибка при удалении услуги:', error);
                    toast.error('Не удалось удалить услугу. Возможно, на нее есть записи.');
                }
                closeToast();
            };
            const cancelAction = () => closeToast();

            return (
                <div>
                    <p style={{ margin: 0, marginBottom: '10px', fontSize: '1rem' }}>
                        Вы уверены, что хотите удалить эту услугу?
                    </p>
                    <button onClick={confirmAction} className={styles.toastConfirmButton}>
                        Да, удалить
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

    const fetchMasters = async () => { 
        try {
            const response = await api.get('/api/masters'); 
            setMasters(response.data);
        } catch (error) {
            console.error('Ошибка при загрузке мастеров:', error);
            toast.error('Ошибка при загрузке мастеров.');
        }
    };

    const handleDeleteMaster = (id) => { 
        
        const ConfirmationToast = ({ closeToast }) => {
            const confirmAction = async () => {
                try {
                    await api.delete(`/api/masters/${id}`);
                    toast.success('Мастер успешно удален.');
                    fetchMasters(); 
                } catch (error) {
                    console.error('Ошибка при удалении мастера:', error);
                    toast.error('Не удалось удалить мастера. Возможно, у него есть будущие записи.');
                }
                closeToast();
            };
            const cancelAction = () => closeToast();

            return (
                <div>
                    <p style={{ margin: 0, marginBottom: '10px', fontSize: '1rem' }}>
                        Вы уверены, что хотите удалить этого мастера?
                    </p>
                    <button onClick={confirmAction} className={styles.toastConfirmButton}>
                        Да, удалить
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

    const handleAddMaster = async () => { 
        if (!newMasterName.trim()) {
            toast.error('Пожалуйста, введите имя мастера.');
            return;
        }
        try {
            const masterToAdd = { name: newMasterName };
            await api.post('/api/masters', masterToAdd);
            toast.success('Мастер успешно добавлен!');
            setNewMasterName(''); 
            fetchMasters(); 
        } catch (error) {
            console.error('Ошибка при добавлении мастера:', error);
            toast.error('Не удалось добавить мастера.');
        }
    };

    const fetchReviews = async () => { 
        try {
            const response = await api.get('/api/reviews');
            setReviews(response.data);
        } catch (error) {
            console.error('Ошибка при загрузке отзывов:', error);
            toast.error('Ошибка при загрузке отзывов.');
        }
    };

    const renderStars = (rating) => { 
        if (typeof rating !== 'number' || rating < 1 || rating > 5) {
            return 'Нет оценки';
        }
        const fullStar = '★';
        const emptyStar = '☆';
        return fullStar.repeat(rating) + emptyStar.repeat(5 - Math.round(rating));
    };

    const handleDeleteReview = (id) => { 
        
        const ConfirmationToast = ({ closeToast }) => {
            const confirmAction = async () => {
                try {
                    await api.delete(`/api/reviews/${id}`);
                    toast.success('Отзыв успешно удален.');
                    fetchReviews(); 
                } catch (error) {
                    console.error('Ошибка при удалении отзыва:', error);
                    toast.error('Не удалось удалить отзыв.');
                }
                closeToast();
            };
            const cancelAction = () => closeToast();
            
            return (
                <div>
                    <p style={{ margin: 0, marginBottom: '10px', fontSize: '1rem' }}>
                        Вы уверены, что хотите удалить этот отзыв?
                    </p>
                    <button onClick={confirmAction} className={styles.toastConfirmButton}>
                        Да, удалить
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

    return (
        <div className={styles.dashboard}> 
            <div className={styles.reviewWidget} style={{marginBottom: '30px'}}> 
                <h3>Отзывы клиентов</h3>
                
                <ul className={styles.reviewList}>
                    {reviews.map((review) => (
                         <li key={review.id} className={styles.reviewItem}>
                             <span className={styles.reviewInfo}>
                                 {review.appointment ? (
                                     <>
                                         <strong>{review.appointment.service?.name || 'Услуга'}</strong> ({renderStars(review.rating)})
                                         <br/>
                                         <small style={{color: '#555', fontStyle: 'italic'}}>"{review.reviewText}"</small>
                                         <br/>
                                         <small>Мастер: {review.appointment.master?.name || 'Неизвестно'}</small>
                                     </>
                                 ) : (
                                     <>
                                         <strong>Отзыв без привязки</strong> ({renderStars(review.rating)})
                                         <br/>
                                         <small style={{color: '#555', fontStyle: 'italic'}}>"{review.reviewText}"</small>
                                     </>
                                 )}
                               </span>
                             <div className={styles.reviewButtons}>
                                 <button onClick={() => handleDeleteReview(review.id)} className={styles.deleteButton}>
                                     Удалить
                                 </button>
                             </div>
                         </li>
                    ))}
                     {reviews.length === 0 && <li className={styles.reviewItem}>Список отзывов пуст.</li>} 
                </ul>
            </div>

            <div className={styles.managementGrid}>
                
                <div className={styles.managementWidget}>
                    <h3>Список услуг</h3>
                    <ul className={styles.managementList}>
                        {services.map((service) => (
                            <li key={service.id} className={styles.managementItem}>
                                {editingService?.id === service.id ? (
                                    <div className={styles.editForm}>
                                        <input type="text" 
                                            name="name"
                                            value={editingService.name} 
                                            onChange={handleEditFormChange} />
                                        
                                        <input type="number" 
                                            name="price"
                                            value={editingService.price} 
                                            min="0"
                                            onChange={handleEditFormChange} />
                                        
                                        <input type="number" 
                                            name="duration"
                                            placeholder="Длительность (мин)" 
                                            value={editingService.duration || ''} 
                                            min="0"
                                            onChange={handleEditFormChange} />
                                        
                                        <select 
                                            name="type"
                                            value={editingService.type} 
                                            onChange={handleEditFormChange} >

                                            <option value="MEN">Мужская</option>
                                            <option value="WOMEN">Женская</option>
                                        </select>
                                        <select name="category" value={editingService.category} onChange={handleEditFormChange}>
                                            <option value="HAIR">Парикмахерский зал</option>
                                            <option value="BEARD">Борода и усы</option>
                                            <option value="NAILS">Ногтевой сервис</option>
                                            <option value="FACE">Лицо и брови</option>
                                        </select>
                                        <button onClick={handleSaveEdit}>Сохранить</button>
                                    </div>
                                ) : (
                                    <>
                                        <span className={styles.itemInfo}>
                                            {service.name} - {service.price} р. ({service.duration || 'N/A'} мин.)
                                        </span>
                                        <div className={styles.itemButtons}>
                                            <button onClick={() => handleEditService(service)} className={styles.editButton}>Редактировать</button>
                                            <button onClick={() => handleDeleteService(service.id)} className={styles.deleteButton}>Удалить</button>
                                        </div>
                                    </>
                                )}
                            </li>
                        ))}
                    </ul>
                    
                    <h3 className={styles.formToggle}>Добавить услугу</h3>
                    <div className={styles.editForm}>
                        <input type="text" 
                            name="name"
                            placeholder="Название"
                            value={newService.name} 
                            onChange={handleServiceFormChange} />
                        
                        <input type="number" 
                            name="price"
                            placeholder="Цена (руб.)"
                            value={newService.price} 
                            min="0"
                            onChange={handleServiceFormChange} />
                        
                        <input type="number" 
                            name="duration"
                            placeholder="Длительность (мин)" 
                            value={newService.duration || ''} 
                            min="0"
                            onChange={handleServiceFormChange} />
                        <select name="type" value={newService.type} onChange={handleServiceFormChange} >
                            <option value="MEN">Мужская</option>
                            <option value="WOMEN">Женская</option>
                        </select>
                        <select name="category" value={newService.category} onChange={handleServiceFormChange}>
                            <option value="HAIR">Парикмахерский зал</option>
                            <option value="BEARD">Борода и усы</option>
                            <option value="NAILS">Ногтевой сервис</option>
                            <option value="FACE">Лицо и брови</option>
                        </select>
                        <button onClick={handleAddService}>Добавить услугу</button>
                    </div>
                </div>

                <div className={styles.managementWidget}>
                    <h3>Список мастеров</h3>
                    <ul className={styles.managementList}>
                        {masters.map((master) => (
                            <li key={master.id} className={styles.managementItem}>
                                <span className={styles.itemInfo}>{master.name}</span>
                                <div className={styles.itemButtons}>
                                    <button onClick={() => handleDeleteMaster(master.id)} className={styles.deleteButton}>
                                        Удалить
                                    </button>
                                </div>
                            </li>
                        ))}
                    </ul>
                    
                    <h3 className={styles.formToggle}>Добавить мастера</h3>
                    <div className={styles.editForm}>
                        <input 
                            type="text" 
                            placeholder="Имя мастера" 
                            value={newMasterName} 
                            onChange={(e) => setNewMasterName(e.target.value)} 
                        />
                        <button onClick={handleAddMaster}>Добавить мастера</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AdminManagement;