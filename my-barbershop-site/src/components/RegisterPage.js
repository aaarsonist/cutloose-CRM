import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import Footer from './Footer/Footer';
import Register from './pages/Auth/Register';

function RegisterPage(){
    return (
    <div className="App">
      <Navigation /> 
      <Register/ >
      <section id="about">
        <Footer/>
      </section>
      
    </div>
    )
}

export default RegisterPage;