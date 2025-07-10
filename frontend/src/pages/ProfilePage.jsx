import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faUserCircle,
  faEdit,
  faSave,
  faTimes,
  faGamepad,
  faLaptop,
  faStar,
  faLevelUpAlt,
  faAward,
} from '@fortawesome/free-solid-svg-icons';

const ProfilePage = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({});

  // States for games, platforms, and skills management
  const [allGames, setAllGames] = useState([]);
  const [allPlatforms, setAllPlatforms] = useState([]);
  const [preferredGames, setPreferredGames] = useState([]); // Stores game IDs
  const [selectedPlatforms, setSelectedPlatforms] = useState([]); // Stores platform IDs
  const [skillLevels, setSkillLevels] = useState({}); // { gameId: skillLevelString }

  const skillOptions = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

  useEffect(() => {
    fetchUserProfile();
    fetchAllGamesAndPlatforms();
  }, [navigate]);

  const fetchUserProfile = async () => {
    setLoading(true);
    setError('');
    try {
      const userRes = await axiosWithAuth.get('/auth/me');
      const userData = userRes.data;
      setUser(userData);
      setFormData({
        username: userData.username,
        email: userData.email,
        avatarUrl: userData.avatarUrl || '',
        bio: userData.bio || '',
        level: userData.level,
        rating: userData.rating,
      });

      // Initialize preferred games, platforms, and skill levels from user data
      if (userData.preferredGames) {
        setPreferredGames(userData.preferredGames.map((g) => g.id));
        const initialSkills = {};
        if (userData.skillLevelMap) {
          for (const gameName in userData.skillLevelMap) {
            // Find the game ID by name from allGames (will be populated by fetchAllGamesAndPlatforms)
            // This might cause a slight delay if allGames is not yet fetched,
            // but the data will eventually reconcile.
            const game = allGames.find((g) => g.name === gameName);
            if (game) {
              initialSkills[game.id] = userData.skillLevelMap[gameName];
            }
          }
        }
        setSkillLevels(initialSkills);
      } else {
        setPreferredGames([]);
        setSkillLevels({});
      }

      if (userData.platforms) {
        setSelectedPlatforms(userData.platforms.map((p) => p.id));
      } else {
        setSelectedPlatforms([]);
      }
    } catch (err) {
      console.error('Errore nel recupero del profilo:', err);
      setError(
        'Errore durante il caricamento del profilo. Potresti non avere i permessi o la sessione è scaduta.'
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchAllGamesAndPlatforms = async () => {
    try {
      const gamesRes = await axiosWithAuth.get('/games');
      setAllGames(gamesRes.data);

      const platformsRes = await axiosWithAuth.get('/platforms');
      setAllPlatforms(platformsRes.data);
    } catch (err) {
      console.error('Errore nel recupero di giochi e piattaforme:', err);
      // Non impostare un errore grave che blocca il caricamento del profilo
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleEditClick = () => {
    setIsEditing(true);
    setMessage('');
    setError('');
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    // Reset form data to original user data
    if (user) {
      setFormData({
        username: user.username,
        email: user.email,
        avatarUrl: user.avatarUrl || '',
        bio: user.bio || '',
        level: user.level,
        rating: user.rating,
      });
      // Reset game/platform/skill selections
      setPreferredGames(
        user.preferredGames ? user.preferredGames.map((g) => g.id) : []
      );
      setSelectedPlatforms(
        user.platforms ? user.platforms.map((p) => p.id) : []
      );
      const initialSkills = {};
      if (user.skillLevelMap) {
        for (const gameName in user.skillLevelMap) {
          const game = allGames.find((g) => g.name === gameName);
          if (game) {
            initialSkills[game.id] = user.skillLevelMap[gameName];
          }
        }
      }
      setSkillLevels(initialSkills);
    }
    setError('');
    setMessage('');
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      // Update basic user fields
      await axiosWithAuth.put('/auth/me/update', formData);

      // Prepare data for preferredGames
      const gameNamesToSend = allGames
        .filter((game) => preferredGames.includes(game.id))
        .map((game) => game.name);

      // Prepare data for platforms
      const platformNamesToSend = allPlatforms
        .filter((platform) => selectedPlatforms.includes(platform.id))
        .map((platform) => platform.name);

      // Prepare data for skillLevels (map gameName -> skillLevel)
      const skillLevelsToSend = {};
      for (const gameId in skillLevels) {
        const game = allGames.find((g) => g.id === gameId);
        if (game) {
          skillLevelsToSend[game.name] = skillLevels[gameId];
        }
      }

      // Update preferred games
      await axiosWithAuth.put('/users/preferredGames', gameNamesToSend);

      // Update platforms
      await axiosWithAuth.put('/users/platforms', platformNamesToSend);

      // Update skill levels
      await axiosWithAuth.put('/users/skillLevels', skillLevelsToSend);

      setMessage('Profilo aggiornato con successo!');
      setIsEditing(false);
      fetchUserProfile(); // Ricarica i dati per visualizzare le modifiche
    } catch (err) {
      console.error('Errore nel salvataggio del profilo:', err);
      setError("Errore durante l'aggiornamento del profilo. Riprova.");
    } finally {
      setLoading(false);
    }
  };

  // Handlers for game/platform/skill selection (copied from SetupProfilePage)
  const handleGameSelection = (gameId) => {
    setPreferredGames((prev) =>
      prev.includes(gameId)
        ? prev.filter((id) => id !== gameId)
        : [...prev, gameId]
    );
    // Remove skill level if game is deselected
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

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white text-3xl font-bold font-oxanium">
          Caricamento profilo...
        </div>
        <Footer />
      </>
    );
  }

  if (!user) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white text-3xl font-bold font-oxanium">
          Profilo non trovato o accesso non autorizzato.
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
          <div className="max-w-4xl mx-auto bg-gray-800 bg-opacity-90 p-8 rounded-xl shadow-2xl border border-purple-700 space-y-8">
            <div className="flex flex-col items-center mb-6">
              <img
                src={
                  user.avatarUrl ||
                  `https://placehold.co/150x150/3B82F6/FFFFFF?text=${user.username
                    .charAt(0)
                    .toUpperCase()}`
                }
                alt="Avatar Utente"
                className="w-32 h-32 rounded-full border-4 border-blue-500 shadow-lg object-cover mb-4"
              />
              <h1 className="text-4xl font-extrabold text-blue-400 font-oxanium drop-shadow-lg">
                {user.username.toUpperCase()}
              </h1>
              <p className="text-gray-300 text-lg mt-2">{user.email}</p>
              <div className="flex items-center text-lg text-gray-200 mt-2">
                <FontAwesomeIcon
                  icon={faLevelUpAlt}
                  className="mr-2 text-green-400"
                />
                Livello:{' '}
                <span className="font-bold text-green-300 ml-1">
                  {user.level}
                </span>
                <FontAwesomeIcon
                  icon={faStar}
                  className="ml-4 mr-2 text-yellow-400"
                />
                Rating:{' '}
                <span className="font-bold text-yellow-300 ml-1">
                  {user.rating?.toFixed(1)}
                </span>
              </div>
            </div>

            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            {!isEditing ? (
              <div className="space-y-6">
                <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
                  <h2 className="text-xl font-bold text-cyan-400 font-oxanium mb-3">
                    BIO
                  </h2>
                  <p className="text-gray-200">
                    {user.bio || 'Nessuna bio impostata.'}
                  </p>
                </section>

                <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
                  <h2 className="text-xl font-bold text-orange-400 font-oxanium mb-3">
                    GIOCHI PREFERITI
                  </h2>
                  {user.preferredGames && user.preferredGames.length > 0 ? (
                    <div className="flex flex-wrap gap-3">
                      {user.preferredGames.map((game) => (
                        <span
                          key={game.id}
                          className="bg-blue-600 text-white text-sm px-3 py-1 rounded-full shadow-md"
                        >
                          {game.name}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-400">
                      Nessun gioco preferito impostato.
                    </p>
                  )}
                </section>

                <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
                  <h2 className="text-xl font-bold text-lime-400 font-oxanium mb-3">
                    PIATTAFORME
                  </h2>
                  {user.platforms && user.platforms.length > 0 ? (
                    <div className="flex flex-wrap gap-3">
                      {user.platforms.map((platform) => (
                        <span
                          key={platform.id}
                          className="bg-green-600 text-white text-sm px-3 py-1 rounded-full shadow-md"
                        >
                          {platform.name}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <p className="text-gray-400">
                      Nessuna piattaforma impostata.
                    </p>
                  )}
                </section>

                <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
                  <h2 className="text-xl font-bold text-pink-400 font-oxanium mb-3">
                    LIVELLI DI SKILL
                  </h2>
                  {user.skillLevelMap &&
                  Object.keys(user.skillLevelMap).length > 0 ? (
                    <ul className="list-disc list-inside text-gray-200">
                      {Object.entries(user.skillLevelMap).map(
                        ([gameName, skillLevel]) => (
                          <li key={gameName} className="mb-1">
                            <span className="font-semibold">{gameName}:</span>{' '}
                            {skillLevel}
                          </li>
                        )
                      )}
                    </ul>
                  ) : (
                    <p className="text-gray-400">
                      Nessun livello di skill impostato.
                    </p>
                  )}
                </section>

                <div className="flex justify-center">
                  <button
                    onClick={handleEditClick}
                    className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center"
                  >
                    <FontAwesomeIcon icon={faEdit} className="mr-3" /> Modifica
                    Profilo
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleSave} className="space-y-6">
                {/* Campi modificabili */}
                <Input
                  label="Username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                />
                <Input
                  label="Email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  type="email"
                />
                <Input
                  label="Avatar URL"
                  name="avatarUrl"
                  value={formData.avatarUrl}
                  onChange={handleChange}
                />
                <Textarea
                  label="Bio"
                  name="bio"
                  value={formData.bio}
                  onChange={handleChange}
                />

                {/* Sezione Giochi Preferiti (simile a SetupProfilePage) */}
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

                {/* Sezione Livello di Skill per Giochi Selezionati */}
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

                {/* Sezione Piattaforme (simile a SetupProfilePage) */}
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
                    Modifiche
                  </button>
                  <button
                    type="button"
                    onClick={handleCancelEdit}
                    className="bg-gray-700 hover:bg-gray-600 text-gray-200 font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center"
                  >
                    <FontAwesomeIcon icon={faTimes} className="mr-3" /> Annulla
                  </button>
                </div>
              </form>
            )}
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

// Componenti riutilizzabili (Input, Textarea, Alert)
const Input = ({ label, name, value, onChange, type = 'text' }) => (
  <div>
    <label className="block text-sm font-medium text-gray-300 mb-1">
      {label}
    </label>
    <input
      name={name}
      value={value}
      onChange={onChange}
      type={type}
      className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
    />
  </div>
);

const Textarea = ({ label, name, value, onChange }) => (
  <div>
    <label className="block text-sm font-medium text-gray-300 mb-1">
      {label}
    </label>
    <textarea
      name={name}
      value={value}
      onChange={onChange}
      rows={4}
      className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
    />
  </div>
);

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

export default ProfilePage;
