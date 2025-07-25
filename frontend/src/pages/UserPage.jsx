import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/Alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faUserCircle,
  faGamepad,
  faLaptop,
  faStar,
  faLevelUpAlt,
  faAward,
  faQuoteLeft,
  faHome,
} from '@fortawesome/free-solid-svg-icons';

const UserPage = () => {
  const navigate = useNavigate();
  const { userId } = useParams();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchUserProfile = async () => {
      setLoading(true);
      setError('');
      try {
        const userRes = await axiosWithAuth.get(`/users/${userId}`);
        const userData = userRes.data;
        setUser(userData);
      } catch (err) {
        console.error(`Errore nel recupero del profilo utente ${userId}:`, err);
        setError(
          "Errore durante il caricamento del profilo utente. L'utente potrebbe non esistere o la sessione è scaduta."
        );
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchUserProfile();
    } else {
      setError('ID utente non fornito.');
      setLoading(false);
    }
  }, [userId, navigate]);

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white text-3xl font-bold font-oxanium">
          Caricamento profilo utente...
        </div>
        <Footer />
      </>
    );
  }

  if (error) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white text-xl font-bold font-oxanium">
          <Alert type="error" message={error} />
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
          Profilo utente non disponibile.
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

            <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
              <h2 className="text-xl font-bold text-cyan-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faQuoteLeft} className="mr-2" /> BIO
              </h2>
              <p className="text-gray-200">
                {user.bio || 'Nessuna bio impostata.'}
              </p>
            </section>

            <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
              <h2 className="text-xl font-bold text-orange-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faGamepad} className="mr-2" /> GIOCHI
                PREFERITI
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
              <h2 className="text-xl font-bold text-lime-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faLaptop} className="mr-2" /> PIATTAFORME
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
                <p className="text-gray-400">Nessuna piattaforma impostata.</p>
              )}
            </section>

            <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
              <h2 className="text-xl font-bold text-pink-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faStar} className="mr-2" /> LIVELLI DI
                SKILL
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

            <section className="bg-gray-700 bg-opacity-70 p-6 rounded-lg border border-gray-600 shadow-md">
              <h2 className="text-xl font-bold text-orange-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faAward} className="mr-2" /> I Suoi Badge
              </h2>
              {user.badges && user.badges.length > 0 ? (
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                  {user.badges.map((badge) => (
                    <div
                      key={badge.id}
                      className="bg-gray-600 p-3 rounded-lg flex flex-col items-center text-center border border-gray-500 shadow-md transform hover:scale-105 transition duration-200"
                      title={badge.description}
                    >
                      <img
                        src={
                          badge.imageUrl ||
                          `https://placehold.co/60x60/FFD700/000000?text=Badge`
                        }
                        alt={badge.name}
                        className="w-16 h-16 object-contain mb-2"
                      />
                      <p className="text-sm font-semibold text-white">
                        {badge.name}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400 italic">
                  Nessun badge ancora per questo utente.
                </p>
              )}
            </section>

            <div className="flex justify-center mt-8">
              <button
                onClick={() => navigate('/home')}
                className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center"
              >
                <FontAwesomeIcon icon={faHome} className="mr-3" /> Torna alla
                Homepage
              </button>
            </div>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default UserPage;
