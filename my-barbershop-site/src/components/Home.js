import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import SalonHeader from './pages/Public/SalonHeader';
import Footer from './Footer/Footer';

function Home(){
    return(
    <div className="App">
    <Navigation />
      <SalonHeader/>
      <section id="about">
        <Footer/>
      </section>     
    </div>
    )
}

export default Home;