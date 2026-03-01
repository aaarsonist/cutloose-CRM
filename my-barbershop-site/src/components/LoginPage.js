import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import Footer from './Footer/Footer';
import Login from './pages/Auth/Login';

function LoginPage(){
    return (
    <div className="App">
      <Navigation /> 
      <Login/>
      <section id="about">
        <Footer/>
      </section>
      
    </div>
    )
}

export default LoginPage;