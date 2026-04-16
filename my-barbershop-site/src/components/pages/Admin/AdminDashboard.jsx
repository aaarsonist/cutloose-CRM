import React, { useState, useEffect } from 'react';
import AdminManagement from './AdminManagement';
import AdminSchedule from './AdminSchedule';
import AdminAnalytics from './AdminAnalytics';
import AdminForecast from './AdminForecast';
import AdminCustomerAnalytics from './AdminCustomerAnalytics'; 
import styles from './AdminDashboard.module.css';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function AdminDashboard() {
    const [activeTab, setActiveTab] = useState(() => {
        return localStorage.getItem('adminActiveTab') || 'management';
    });
    
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    useEffect(() => {
        localStorage.setItem('adminActiveTab', activeTab);
    }, [activeTab]);

    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return (
        <div className={styles.adminContainer}>
            <ToastContainer 
                position="bottom-right" 
                autoClose={3000} 
                newestOnTop
                pauseOnHover
            />

            {/* НОВОЕ: Добавлен динамический класс styles.hamburgerBtnOpen */}
            <button 
                className={`${styles.hamburgerBtn} ${isSidebarOpen ? styles.hamburgerBtnOpen : ''}`} 
                onClick={toggleSidebar}
                onMouseEnter={() => setIsSidebarOpen(true)}
            >
                ☰
            </button>

            <div 
                className={`${styles.sidebar} ${!isSidebarOpen ? styles.sidebarClosed : ''}`}
                onMouseLeave={() => setIsSidebarOpen(false)}
            >
                <h3>Панель управления</h3>
                <button 
                    className={activeTab === 'management' ? styles.active : ''} 
                    onClick={() => setActiveTab('management')}
                >
                    Управление
                </button>
                <button 
                    className={activeTab === 'schedule' ? styles.active : ''} 
                    onClick={() => setActiveTab('schedule')}
                >
                    Расписание
                </button>
                <button 
                    className={activeTab === 'analytics' ? styles.active : ''} 
                    onClick={() => setActiveTab('analytics')}
                >
                    Аналитика салона
                </button>
                
                <button 
                    className={activeTab === 'customerAnalytics' ? styles.active : ''} 
                    onClick={() => setActiveTab('customerAnalytics')}
                >
                    Клиенты
                </button>

                <button 
                    className={activeTab === 'forecast' ? styles.active : ''} 
                    onClick={() => setActiveTab('forecast')}
                >
                    Прогноз и рекомендации
                </button>
            </div>

            <div className={styles.content}>
                {activeTab === 'management' && <AdminManagement />}
                {activeTab === 'schedule' && <AdminSchedule />}
                {activeTab === 'analytics' && <AdminAnalytics />}
                {activeTab === 'customerAnalytics' && <AdminCustomerAnalytics />} 
                {activeTab === 'forecast' && <AdminForecast />}
            </div>
        </div>
    );
}

export default AdminDashboard;