import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import Men from './pages/Public/Men';
import Women from './pages/Public/Women';
import Footer from './Footer/Footer';

function Services(){
    return (
    <div className="App">
      <Navigation /> 
      <Women/>
      <Men/>
      <section id="about">
        <Footer/>
      </section>      
    </div>
    )
}

export default Services;