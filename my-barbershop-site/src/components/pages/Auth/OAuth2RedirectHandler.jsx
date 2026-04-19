import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/api'; // Убедись, что путь к твоему axios-инстансу правильный

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const fetchUserAndRedirect = async () => {
            try {
                // 1. Стучимся на бэкенд. Так как кука сессии уже стоит, бэкенд отдаст данные
                const response = await api.get('/api/users/current');
                const currentUser = response.data;

                // 2. Легализуем пользователя во фронтенде (записываем в localStorage)
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                // 3. Распределяем по кабинетам
                if (currentUser.role === 'ADMIN') {
                    navigate('/admin');
                } else {
                    navigate('/user');
                }
            } catch (error) {
                console.error("Ошибка при получении данных Google-сессии", error);
                navigate('/login'); // Если что-то пошло не так, кидаем на логин
            }
        };

        fetchUserAndRedirect();
    }, [navigate]);

    // Пользователь будет видеть этот экран буквально долю секунды
    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'sans-serif' }}>
            <h2>Успешный вход! Перенаправляем в личный кабинет...</h2>
        </div>
    );
};

export default OAuth2RedirectHandler;