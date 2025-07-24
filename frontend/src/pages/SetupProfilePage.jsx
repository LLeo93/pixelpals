import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faGamepad,
  faLaptop,
  faStar,
  faSave,
  faTimesCircle,
  faCheck,
} from '@fortawesome/free-solid-svg-icons';
const SetupProfilePage = () => {
  const navigate = useNavigate();
  const [allGames, setAllGames] = useState([]);
  const [allPlatforms, setAllPlatforms] = useState([]);
  const [preferredGames, setPreferredGames] = useState([]);
  const [selectedPlatforms, setSelectedPlatforms] = useState([]);
  const [skillLevels, setSkillLevels] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const skillOptions = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const gamesRes = await axiosWithAuth.get('/games');
        setAllGames(gamesRes.data);
        const platformsRes = await axiosWithAuth.get('/platforms');
        setAllPlatforms(platformsRes.data);
        const userRes = await axiosWithAuth.get('/auth/me');
        const userData = userRes.data;
        if (userData.preferredGames) {
          setPreferredGames(userData.preferredGames.map((g) => g.id));
          const initialSkills = {};
          if (userData.skillLevelMap) {
            for (const gameName in userData.skillLevelMap) {
              const game = gamesRes.data.find((g) => g.name === gameName);
              if (game) {
                initialSkills[game.id] = userData.skillLevelMap[gameName];
              }
            }
          }
          setSkillLevels(initialSkills);
        }
        if (userData.platforms) {
          setSelectedPlatforms(userData.platforms.map((p) => p.id));
        }
      } catch (err) {
        console.error('Errore nel recupero dati iniziali:', err);
        setError('Errore durante il caricamento dei dati. Riprova più tardi.');
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

  const handleGameSelection = (gameId) => {
    setPreferredGames((prev) =>
      prev.includes(gameId)
        ? prev.filter((id) => id !== gameId)
        : [...prev, gameId]
    );
    if (preferredGames.includes(gameId)) {
      setSkillLevels((prev) => {
        const newSkills = { ...prev };
        delete newSkills[gameId];
        return newSkills;
      });
    }
  };
  const handlePlatformSelection = (platformId) => {
    setSelectedPlatforms((prev) =>
      prev.includes(platformId)
        ? prev.filter((id) => id !== platformId)
        : [...prev, platformId]
    );
  };
  const handleSkillLevelChange = (gameId, level) => {
    setSkillLevels((prev) => ({
      ...prev,
      [gameId]: level,
    }));
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const gameNamesToSend = allGames
        .filter((game) => preferredGames.includes(game.id))
        .map((game) => game.name);

      const platformNamesToSend = allPlatforms
        .filter((platform) => selectedPlatforms.includes(platform.id))
        .map((platform) => platform.name);

      const skillLevelsToSend = {};
      for (const gameId in skillLevels) {
        const game = allGames.find((g) => g.id === gameId);
        if (game) {
          skillLevelsToSend[game.name] = skillLevels[gameId];
        }
      }

      await axiosWithAuth.put('/users/preferredGames', gameNamesToSend);

      await axiosWithAuth.put('/users/platforms', platformNamesToSend);

      await axiosWithAuth.put('/users/skillLevels', skillLevelsToSend);

      setMessage('Preferenze salvate con successo!');
      setLoading(false);
      navigate('/home');
    } catch (err) {
      console.error('Errore nel salvataggio preferenze:', err);
      setError('Errore durante il salvataggio delle preferenze. Riprova.');
      setLoading(false);
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
            Caricamento preferenze...
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
          <div className="max-w-4xl mx-auto bg-gray-800 bg-opacity-90 p-8 rounded-xl shadow-2xl border border-blue-700 space-y-8">
            <h1 className="text-3xl font-extrabold text-white text-center drop-shadow-lg font-oxanium">
              CONFIGURA IL TUO PROFILO GAMING
            </h1>
            <p className="text-gray-300 text-center mb-6">
              Seleziona i tuoi giochi preferiti, le piattaforme e il tuo livello
              di skill.
            </p>

            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            <form onSubmit={handleSubmit} className="space-y-8">
              <section>
                <h2 className="text-2xl font-bold text-purple-400 font-oxanium mb-4 flex items-center">
                  <FontAwesomeIcon icon={faGamepad} className="mr-3" /> GIOCHI
                  PREFERITI
                </h2>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                  {allGames.map((game) => (
                    <div
                      key={game.id}
                      className={`relative rounded-lg overflow-hidden shadow-md cursor-pointer transition duration-200
                        ${
                          preferredGames.includes(game.id)
                            ? 'border-4 border-green-500 scale-105'
                            : 'border border-gray-600 hover:scale-105'
                        }`}
                      onClick={() => handleGameSelection(game.id)}
                    >
                      <img
                        src={
                          game.imageUrl ||
                          `https://placehold.co/150x100/4B5563/FFFFFF?text=${game.name.substring(
                            0,
                            5
                          )}`
                        }
                        alt={game.name}
                        className="w-full h-24 object-cover"
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src =
                            'https://placehold.co/150x100/4B5563/FFFFFF?text=No+Image';
                        }}
                      />
                      <div className="p-2 bg-gray-700 bg-opacity-80 absolute bottom-0 w-full">
                        <h3 className="text-gray-100 font-semibold text-sm truncate">
                          {game.name}
                        </h3>
                      </div>
                      {preferredGames.includes(game.id) && (
                        <div className="absolute top-2 right-2 text-green-400">
                          <FontAwesomeIcon icon={faCheck} size="lg" />
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </section>

              {preferredGames.length > 0 && (
                <section>
                  <h2 className="text-2xl font-bold text-orange-400 font-oxanium mb-4 flex items-center">
                    <FontAwesomeIcon icon={faStar} className="mr-3" /> LIVELLO
                    DI SKILL
                  </h2>
                  <div className="space-y-4">
                    {preferredGames.map((gameId) => {
                      const game = allGames.find((g) => g.id === gameId);
                      if (!game) return null;
                      return (
                        <div
                          key={game.id}
                          className="bg-gray-700 p-4 rounded-lg flex flex-col sm:flex-row items-center justify-between border border-gray-600 shadow-md"
                        >
                          <span className="text-gray-100 font-semibold mb-2 sm:mb-0 sm:w-1/3">
                            {game.name}
                          </span>
                          <select
                            value={skillLevels[game.id] || ''}
                            onChange={(e) =>
                              handleSkillLevelChange(game.id, e.target.value)
                            }
                            className="w-full sm:w-2/3 px-4 py-2 bg-gray-600 border border-gray-500 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                          >
                            <option value="" disabled>
                              Seleziona livello
                            </option>
                            {skillOptions.map((option) => (
                              <option key={option} value={option}>
                                {option}
                              </option>
                            ))}
                          </select>
                        </div>
                      );
                    })}
                  </div>
                </section>
              )}

              <section>
                <h2 className="text-2xl font-bold text-cyan-400 font-oxanium mb-4 flex items-center">
                  <FontAwesomeIcon icon={faLaptop} className="mr-3" />{' '}
                  PIATTAFORME
                </h2>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                  {allPlatforms.map((platform) => (
                    <div
                      key={platform.id}
                      className={`relative rounded-lg p-4 text-center shadow-md cursor-pointer transition duration-200
                        ${
                          selectedPlatforms.includes(platform.id)
                            ? 'border-4 border-green-500 scale-105'
                            : 'border border-gray-600 hover:scale-105'
                        }`}
                      onClick={() => handlePlatformSelection(platform.id)}
                    >
                      <img
                        src={
                          platform.iconUrl ||
                          `https://placehold.co/60x60/4B5563/FFFFFF?text=${platform.name
                            .charAt(0)
                            .toUpperCase()}`
                        }
                        alt={platform.name}
                        className="w-16 h-16 mx-auto mb-2 object-contain"
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src = `https://placehold.co/60x60/4B5563/FFFFFF?text=${platform.name
                            .charAt(0)
                            .toUpperCase()}`;
                        }}
                      />
                      <h3 className="text-gray-100 font-semibold text-sm">
                        {platform.name}
                      </h3>
                      {selectedPlatforms.includes(platform.id) && (
                        <div className="absolute top-2 right-2 text-green-400">
                          <FontAwesomeIcon icon={faCheck} size="lg" />
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </section>

              <div className="flex justify-center space-x-4 mt-8">
                <button
                  type="submit"
                  className="bg-gradient-to-r from-green-600 to-blue-600 hover:from-green-700 hover:to-blue-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center"
                >
                  <FontAwesomeIcon icon={faSave} className="mr-3" /> Salva
                  Preferenze
                </button>
                <button
                  type="button"
                  onClick={() => navigate('/home')}
                  className="bg-gray-700 hover:bg-gray-600 text-gray-200 font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center"
                >
                  <FontAwesomeIcon icon={faTimesCircle} className="mr-3" />{' '}
                  Salta
                </button>
              </div>
            </form>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

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

export default SetupProfilePage;
