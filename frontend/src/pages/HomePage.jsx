import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const HomePage = () => {
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleProfileClick = () => {
    navigate('/profile'); // Naviga alla pagina del profilo
  };

  const handleLogout = () => {
    localStorage.clear(); // Pulisce il token di autenticazione
    navigate('/'); // Reindirizza alla pagina di login
  };

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-green-500 to-blue-500 text-white">
      {/* Navbar */}
      <nav className="bg-gray-800 p-4 shadow-lg">
        <div className="container mx-auto flex justify-between items-center">
          {/* Logo o Nome App */}
          <div className="text-2xl font-bold text-white">PixelPals</div>

          {/* Hamburger Menu per Mobile */}
          <div className="md:hidden">
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="text-white focus:outline-none"
            >
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16M4 18h16"
                ></path>
              </svg>
            </button>
          </div>

          {/* Menu di Navigazione Desktop */}
          <div className="hidden md:flex items-center space-x-6">
            <a
              href="/home"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Home
            </a>
            {/* Aggiungi altri link se necessario */}
            <button
              onClick={handleProfileClick}
              className="flex items-center text-white hover:text-gray-300 transition duration-300 focus:outline-none"
              aria-label="Vai al profilo"
            >
              {/* Icona Utente (SVG semplice) */}
              <svg
                className="w-6 h-6 mr-1"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                ></path>
              </svg>
              Profilo
            </button>
            <button
              onClick={handleLogout}
              className="text-red-400 hover:text-red-300 transition duration-300 focus:outline-none"
            >
              Logout
            </button>
          </div>
        </div>

        {/* Menu Mobile (mostrato solo quando isMobileMenuOpen è true) */}
        {isMobileMenuOpen && (
          <div className="md:hidden mt-4 space-y-2">
            <a
              href="/home"
              className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              Home
            </a>
            {/* Aggiungi altri link se necessario */}
            <button
              onClick={handleProfileClick}
              className="w-full text-left flex items-center text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md focus:outline-none"
            >
              <svg
                className="w-6 h-6 mr-1"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                ></path>
              </svg>
              Profilo
            </button>
            <button
              onClick={handleLogout}
              className="w-full text-left text-red-400 hover:text-red-300 transition duration-300 px-3 py-2 rounded-md focus:outline-none"
            >
              Logout
            </button>
          </div>
        )}
      </nav>

      {/* Contenuto Principale della Homepage */}
      <main className="flex-grow flex items-center justify-center p-4">
        <h1 className="text-4xl md:text-5xl font-extrabold text-white text-center drop-shadow-lg">
          Benvenuto nella Homepage!
        </h1>
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 p-6 text-center text-gray-400 text-sm shadow-inner">
        <div className="container mx-auto">
          <p>
            &copy; {new Date().getFullYear()} PixelPals. Tutti i diritti
            riservati.
          </p>
          <div className="mt-2 space-x-4">
            <a href="#" className="hover:text-white transition duration-300">
              Privacy Policy
            </a>
            <a href="#" className="hover:text-white transition duration-300">
              Termini di Servizio
            </a>
            <a href="#" className="hover:text-white transition duration-300">
              Contatti
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default HomePage;
