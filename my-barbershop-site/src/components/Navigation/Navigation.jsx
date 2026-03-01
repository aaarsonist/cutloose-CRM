import React from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import styles from "./Navigation.module.css";
import logo from "../../images/logo.png";
import api from '../../api/api';

const Navigation = () => {
  const location = useLocation(); 
  const navigate = useNavigate();

  const handleLogout = async () => {
  try {
    await api.post('/api/users/logout');
    console.log("Backend logout request successful."); 
    
    localStorage.removeItem('currentUser'); 
    console.log("Frontend state cleared."); 
    
    navigate("/"); 
    console.log("Navigated to /login."); 
    
    
    } catch (error) {
    console.error("Error during logout:", error);
    localStorage.removeItem('currentUser');
    navigate("/");
    alert("Произошла ошибка при выходе."); 
    }
  };

  const navItems = [
    { label: "Услуги", href: "/services", className: styles.navItem },
    { label: "Контакты", href: "#about" },
  ];

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.wrapper}>
          <section 
            className={styles.logoSection} 
            onClick={() => navigate("/")} 
            style={{ cursor: "pointer" }} 
          >
            <div className={styles.logoWrapper}>
              <img
                loading="lazy"
                src={logo}
                className={styles.logo}
                alt="CutLoose logo"
              />
              <h1 className={styles.brandName}>CutLoose</h1>
            </div>
          </section>

          <nav className={styles.navSection}>
            <ul className={styles.navList}>
              {navItems
                .filter((item) => !(location.pathname === "/services" && item.href === "/services")) 
                .map((item, index) => (
                  <li key={index} className={item.className}>
                    {item.href.startsWith("/") ? (
                      <Link to={item.href} tabIndex="0">
                        {item.label}
                      </Link>
                    ) : (
                      <a href={item.href} tabIndex="0">
                        {item.label}
                      </a>
                    )}
                  </li>
                ))}

              <li>
                {location.pathname === "/user" || location.pathname === "/admin" ? (
                  <button
                    onClick={handleLogout}
                    className={styles.bookingButton}
                    tabIndex="0"
                  >
                    Выйти из личного кабинета
                  </button>
                ) : location.pathname === "/login" || location.pathname === "/register" ? null : ( 
                  <Link to="/login">
                    <button className={styles.bookingButton} tabIndex="0">
                      Личный кабинет
                    </button>
                  </Link>
                )}
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Navigation;