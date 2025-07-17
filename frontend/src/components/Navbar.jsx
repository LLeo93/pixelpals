import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useUnreadMessages } from './UnreadMessagesContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faHome,
  faUser,
  faUsers,
  faGamepad,
  faEnvelope,
  faCog,
  faSignOutAlt,
  faBell,
  faDoorOpen,
  faComments,
  faHourglassHalf,
  faTachometerAlt,
  faBars, // Icona per aprire il menu mobile
  faTimes, // Icona per chiudere il menu mobile
  faEye, // Icona per visualizzare
  faCheckCircle, // Icona per accettato/completato
  faTimesCircle, // Icona per rifiutato
  faEnvelopeOpenText, // Icona per richiesta
} from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2';

const Navbar = () => {
  const navigate = useNavigate();
  const { totalUnreadCount, matchNotifications, clearMatchNotification } =
    useUnreadMessages();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [activeMatchId, setActiveMatchId] = useState(null);
  const [showMatchNotifications, setShowMatchNotifications] = useState(false);
  const notificationsRef = useRef(null); // Riferimento per il click fuori dal dropdown notifiche
  const mobileMenuRef = useRef(null); // Riferimento per il click fuori dal menu mobile

  useEffect(() => {
    const checkActiveMatch = () => {
      const storedMatchId = localStorage.getItem('activeMatchId');
      setActiveMatchId(storedMatchId);
    };

    checkActiveMatch();
    window.addEventListener('storage', checkActiveMatch);

    return () => {
      window.removeEventListener('storage', checkActiveMatch);
    };
  }, []);

  // Gestore per chiudere dropdown e menu al click fuori
  useEffect(() => {
    const handleClickOutside = (event) => {
      // Chiudi il dropdown delle notifiche se cliccato fuori dalla sua area
      if (
        notificationsRef.current &&
        !notificationsRef.current.contains(event.target)
      ) {
        setShowMatchNotifications(false);
      }

      // Chiudi il menu mobile se cliccato fuori dalla sua area E il menu è aperto
      // E il click NON è stato sul pulsante hamburger stesso
      const hamburgerButton = document.querySelector(
        'button[aria-controls="mobile-menu"]'
      );
      const clickedOnHamburger =
        hamburgerButton && hamburgerButton.contains(event.target);
      const clickedInsideMobileMenu =
        mobileMenuRef.current && mobileMenuRef.current.contains(event.target);

      if (isMenuOpen && !clickedInsideMobileMenu && !clickedOnHamburger) {
        setIsMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isMenuOpen]);

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  const toggleMenu = () => {
    setIsMenuOpen((prev) => !prev); // Toggle the menu state
    setShowMatchNotifications(false); // Chiudi il dropdown delle notifiche quando si togliona il menu mobile
  };

  const handleProfileClick = () => {
    navigate('/profile');
    setIsMenuOpen(false);
  };

  const toggleMatchNotifications = (e) => {
    e.preventDefault(); // Impedisci la navigazione del link
    setShowMatchNotifications((prev) => !prev);
    // Non chiudiamo più il menu mobile qui.
    // L'utente può voler vedere il dropdown dentro il menu mobile aperto.
  };

  const handleNotificationClick = (notification) => {
    console.log(
      'handleNotificationClick called for notification:',
      notification
    );
    // Gestisci la navigazione in base al tipo di notifica
    if (notification.type === 'MATCH_REQUEST') {
      console.log('Navigating to /pending-matches for MATCH_REQUEST');
      setTimeout(() => {
        navigate('/pending-matches');
      }, 0);
    } else if (
      notification.type === 'MATCH_ACCEPTED' ||
      notification.type === 'MATCH_CLOSED'
    ) {
      console.log(
        `Navigating to /match-room/${notification.matchId} for ${notification.type}`
      );
      setTimeout(() => {
        navigate(`/match-room/${notification.matchId}`);
      }, 0);
    } else if (notification.type === 'MATCH_DECLINED') {
      console.log(
        'Showing Swal for MATCH_DECLINED and navigating to /pending-matches'
      );
      // Per le notifiche di rifiuto, mostra un alert e poi naviga
      Swal.fire({
        title: 'Richiesta Rifiutata',
        text: `${notification.declinerUsername} ha rifiutato la tua richiesta di partita per ${notification.gameName}.`,
        icon: 'info',
        confirmButtonText: 'Ok',
        customClass: {
          popup:
            'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
          title: 'text-blue-400',
          confirmButton:
            'bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md',
        },
      }).then(() => {
        setTimeout(() => {
          navigate('/pending-matches'); // Naviga alla pagina pending-matches anche per i rifiuti
        }, 0);
      });
    }
    console.log('Clearing match notification and closing dropdown/menu.');
    clearMatchNotification(notification.matchId, notification.type); // Rimuovi la notifica dopo averla gestita
    setShowMatchNotifications(false); // Chiudi il dropdown
    setIsMenuOpen(false); // Chiudi il menu mobile dopo aver gestito la notifica e navigato
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case 'MATCH_REQUEST':
        return faEnvelopeOpenText;
      case 'MATCH_ACCEPTED':
        return faCheckCircle;
      case 'MATCH_DECLINED':
        return faTimesCircle;
      case 'MATCH_CLOSED':
        return faDoorOpen; // O un'altra icona più appropriata per "partita chiusa"
      default:
        return faBell;
    }
  };

  const userRole = localStorage.getItem('userRole');
  const isLoggedIn = localStorage.getItem('accessToken');

  const totalMatchNotifications = matchNotifications.length;

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-gray-900 bg-opacity-90 backdrop-blur-md shadow-lg border-b border-blue-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <Link
              to="/home"
              className="flex-shrink-0 text-white text-2xl font-bold font-oxanium"
            >
              PixelPals
            </Link>
          </div>
          <div className="hidden md:block">
            <div className="ml-10 flex items-baseline space-x-4">
              <Link
                to="/home"
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
              >
                <FontAwesomeIcon icon={faHome} className="mr-1" /> Home
              </Link>
              <Link
                to="/profile"
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
              >
                <FontAwesomeIcon icon={faUser} className="mr-1" /> Profilo
              </Link>
              <Link
                to="/friends"
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
              >
                <FontAwesomeIcon icon={faUsers} className="mr-1" /> Amici
              </Link>
              <Link
                to="/matchmaking"
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
              >
                <FontAwesomeIcon icon={faGamepad} className="mr-1" />{' '}
                Matchmaking
              </Link>
              <Link
                to="/chat"
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200 relative"
              >
                <FontAwesomeIcon icon={faEnvelope} className="mr-1" /> Chat
                {totalUnreadCount > 0 && (
                  <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2">
                    {totalUnreadCount}
                  </span>
                )}
              </Link>
              {/* Contenitore per il link Inviti e il dropdown (Desktop) */}
              <div className="relative" ref={notificationsRef}>
                <Link
                  to="/pending-matches"
                  onClick={toggleMatchNotifications}
                  className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200 relative"
                >
                  <FontAwesomeIcon icon={faBell} className="mr-1" /> Inviti
                  {totalMatchNotifications > 0 && (
                    <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2">
                      {totalMatchNotifications}
                    </span>
                  )}
                </Link>
                {showMatchNotifications && (
                  <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-gray-800 rounded-md shadow-lg py-1 z-20 border border-blue-700 max-h-80 overflow-y-auto">
                    {totalMatchNotifications === 0 ? (
                      <p className="text-gray-400 px-4 py-2 text-sm">
                        Nessuna nuova notifica di partita.
                      </p>
                    ) : (
                      matchNotifications.map((notif, index) => (
                        <div
                          key={index}
                          className="flex items-center justify-between px-4 py-2 hover:bg-gray-700 border-b border-gray-700 last:border-b-0"
                        >
                          <div className="flex items-center">
                            <FontAwesomeIcon
                              icon={getNotificationIcon(notif.type)}
                              className="mr-2 text-blue-400"
                            />
                            <span className="text-sm text-white">
                              {notif.message}
                            </span>
                          </div>
                          <button
                            onClick={() => handleNotificationClick(notif)}
                            className="ml-2 text-blue-400 hover:text-blue-200"
                            title="Visualizza/Gestisci"
                          >
                            <FontAwesomeIcon icon={faEye} />
                          </button>
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>

              {userRole === 'ROLE_ADMIN' && isLoggedIn && (
                <Link
                  to="/admin-dashboard"
                  className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
                >
                  <FontAwesomeIcon icon={faTachometerAlt} className="mr-1" />{' '}
                  Admin Dashboard
                </Link>
              )}

              {activeMatchId && (
                <Link
                  to={`/match-room/${activeMatchId}`}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200 flex items-center"
                >
                  <FontAwesomeIcon icon={faDoorOpen} className="mr-1" /> Torna
                  nella Room
                </Link>
              )}

              <button
                onClick={handleLogout}
                className="text-gray-300 hover:bg-red-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200"
              >
                <FontAwesomeIcon icon={faSignOutAlt} className="mr-1" /> Logout
              </button>
            </div>
          </div>
          <div className="-mr-2 flex md:hidden relative">
            <button
              onClick={toggleMenu}
              type="button"
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
              aria-controls="mobile-menu"
              aria-expanded="false"
            >
              <span className="sr-only">Open main menu</span>
              {!isMenuOpen ? (
                <FontAwesomeIcon icon={faBars} className="block h-6 w-6" />
              ) : (
                <FontAwesomeIcon icon={faTimes} className="block h-6 w-6" />
              )}
            </button>
            {/* Badge per le notifiche di partita sul pulsante del menu mobile */}
            {totalMatchNotifications > 0 && !isMenuOpen && (
              <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                {totalMatchNotifications}
              </span>
            )}
          </div>
        </div>
      </div>

      {isMenuOpen && (
        <div className="md:hidden" id="mobile-menu" ref={mobileMenuRef}>
          <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
            <Link
              to="/home"
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faHome} className="mr-1" /> Home
            </Link>
            <Link
              to="/profile"
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faUser} className="mr-1" /> Profilo
            </Link>
            <Link
              to="/friends"
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faUsers} className="mr-1" /> Amici
            </Link>
            <Link
              to="/matchmaking"
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faGamepad} className="mr-1" /> Matchmaking
            </Link>
            <Link
              to="/chat"
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium relative"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faEnvelope} className="mr-1" />
              Chat
              {totalUnreadCount > 0 && (
                <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                  {totalUnreadCount}
                </span>
              )}
            </Link>
            {isLoggedIn && (
              <div className="relative">
                {/* MODIFICA QUI: Per mobile, il link "Inviti" naviga direttamente */}
                <Link
                  to="/pending-matches"
                  className="relative block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
                  onClick={() => {
                    // Funzione anonima per navigare e chiudere il menu
                    navigate('/pending-matches');
                    toggleMenu(); // Chiude il menu hamburger dopo la navigazione
                  }}
                >
                  <FontAwesomeIcon icon={faHourglassHalf} className="mr-1" />
                  Inviti
                  {totalMatchNotifications > 0 && (
                    <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                      {totalMatchNotifications}
                    </span>
                  )}
                </Link>
                {/* Il dropdown delle notifiche NON sarà più visibile cliccando su "Inviti" da mobile */}
                {/* Potrebbe essere utile avere un pulsante separato per il dropdown se lo si vuole anche su mobile */}
              </div>
            )}
            {userRole === 'ROLE_ADMIN' && isLoggedIn && (
              <Link
                to="/admin-dashboard"
                className="block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
                onClick={toggleMenu}
              >
                <FontAwesomeIcon icon={faTachometerAlt} className="mr-1" />{' '}
                Admin Dashboard
              </Link>
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
                {activeMatchId && (
                  <Link
                    to={`/match-room/${activeMatchId}`}
                    className="bg-blue-600 hover:bg-blue-700 text-white block px-3 py-2 rounded-md text-base font-medium"
                    onClick={toggleMenu}
                  >
                    <FontAwesomeIcon icon={faDoorOpen} className="mr-1" /> Torna
                    nella Room
                  </Link>
                )}
                <button
                  onClick={handleLogout}
                  className="w-full text-left text-red-400 hover:text-red-300 transition duration-300 px-3 py-2 rounded-md focus:outline-none"
                >
                  Logout
                </button>
              </>
            )}
            {!isLoggedIn && (
              <Link
                to="/"
                className="block text-white hover:text-gray-300 transition duration-300"
                onClick={toggleMenu}
              >
                Login/Registrazione
              </Link>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
