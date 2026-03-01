import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import UserDashboard from './pages/User/UserDashboard';
import Footer from './Footer/Footer';

function User(){
    return(
    <div className="App">
      <Navigation />
      <UserDashboard />
      <section id="about">
        <Footer/>
      </section>     
    </div>
    )
}

export default User;