import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import Alert from '../components/Alert';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faPaperPlane,
  faComments,
  faInfoCircle,
  faTimesCircle,
  faCheckCircle,
  faDoorClosed,
} from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { WS_URL } from '../services/config';

const MatchRoomPage = () => {
  const { matchId } = useParams();
  const navigate = useNavigate();
  const [matchDetails, setMatchDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [chatMessages, setChatMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [currentUser, setCurrentUser] = useState(null);
  const messagesEndRef = useRef(null);
  const stompClient = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chatMessages]);

  useEffect(() => {
    const fetchMatchDetails = async () => {
      try {
        const response = await axiosWithAuth.get(`/match/${matchId}`);
        setMatchDetails(response.data);
        setMessage('Dettagli partita caricati con successo.');
        const chatHistoryRes = await axiosWithAuth.get(
          `/messages/match/${matchId}/history`
        );
        setChatMessages(chatHistoryRes.data);
        const userRes = await axiosWithAuth.get('/auth/me');
        setCurrentUser(userRes.data);
        setLoading(false);
      } catch (err) {
        setError(
          `Errore durante il caricamento della partita: ${
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
      fetchMatchDetails();
    } else {
      setError('ID partita non fornito.');
      setLoading(false);
    }
  }, [matchId, navigate]);

  useEffect(() => {
    if (!matchId || !currentUser) return;

    const socket = new SockJS(WS_URL);
    stompClient.current = new Client({
      webSocketFactory: () => socket,
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
      },
      onConnect: () => {
        setMessage('Connesso alla chat di partita.');
        stompClient.current.subscribe(
          `/topic/chatRoom/${matchId}`,
          (message) => {
            const receivedMessage = JSON.parse(message.body);
            setChatMessages((prevMessages) => [
              ...prevMessages,
              receivedMessage,
            ]);
          }
        );
        stompClient.current.subscribe(
          `/user/${currentUser.username}/queue/match-updates`,
          (message) => {
            const update = JSON.parse(message.body);
            if (update.type === 'MATCH_CLOSED' && update.matchId === matchId) {
              Swal.fire({
                title: 'Partita Terminata!',
                text: `${update.closerUsername} ha chiuso la partita. Ora puoi valutare il tuo avversario.`,
                icon: 'info',
                confirmButtonText: 'Valuta Avversario',
                allowOutsideClick: false,
                allowEscapeKey: false,
                customClass: {
                  popup:
                    'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
                  title: 'text-blue-400',
                  confirmButton:
                    'bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md',
                },
              }).then(() => {
                navigate(`/rate-match/${matchId}`);
              });
            }
          }
        );
      },
      onStompError: (frame) => {
        setError(
          `Errore di connessione alla chat: ${
            frame.headers.message || 'Errore sconosciuto.'
          }`
        );
      },
      onDisconnect: () => {
        setMessage('Disconnesso dalla chat di partita.');
      },
    });

    stompClient.current.activate();

    return () => {
      if (stompClient.current && stompClient.current.connected) {
        stompClient.current.deactivate();
      }
    };
  }, [matchId, currentUser, navigate]);

  const handleSendMessage = () => {
    if (newMessage.trim() && stompClient.current?.connected) {
      const chatMessage = {
        senderId: currentUser.id,
        senderUsername: currentUser.username,
        receiverId:
          matchDetails.userAId === currentUser.id
            ? matchDetails.userBId
            : matchDetails.userAId,
        content: newMessage.trim(),
        chatRoomId: matchId,
      };

      stompClient.current.publish({
        destination: `/app/chat.sendMessage`,
        body: JSON.stringify(chatMessage),
      });

      setNewMessage('');
    }
  };

  const handleAcceptDeclineMatch = async (action) => {
    setError('');
    setMessage('');
    try {
      const endpoint = `/match/${matchId}/${action}`;
      const response = await axiosWithAuth.put(endpoint);
      setMessage(
        `Richiesta di partita ${
          action === 'accept' ? 'accettata' : 'rifiutata'
        } con successo!`
      );
      setMatchDetails(response.data);
      if (action === 'decline') navigate('/pending-matches');
    } catch (err) {
      setError(
        `Impossibile ${action} la partita: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
    }
  };

  const handleCloseMatch = async () => {
    Swal.fire({
      title: 'Sei sicuro?',
      text: 'Vuoi davvero chiudere questa partita? Non potrai più riaprirla.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Sì, chiudi partita!',
      cancelButtonText: 'Annulla',
      customClass: {
        popup:
          'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
        title: 'text-orange-400',
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
          const response = await axiosWithAuth.put(`/match/close/${matchId}`);
          setMatchDetails(response.data);
          Swal.fire({
            title: 'Partita Chiusa!',
            text: 'La partita è stata chiusa con successo. Ora puoi valutare il tuo avversario.',
            icon: 'success',
            confirmButtonText: 'Vai al Rating',
            customClass: {
              popup:
                'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
              title: 'text-green-400',
              confirmButton:
                'bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-md',
            },
          }).then(() => {
            navigate(`/rate-match/${matchId}`);
          });
        } catch (err) {
          setError(
            `Impossibile chiudere la partita: ${
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
            Caricamento partita...
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
                Impossibile caricare i dettagli della partita.
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
              PARTITA: {matchDetails?.gameName || 'Caricamento...'}
            </h1>
            <p className="text-center text-gray-300 text-lg mb-6">
              Tra:{' '}
              <span className="font-bold text-cyan-400">
                {matchDetails?.userAUsername}
              </span>{' '}
              vs{' '}
              <span className="font-bold text-purple-400">
                {matchDetails?.userBUsername}
              </span>
            </p>
            <p className="text-center text-gray-400 text-sm mb-4">
              Stato:{' '}
              <span className="font-bold text-yellow-300">
                {matchDetails?.status}
              </span>
            </p>

            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}

            <section className="bg-gray-700 p-4 rounded-lg shadow-inner flex flex-col h-[50vh]">
              <h2 className="text-xl font-bold text-lime-400 font-oxanium mb-3 flex items-center">
                <FontAwesomeIcon icon={faComments} className="mr-2" /> Chat di
                Partita
              </h2>
              <div className="flex-grow overflow-y-auto space-y-3 p-2 custom-scrollbar">
                {chatMessages.length === 0 ? (
                  <p className="text-gray-400 text-center">
                    Nessun messaggio ancora. Inizia a chattare!
                  </p>
                ) : (
                  chatMessages.map((msg, index) => (
                    <div
                      key={msg.id || index}
                      className={`flex ${
                        msg.senderId === currentUser?.id
                          ? 'justify-end'
                          : 'justify-start'
                      }`}
                    >
                      <div
                        className={`max-w-[70%] p-3 rounded-lg shadow-md ${
                          msg.senderId === currentUser?.id
                            ? 'bg-blue-600 text-white rounded-br-none'
                            : 'bg-gray-600 text-gray-100 rounded-bl-none'
                        }`}
                      >
                        <p className="font-bold text-sm mb-1">
                          {msg.senderUsername || 'Utente Sconosciuto'}
                        </p>
                        <p className="text-sm">{msg.content}</p>
                        <p className="text-xs text-right opacity-75 mt-1">
                          {new Date(msg.timestamp).toLocaleTimeString()}
                        </p>
                      </div>
                    </div>
                  ))
                )}
                <div ref={messagesEndRef} />{' '}
              </div>

              <div className="mt-4 flex">
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                      handleSendMessage();
                    }
                  }}
                  placeholder="Scrivi un messaggio..."
                  className="flex-grow px-4 py-2 bg-gray-600 border border-gray-500 rounded-l-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleSendMessage}
                  className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-r-md transition duration-200 flex items-center justify-center"
                >
                  <FontAwesomeIcon icon={faPaperPlane} className="mr-2" /> Invia
                </button>
              </div>
            </section>

            {matchDetails?.status === 'PENDING' && (
              <section className="bg-gray-700 p-4 rounded-lg shadow-inner mt-6 text-center">
                <h2 className="text-xl font-bold text-orange-400 font-oxanium mb-3 flex items-center justify-center">
                  <FontAwesomeIcon icon={faInfoCircle} className="mr-2" />{' '}
                  Richiesta di Partita Pendente
                </h2>
                <p className="text-gray-300 mb-4">
                  Hai ricevuto una richiesta di partita da{' '}
                  <span className="font-bold text-cyan-300">
                    {matchDetails.userAId === currentUser?.id
                      ? matchDetails.userBUsername
                      : matchDetails.userAUsername}
                  </span>{' '}
                  per{' '}
                  <span className="font-bold text-yellow-300">
                    {matchDetails.gameName}
                  </span>
                  .
                </p>
                <div className="flex justify-center gap-4">
                  <button
                    onClick={() => handleAcceptDeclineMatch('accept')}
                    className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-5 rounded-md transition duration-200 flex items-center justify-center"
                  >
                    <FontAwesomeIcon icon={faCheckCircle} className="mr-2" />{' '}
                    Accetta
                  </button>
                  <button
                    onClick={() => handleAcceptDeclineMatch('decline')}
                    className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-5 rounded-md transition duration-200 flex items-center justify-center"
                  >
                    <FontAwesomeIcon icon={faTimesCircle} className="mr-2" />{' '}
                    Rifiuta
                  </button>
                </div>
              </section>
            )}

            {matchDetails?.status === 'ACCEPTED' && (
              <div className="text-center mt-8">
                <button
                  onClick={handleCloseMatch}
                  className="bg-red-700 hover:bg-red-800 text-white font-bold py-3 px-6 rounded-md transition duration-200 flex items-center justify-center mx-auto"
                >
                  <FontAwesomeIcon icon={faDoorClosed} className="mr-2" />{' '}
                  Chiudi Partita
                </button>
              </div>
            )}

            <div className="text-center mt-8">
              <button
                onClick={() => navigate('/matchmaking')}
                className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-6 rounded-md transition duration-200"
              >
                Torna a Matchmaking
              </button>
            </div>
          </div>
        </main>
      </div>
      <Footer />
    </>
  );
};

export default MatchRoomPage;
