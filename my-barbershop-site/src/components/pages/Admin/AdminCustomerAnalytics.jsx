import React, { useState, useEffect, useCallback } from 'react';
import api from '../../../api/api';
import styles from './AdminDashboard.module.css'; 
import Range from 'rc-slider'; 
import 'rc-slider/assets/index.css'; 

import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend } from 'chart.js';
import { Doughnut, Line } from 'react-chartjs-2';
import ClientActionModal from './ClientActionModal'; // <-- ИМПОРТИРУЕМ МОДАЛКУ

ChartJS.register( CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Title, Tooltip, Legend );

const TODAY_TS = Date.now();
const ONE_YEAR_AGO_TS = new Date(new Date().setFullYear(new Date().getFullYear() - 1)).getTime();
const ONE_MONTH_AGO_TS = new Date(new Date().setMonth(new Date().getMonth() - 1)).getTime();

const toInputFormat = (date) => (!date || isNaN(date.getTime())) ? new Date().toISOString().split('T')[0] : date.toISOString().split('T')[0];
const toLabelFormat = (timestamp) => (timestamp === null || timestamp === undefined) ? "" : new Date(timestamp).toLocaleDateString('ru-RU');

const AdminCustomerAnalytics = () => {
    const [sliderValues, setSliderValues] = useState([ONE_MONTH_AGO_TS, TODAY_TS]);
    const [startDate, setStartDate] = useState(toInputFormat(new Date(ONE_MONTH_AGO_TS))); 
    const [endDate, setEndDate] = useState(toInputFormat(new Date(TODAY_TS))); 
    
    const [kpiData, setKpiData] = useState(null);
    const [loadingKpi, setLoadingKpi] = useState(false);
    
    const [atRiskClients, setAtRiskClients] = useState([]);
    const [loadingTable, setLoadingTable] = useState(false);
    const [error, setError] = useState(null);

    // Стейт для управления модальным окном
    const [selectedClientId, setSelectedClientId] = useState(null);

    const loadKpiData = useCallback(async () => {
        try {
            setLoadingKpi(true); setError(null);
            const response = await api.get('/api/admin/customer-analytics/kpi', { params: { startDate, endDate } });
            setKpiData(response.data);
        } catch (err) {
            console.error("Ошибка при загрузке KPI:", err); setError("Не удалось загрузить часть данных.");
        } finally {
            setLoadingKpi(false);
        }
    }, [startDate, endDate]);

    const loadAtRiskTable = useCallback(async () => {
        try {
            setLoadingTable(true);
            const response = await api.get('/api/admin/customer-analytics/at-risk');
            setAtRiskClients(response.data);
        } catch (err) {
            console.error("Ошибка при загрузке таблицы риска:", err);
        } finally {
            setLoadingTable(false);
        }
    }, []);

    useEffect(() => { loadKpiData(); }, [loadKpiData]);
    useEffect(() => { loadAtRiskTable(); }, [loadAtRiskTable]);

    const handleSliderStop = (values) => {
        if (!values || typeof values[0] !== 'number' || typeof values[1] !== 'number') return;
        setStartDate(toInputFormat(new Date(values[0])));
        setEndDate(toInputFormat(new Date(values[1])));
    };

    const calculateTrend = (current, previous) => {
        if (!previous || previous === 0) return { value: 0, isPositive: true };
        const percent = ((current - previous) / previous) * 100;
        return { value: percent.toFixed(1), isPositive: current - previous >= 0 };
    };

    const getSegmentationData = () => {
        if (!kpiData) return null;
        return {
            labels: ['Новые', 'Активные', 'Зона риска', 'Окончательный отток'],
            datasets: [{
                data: [kpiData.newClients, kpiData.activeClients, kpiData.atRiskClientsSegment, kpiData.churnedClients],
                backgroundColor: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444'], borderWidth: 1,
            }]
        };
    };

    const getDynamicsData = () => {
        if (!kpiData || !kpiData.dynamicsDates) return null;
        return {
            labels: kpiData.dynamicsDates.map(d => new Date(d).toLocaleDateString('ru-RU', {day:'numeric', month:'short'})),
            datasets: [
                { label: 'Средний LTV (BYN)', data: kpiData.dynamicsLtv, borderColor: '#10b981', backgroundColor: 'rgba(16, 185, 129, 0.1)', yAxisID: 'y', tension: 0.4, fill: true },
                { label: 'Зона риска (%)', data: kpiData.dynamicsChurnRate, borderColor: '#ef4444', backgroundColor: 'rgba(239, 68, 68, 0.1)', yAxisID: 'y1', tension: 0.4, fill: true }
            ]
        };
    };

    const dynamicsOptions = {
        responsive: true, maintainAspectRatio: false, interaction: { mode: 'index', intersect: false },
        scales: {
            x: { grid: { display: false } },
            y: { type: 'linear', display: true, position: 'left', title: { display: true, text: 'LTV (BYN)' }, beginAtZero: true },
            y1: { type: 'linear', display: true, position: 'right', title: { display: true, text: 'Зона риска (%)' }, grid: { drawOnChartArea: false }, beginAtZero: true, max: 100 }
        }
    };

    return (
        <div className={styles.analyticsPage}>

            <div className={styles.dateSliderBar}>
                <span className={styles.sliderLabel}>{toLabelFormat(sliderValues[0])}</span>
                <div className={styles.sliderWrapper}>
                    <Range range min={ONE_YEAR_AGO_TS} max={TODAY_TS} value={sliderValues} onChange={setSliderValues} onChangeComplete={handleSliderStop} allowCross={false} />
                </div>
                <span className={styles.sliderLabel}>{toLabelFormat(sliderValues[1])}</span>
                {loadingKpi && <div className={styles.loader}>Обновление...</div>}
            </div>

            {error && <div style={{ color: '#dc2626', padding: '10px' }}>{error}</div>}

            {!loadingKpi && kpiData && (
                <>
                    {/* Верхний блок KPI */}
                    <div className={styles.kpiGrid}>
                        <div className={styles.kpiCard}>
                            <h4>Средний LTV базы</h4>
                            <div className={styles.kpiValue}>{kpiData.averageLtv} BYN</div>
                            <div className={styles.kpiSubtext}>
                                {(() => {
                                    const trend = calculateTrend(kpiData.averageLtv, kpiData.previousAverageLtv);
                                    return (
                                        <span className={trend.isPositive ? styles.trendPositive : styles.trendNegative}>
                                            {trend.isPositive ? '▲' : '▼'} {Math.abs(trend.value)}% 
                                            <span style={{ color: '#777', fontWeight: 'normal', marginLeft: '5px' }}>к предыдущему периоду</span>
                                        </span>
                                    );
                                })()}
                            </div>
                        </div>
                        <div className={styles.kpiCard}>
                            <h4>Клиентов в зоне риска</h4>
                            <div className={styles.kpiValue} style={{ color: '#dc2626' }}>{kpiData.clientsAtRisk} <span style={{fontSize: '1.2rem'}}>чел.</span></div>
                            <div className={styles.kpiSubtext}>Не посещали салон более 60 дней</div>
                        </div>
                        <div className={styles.kpiCard}>
                            <h4>Удержанная выручка</h4>
                            <div className={styles.kpiValue} style={{ color: '#16a34a' }}>{kpiData.retainedRevenue} BYN</div>
                            <div className={styles.kpiSubtext}>Выручка от вернувшихся (пауза {'>'}60 дней)</div>
                        </div>
                    </div>

                    {/* Графики */}
                    <div className={styles.dashboardGrid}>
                        <div className={styles.widget}>
                            <h3>Сегментация базы</h3>
                            <div style={{ height: '300px', display: 'flex', justifyContent: 'center' }}>
                                <Doughnut data={getSegmentationData()} options={{ maintainAspectRatio: false }} />
                            </div>
                        </div>
                        <div className={styles.widget}>
                            <h3>Динамика LTV и Зоны Риска</h3>
                            <div style={{ height: '300px' }}><Line data={getDynamicsData()} options={dynamicsOptions} /></div>
                        </div>
                    </div>
                </>
            )}

            {/* Таблица */}
            <div className={styles.dashboardGrid} style={{ gridTemplateColumns: '1fr', marginTop: '30px' }}>
                <div className={styles.widget}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '15px' }}>
                        <h3 style={{ margin: 0 }}>Клиенты в зоне риска</h3>
                        <span style={{ fontSize: '0.85rem', color: '#888', fontStyle: 'italic' }}>*Срез на текущий момент (не зависит от фильтра)</span>
                    </div>
                    
                    <div className={styles.tableContainer}>
                        {loadingTable ? (
                            <div className={styles.loader} style={{ textAlign: 'center', padding: '20px' }}>Загрузка таблицы...</div>
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th>Имя клиента</th>
                                        <th>Дата последнего визита</th>
                                        <th>LTV (Ценность)</th>
                                        <th>Вероятность оттока</th>
                                        <th style={{ textAlign: 'center' }}>Действия</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {atRiskClients.length > 0 ? atRiskClients.map(client => (
                                        <tr key={client.clientId}>
                                            <td style={{ fontWeight: '600' }}>{client.clientName}</td>
                                            <td>{new Date(client.lastVisitDate).toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })}</td>
                                            <td style={{ fontWeight: 'bold', color: '#10b981' }}>{client.ltv} BYN</td>
                                            <td>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                                    <div style={{ width: '100%', maxWidth: '120px', backgroundColor: '#e5e7eb', borderRadius: '4px', height: '8px', overflow: 'hidden' }}>
                                                        <div style={{ width: `${client.churnProbability}%`, backgroundColor: client.churnProbability >= 85 ? '#ef4444' : '#f59e0b', height: '100%' }}></div>
                                                    </div>
                                                    <span style={{ color: client.churnProbability >= 85 ? '#ef4444' : '#f59e0b', fontWeight: 'bold', minWidth: '40px' }}>
                                                        {client.churnProbability}%
                                                    </span>
                                                </div>
                                            </td>
                                            <td style={{ textAlign: 'center' }}>
                                                <button 
                                                    className={styles.editButton} 
                                                    style={{ minWidth: 'auto', padding: '6px 12px' }}
                                                    onClick={() => setSelectedClientId(client.clientId)} // Открываем модалку по ID
                                                >
                                                    Связаться
                                                </button>
                                            </td>
                                        </tr>
                                    )) : (
                                        <tr><td colSpan="5" style={{ textAlign: 'center', padding: '30px', color: '#777' }}>В зоне риска нет клиентов. Отличная работа!</td></tr>
                                    )}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            </div>

            {/* РЕНДЕР МОДАЛКИ (Если selectedClientId не null) */}
            {selectedClientId && (
                <ClientActionModal 
                    clientId={selectedClientId} 
                    onClose={() => setSelectedClientId(null)} 
                    onSuccess={() => {
                        setSelectedClientId(null);
                        loadAtRiskTable(); // Обновляем таблицу после успешного действия
                    }}
                />
            )}
        </div>
    );
};

export default AdminCustomerAnalytics;