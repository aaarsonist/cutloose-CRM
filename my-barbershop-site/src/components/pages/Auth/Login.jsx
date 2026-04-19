import React from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";  
import styles from './Login.module.css';
import axios from "axios";
import api from '../../../api/api';

function Login() {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm();
    
    const navigate = useNavigate(); 

    const onSubmit = async (data) => {
        try {
            const formData = new URLSearchParams();
            formData.append('username', data.email); 
            formData.append('password', data.password); 

            const response = await axios.post('http://localhost:8080/api/users/login', formData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded' 
                },
                withCredentials: true
            });

            if (response.status === 200) {

                const userResponse = await api.get('/api/users/current'); 
                const currentUser = userResponse.data;

                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                if (currentUser && currentUser.role === 'ADMIN') {
                    navigate('/admin');
                } else {
                    navigate('/user'); 
                }
            } else {
                 throw new Error(`Login failed with status code: ${response.status}`);
            }

        } catch (error) {
            console.error("Error during login:", error); 

            if (axios.isAxiosError(error) && error.response) {
                 if (error.response.status === 401) {
                      alert('Неверный email или пароль.'); 
                 } else {
                      alert(`Произошла ошибка входа. Статус: ${error.response.status}`); 
                 }
            } else {
                 alert('Произошла ошибка сети или другая ошибка.'); 
            }
        }
    };
    
    return (
        <div className={styles.App}>
            <p className={styles.title}>Вход</p>

            <form onSubmit={handleSubmit(onSubmit)}>
                <input 
                    type="email" 
                    placeholder="Введите email" 
                    {...register("email", { required: true })} 
                />
                {errors.email && <span style={{ color: "red", display: 'block', fontSize: '12px' }}>*Email обязателен</span>}

                <input 
                    type="password" 
                    placeholder="Введите пароль" 
                    {...register("password", { required: true })} 
                />
                {errors.password && <span style={{ color: "red", display: 'block', fontSize: '12px' }}>*Пароль обязателен</span>}

                <input 
                    type="submit" 
                    value="Войти" 
                />
            </form>

            {/* --- НАЧАЛО ДОБАВЛЕННОГО КОДА ДЛЯ GOOGLE --- */}
            <div style={{ display: 'flex', alignItems: 'center', margin: '20px 0', color: '#aaa', fontSize: '14px' }}>
                <div style={{ flex: 1, borderBottom: '1px solid #ddd' }}></div>
                <span style={{ padding: '0 10px' }}>ИЛИ</span>
                <div style={{ flex: 1, borderBottom: '1px solid #ddd' }}></div>
            </div>

            <a 
                href="http://localhost:8080/oauth2/authorization/google" 
                style={{
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px',
                    width: '100%', padding: '12px', border: '1px solid #dadce0', borderRadius: '4px',
                    backgroundColor: '#fff', color: '#3c4043', fontSize: '15px', fontWeight: '500',
                    cursor: 'pointer', textDecoration: 'none', marginBottom: '20px'
                }}
            >
                <img 
                    src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_Logo.svg" 
                    alt="Google" 
                    style={{ width: '20px', height: '20px' }}
                />
                Вход через Google
            </a>
            {/* --- КОНЕЦ ДОБАВЛЕННОГО КОДА ДЛЯ GOOGLE --- */}

            <p>
                Еще не зарегистрированы?{" "}
                <Link to="/register" style={{ color: 'rgb(222, 89, 1)' }}>
                    Зарегистрируйтесь 
                </Link>
            </p>
        </div>
    );
}

export default Login;