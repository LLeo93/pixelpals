import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import FriendList from '../components/FriendList';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faSearch,
  faUserCircle,
  faGamepad,
  faStar,
  faLevelUpAlt,
  faUsers,
  faUserFriends,
  faPlus,
  faCheck,
  faUserClock,
} from '@fortawesome/free-solid-svg-icons';

const HomePage = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);

  const token = localStorage.getItem('accessToken');
  const [allGames, setAllGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [searchTerm, setSearchTerm] = useState('');
  const [searchError, setSearchError] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [friendshipStatuses, setFriendshipStatuses] = useState({});
  const [alertMessage, setAlertMessage] = useState({ type: '', message: '' });

  const [gameSearchTerm, setGameSearchTerm] = useState('');
  const [gameSearchResults, setGameSearchResults] = useState([]);
  const [gameSearchError, setGameSearchError] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        const userRes = await axiosWithAuth.get('/auth/me');
        setCurrentUser(userRes.data);
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

  const fetchFriendshipStatus = async (userId) => {
    try {
      const response = await axiosWithAuth.get(`/friends/status/${userId}`);
      return {
        status: response.data.status,
        requestId: response.data.requestId,
        friendshipId: response.data.friendshipId,
      };
    } catch (err) {
      console.error(`Errore nel recupero stato amicizia per ${userId}:`, err);
      return { status: 'UNKNOWN' };
    }
  };

  const updateAllSearchResultsFriendshipStatuses = async (results) => {
    const newStatuses = {};
    for (const user of results) {
      if (currentUser && user.id !== currentUser.id) {
        const statusData = await fetchFriendshipStatus(user.id);
        newStatuses[user.id] = {
          status: statusData.status,
          idForAction: statusData.requestId || statusData.friendshipId || null,
        };
      } else if (currentUser && user.id === currentUser.id) {
        newStatuses[user.id] = { status: 'SELF' };
      }
    }
    setFriendshipStatuses(newStatuses);
  };

  const handleSearchKeyPress = async (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      setSearchError('');
      setSearchResults([]);
      setFriendshipStatuses({});

      if (!searchTerm.trim()) {
        setSearchError('Inserisci un termine di ricerca valido.');
        return;
      }

      try {
        const response = await axiosWithAuth.get(
          `/users?username=${encodeURIComponent(searchTerm)}`
        );

        if (response.data.length === 0) {
          setSearchError(
            `Nessun PixelPal trovato con "${searchTerm}". Prova con un altro nome o gioco.`
          );
        } else {
          setSearchResults(response.data);
          updateAllSearchResultsFriendshipStatuses(response.data);
        }
      } catch (err) {
        console.error('Errore durante la ricerca:', err);
        setSearchError('Errore durante la ricerca. Riprova più tardi.');
      }
    }
  };

  const handleGameSearch = async () => {
    setGameSearchError('');

    if (!gameSearchTerm.trim()) {
      setGameSearchResults([]);
      return;
    }

    try {
      const response = await axiosWithAuth.get(
        `/games?search=${encodeURIComponent(gameSearchTerm)}`
      );

      if (response.data.length === 0) {
        setGameSearchError(`Nessun gioco trovato con "${gameSearchTerm}".`);
        setGameSearchResults([]);
      } else {
        setGameSearchResults(response.data);
      }
    } catch (err) {
      console.error('Errore durante la ricerca giochi:', err);
      setGameSearchError(
        'Errore durante la ricerca giochi. Riprova più tardi.'
      );
      setGameSearchResults([]);
    }
  };

  const handleGameSearchKeyPress = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleGameSearch();
    }
  };

  useEffect(() => {
    if (gameSearchTerm === '') {
      setGameSearchResults([]);
      setGameSearchError('');
    }
  }, [gameSearchTerm]);

  const handleSendFriendRequest = async (user) => {
    try {
      await axiosWithAuth.post(`/friends/request/${user.username}`);
      setAlertMessage({
        type: 'success',
        message: 'Richiesta di amicizia inviata!',
      });
      setFriendshipStatuses((prev) => ({
        ...prev,
        [user.id]: { status: 'PENDING_SENT', idForAction: null },
      }));
    } catch (err) {
      console.error('Errore invio richiesta:', err);
      setAlertMessage({
        type: 'error',
        message:
          "Errore nell'invio della richiesta di amicizia: " +
          (err.response?.data?.message || err.message),
      });
    }
  };

  const handleAcceptFriendRequest = async (userId, requestId) => {
    try {
      await axiosWithAuth.put(`/friends/accept/${requestId}`);
      setAlertMessage({
        type: 'success',
        message: 'Richiesta di amicizia accettata!',
      });
      setFriendshipStatuses((prev) => ({
        ...prev,
        [userId]: { status: 'ACCEPTED', idForAction: requestId },
      }));
    } catch (err) {
      console.error('Errore accettazione richiesta:', err);
      setAlertMessage({
        type: 'error',
        message:
          "Errore nell'accettazione della richiesta di amicizia: " +
          (err.response?.data?.message || err.message),
      });
    }
  };

  const handleDeclineOrRemoveFriend = async (userId, idForAction) => {
    try {
      const currentStatus = friendshipStatuses[userId]?.status;

      if (currentStatus === 'ACCEPTED') {
        await axiosWithAuth.delete(`/friends/remove/${userId}`);
        setAlertMessage({ type: 'success', message: 'Amicizia rimossa.' });
        setFriendshipStatuses((prev) => ({
          ...prev,
          [userId]: { status: 'NONE', idForAction: null },
        }));
      } else if (currentStatus === 'PENDING_RECEIVED') {
        await axiosWithAuth.put(`/friends/reject/${idForAction}`);
        setAlertMessage({
          type: 'success',
          message: 'Richiesta di amicizia rifiutata.',
        });
        setFriendshipStatuses((prev) => ({
          ...prev,
          [userId]: { status: 'REJECTED', idForAction: null },
        }));
      }
    } catch (err) {
      console.error('Errore rifiuto/rimozione amicizia:', err);
      setAlertMessage({
        type: 'error',
        message:
          "Errore nel rifiutare/rimuovere l'amicizia: " +
          (err.response?.data?.message || err.message),
      });
    }
  };

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
      <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden">
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>
        <main className="flex-grow p-4 pt-20 pb-24 relative z-10">
          {' '}
          <div className="max-w-7xl mx-auto space-y-8">
            <h1 className="text-4xl font-extrabold text-white text-center drop-shadow-lg font-oxanium mb-8">
              BENVENUTO, {currentUser?.username?.toUpperCase()}!
            </h1>
            {error && <Alert type="error" message={error} />}
            {alertMessage.message && (
              <Alert type={alertMessage.type} message={alertMessage.message} />
            )}
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
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={handleSearchKeyPress}
                />
                <button
                  onClick={() => navigate('/matchmaking')}
                  className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-3 px-6 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center justify-center text-base"
                >
                  <FontAwesomeIcon icon={faSearch} className="mr-2" /> Cerca
                </button>
              </div>
              {searchError && (
                <div
                  className="mt-4 bg-red-900 bg-opacity-70 border border-red-700 text-red-300 px-4 py-3 rounded relative text-sm"
                  role="alert"
                >
                  <strong className="font-bold">ERRORE: </strong>
                  <span className="block sm:inline">{searchError}</span>
                  <span
                    onClick={() => setSearchError('')}
                    className="absolute top-0 bottom-0 right-0 px-4 py-3 cursor-pointer"
                  >
                    <svg
                      className="fill-current h-6 w-6 text-red-500"
                      role="button"
                      viewBox="0 0 20 20"
                    >
                      <title>Chiudi</title>
                      <path d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z" />
                    </svg>
                  </span>
                </div>
              )}

              {searchResults.length > 0 && !searchError && (
                <div className="mt-6">
                  <h3 className="text-xl font-bold text-blue-400 mb-3">
                    Risultati della Ricerca:
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {searchResults.map((user) => (
                      <div
                        key={user.id}
                        className="bg-gray-700 p-4 rounded-lg flex items-center justify-between space-x-4 border border-blue-600 shadow-md "
                      >
                        <div className="flex items-center space-x-4">
                          <img
                            onClick={() => navigate(`/users/${user.id}`)}
                            src={
                              user.avatarUrl ||
                              `https://placehold.co/50x50/3B82F6/FFFFFF?text=${user.username
                                .charAt(0)
                                .toUpperCase()}`
                            }
                            alt={user.username}
                            className="w-12 h-12 rounded-full object-cover border-2 border-blue-500 cursor-pointer transform hover:scale-105 transition duration-200"
                          />
                          <div>
                            <p className="text-white font-semibold">
                              {user.username}
                            </p>
                            <p className="text-gray-400 text-sm">
                              Livello: {user.level} | Rating:{' '}
                              {user.rating?.toFixed(1)}
                            </p>
                          </div>
                        </div>
                        {currentUser && user.id !== currentUser.id && (
                          <div className="flex-shrink-0">
                            {friendshipStatuses[user.id]?.status === 'NONE' && (
                              <button
                                onClick={() => handleSendFriendRequest(user)}
                                className="bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold py-1 px-2 rounded transition duration-200 flex items-center"
                                title="Invia richiesta di amicizia"
                              >
                                <FontAwesomeIcon
                                  icon={faPlus}
                                  className="mr-1"
                                />{' '}
                                Aggiungi
                              </button>
                            )}
                            {friendshipStatuses[user.id]?.status ===
                              'PENDING_SENT' && (
                              <button
                                className="bg-gray-500 text-white text-xs font-bold py-1 px-2 rounded cursor-not-allowed flex items-center"
                                disabled
                                title="Richiesta di amicizia già inviata"
                              >
                                <FontAwesomeIcon
                                  icon={faUserClock}
                                  className="mr-1"
                                />{' '}
                                Richiesta Inviata
                              </button>
                            )}
                            {friendshipStatuses[user.id]?.status ===
                              'PENDING_RECEIVED' && (
                              <div className="flex flex-col space-y-1">
                                <button
                                  onClick={() =>
                                    handleAcceptFriendRequest(
                                      user.id,
                                      friendshipStatuses[user.id]?.idForAction
                                    )
                                  }
                                  className="bg-green-600 hover:bg-green-700 text-white text-xs font-bold py-1 px-2 rounded transition duration-200 flex items-center"
                                  title="Accetta richiesta di amicizia"
                                >
                                  <FontAwesomeIcon
                                    icon={faCheck}
                                    className="mr-1"
                                  />{' '}
                                  Accetta
                                </button>
                                <button
                                  onClick={() =>
                                    handleDeclineOrRemoveFriend(
                                      user.id,
                                      friendshipStatuses[user.id]?.idForAction
                                    )
                                  }
                                  className="bg-red-600 hover:bg-red-700 text-white text-xs font-bold py-1 px-2 rounded transition duration-200 flex items-center"
                                  title="Rifiuta richiesta di amicizia"
                                >
                                  <FontAwesomeIcon
                                    icon={faPlus}
                                    rotation={45}
                                    className="mr-1 transform rotate-45"
                                  />{' '}
                                  Rifiuta
                                </button>
                              </div>
                            )}
                            {friendshipStatuses[user.id]?.status ===
                              'ACCEPTED' && (
                              <button
                                onClick={() =>
                                  handleDeclineOrRemoveFriend(
                                    user.id,
                                    friendshipStatuses[user.id]?.idForAction
                                  )
                                }
                                className="bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold py-1 px-2 rounded transition duration-200 flex items-center"
                                title="Siete amici! Clicca per rimuovere."
                              >
                                <FontAwesomeIcon
                                  icon={faUserFriends}
                                  className="mr-1"
                                />{' '}
                                Amici
                              </button>
                            )}
                            {friendshipStatuses[user.id]?.status ===
                              'REJECTED' && (
                              <span className="text-red-400 text-xs flex items-center">
                                <FontAwesomeIcon
                                  icon={faPlus}
                                  rotation={45}
                                  className="mr-1 transform rotate-45"
                                />{' '}
                                Rifiutato
                              </span>
                            )}
                            {friendshipStatuses[user.id]?.status ===
                              'UNKNOWN' && (
                              <span className="text-gray-400 text-xs">
                                Stato ignoto
                              </span>
                            )}
                          </div>
                        )}
                        {currentUser && user.id === currentUser.id && (
                          <span className="text-blue-400 text-xs font-bold">
                            Tu
                          </span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </section>

            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-green-700">
              <FriendList userToken={token} username={currentUser?.username} />
            </section>

            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-orange-700">
              <h2 className="text-2xl font-bold text-orange-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faGamepad} className="mr-3" />
                {gameSearchResults.length > 0
                  ? 'RISULTATI RICERCA GIOCHI'
                  : 'GIOCHI IN EVIDENZA'}
              </h2>
              <div className="flex flex-col sm:flex-row gap-4 mb-6">
                <input
                  type="text"
                  placeholder="Cerca un gioco per nome..."
                  className="flex-grow px-4 py-3 bg-gray-700 bg-opacity-70 border border-orange-600 rounded-md text-white placeholder-orange-300 focus:outline-none focus:ring-2 focus:ring-orange-500 transition duration-200"
                  value={gameSearchTerm}
                  onChange={(e) => setGameSearchTerm(e.target.value)}
                  onKeyPress={handleGameSearchKeyPress}
                  style={{
                    backgroundColor: 'rgba(55, 65, 81, 0.7)',
                    borderColor: 'rgba(234, 88, 12, 0.8)',
                  }}
                />
                <button
                  onClick={handleGameSearch}
                  className="bg-gradient-to-r from-orange-600 to-amber-500 hover:from-orange-700 hover:to-amber-600 text-white font-bold py-3 px-6 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center justify-center text-base"
                >
                  <FontAwesomeIcon icon={faSearch} className="mr-2" /> Cerca
                  Gioco
                </button>
              </div>

              {gameSearchError && (
                <div
                  className="mt-4 bg-red-900 bg-opacity-70 border border-red-700 text-red-300 px-4 py-3 rounded relative text-sm"
                  role="alert"
                >
                  <strong className="font-bold">ERRORE: </strong>
                  <span className="block sm:inline">{gameSearchError}</span>
                  <span
                    onClick={() => setGameSearchError('')}
                    className="absolute top-0 bottom-0 right-0 px-4 py-3 cursor-pointer"
                  >
                    <svg
                      className="fill-current h-6 w-6 text-red-500"
                      role="button"
                      viewBox="0 0 20 20"
                    >
                      <title>Chiudi</title>
                      <path d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z" />
                    </svg>
                  </span>
                </div>
              )}

              {gameSearchResults.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {gameSearchResults.map((game) => (
                    <div
                      key={game.id}
                      className="bg-gray-700 rounded-lg overflow-hidden shadow-md border border-orange-600 transform hover:scale-105 transition duration-200 cursor-pointer"
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
                        }}
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
                  ))}
                </div>
              ) : allGames.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                  {allGames.slice(0, 10).map((game) => (
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
                        }}
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
                  ))}
                </div>
              ) : (
                <p className="text-gray-400">
                  Nessun gioco disponibile al momento.
                </p>
              )}
            </section>

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

export default HomePage;
