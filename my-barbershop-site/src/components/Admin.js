import React from 'react';
import '../App.css';
import Navigation from './Navigation/Navigation';
import AdminDashboard from './pages/Admin/AdminDashboard';
import Footer from './Footer/Footer';

function Admin(){
    return(
    <div className="App">
      <Navigation />
      <AdminDashboard />
      <section id="about">
        <Footer/>
      </section>     
    </div>
    )
}

export default Admin;