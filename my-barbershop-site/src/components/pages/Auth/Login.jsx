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
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="20px" height="20px">
                    <path fill="#FFC107" d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z"/>
                    <path fill="#FF3D00" d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z"/>
                    <path fill="#4CAF50" d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z"/>
                    <path fill="#1976D2" d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571c0.001-0.001,0.002-0.001,0.003-0.002l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z"/>
                </svg>
                Вход через Google
            </a>

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