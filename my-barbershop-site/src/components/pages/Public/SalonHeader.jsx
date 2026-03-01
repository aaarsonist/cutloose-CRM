import React from 'react';
import { Link } from 'react-router-dom'
import styles from './SalonHeader.module.css';
import mainPicture from '../../../images/mainPicture.png';
import YandexMap from '../../YandexMap/YandexMap';

export default function SalonHeader() {
  return (
    <header className={styles.mainSection}>
      <img 
        loading="lazy" 
        src={mainPicture}
        className={styles.heroImage}
        alt="Salon interior view" 
      />
      
      <section className={styles.contentWrapper}>
        <h1 className={styles.title}>
          Ваша красота – это <br /> отражение заботы о себе!
        </h1>
        <p className={styles.subtitle}>
          Вы готовы начать свое преображение? <br />
          Тогда Добро Пожаловать! <br />
        </p>
      </section>

      <Link to="/services">
        <button className={styles.shopButton}>
          Перейти в каталог услуг
        </button>
      </Link>

    <section className={styles.container}>
    <div className={styles.card}>
        <div className={styles.iconWrapper}>
        <div className={`${styles.icon} ${styles.phoneIcon}`} aria-hidden="true" />
        </div>
        <a href="tel:+375256554433" className={styles.phoneNumber}>
        +375 25 655-44-33
        </a>
        <p className={styles.description}>Запись и информация</p>
    </div>

    <div className={`${styles.card} ${styles.highlightedCard}`}>
        <div className={styles.iconWrapper}>
        <div className={`${styles.icon} ${styles.locationIcon}`} aria-hidden="true" />
        </div>
        <address className={styles.address}>
        г. Минск <br />пр. Машерова, 54
        </address>
        <p className={styles.descriptionAdress}>Вход со стороны ул. Кропоткина</p>
    </div>

    <div className={styles.card}>
        <div className={styles.iconWrapper}>
        <div className={`${styles.icon} ${styles.hoursIcon}`} aria-hidden="true" />
        </div>
        <time className={styles.hours}>9:00 - 21:00</time>
        <p className={styles.description}>Ежедневно</p>
    </div>
    </section>
    <div className={styles.locationSection}> 
         <h2 className={styles.locationTitle}>Мы находимся здесь</h2>
         <YandexMap />
      </div>
    </header>
  );
}