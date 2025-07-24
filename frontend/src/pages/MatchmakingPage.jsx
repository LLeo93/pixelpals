import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faSearch,
  faGamepad,
  faLaptop,
  faStar,
  faUsers,
  faUserPlus,
  faDice,
  faHourglassHalf,
  faUserFriends,
} from '@fortawesome/free-solid-svg-icons';

const MatchmakingPage = () => {
  const navigate = useNavigate();
  const [allGames, setAllGames] = useState([]);
  const [allPlatforms, setAllPlatforms] = useState([]);
  const [searchCriteria, setSearchCriteria] = useState({
    gameName: '',
    platformName: '',
    skillLevel: '',
    maxResults: 10,
  });
  const [matchedUsers, setMatchedUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [sentRequests, setSentRequests] = useState({});
  const [acceptedFriendIds, setAcceptedFriendIds] = useState(new Set());
  const skillOptions = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const gamesRes = await axiosWithAuth.get('/games');
        setAllGames(gamesRes.data);
        const platformsRes = await axiosWithAuth.get('/platforms');
        setAllPlatforms(platformsRes.data);
        const friendsRes = await axiosWithAuth.get('/friends');
        const friendIds = new Set(friendsRes.data.map((friend) => friend.id));
        setAcceptedFriendIds(friendIds);
      } catch (err) {
        setError(
          'Errore durante il caricamento delle opzioni di ricerca o della lista amici.'
        );
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchInitialData();
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setSearchCriteria((prev) => ({ ...prev, [name]: value }));
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setIsSearching(true);
    setError('');
    setMessage('');
    setMatchedUsers([]);
    setSentRequests({});
    try {
      const response = await axiosWithAuth.post('/match/find', searchCriteria);
      if (response.data && response.data.length > 0) {
        setMatchedUsers(response.data);
        setMessage('Match trovati!');
      } else {
        setMessage('Nessun match trovato con i criteri specificati.');
      }
    } catch (err) {
      setError(
        `Errore durante la ricerca di match: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setIsSearching(false);
    }
  };

  const handleSendFriendRequest = async (username) => {
    setError('');
    setMessage('');
    try {
      await axiosWithAuth.post(`/friends/request/${username}`);
      setMessage(`Richiesta di amicizia inviata a ${username}!`);
    } catch (err) {
      if (err.response?.status === 400 && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError(
          `Impossibile inviare richiesta a ${username}: ${
            err.response?.data?.message || 'Errore sconosciuto.'
          }`
        );
      }
    }
  };

  const handleRequestGameMatch = async (receiverId, gameId) => {
    setError('');
    setMessage('');
    setSentRequests((prev) => ({ ...prev, [receiverId]: true }));
    try {
      await axiosWithAuth.post('/match/request', { receiverId, gameId });
      const username = matchedUsers.find((u) => u.id === receiverId)?.username;
      const gameName = allGames.find((g) => g.id === gameId)?.name;
      setMessage(
        `Richiesta di partita inviata a ${username} per ${gameName}! In attesa di accettazione...`
      );
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Non sei autorizzato a inviare richieste di partita.');
        localStorage.clear();
        navigate('/');
      } else {
        setError(
          `Impossibile inviare richiesta di partita: ${
            err.response?.data?.message || 'Errore sconosciuto.'
          }`
        );
      }
      setSentRequests((prev) => ({ ...prev, [receiverId]: false }));
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
            Caricamento opzioni matchmaking...
          </div>
        </div>
        <Footer />
      </>
    );
  }

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
            Caricamento opzioni matchmaking...
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
              TROVA IL TUO PIXELPAL PERFETTO
            </h1>
            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            {/* Sezione Criteri di Ricerca */}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-blue-700">
              <h2 className="text-2xl font-bold text-cyan-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faSearch} className="mr-3" /> CRITERI DI
                RICERCA
              </h2>
              <form
                onSubmit={handleSearch}
                className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
              >
                <div>
                  <label
                    htmlFor="gameName"
                    className="block text-gray-300 text-sm font-bold mb-2"
                  >
                    <FontAwesomeIcon icon={faGamepad} className="mr-2" /> Gioco
                    Preferito
                  </label>
                  <select
                    id="gameName"
                    name="gameName"
                    value={searchCriteria.gameName}
                    onChange={handleChange}
                    className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                  >
                    <option value="">Qualsiasi Gioco</option>
                    {allGames.map((game) => (
                      <option key={game.id} value={game.name}>
                        {game.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label
                    htmlFor="platformName"
                    className="block text-gray-300 text-sm font-bold mb-2"
                  >
                    <FontAwesomeIcon icon={faLaptop} className="mr-2" />{' '}
                    Piattaforma Preferita
                  </label>
                  <select
                    id="platformName"
                    name="platformName"
                    value={searchCriteria.platformName}
                    onChange={handleChange}
                    className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                  >
                    <option value="">Qualsiasi Piattaforma</option>
                    {allPlatforms.map((platform) => (
                      <option key={platform.id} value={platform.name}>
                        {platform.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label
                    htmlFor="skillLevel"
                    className="block text-gray-300 text-sm font-bold mb-2"
                  >
                    <FontAwesomeIcon icon={faStar} className="mr-2" /> Livello
                    di Skill
                  </label>
                  <select
                    id="skillLevel"
                    name="skillLevel"
                    value={searchCriteria.skillLevel}
                    onChange={handleChange}
                    className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                  >
                    <option value="">Qualsiasi Livello</option>
                    {skillOptions.map((option) => (
                      <option key={option} value={option}>
                        {option}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="md:col-span-2 lg:col-span-3 flex justify-center mt-4">
                  <button
                    type="submit"
                    className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center justify-center"
                    disabled={isSearching}
                  >
                    <FontAwesomeIcon icon={faUsers} className="mr-3" />
                    {isSearching ? 'Ricerca in corso...' : 'TROVA PIXELPALS!'}
                  </button>
                </div>
              </form>
            </section>

            {isSearching && (
              <p className="text-center text-gray-400 text-lg mt-8">
                <FontAwesomeIcon
                  icon={faHourglassHalf}
                  className="mr-2 animate-spin"
                />
                Ricerca in corso...
              </p>
            )}
            {!isSearching && matchedUsers.length === 0 && !error && message && (
              <p className="text-center text-gray-400 text-lg mt-8">
                {message}
              </p>
            )}
            {!isSearching && matchedUsers.length > 0 && (
              <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-green-700">
                <h2 className="text-2xl font-bold text-lime-400 font-oxanium mb-4 flex items-center">
                  <FontAwesomeIcon icon={faUsers} className="mr-3" /> PIXELPALS
                  TROVATI ({matchedUsers.length})
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {matchedUsers.map((user) => (
                    <div
                      key={user.id}
                      className="bg-gray-700 p-4 rounded-lg flex flex-col items-center border border-gray-600 shadow-md transform hover:scale-105 transition duration-200"
                    >
                      <img
                        src={
                          user.avatarUrl ||
                          `https://placehold.co/80x80/3B82F6/FFFFFF?text=${user.username
                            .charAt(0)
                            .toUpperCase()}`
                        }
                        alt="Avatar"
                        className="w-20 h-20 rounded-full object-cover border-4 border-blue-500 mb-3"
                      />
                      <h3 className="text-xl font-bold text-white font-oxanium">
                        {user.username}
                      </h3>
                      <p className="text-gray-300 text-sm mb-2">
                        Livello: {user.level} | Rating:{' '}
                        {user.rating?.toFixed(1)}
                      </p>

                      <div className="text-center text-gray-400 text-xs mb-3">
                        {user.online ? (
                          <span className="text-green-400">Online</span>
                        ) : (
                          <span className="text-red-400">Offline</span>
                        )}
                      </div>

                      {user.commonGames && user.commonGames.length > 0 && (
                        <div className="mb-2">
                          <p className="text-gray-300 font-semibold text-sm">
                            Giochi in comune:
                          </p>
                          <div className="flex flex-wrap justify-center gap-1">
                            {user.commonGames.map((game, idx) => (
                              <span
                                key={idx}
                                className="bg-blue-600 text-white text-xs px-2 py-0.5 rounded-full"
                              >
                                {game}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}

                      {user.commonPlatforms &&
                        user.commonPlatforms.length > 0 && (
                          <div className="mb-2">
                            <p className="text-gray-300 font-semibold text-sm">
                              Piattaforme in comune:
                            </p>
                            <div className="flex flex-wrap justify-center gap-1">
                              {user.commonPlatforms.map((platform, idx) => (
                                <span
                                  key={idx}
                                  className="bg-purple-600 text-white text-xs px-2 py-0.5 rounded-full"
                                >
                                  {platform}
                                </span>
                              ))}
                            </div>
                          </div>
                        )}

                      {user.skillLevelForGame && searchCriteria.gameName && (
                        <p className="text-gray-300 text-sm mb-3">
                          Skill in {searchCriteria.gameName}:{' '}
                          <span className="font-bold text-yellow-300">
                            {user.skillLevelForGame}
                          </span>
                        </p>
                      )}

                      <div className="mt-auto pt-3">
                        <span className="text-lg font-bold text-green-400">
                          Compatibilità: {user.compatibilityScore?.toFixed(0)}%
                        </span>
                      </div>

                      {acceptedFriendIds.has(user.id) ? (
                        <span className="mt-4 bg-gray-600 text-white font-bold py-2 px-5 rounded-md flex items-center justify-center text-sm opacity-80 cursor-default">
                          <FontAwesomeIcon
                            icon={faUserFriends}
                            className="mr-2"
                          />{' '}
                          Già Amici
                        </span>
                      ) : (
                        <button
                          onClick={() => handleSendFriendRequest(user.username)}
                          className="mt-4 bg-gradient-to-r from-green-600 to-lime-600 hover:from-green-700 hover:to-lime-700 text-white font-bold py-2 px-5 rounded-md transition duration-200 flex items-center justify-center text-sm"
                        >
                          <FontAwesomeIcon icon={faUserPlus} className="mr-2" />{' '}
                          Aggiungi Amico
                        </button>
                      )}

                      {searchCriteria.gameName && (
                        <button
                          onClick={() =>
                            handleRequestGameMatch(
                              user.id,
                              allGames.find(
                                (g) => g.name === searchCriteria.gameName
                              )?.id
                            )
                          }
                          disabled={
                            !searchCriteria.gameName || sentRequests[user.id]
                          }
                          className="mt-2 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-2 px-5 rounded-md transition duration-200 flex items-center justify-center text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          <FontAwesomeIcon icon={faDice} className="mr-2" />{' '}
                          {sentRequests[user.id]
                            ? 'Richiesta Inviata'
                            : 'Richiedi Partita'}
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              </section>
            )}

            {!isSearching &&
              matchedUsers.length === 0 &&
              !loading &&
              !error && (
                <p className="text-center text-gray-400 text-lg mt-8">
                  Nessun PixelPal trovato con i criteri specificati. Prova a
                  modificare la ricerca!
                </p>
              )}
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default MatchmakingPage;
