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
  faSignOutAlt,
  faBell,
  faDoorOpen,
  faTachometerAlt,
  faBars,
  faTimes,
  faEye,
  faCheckCircle,
  faTimesCircle,
  faEnvelopeOpenText,
  faHourglassHalf,
} from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2';

const Navbar = () => {
  const navigate = useNavigate();
  const {
    totalUnreadCount,
    matchNotifications,
    clearMatchNotification,
    friendChatUnreadCount,
    pendingFriendRequestCount,
  } = useUnreadMessages();

  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [activeMatchId, setActiveMatchId] = useState(null);
  const [showMatchNotifications, setShowMatchNotifications] = useState(false);
  const notificationsRef = useRef(null);
  const mobileMenuRef = useRef(null);

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

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        notificationsRef.current &&
        !notificationsRef.current.contains(event.target)
      ) {
        setShowMatchNotifications(false);
      }

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
    setIsMenuOpen((prev) => !prev);
    setShowMatchNotifications(false);
  };

  const toggleMatchNotifications = (e) => {
    e.preventDefault();
    setShowMatchNotifications((prev) => !prev);
  };

  const handleNotificationClick = (notification) => {
    if (notification.type === 'MATCH_REQUEST') {
      setTimeout(() => {
        navigate('/pending-matches');
      }, 0);
    } else if (
      notification.type === 'MATCH_ACCEPTED' ||
      notification.type === 'MATCH_CLOSED'
    ) {
      setTimeout(() => {
        navigate(`/match-room/${notification.matchId}`);
      }, 0);
    } else if (notification.type === 'MATCH_DECLINED') {
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
          navigate('/pending-matches');
        }, 0);
      });
    }
    clearMatchNotification(notification.matchId, notification.type);
    setShowMatchNotifications(false);
    setIsMenuOpen(false);
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
        return faDoorOpen;
      default:
        return faBell;
    }
  };
  const userRole = localStorage.getItem('userRole');
  const isLoggedIn = localStorage.getItem('accessToken');
  const totalMatchNotifications = matchNotifications.length;
  const totalCombinedNotifications =
    friendChatUnreadCount + totalMatchNotifications + pendingFriendRequestCount;
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
                className="text-gray-300 hover:bg-gray-700 hover:text-white px-3 py-2 rounded-md text-sm font-medium transition duration-200 relative"
              >
                <FontAwesomeIcon icon={faUsers} className="mr-1" /> Amici
                {pendingFriendRequestCount > 0 && (
                  <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-900">
                    {pendingFriendRequestCount}
                  </span>
                )}
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
                {friendChatUnreadCount > 0 && !isMenuOpen && (
                  <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                    {friendChatUnreadCount}
                  </span>
                )}
              </Link>
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
            {totalCombinedNotifications > 0 && !isMenuOpen && (
              <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                {totalCombinedNotifications}
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
              className="text-gray-300 hover:bg-gray-700 hover:text-white block px-3 py-2 rounded-md text-base font-medium relative"
              onClick={toggleMenu}
            >
              <FontAwesomeIcon icon={faUsers} className="mr-1" /> Amici
              {pendingFriendRequestCount > 0 && (
                <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                  {pendingFriendRequestCount}
                </span>
              )}
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
              {friendChatUnreadCount > 0 && (
                <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-800">
                  {friendChatUnreadCount}
                </span>
              )}
            </Link>
            {isLoggedIn && (
              <div className="relative">
                <Link
                  to="/pending-matches"
                  className="relative block text-white hover:text-gray-300 transition duration-300 px-3 py-2 rounded-md"
                  onClick={() => {
                    navigate('/pending-matches');
                    toggleMenu();
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
