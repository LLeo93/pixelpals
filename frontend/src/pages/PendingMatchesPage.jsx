import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/Alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faHourglassHalf,
  faCheckCircle,
  faTimesCircle,
  faGamepad,
} from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2';

const PendingMatchesPage = () => {
  const [pendingMatches, setPendingMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const fetchPendingMatches = async () => {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const response = await axiosWithAuth.get('/match/pending-game-match');
      setPendingMatches(response.data);
      if (response.data.length === 0) {
        setMessage('Nessuna richiesta di partita in sospeso.');
      }
    } catch (err) {
      setError(
        `Impossibile caricare le richieste di partita: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingMatches();
  }, []);

  const handleAction = async (matchId, action) => {
    Swal.fire({
      title: action === 'accept' ? 'Accetta Partita?' : 'Rifiuta Partita?',
      text:
        action === 'accept'
          ? 'Sei sicuro di voler accettare questa partita?'
          : 'Sei sicuro di voler rifiutare questa partita?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: action === 'accept' ? 'Sì, Accetta!' : 'Sì, Rifiuta!',
      cancelButtonText: 'Annulla',
      customClass: {
        popup:
          'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
        title: 'text-blue-400',
        confirmButton:
          'bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md',
        cancelButton:
          'bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-md',
      },
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          const response = await axiosWithAuth.put(
            `/match/${matchId}/${action}`
          );
          setMessage(
            `Richiesta di partita ${
              action === 'accept' ? 'accettata' : 'rifiutata'
            } con successo!`
          );

          if (action === 'accept') {
            localStorage.setItem('activeMatchId', matchId);
            Swal.fire({
              title: 'Partita Accettata!',
              text: 'Verrai reindirizzato alla stanza della partita.',
              icon: 'success',
              timer: 2000,
              timerProgressBar: true,
              showConfirmButton: false,
              customClass: {
                popup:
                  'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
                title: 'text-green-400',
              },
            }).then(() => {
              navigate(`/match-room/${matchId}`);
            });
          } else {
            fetchPendingMatches();
          }
        } catch (err) {
          setError(
            `Impossibile ${action} la partita: ${
              err.response?.data?.message || 'Errore sconosciuto.'
            }`
          );
        }
      }
    });
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
            Caricamento richieste in sospeso...
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
          <div className="max-w-4xl mx-auto space-y-8 bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-blue-700">
            <h1 className="text-3xl font-extrabold text-white text-center drop-shadow-lg font-oxanium mb-8">
              RICHIESTE PARTITA IN SOSPESO
            </h1>

            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            {pendingMatches.length === 0 ? (
              <p className="text-center text-gray-400 text-lg mt-8">
                Nessuna richiesta di partita in sospeso al momento.
              </p>
            ) : (
              <div className="grid grid-cols-1 gap-6">
                {pendingMatches.map((match) => (
                  <div
                    key={match.id}
                    className="bg-gray-700 p-4 rounded-lg shadow-md border border-gray-600 flex flex-col md:flex-row items-center justify-between"
                  >
                    <div className="flex items-center mb-4 md:mb-0">
                      <FontAwesomeIcon
                        icon={faGamepad}
                        className="text-5xl text-blue-400 mr-4"
                      />
                      <div>
                        <p className="text-white font-bold text-xl">
                          Richiesta da: {match.userAUsername}
                        </p>
                        <p className="text-gray-300 text-sm">
                          Gioco: {match.gameName}
                        </p>
                        <p className="text-gray-400 text-xs mt-1">
                          Inviata il:{' '}
                          {new Date(match.matchedAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                    <div className="flex space-x-3">
                      <button
                        onClick={() => handleAction(match.id, 'accept')}
                        className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md transition duration-200 flex items-center"
                      >
                        <FontAwesomeIcon
                          icon={faCheckCircle}
                          className="mr-2"
                        />{' '}
                        Accetta
                      </button>
                      <button
                        onClick={() => handleAction(match.id, 'decline')}
                        className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-md transition duration-200 flex items-center"
                      >
                        <FontAwesomeIcon
                          icon={faTimesCircle}
                          className="mr-2"
                        />{' '}
                        Rifiuta
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
            <div className="text-center mt-8">
              <button
                onClick={() => navigate('/home')}
                className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-6 rounded-md transition duration-200"
              >
                Torna alla Home
              </button>
            </div>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default PendingMatchesPage;
