    import React from "react";
    import { useForm } from "react-hook-form";
    import { Link, useNavigate } from "react-router-dom";  
    import axios from "axios";
    import styles from './Login.module.css';
    import api from '../../../api/api';

    function Register() {
        const {
            register,
            handleSubmit,
            formState: { errors },
        } = useForm();

        const navigate = useNavigate();

        const onSubmit = async data => {
            try {
              await api.post('/api/users/register', {
                username: data.email,
                password: data.password,
                name: data.name
              });

              const loginFormData = new URLSearchParams();
              loginFormData.append('username', data.email);
              loginFormData.append('password', data.password);

              await axios.post('http://localhost:8080/api/users/login', loginFormData, {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded' 
                },
                withCredentials: true 
              });

              const me = await api.get('/api/users/current'); 
              const currentUser = me.data;

              localStorage.setItem('currentUser', JSON.stringify(currentUser));

              if (currentUser && currentUser.role === 'ADMIN') {
                 navigate('/admin');
              } else {
                 navigate('/user'); 
              }

            } catch (error) { 
              console.error("Error during registration or auto-login:", error);

              if (error.response && error.response.status === 400) { 
                 alert('Ошибка при регистрации: возможно, email занят.');
              } else if (error.response && error.response.status === 401) { 
                 alert('Ошибка при автоматическом входе после регистрации. Попробуйте войти вручную.');
              } else { 
                 alert('Произошла ошибка. Пожалуйста, попробуйте еще раз.');
              }
            }
          };
        return (
            <div className={styles.App}>
                <p className={styles.title}>Регистрация</p>

                <form onSubmit={handleSubmit(onSubmit)}>
                    <input 
                        type="text" 
                        placeholder="Введите ваше имя" 
                        {...register("name", { required: true })} 
                    />
                    {errors.name && <span style={{ color: "red" }}>*Имя обязательно</span>}

                    <input 
                        type="email" 
                        placeholder="Введите email" 
                        {...register("email", { required: true })} 
                    />
                    {errors.email && <span style={{ color: "red" }}>*Email обязателен</span>}

                    <input 
                        type="password" 
                        placeholder="Введите пароль" 
                        {...register("password", { required: true, minLength: 6 })} 
                    />
                    {errors.password && <span style={{ color: "red" }}>*Пароль должен быть не менее 6 символов</span>}

                    <input 
                        type="submit" 
                        value="Зарегистрироваться" 
                    />
                </form>

                <p>
                    Уже зарегистрированы?{" "}
                    <Link to="/login" style={{ color: 'rgb(222, 89, 1)' }}>
                        Войдите в личный кабинет
                    </Link>
                </p>
            </div>
        );
    }

    export default Register;
