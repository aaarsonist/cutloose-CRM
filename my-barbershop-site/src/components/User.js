import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import UserDashboard from './pages/User/UserDashboard';
import Footer from './Footer/Footer';

function User(){
    return(
    <div className="App">
      <Navigation />
      <div className="main-wrapper">
                <UserDashboard />
            </div>
      <section id="about">
        <Footer/>
      </section>     
    </div>
    )
}

export default User;