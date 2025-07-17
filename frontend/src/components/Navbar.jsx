import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faBars,
  faTimes,
  faUser,
  faUserFriends,
  faUsers,
  faComments,
  faHourglassHalf,
} from '@fortawesome/free-solid-svg-icons';
import { UnreadMessagesContext } from './UnreadMessagesContext';

const Navbar = () => {
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  // Usa il contesto per accedere al conteggio totale dei messaggi non letti
  const { totalUnreadCount } = useContext(UnreadMessagesContext);

  const handleProfileClick = () => {
    navigate('/profile');
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const userRole = localStorage.getItem('userRole');
  const isLoggedIn = localStorage.getItem('accessToken');

  return (
    <nav className="bg-gray-800 p-4 shadow-lg fixed top-0 w-full z-50">
      {/* Contenitore interno della navbar con padding orizzontale */}
      <div className="flex justify-between items-center px-4 md:px-8 lg:px-16">
        <div className="text-2xl font-bold text-white">PixelPals</div>

        <div className="md:hidden">
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="text-white focus:outline-none"
            aria-label="Toggle mobile menu"
          >
            <FontAwesomeIcon
              icon={isMobileMenuOpen ? faTimes : faBars}
              className="w-6 h-6"
            />
          </button>
        </div>

        <div className="hidden md:flex items-center space-x-6">
          {isLoggedIn && (
            <a
              href="/home"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Home
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/friends"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Amici
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/matchmaking"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Matchmaking
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/chat"
              className="relative text-white hover:text-gray-300 transition duration-300"
            >
              <FontAwesomeIcon icon={faComments} className="mr-1" />
              Chat
              {/* Badge di notifica per la Navbar */}
              {totalUnreadCount > 0 && (
                <span className="absolute -top-2 -right-3 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                  {totalUnreadCount}
                </span>
              )}
            </a>
          )}
          {/* NUOVO LINK PER LE RICHIESTE DI PARTITA PENDENTI */}
          {isLoggedIn && (
            <a
              href="/pending-matches"
              className="relative text-white hover:text-gray-300 transition duration-300"
            >
              <FontAwesomeIcon icon={faHourglassHalf} className="mr-1" />
              Richieste Partita
              {/* Potresti aggiungere un badge di notifica qui per le richieste di partita pendenti */}
              {/* Esempio: {pendingMatchRequestsCount > 0 && ( ... )} */}
            </a>
          )}
          {userRole === 'ROLE_ADMIN' && isLoggedIn && (
            <a
              href="/admin-dashboard"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Admin Dashboard
            </a>
          )}

          {isLoggedIn && (
            <>
              <button
                onClick={handleProfileClick}
                className="flex items-center text-white hover:text-gray-300 transition duration-300 focus:outline-none"
                aria-label="Vai al profilo"
              >
                <FontAwesomeIcon icon={faUser} className="w-6 h-6 mr-1" />
                Profilo
              </button>
              <button
                onClick={handleLogout}
                className="text-red-400 hover:text-red-300 transition duration-300 focus:outline-none"
              >
                Logout
              </button>
            </>
          )}
          {!isLoggedIn && (
            <a
              href="/"
              className="text-white hover:text-gray-300 transition duration-300"
            >
              Login/Registrazione
            </a>
          )}
        </div>
      </div>

      {isMobileMenuOpen && (
        <div className="md:hidden mt-4 space-y-2 px-4">
          {isLoggedIn && (
            <a
              href="/home"
              className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              Home
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/friends"
              className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              Amici
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/matchmaking"
              className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              Matchmaking
            </a>
          )}
          {isLoggedIn && (
            <a
              href="/chat"
              className="relative block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              <FontAwesomeIcon icon={faComments} className="mr-1" />
              Chat
              {/* Badge di notifica per la Navbar Mobile */}
              {totalUnreadCount > 0 && (
                <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                  {totalUnreadCount}
                </span>
              )}
            </a>
          )}
          {/* NUOVO LINK PER LE RICHIESTE DI PARTITA PENDENTI (MOBILE) */}
          {isLoggedIn && (
            <a
              href="/pending-matches"
              className="relative block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              <FontAwesomeIcon icon={faHourglassHalf} className="mr-1" />
              Richieste Partita
              {/* Potresti aggiungere un badge di notifica qui per le richieste di partita pendenti */}
              {/* Esempio: {pendingMatchRequestsCount > 0 && ( ... )} */}
            </a>
          )}
          {userRole === 'ROLE_ADMIN' && isLoggedIn && (
            <a
              href="/admin-dashboard"
              className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
            >
              Admin Dashboard
            </a>
          )}
          {isLoggedIn && (
            <>
              <button
                onClick={handleProfileClick}
                className="w-full text-left flex items-center text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md focus:outline-none"
              >
                <FontAwesomeIcon icon={faUser} className="w-6 h-6 mr-1" />
                Profilo
              </button>
              <button
                onClick={handleLogout}
                className="w-full text-left text-red-400 hover:text-red-300 transition duration-300 px-3 py-2 rounded-md focus:outline-none"
              >
                Logout
              </button>
            </>
          )}
          {!isLoggedIn && (
            <a
              href="/"
              className="block text-white hover:text-gray-300 transition duration-300"
            >
              Login/Registrazione
            </a>
          )}
        </div>
      )}
    </nav>
  );
};

export default Navbar;
