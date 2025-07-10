import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer'; // Importa il componente Navbar

const HomePage = () => {
  // Rimosse le logiche della navbar e del logout, ora gestite dal componente Navbar
  const navigate = useNavigate(); // Manteniamo navigate se usato per altre logiche nella HomePage

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-green-500 to-blue-500 text-white">
      {/* Navbar riutilizzabile */}
      <Navbar />

      {/* Contenuto Principale della Homepage */}
      <main className="flex-grow flex items-center justify-center p-4 pt-16 pb-24">
        <h1 className="text-4xl md:text-5xl font-extrabold text-white text-center drop-shadow-lg">
          Benvenuto nella Homepage!
        </h1>
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
