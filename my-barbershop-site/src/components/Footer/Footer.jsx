import React from 'react';
import styles from './Footer.module.css';
import logo from '../../images/logo.png';

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div className={styles.logoSection} onClick={() => window.location.href = '/'}>
                <img src={logo} alt="Logo" className={styles.logo} />
                <h1 className={styles.brandName}>CutLoose</h1>
            </div>

            <div className={styles.infoSection}>
                <div className={styles.infoItem}>
                    <a href="mailto:anyalukashik@gmail.com" className={styles.infoText}>
                        anyalukashik@gmail.com
                    </a>
                </div>
                <div className={styles.infoItem}>
                    <p className={styles.infoText}>
                        г. Минск, пр. Машерова, 54
                    </p>
                </div>
            </div>

            <div className={styles.contactSection}>
                <div className={styles.contactItem}>
                    <span className={`${styles.icon} ${styles.phoneIcon}`} />
                    <a href="tel:+375256554433" className={styles.contactText}>+375 25 655-44-33</a>
                </div>
                <div className={styles.contactItem}>
                    <span className={`${styles.icon} ${styles.clockIcon}`} />
                    <p className={styles.contactText}>9:00 - 21:00</p>
                </div>
            </div>
        </footer>
    );
};

export default Footer;