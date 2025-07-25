import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/Alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faStar, faPaperPlane } from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2';

const RateMatchPage = () => {
  const { matchId } = useParams();
  const navigate = useNavigate();

  const [matchDetails, setMatchDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [rating, setRating] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [opponentUser, setOpponentUser] = useState(null);
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const fetchMatchAndUserDetails = async () => {
      try {
        const matchRes = await axiosWithAuth.get(`/match/${matchId}`);
        setMatchDetails(matchRes.data);
        const userRes = await axiosWithAuth.get('/auth/me');
        setCurrentUser(userRes.data);

        const opponentId =
          matchRes.data.userAId === userRes.data.id
            ? matchRes.data.userBId
            : matchRes.data.userAId;
        const opponentRes = await axiosWithAuth.get(`/users/${opponentId}`);
        setOpponentUser(opponentRes.data);

        setLoading(false);
      } catch (err) {
        console.error('Errore nel recupero dettagli partita o utente:', err);
        setError(
          `Impossibile caricare i dettagli per il rating: ${
            err.response?.data?.message || 'Errore sconosciuto.'
          }`
        );
        setLoading(false);
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      }
    };

    if (matchId) {
      fetchMatchAndUserDetails();
    } else {
      setError('ID partita non fornito per il rating.');
      setLoading(false);
    }
  }, [matchId, navigate]);

  const handleRatingSubmit = async () => {
    if (rating === 0) {
      setError('Per favore, seleziona un rating.');
      return;
    }
    if (!opponentUser) {
      setError("Impossibile determinare l'avversario da valutare.");
      return;
    }

    Swal.fire({
      title: 'Conferma Rating',
      text: `Stai per valutare ${opponentUser?.username} con ${rating} stelle. Vuoi procedere?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sì, valuta!',
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
        setLoading(true);
        setError('');
        setMessage('');
        try {
          await axiosWithAuth.post(`/match/rate/${matchId}`, {
            ratedUserId: opponentUser.id,
            rating: rating,
            feedback: feedback,
          });

          Swal.fire({
            title: 'Rating Inviato!',
            text: 'Grazie per aver valutato la partita. Torna al matchmaking per trovare nuovi PixelPals!',
            icon: 'success',
            confirmButtonText: 'Torna al Matchmaking',
            customClass: {
              popup:
                'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
              title: 'text-green-400',
              confirmButton:
                'bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md',
            },
          }).then(() => {
            navigate('/matchmaking');
          });
        } catch (err) {
          console.error("Errore durante l'invio del rating:", err);
          setError(
            `Impossibile inviare il rating: ${
              err.response?.data?.message || 'Errore sconosciuto.'
            }`
          );
          setLoading(false);
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
            Caricamento pagina di rating...
          </div>
        </div>
        <Footer />
      </>
    );
  }

  if (error && !matchDetails) {
    return (
      <>
        <Navbar />
        <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden">
          <main className="flex-grow p-4 pt-20 pb-24 relative z-10">
            <div className="max-w-7xl mx-auto space-y-8">
              <Alert type="error" message={error} />
              <p className="text-center text-gray-400 text-lg mt-8">
                Impossibile caricare i dettagli della partita per il rating.
              </p>
            </div>
          </main>
          <Footer />
        </div>
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
            <h1 className="text-3xl font-extrabold text-white text-center drop-shadow-lg font-oxanium mb-4">
              VALUTA LA PARTITA
            </h1>
            <p className="text-center text-gray-300 text-lg mb-6">
              Hai appena terminato una partita con:{' '}
              <span className="font-bold text-purple-400">
                {opponentUser?.username || 'Caricamento...'}
              </span>{' '}
              per il gioco{' '}
              <span className="font-bold text-cyan-400">
                {matchDetails?.gameName || 'Caricamento...'}
              </span>
              .
            </p>

            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            <section className="bg-gray-700 p-6 rounded-lg shadow-inner text-center">
              <h2 className="text-xl font-bold text-lime-400 font-oxanium mb-4">
                Come valuteresti il tuo PixelPal?
              </h2>
              <div className="flex justify-center items-center space-x-2 mb-6">
                {[1, 2, 3, 4, 5].map((star) => (
                  <FontAwesomeIcon
                    key={star}
                    icon={faStar}
                    className={`text-4xl cursor-pointer transition-colors duration-200 ${
                      star <= rating ? 'text-yellow-400' : 'text-gray-500'
                    }`}
                    onClick={() => setRating(star)}
                  />
                ))}
              </div>

              <label
                htmlFor="feedback"
                className="block text-gray-300 text-sm font-bold mb-2"
              >
                Lascia un feedback (opzionale):
              </label>
              <textarea
                id="feedback"
                value={feedback}
                onChange={(e) => setFeedback(e.target.value)}
                rows="4"
                className="w-full px-4 py-3 bg-gray-600 border border-gray-500 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                placeholder="Condividi la tua esperienza..."
              ></textarea>

              <button
                onClick={handleRatingSubmit}
                className="mt-6 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-3 px-8 rounded-md transition duration-300 transform hover:scale-105 shadow-lg text-lg flex items-center justify-center mx-auto"
                disabled={loading}
              >
                <FontAwesomeIcon icon={faPaperPlane} className="mr-3" /> Invia
                Rating
              </button>
            </section>

            <div className="text-center mt-8">
              <button
                onClick={() => navigate('/matchmaking')}
                className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-6 rounded-md transition duration-200"
              >
                Torna al Matchmaking
              </button>
            </div>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default RateMatchPage;
