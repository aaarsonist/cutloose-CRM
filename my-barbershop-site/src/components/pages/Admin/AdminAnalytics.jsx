import React, { useEffect, useState, useCallback, useRef } from 'react'; 
import api from '../../../api/api';
import styles from './AdminDashboard.module.css'; 

import Range from 'rc-slider'; 
import 'rc-slider/assets/index.css'; 

import { 
    Chart as ChartJS, 
    CategoryScale, 
    LinearScale, 
    BarElement, 
    PointElement, 
    LineElement, 
    Title, 
    Tooltip, 
    Legend,
    ArcElement 
} from 'chart.js';
import { Bar, Line, Doughnut } from 'react-chartjs-2';
import Select from 'react-select';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import moment from 'moment';
import '../../../assets/fonts/Mulish-Regular-normal.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, Title, Tooltip, Legend, ArcElement);

const formatLocalDate = (localDate) => {
    if (!localDate) return '';
    if (Array.isArray(localDate)) { 
        const date = new Date(localDate[0], localDate[1] - 1, localDate[2]);
        return date.toLocaleString('ru-RU', { month: 'long', day: 'numeric' });
    }
    if (typeof localDate === 'string') { 
        const parts = localDate.split('-');
        if (parts.length === 3) {
            const date = new Date(parts[0], parts[1] - 1, parts[2]);
            return date.toLocaleString('ru-RU', { month: 'long', day: 'numeric' });
        }
    }
    return String(localDate); 
};
const renderStars = (rating) => {
    if (typeof rating !== 'number' || rating < 1 || rating > 5) return 'N/A';
    const fullStar = '★';
    const emptyStar = '☆';
    return fullStar.repeat(Math.round(rating)) + emptyStar.repeat(5 - Math.round(rating));
};
const formatCurrency = (amount) => {
    if (amount === null || amount === undefined) return '0 BYN';
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'BYN' }).format(amount);
};
const toInputFormat = (date) => {
    if (!date || isNaN(date.getTime())) {
        return new Date().toISOString().split('T')[0]; 
    }
    return date.toISOString().split('T')[0];
};
const toLabelFormat = (timestamp) => {
    if (timestamp === null || timestamp === undefined) return "";
    return new Date(timestamp).toLocaleDateString('ru-RU');
};


const TODAY_TS = Date.now();
const ONE_YEAR_AGO_TS = new Date(new Date().setFullYear(new Date().getFullYear() - 1)).getTime();
const ONE_MONTH_AGO_TS = new Date(new Date().setMonth(new Date().getMonth() - 1)).getTime();


function AdminAnalytics() {
    const [salesData, setSalesData] = useState(null); 
    const [serviceData, setServiceData] = useState(null);
    const [masterData, setMasterData] = useState(null); 
    
    const [sliderValues, setSliderValues] = useState([ONE_MONTH_AGO_TS, TODAY_TS]);
    const [startDate, setStartDate] = useState(toInputFormat(new Date(ONE_MONTH_AGO_TS))); 
    const [endDate, setEndDate] = useState(toInputFormat(new Date(TODAY_TS))); 
    const [selectedServices, setSelectedServices] = useState([]);
    const [selectedMasters, setSelectedMasters] = useState([]);
    const [serviceOptions, setServiceOptions] = useState([]);
    const [masterOptions, setMasterOptions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [extendedData, setExtendedData] = useState(null);

    const revenueChartRef = useRef(null);
    const visitChartRef = useRef(null);

    useEffect(() => {
        const fetchFilterOptions = async () => {
            try {
                const [servicesRes, mastersRes] = await Promise.all([
                    api.get('/services'), 
                    api.get('/api/masters') 
                ]);
                setServiceOptions(servicesRes.data.map(s => ({ value: s.id, label: s.name })));
                setMasterOptions(mastersRes.data.map(m => ({ value: m.id, label: m.name })));
            } catch (error) {
                console.error("Ошибка загрузки опций для фильтров:", error);
            }
        };
        fetchFilterOptions();
    }, []);

    const fetchDashboardData = useCallback(async () => {
        setIsLoading(true);

        const isoStartDate = startDate ? new Date(startDate).toISOString() : null;
        const isoEndDate = endDate ? new Date(new Date(endDate).setHours(23, 59, 59)).toISOString() : null; 
        const serviceIds = selectedServices.map(s => s.value);
        const masterIds = selectedMasters.map(m => m.value);
        const params = { startDate: isoStartDate, endDate: isoEndDate, serviceIds: serviceIds, masterIds: masterIds };
        
        const config = {
            params: params,
            paramsSerializer: {
                serialize: (params) => {
                    const searchParams = new URLSearchParams();
                    for (const key in params) {
                        const value = params[key];
                        if (Array.isArray(value)) {
                            value.forEach(item => searchParams.append(key, item));
                        } else if (value !== null && value !== undefined) {
                            searchParams.append(key, value);
                        }
                    }
                    return searchParams.toString();
                }
            }
        };

        try {
            const [ salesRes, serviceRes, masterRes, extendedRes ] = await Promise.all([
                api.get('/api/reports/sales', config), 
                api.get('/api/reports/services', config), 
                api.get('/api/reports/masters', config),
                api.get('/api/reports/extended', { params: { startDate: isoStartDate, endDate: isoEndDate } })
            ]);

            setSalesData(salesRes.data);
            setServiceData(serviceRes.data);
            setMasterData(masterRes.data); 
            setExtendedData(extendedRes.data);
            setExtendedData(extendedRes.data);

        } catch (error) {
            console.error("Ошибка при загрузке данных дашборда:", error);
            alert("Не удалось загрузить аналитику. " + (error.response?.data?.message || error.message));
        } finally {
            setIsLoading(false);
        }
    }, [startDate, endDate, selectedServices, selectedMasters]); 

    useEffect(() => {
        fetchDashboardData();
    }, [fetchDashboardData]); 

    const handleSliderStop = (values) => {
        if (!values || typeof values[0] !== 'number' || typeof values[1] !== 'number') {
            console.error("handleSliderStop: получены неверные значения", values);
            return;
        }
        const newStartDate = toInputFormat(new Date(values[0]));
        const newEndDate = toInputFormat(new Date(values[1]));
        setStartDate(newStartDate);
        setEndDate(newEndDate);
    };

    const chartOptions = (title) => ({
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { display: false },
            title: { display: true, text: title, font: { size: 14 } },
        },
        scales: { y: { beginAtZero: true } }
    });
    
    const salesChart = {
        labels: salesData?.dailyRevenueDataPoints.map(dp => formatLocalDate(dp.date)) || [],
        datasets: [{
            label: 'Выручка (BYN)',
            data: salesData?.dailyRevenueDataPoints.map(dp => dp.totalRevenue) || [],
            borderColor: '#FF9966',
            backgroundColor: '#FF9966',
            fill: true,
        }],
    };

    const serviceChart = {
        labels: serviceData?.visitData.map(dp => formatLocalDate(dp.date)) || [],
        datasets: [{
            label: 'Кол-во записей',
            data: serviceData?.visitData.map(dp => dp.totalVisits) || [],
            backgroundColor: '#fe7777ff',
        }],
    };
    
const handleDownloadPDF = () => {
        const doc = new jsPDF();
        let y = 20;

        doc.setFont('Mulish', 'normal');

        doc.setFontSize(18);
        doc.text("Отчет по работе салона красоты CutLoose", 14, y);
        y += 15;

        doc.setFontSize(12);
        doc.text("Параметры отчета:", 14, y);
        y += 7;

        doc.setFontSize(10);
        const dateRangeStr = `Диапазон дат: ${moment(startDate).format('DD.MM.YYYY')} - ${moment(endDate).format('DD.MM.YYYY')}`;
        doc.text(dateRangeStr, 14, y);
        y += 7;

        let mastersStr = "Мастера: ";
        if (selectedMasters.length === 0) {
            mastersStr += "Все мастера";
        } else {
            mastersStr += selectedMasters.map(selected =>
                masterOptions.find(opt => opt.value === selected.value)?.label
            ).join(', ');
        }
        if (mastersStr.length > 90) mastersStr = mastersStr.substring(0, 90) + "...";
        doc.text(mastersStr, 14, y);
        y += 7;

        let servicesStr = "Услуги: ";
        if (selectedServices.length === 0) {
            servicesStr += "Все услуги";
        } else {
            servicesStr += selectedServices.map(selected =>
                serviceOptions.find(opt => opt.value === selected.value)?.label
            ).join(', ');
        }
        if (servicesStr.length > 90) servicesStr = servicesStr.substring(0, 90) + "...";
        doc.text(servicesStr, 14, y);
        y += 15;

        if (extendedData) {
            doc.setFontSize(14);
            doc.text("Ключевые показатели эффективности (KPI)", 14, y);
            y += 8;

            doc.setFontSize(10);

            let revenueText = `Общая выручка: ${formatCurrency(extendedData.totalRevenue)}`;
            if (extendedData.totalRevenueTrend != null) {
                const symbol = extendedData.totalRevenueTrend >= 0 ? "▲" : "▼";
                revenueText += ` (${symbol} ${Math.abs(extendedData.totalRevenueTrend).toFixed(1)}% к пред. периоду)`;
            }
            doc.text(revenueText, 14, y);
            y += 6;

            const retention = extendedData.retentionRate ? extendedData.retentionRate.toFixed(1) : 0;
            doc.text(`Коэффициент удержания: ${retention}% клиентов вернулись за повторной услугой`, 14, y);
            y += 6;

            if (extendedData.topMasterName) {
                doc.text(`Топ мастер: ${extendedData.topMasterName}`, 14, y);
                y += 5;
                doc.setTextColor(100); 
                doc.text(`   Рейтинг: ${extendedData.topMasterRating?.toFixed(2)} | Выручка: ${formatCurrency(extendedData.topMasterRevenue)}`, 14, y);
                doc.setTextColor(0); 
            } else {
                doc.text(`Топ мастер: Нет данных`, 14, y);
            }
            y += 15;
        }

        const tableStyles = {
            font: 'Mulish',
            fontStyle: 'normal'
        };

        if (extendedData) {
            const catMeta = { HAIR: 'Волосы', FACE: 'Лицо', NAILS: 'Ногти', BEARD: 'Борода' };
            const dist = extendedData.categoryDistribution || {};
            const totalCounts = Object.values(dist).reduce((a, b) => a + b, 0);

            const categoryBody = Object.keys(catMeta)
                .map(key => {
                    const count = dist[key] || 0;
                    const percent = totalCounts > 0 ? ((count / totalCounts) * 100).toFixed(1) + '%' : '0%';
                    return [catMeta[key], count, percent];
                })
                .sort((a, b) => b[1] - a[1]); 

            if (categoryBody.length > 0) {
                doc.setFontSize(12);
                doc.text("Доли категорий услуг", 14, y);
                y += 5;

                autoTable(doc, {
                    startY: y,
                    head: [['Категория', 'Кол-во', 'Доля']],
                    body: categoryBody,
                    theme: 'grid',
                    styles: tableStyles,
                    headStyles: { fillColor: [255, 153, 102] } 
                });
                y = doc.lastAutoTable.finalY + 10;
            }

            const topServices = extendedData.topServices || [];
            if (topServices.length > 0) {
                if (y > 240) { doc.addPage(); y = 20; }

                doc.setFontSize(12);
                doc.text("Топ популярных услуг", 14, y);
                y += 5;

                const topBody = topServices.map(s => [s.serviceName, s.usageCount]);

                autoTable(doc, {
                    startY: y,
                    head: [['Услуга', 'Количество записей']],
                    body: topBody,
                    theme: 'grid',
                    styles: tableStyles,
                    headStyles: { fillColor: [240, 165, 0] } 
                });
                y = doc.lastAutoTable.finalY + 15;
            }
        }

        if (salesData && salesData.dailyRevenueDataPoints && salesData.dailyRevenueDataPoints.length > 0) {
            if (y > 240) { doc.addPage(); y = 20; }
            
            doc.setFontSize(14);
            doc.text("Динамика выручки по дням", 14, y);
            y += 10;

            const revenueHead = [['Дата', 'Выручка']];
            const revenueBody = salesData.dailyRevenueDataPoints.map(dp => [
                formatLocalDate(dp.date),
                formatCurrency(dp.totalRevenue)
            ]);

            autoTable(doc, {
                startY: y,
                head: revenueHead,
                body: revenueBody,
                theme: 'grid',
                styles: tableStyles,
                alternateRowStyles: { fillColor: [245, 245, 245] }
            });
            y = doc.lastAutoTable.finalY + 15;
        }

        if (serviceData && serviceData.visitData && serviceData.visitData.length > 0) {
            if (y > 240) { doc.addPage(); y = 20; }

            doc.setFontSize(14);
            doc.text("Динамика посещений по дням", 14, y);
            y += 10;

            const visitsHead = [['Дата', 'Количество записей']];
            const visitsBody = serviceData.visitData.map(dp => [
                formatLocalDate(dp.date),
                dp.totalVisits
            ]);

            autoTable(doc, {
                startY: y,
                head: visitsHead,
                body: visitsBody,
                theme: 'grid',
                styles: tableStyles,
                alternateRowStyles: { fillColor: [245, 245, 245] }
            });
            y = doc.lastAutoTable.finalY + 15;
        }

        if (masterData && masterData.masterPerformanceData.length > 0) {
            doc.setFontSize(14);
            doc.text("Эффективность мастеров", 14, y);
            y += 10;

            const masterHead = [['Мастер', 'Записей', 'Выручка', 'Средняя оценка']];
            const masterBody = masterData.masterPerformanceData.map(m => [
                m.masterFullName,
                m.appointmentCount,
                formatCurrency(m.totalRevenue),
                m.averageRating ? m.averageRating.toFixed(1) : 'N/A'
            ]);

            autoTable(doc, {
                startY: y,
                head: masterHead,
                body: masterBody,
                theme: 'grid',
                styles: tableStyles
            });
            y = doc.lastAutoTable.finalY + 15;
        }

        if (serviceData && serviceData.averageRatings.length > 0) {
            if (y > 250) {
                doc.addPage();
                y = 20;
            }
            doc.setFontSize(14);
            doc.text("Средние оценки услуг", 14, y);
            y += 10;

            const serviceHead = [['Услуга', 'Средняя оценка']];
            const serviceBody = serviceData.averageRatings.map(s => [
                s.serviceName,
                s.averageRating ? `${s.averageRating.toFixed(1)} ${renderStars(s.averageRating)}` : 'N/A'
            ]);

            autoTable(doc, {
                startY: y,
                head: serviceHead,
                body: serviceBody,
                theme: 'grid',
                styles: tableStyles
            });
        }

        doc.save(`analytics_report_${moment(startDate).format('YYYY-MM-DD')}.pdf`);
    };
    const categoryMeta = {
        HAIR:  { label: 'Волосы', color: '#FF6666' }, 
        FACE:  { label: 'Лицо',   color: '#FF9966' }, 
        NAILS: { label: 'Ногти',  color: '#FFCC99' }, 
        BEARD: { label: 'Борода', color: '#FFFFCC' }
    };

    const sortedCategories = Object.keys(categoryMeta)
        .map(key => ({
            label: categoryMeta[key].label,
            value: extendedData?.categoryDistribution?.[key] || 0,
            color: categoryMeta[key].color
        }))
        .sort((a, b) => a.value - b.value);

    const categoryChartData = {
        labels: sortedCategories.map(item => item.label),
        datasets: [{
            data: sortedCategories.map(item => item.value),
            backgroundColor: sortedCategories.map(item => item.color),
            hoverBackgroundColor: sortedCategories.map(item => item.color),
            borderWidth: 1
        }]
    };

    const genderChartOptions = {
        responsive: true,
        maintainAspectRatio: false, 
        plugins: {
            legend: {
                position: 'top',    
                reverse: true,      
                align: 'center',     
                labels: {
                    usePointStyle: true, 
                    padding: 20          
                }
            },
            tooltip: {
                callbacks: {
                    label: function(context) {
                        let label = context.label || '';
                        if (label) {
                            label += ': ';
                        }
                        const value = context.raw;
                        const total = context.dataset.data.reduce((acc, curr) => acc + curr, 0);
                        const percentage = total > 0 ? ((value / total) * 100).toFixed(1) + '%' : '0%';

                        return label + percentage; 
                    }
                }
            }
        }
    };

    const funnelChartData = {
        labels: extendedData?.topServices?.map(s => s.serviceName) || [],
        datasets: [{
            label: 'Количество записей',
            data: extendedData?.topServices?.map(s => s.usageCount) || [],
            backgroundColor: '#f0a500',
            indexAxis: 'y', 
        }]
    };
    
    const funnelOptions = {
        indexAxis: 'y',
        responsive: true,
        plugins: { legend: { display: false }, title: { display: true, text: 'Топ-5 популярных услуг' } }
    };
    
    return (
        <div className={styles.analyticsPage}>
           <div className={styles.kpiGrid}>
                <div className={styles.kpiCard}>
                    <h4>Топ Мастер</h4>
                    {extendedData?.topMasterName ? (
                        <>
                            <div className={styles.kpiValue}>{extendedData.topMasterName}</div>
                            <div className={styles.kpiSubtext}>
                                ★ {extendedData.topMasterRating?.toFixed(2)} | {formatCurrency(extendedData.topMasterRevenue)}
                            </div>
                        </>
                    ) : <span>Нет данных</span>}
                </div>

                <div className={styles.kpiCard}>
                    <h4>Общая выручка</h4>
                    <div className={styles.kpiValue}>
                        {formatCurrency(extendedData?.totalRevenue)}
                    </div>
                    <div className={styles.kpiSubtext}>
                        {extendedData?.totalRevenueTrend != null ? (
                            <>
                                {extendedData.totalRevenueTrend >= 0 ? (
                                    <span style={{color: 'green'}}>▲ {extendedData.totalRevenueTrend.toFixed(1)}%</span>
                                ) : (
                                    <span style={{color: 'red'}}>▼ {Math.abs(extendedData.totalRevenueTrend).toFixed(1)}%</span>
                                )}
                                {' к предыдущему периоду'}
                            </>
                        ) : (
                            <span style={{color: '#ccc', fontSize: '0.9em'}}>Нет данных для сравнения</span>
                        )}
                    </div>
                </div>

                <div className={styles.kpiCard}>
                    <h4>Коэффициент удержания</h4>
                    <div className={styles.kpiValue}>
                        {extendedData?.retentionRate ? extendedData.retentionRate.toFixed(1) : 0}%
                    </div>
                    <div className={styles.kpiSubtext}>Клиентов вернулись за повторной услугой</div>
                </div>
            </div>

            <div className={styles.dashboardGrid}>
                <div className={styles.widget}>
                    <h3>Доли категорий услуг</h3>
                    <div style={{ height: '250px', display: 'flex', justifyContent: 'center' }}>
                        {extendedData ? <Doughnut data={categoryChartData} options={genderChartOptions} /> : "Загрузка..."}
                    </div>
                </div>
                <div className={styles.widget}>
                    <h3>Топ популярных услуг</h3>
                    <div style={{ height: '250px' }}>
                        {extendedData ? <Bar data={funnelChartData} options={funnelOptions} /> : "Загрузка..."}
                    </div>
                </div>
            </div>

            <div className={styles.dateSliderBar}>
                <span className={styles.sliderLabel}>{toLabelFormat(sliderValues[0])}</span>
                <div className={styles.sliderWrapper}>
                    <Range
                        range 
                        min={ONE_YEAR_AGO_TS}
                        max={TODAY_TS}
                        value={sliderValues}
                        onChange={setSliderValues} 
                        onChangeComplete={handleSliderStop} 
                        allowCross={false}
                    />
                </div>
                <span className={styles.sliderLabel}>{toLabelFormat(sliderValues[1])}</span>
                {isLoading && <div className={styles.loader}>Обновление...</div>}
            </div>

            <div className={styles.filterBar}>
                <div className={styles.filterGroupWide}>
                    <Select
                        isMulti options={serviceOptions}
                        value={selectedServices} onChange={setSelectedServices}
                        className={styles.selectMulti} placeholder="Все услуги"
                    />
                </div>
                <div className={styles.filterGroupWide}>
                    <Select
                        isMulti options={masterOptions}
                        value={selectedMasters} onChange={setSelectedMasters}
                        className={styles.selectMulti} placeholder="Все мастера"
                    />
                </div>

                <button 
                    onClick={handleDownloadPDF} 
                    className={styles.pdfButton}
                    disabled={isLoading}
                >
                {isLoading ? "Загрузка..." : "Отчет PDF"}
                </button>
            </div>


            <div className={styles.dashboardGrid}>
                <div className={styles.widget}>
                    {salesData ? <Line options={chartOptions('Динамика выручки')} data={salesChart} ref={revenueChartRef} /> : "Загрузка..."}
                </div>
                <div className={styles.widget}>
                    {serviceData ? <Bar options={chartOptions('Динамика посещений')} data={serviceChart} ref={visitChartRef} /> : "Загрузка..."}
                </div>
            </div>
            <div className={styles.dashboardGrid}>
                <div className={styles.widget}>
                    <h3>Эффективность мастеров</h3>
                    <div className={styles.tableContainer}>
                        <table>
                            <thead>
                                <tr><th>Мастер</th><th>Записей</th><th>Выручка</th><th>Средняя оценка</th></tr>
                            </thead>
                            <tbody>
                                {masterData?.masterPerformanceData.length > 0 ? masterData.masterPerformanceData.map(m => (
                                    <tr key={m.masterId}>
                                        <td>{m.masterFullName}</td>
                                        <td>{m.appointmentCount}</td>
                                        <td>{formatCurrency(m.totalRevenue)}</td>
                                        <td>{m.averageRating ? m.averageRating.toFixed(1) : 'N/A'} {renderStars(m.averageRating)}</td>
                                    </tr>
                                )) : <tr><td colSpan="4">Нет данных</td></tr>}
                            </tbody>
                        </table>
                    </div>
                </div>
                <div className={styles.widget}>
                    <h3>Средние оценки услуг</h3>
                    <div className={styles.tableContainer}>
                        <table>
                            <thead>
                                <tr><th>Услуга</th><th>Средняя оценка</th></tr>
                            </thead>
                            <tbody>
                                {serviceData?.averageRatings.length > 0 ? serviceData.averageRatings.map(r => (
                                    <tr key={r.serviceId}>
                                        <td>{r.serviceName}</td>
                                        <td>{r.averageRating ? r.averageRating.toFixed(1) : 'N/A'} {renderStars(r.averageRating)}</td>
                                    </tr>
                                )) : <tr><td colSpan="2">Нет данных</td></tr>}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            
        </div>
    );
}

export default AdminAnalytics;