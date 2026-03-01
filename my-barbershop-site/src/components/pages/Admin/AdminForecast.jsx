import React, { useEffect, useState } from 'react';
import api from '../../../api/api';
import styles from './AdminDashboard.module.css'; 

import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    Title,
    Tooltip,
    Legend
} from 'chart.js';
import { Bar } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

function AdminForecast() {
    const [forecastData, setForecastData] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchForecast = async () => {
            setIsLoading(true);
            try {
                const response = await api.get('/api/forecast/weekly');
                setForecastData(response.data);
            } catch (error) {
                console.error("Ошибка при загрузке прогноза:", error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchForecast();
    }, []);

    const forecastChartData = {
        labels: forecastData.map(d => d.dayOfWeekRussian),
        datasets: [
            {
                label: 'Предложение (часы)',
                data: forecastData.map(d => d.supplyHours.toFixed(1)),
                backgroundColor: 'rgba(54, 162, 235, 0.6)', 
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
            },
            {
                label: 'Спрос (сред. часы)',
                data: forecastData.map(d => d.demandHours.toFixed(1)),
                backgroundColor: 'rgba(255, 159, 64, 0.6)', 
                borderColor: 'rgba(255, 159, 64, 1)',
                borderWidth: 1,
            }
        ]
    };

    const forecastChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'top' },
            title: { display: false },
        },
        scales: {
            y: {
                beginAtZero: true,
                title: { display: true, text: 'Часы' }
            }
        }
    };

    if (isLoading) {
        return <div className={styles.loader}>Загрузка прогнозов...</div>;
    }

    return (
        <div className={styles.forecastSection} style={{ marginTop: 0 }}>
            <h3>Прогноз спроса и рекомендации</h3>

            <div className={styles.dashboardGrid}>
                <div className={styles.widget}>
                    <h3>Прогноз спроса vs предложение (в часах)</h3>
                    {forecastData.length > 0 ? (
                        <div style={{ height: '300px' }}>
                            <Bar options={forecastChartOptions} data={forecastChartData} />
                        </div>
                    ) : "Нет данных для прогноза"}
                </div>

                <div className={styles.widget}>
                    <h3>Рекомендации по оптимизации</h3>
                    <ul className={styles.recommendationList}>
                        {forecastData.map(day => (
                            <li key={day.dayOfWeek} className={styles[day.recommendationLevel.toLowerCase()]}>
                                <strong>{day.dayOfWeekRussian}:</strong> {day.recommendationText}
                            </li>
                        ))}
                    </ul>
                </div>
            </div>
        </div>
    );
}

export default AdminForecast;