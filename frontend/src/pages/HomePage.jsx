import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth'; // Per chiamate autenticate
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faSearch,
  faUserCircle,
  faGamepad,
  faWifi,
  faStar,
  faLevelUpAlt,
  faUsers, // Aggiunto per la nuova sezione matchmaking
  faUserFriends, // Aggiunto per la nuova sezione matchmaking
} from '@fortawesome/free-solid-svg-icons';

const HomePage = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [allGames, setAllGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Recupera i dati dell'utente corrente
        const userRes = await axiosWithAuth.get('/auth/me');
        setCurrentUser(userRes.data);

        // Recupera gli utenti online
        const onlineRes = await axiosWithAuth.get('/online/users');
        setOnlineUsers(onlineRes.data);

        // Recupera tutti i giochi
        const gamesRes = await axiosWithAuth.get('/games');
        setAllGames(gamesRes.data);
      } catch (err) {
        console.error('Errore nel recupero dati homepage:', err);
        setError(
          'Errore durante il caricamento della homepage. Potresti non avere i permessi o la sessione è scaduta.'
        );
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [navigate]);

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-oxanium relative overflow-hidden pt-16 pb-24">
          <div
            className="absolute inset-0 z-0 opacity-10"
            style={{
              backgroundImage:
                "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
            }}
          ></div>
          <div className="relative z-10 text-3xl font-bold text-purple-400">
            Caricamento della Base Operativa...
          </div>
        </div>
        <Footer />
      </>
    );
  }

  return (
    <>
      <Navbar />
      {/* Sfondo più scuro e "techy" con gradiente e effetto noise */}
      <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden">
        {/* Effetto noise/griglia di sfondo */}
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>

        <main className="flex-grow p-4 pt-20 pb-24 relative z-10">
          {' '}
          {/* Padding superiore e inferiore per Navbar/Footer */}
          <div className="max-w-7xl mx-auto space-y-8">
            <h1 className="text-4xl font-extrabold text-white text-center drop-shadow-lg font-oxanium mb-8">
              BENVENUTO, {currentUser?.username?.toUpperCase()}!
            </h1>

            {error && <Alert type="error" message={error} />}

            {/* Sezione di Benvenuto e Profilo Breve */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-purple-700 flex flex-col md:flex-row items-center justify-between gap-6">
              <div className="flex items-center gap-4">
                <img
                  src={
                    currentUser?.avatarUrl ||
                    `https://placehold.co/100x100/3B82F6/FFFFFF?text=${
                      currentUser?.username?.charAt(0).toUpperCase() || '?'
                    }`
                  }
                  alt="Avatar Utente"
                  className="w-24 h-24 rounded-full border-4 border-blue-500 shadow-lg object-cover"
                />
                <div>
                  <h1 className="text-3xl font-bold text-blue-400 font-oxanium drop-shadow-lg">
                    BENVENUTO, {currentUser?.username?.toUpperCase()}!
                  </h1>
                  <p className="text-gray-300 text-lg mt-1">
                    Pronto per la prossima avventura?
                  </p>
                </div>
              </div>
              <div className="flex flex-col items-center md:items-end space-y-2">
                <div className="flex items-center text-lg text-gray-200">
                  <FontAwesomeIcon
                    icon={faLevelUpAlt}
                    className="mr-2 text-green-400"
                  />
                  Livello:{' '}
                  <span className="font-bold text-green-300 ml-1">
                    {currentUser?.level}
                  </span>
                </div>
                <div className="flex items-center text-lg text-gray-200">
                  <FontAwesomeIcon
                    icon={faStar}
                    className="mr-2 text-yellow-400"
                  />
                  Rating:{' '}
                  <span className="font-bold text-yellow-300 ml-1">
                    {currentUser?.rating?.toFixed(1)}
                  </span>
                </div>
                <button
                  onClick={() => navigate('/profile')}
                  className="mt-4 bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-2 px-6 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-base"
                >
                  <FontAwesomeIcon icon={faUserCircle} className="mr-2" /> Vedi
                  Profilo
                </button>
              </div>
            </section>

            {/* Sezione Trova un Compagno / Ricerca Rapida */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-blue-700">
              <h2 className="text-2xl font-bold text-cyan-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faSearch} className="mr-3" /> RICERCA
                RAPIDA PIXELPALS
              </h2>
              <div className="flex flex-col sm:flex-row gap-4">
                <input
                  type="text"
                  placeholder="Cerca giocatori per username, gioco o piattaforma..."
                  className="flex-grow px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                />
                <button
                  onClick={() => navigate('/matchmaking')}
                  className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-3 px-6 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center justify-center text-base"
                >
                  <FontAwesomeIcon icon={faSearch} className="mr-2" /> Cerca
                </button>
              </div>
            </section>

            {/* Sezione Amici Online / Utenti Suggeriti */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-green-700">
              <h2 className="text-2xl font-bold text-lime-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faWifi} className="mr-3" /> AMICI ONLINE
              </h2>
              {onlineUsers.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                  {onlineUsers.map((username, index) => (
                    <div
                      key={index}
                      className="bg-gray-700 p-3 rounded-lg flex items-center space-x-3 border border-gray-600 shadow-md"
                    >
                      <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>{' '}
                      {/* Indicatore online */}
                      <span className="text-gray-100 font-medium">
                        {username}
                      </span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400">
                  Nessun amico online al momento. Esplora per trovarne di nuovi!
                </p>
              )}
            </section>

            {/* Sezione Giochi in Evidenza */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-orange-700">
              <h2 className="text-2xl font-bold text-orange-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faGamepad} className="mr-3" /> GIOCHI IN
                EVIDENZA
              </h2>
              {allGames.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {allGames.slice(0, 10).map(
                    (
                      game // Mostra solo i primi 10 giochi
                    ) => (
                      <div
                        key={game.id}
                        className="bg-gray-700 rounded-lg overflow-hidden shadow-md border border-gray-600 transform hover:scale-105 transition duration-200 cursor-pointer"
                      >
                        <img
                          src={
                            game.imageUrl ||
                            `https://placehold.co/200x150/4B5563/FFFFFF?text=${game.name.substring(
                              0,
                              5
                            )}`
                          }
                          alt={game.name}
                          className="w-full h-32 object-cover"
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src =
                              'https://placehold.co/200x150/4B5563/FFFFFF?text=No+Image';
                          }} // Fallback immagine
                        />
                        <div className="p-3">
                          <h3 className="text-gray-100 font-semibold text-sm truncate">
                            {game.name}
                          </h3>
                          <p className="text-gray-400 text-xs mt-1">
                            {game.genre}
                          </p>
                        </div>
                      </div>
                    )
                  )}
                </div>
              ) : (
                <p className="text-gray-400">
                  Nessun gioco disponibile al momento.
                </p>
              )}
            </section>

            {/* NUOVA SEZIONE: Trova PixelPals (come pulsante CTA) */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-cyan-700 text-center">
              <h2 className="text-2xl font-bold text-cyan-400 font-oxanium mb-4 flex items-center justify-center">
                <FontAwesomeIcon icon={faUsers} className="mr-3" /> TROVA I TUOI
                PROSSIMI COMPAGNI DI GIOCO!
              </h2>
              <p className="text-gray-300 mb-6">
                Utilizza il nostro sistema di matchmaking avanzato per trovare
                PixelPals con interessi e livelli di skill simili.
              </p>
              <button
                onClick={() => navigate('/matchmaking')}
                className="bg-gradient-to-r from-green-600 to-blue-600 hover:from-green-700 hover:to-blue-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center justify-center mx-auto"
              >
                <FontAwesomeIcon icon={faUserFriends} className="mr-3" /> Inizia
                il Matchmaking
              </button>
            </section>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

// Componente Alert (copiato da AdminDashboard/ProfilePages per coerenza)
const Alert = ({ type, message }) => {
  const base =
    type === 'success'
      ? 'bg-green-900 border-green-700 text-green-300'
      : 'bg-red-900 border-red-700 text-red-300';
  return (
    <div
      className={`relative z-50 border px-4 py-3 rounded mb-4 ${base}`}
      role="alert"
      aria-live="polite"
    >
      {type === 'success' ? '✅' : '❌'} {message}
    </div>
  );
};

export default HomePage;
