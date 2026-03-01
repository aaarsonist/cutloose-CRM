import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import styles from "./PricingSection.module.css";
import men from "../../../images/men.png";

function Men() {
  const [services, setServices] = useState([]);

  useEffect(() => {
    const fetchServices = async () => {
      try {
        const response = await axios.get("http://localhost:8080/services/men"); 
        setServices(response.data); 
      } catch (error) {
        console.error("Ошибка при загрузке мужских услуг:", error);
      }
    };

    fetchServices();
  }, []);

  return (
    <section className={styles.pricingSection}>
      <h2 className={styles.sectionTitle}>Мужской зал</h2>
      <div className={styles.pricingContainer}>
        <div className={styles.imageColumn}>
          <img
            loading="lazy"
            src={men}
            className={styles.serviceImage}
            alt="Barbershop services illustration"
          />
        </div>
        <div className={styles.serviceColumn}>
          <ul className={styles.serviceList}>
            {services.map((service) => (
              <li key={service.id} className={styles.serviceItem}>
                <span className={styles.serviceName}>{service.name}</span>
                <span className={styles.servicePrice}>{service.price} руб.</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
      <Link to="/login">
        <button className={styles.bookButton} tabIndex="0">
          Записаться онлайн
        </button>
      </Link>
    </section>
  );
}

export default Men;
