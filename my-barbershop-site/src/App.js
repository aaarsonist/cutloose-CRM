import React, { useEffect } from 'react';
import api from './api/api';
import { Routes, Route, Navigate } from 'react-router-dom';
import Services from './components/Services';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import Home from './components/Home';
import User from './components/User';
import Admin from './components/Admin';
import './App.css';

function PrivateRoute({ children, role }) {
  const user = JSON.parse(localStorage.getItem('currentUser') || 'null');
  if (!user) return <Navigate to="/login" />;
  if (role && user.role !== role) return <Navigate to="/" />;
  return children;
}

function LoginRouteHandler({ children }) {
  const user = JSON.parse(localStorage.getItem('currentUser') || 'null');

  if (user) {
      console.log("User found in localStorage, redirecting from /login");
      return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/user'} replace />; 
  }

  console.log("User not found in localStorage, rendering LoginPage"); 
  return <LoginPage />;
}

function App() {
  useEffect(() => {
    api.get('/api/users/current')
       .then(resp => localStorage.setItem('currentUser', JSON.stringify(resp.data)))
       .catch(() => localStorage.removeItem('currentUser'));
  }, []);

  return (
    <div className="App">
        <Routes>
          <Route path="/" element={<Home />} />        
          <Route path="/services" element={<Services />} /> 
          <Route path="/login" element={<LoginRouteHandler />} />   
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/user"
          element={
            <PrivateRoute role="USER">
              <User />
            </PrivateRoute>
          }
        />
        <Route path="/admin"
          element={
            <PrivateRoute role="ADMIN">
              <Admin />
            </PrivateRoute>
          }
        />

        <Route path="*" element={<div>404 — страница не найдена</div>} />
        </Routes>
    </div>
  );
}

export default App;
