import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faUserPlus,
  faCheck,
  faTimes,
  faTrashAlt,
  faSearch,
  faUserFriends,
  faPaperPlane,
} from '@fortawesome/free-solid-svg-icons';
import { useUnreadMessages } from '../components/UnreadMessagesContext';

const FriendsPage = () => {
  const navigate = useNavigate();
  const [friends, setFriends] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [sentRequests, setSentRequests] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [friendToRemove, setFriendToRemove] = useState(null);
  const currentUsername = localStorage.getItem('username');
  const [currentUserId, setCurrentUserId] = useState(
    localStorage.getItem('userId')
  );

  const { fetchPendingFriendRequests } = useUnreadMessages();

  useEffect(() => {
    if (!currentUserId) {
      axiosWithAuth
        .get('/auth/me')
        .then((res) => {
          localStorage.setItem('userId', res.data.id);
          setCurrentUserId(res.data.id);

          fetchFriendshipData(res.data.id);
        })
        .catch((err) => {
          console.error("Errore nel recupero dell'ID utente corrente:", err);
          setError(
            "Impossibile recuperare l'ID utente corrente. Riprova il login."
          );
          setLoading(false);
          if (err.response?.status === 401 || err.response?.status === 403) {
            localStorage.clear();
            navigate('/');
          }
        });
    } else {
      fetchFriendshipData(currentUserId);
    }
  }, [currentUserId, navigate]);

  const fetchFriendshipData = async (userId) => {
    setLoading(true);
    setError('');
    setMessage('');
    try {
      const friendsRes = await axiosWithAuth.get('/friends');

      const filteredFriends = friendsRes.data.filter(
        (friend) => friend.id !== userId
      );

      const uniqueFriends = Array.from(
        new Set(filteredFriends.map((f) => f.id))
      ).map((id) => filteredFriends.find((f) => f.id === id));
      setFriends(uniqueFriends);

      const pendingRes = await axiosWithAuth.get('/friends/pending');
      setPendingRequests(pendingRes.data);

      const sentRes = await axiosWithAuth.get('/friends/sent');
      setSentRequests(sentRes.data);

      fetchPendingFriendRequests();

      if (searchQuery.trim()) {
        const res = await axiosWithAuth.get(`/users?username=${searchQuery}`);
        const updatedSearchResults = res.data.filter(
          (user) => user.username !== currentUsername && user.id !== userId
        );
        setSearchResults(updatedSearchResults);
      }
    } catch (err) {
      console.error('Errore nel recupero dati amicizie:', err);
      setError(
        'Errore durante il caricamento delle amicizie. Potresti non avere i permessi o la sessione è scaduta.'
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      const res = await axiosWithAuth.get(`/users?username=${searchQuery}`);

      const filteredUsers = res.data.filter(
        (user) => user.username !== currentUsername && user.id !== currentUserId
      );
      setSearchResults(filteredUsers);

      if (filteredUsers.length === 0) {
        setMessage('Nessun utente trovato con questo username.');
      } else {
        setMessage('');
      }
    } catch (err) {
      console.error('Errore nella ricerca utenti:', err);
      setError('Errore durante la ricerca utenti.');
      setSearchResults([]);
    }
  };

  const getFriendshipStatus = (user) => {
    if (friends.some((f) => f.id === user.id)) {
      return 'Amico';
    }

    if (pendingRequests.some((req) => req.senderId === user.id)) {
      return 'Richiesta Ricevuta';
    }

    if (sentRequests.some((req) => req.receiverId === user.id)) {
      return 'Richiesta Inviata';
    }

    return 'Nessuna';
  };

  const handleSendRequest = async (receiverUsername) => {
    setError('');
    setMessage('');
    try {
      setSentRequests((prevSent) => [
        ...prevSent,
        {
          id: 'temp-' + Date.now(),
          receiverId: '',
          receiverUsername: receiverUsername,
          status: 'PENDING',
        },
      ]);
      setMessage(`Richiesta di amicizia inviata a ${receiverUsername}!`);

      await axiosWithAuth.post(`/friends/request/${receiverUsername}`);
      fetchFriendshipData(currentUserId);
    } catch (err) {
      console.error('Errore invio richiesta:', err);
      setError(
        `Impossibile inviare richiesta: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
      fetchFriendshipData(currentUserId);
    }
  };

  const handleAcceptRequest = async (requestId) => {
    setError('');
    setMessage('');
    try {
      await axiosWithAuth.put(`/friends/accept/${requestId}`);
      setMessage('Richiesta di amicizia accettata!');
      fetchFriendshipData(currentUserId);
    } catch (err) {
      console.error('Errore accettazione richiesta:', err);
      setError(
        `Impossibile accettare richiesta: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
    }
  };
  const handleRejectRequest = async (requestId) => {
    setError('');
    setMessage('');
    try {
      await axiosWithAuth.put(`/friends/reject/${requestId}`);
      setMessage('Richiesta di amicizia rifiutata.');
      fetchFriendshipData(currentUserId);
    } catch (err) {
      console.error('Errore rifiuto richiesta:', err);
      setError(
        `Impossibile rifiutare richiesta: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
    }
  };

  const handleRemoveFriend = async (friendId, friendUsername) => {
    setFriendToRemove({ id: friendId, username: friendUsername });
    setShowConfirmModal(true);
  };
  const confirmRemoveFriend = async () => {
    setError('');
    setMessage('');
    setShowConfirmModal(false);
    if (!friendToRemove) return;

    try {
      await axiosWithAuth.delete(`/friends/remove/${friendToRemove.id}`);
      setMessage(`${friendToRemove.username} rimosso dagli amici.`);
      fetchFriendshipData(currentUserId);
    } catch (err) {
      console.error('Errore rimozione amico:', err);
      setError(
        `Impossibile rimuovere amico: ${
          err.response?.data?.message || 'Errore sconosciuto.'
        }`
      );
    } finally {
      setFriendToRemove(null);
    }
  };

  const cancelRemoveFriend = () => {
    setShowConfirmModal(false);
    setFriendToRemove(null);
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
            Caricamento Amicizie...
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
          <div className="max-w-7xl mx-auto space-y-8">
            <h1 className="text-4xl font-extrabold text-white text-center drop-shadow-lg font-oxanium mb-8">
              LA TUA RETE DI PIXELPALS
            </h1>
            {message && <Alert type="success" message={message} />}
            {error && <Alert type="error" message={error} />}
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-blue-700">
              <h2 className="text-2xl font-bold text-cyan-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faSearch} className="mr-3" /> CERCA NUOVI
                PALS
              </h2>
              <form
                onSubmit={handleSearch}
                className="flex flex-col sm:flex-row gap-4"
              >
                <input
                  type="text"
                  placeholder="Cerca per username..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="flex-grow px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
                />
                <button
                  type="submit"
                  className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-bold py-3 px-6 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center justify-center text-base"
                >
                  <FontAwesomeIcon icon={faSearch} className="mr-2" /> Cerca
                </button>
              </form>
              {searchQuery.trim() && searchResults.length === 0 && message && (
                <p className="text-gray-400 mt-4 text-center">{message}</p>
              )}

              {searchResults.length > 0 && (
                <div className="mt-6">
                  <h3 className="text-xl font-bold text-gray-300 font-oxanium mb-3">
                    Risultati Ricerca:
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {searchResults.map((user) => {
                      const status = getFriendshipStatus(user);
                      return (
                        <div
                          key={user.id}
                          className="bg-gray-700 p-4 rounded-lg flex items-center justify-between border border-gray-600 shadow-md"
                        >
                          <div className="flex items-center space-x-3">
                            <img
                              src={
                                user.avatarUrl ||
                                `https://placehold.co/50x50/3B82F6/FFFFFF?text=${user.username
                                  .charAt(0)
                                  .toUpperCase()}`
                              }
                              alt="Avatar"
                              className="w-12 h-12 rounded-full object-cover border-2 border-blue-500"
                            />
                            <span className="text-gray-100 font-semibold">
                              {user.username}
                            </span>
                          </div>
                          {status === 'Amico' ? (
                            <span className="text-green-400 font-bold">
                              Amico
                            </span>
                          ) : status === 'Richiesta Ricevuta' ? (
                            <span className="text-yellow-400 font-bold">
                              Richiesta Ricevuta
                            </span>
                          ) : status === 'Richiesta Inviata' ? (
                            <span className="text-purple-400 font-bold">
                              Richiesta Inviata
                            </span>
                          ) : (
                            <button
                              onClick={() => handleSendRequest(user.username)}
                              className="bg-gradient-to-r from-green-600 to-lime-600 hover:from-green-700 hover:to-lime-700 text-white text-sm py-2 px-4 rounded-md transition duration-200 flex items-center"
                            >
                              <FontAwesomeIcon
                                icon={faUserPlus}
                                className="mr-2"
                              />{' '}
                              Aggiungi
                            </button>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </section>
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-yellow-700">
              <h2 className="text-2xl font-bold text-yellow-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faUserFriends} className="mr-3" />{' '}
                RICHIESTE RICEVUTE ({pendingRequests.length})
              </h2>
              {pendingRequests.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {pendingRequests.map((request) => (
                    <div
                      key={request.id}
                      className="bg-gray-700 p-4 rounded-lg flex items-center justify-between border border-gray-600 shadow-md"
                    >
                      <div className="flex items-center space-x-3">
                        <img
                          src={`https://placehold.co/50x50/3B82F6/FFFFFF?text=${request.senderUsername
                            .charAt(0)
                            .toUpperCase()}`}
                          alt="Avatar"
                          className="w-12 h-12 rounded-full object-cover border-2 border-yellow-500"
                        />
                        <span className="text-gray-100 font-semibold">
                          {request.senderUsername}
                        </span>
                      </div>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleAcceptRequest(request.id)}
                          className="bg-green-600 hover:bg-green-700 text-white text-sm py-2 px-3 rounded-md transition duration-200 flex items-center"
                        >
                          <FontAwesomeIcon icon={faCheck} />
                        </button>
                        <button
                          onClick={() => handleRejectRequest(request.id)}
                          className="bg-red-600 hover:bg-red-700 text-white text-sm py-2 px-3 rounded-md transition duration-200 flex items-center"
                        >
                          <FontAwesomeIcon icon={faTimes} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400">
                  Nessuna richiesta di amicizia in sospeso.
                </p>
              )}
            </section>
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-purple-700">
              <h2 className="text-2xl font-bold text-purple-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faPaperPlane} className="mr-3" />{' '}
                RICHIESTE INVIATE ({sentRequests.length})
              </h2>
              {sentRequests.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {sentRequests.map((request) => (
                    <div
                      key={request.id}
                      className="bg-gray-700 p-4 rounded-lg flex items-center justify-between border border-gray-600 shadow-md"
                    >
                      <div className="flex items-center space-x-3">
                        <img
                          src={`https://placehold.co/50x50/3B82F6/FFFFFF?text=${request.receiverUsername
                            .charAt(0)
                            .toUpperCase()}`}
                          alt="Avatar"
                          className="w-12 h-12 rounded-full object-cover border-2 border-purple-500"
                        />
                        <span className="text-gray-100 font-semibold">
                          {request.receiverUsername}
                        </span>
                      </div>
                      <span className="text-gray-400 text-sm">In sospeso</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400">
                  Nessuna richiesta di amicizia inviata.
                </p>
              )}
            </section>
            <section className="bg-gray-800 bg-opacity-90 p-6 rounded-xl shadow-2xl border border-green-700">
              <h2 className="text-2xl font-bold text-lime-400 font-oxanium mb-4 flex items-center">
                <FontAwesomeIcon icon={faUserFriends} className="mr-3" /> I MIEI
                AMICI ({friends.length})
              </h2>
              {friends.length > 0 ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {friends.map((friend) => (
                    <div
                      key={friend.id}
                      className="bg-gray-700 p-4 rounded-lg flex items-center justify-between border border-gray-600 shadow-md"
                    >
                      <div className="flex items-center space-x-3">
                        <img
                          src={
                            friend.avatarUrl ||
                            `https://placehold.co/50x50/3B82F6/FFFFFF?text=${friend.username
                              .charAt(0)
                              .toUpperCase()}`
                          }
                          alt="Avatar"
                          className="w-12 h-12 rounded-full object-cover border-2 border-green-500"
                        />
                        <span className="text-gray-100 font-semibold">
                          {friend.username}
                        </span>
                      </div>
                      <button
                        onClick={() =>
                          handleRemoveFriend(friend.id, friend.username)
                        }
                        className="bg-red-600 hover:bg-red-700 text-white text-sm py-2 px-3 rounded-md transition duration-200 flex items-center"
                      >
                        <FontAwesomeIcon icon={faTrashAlt} /> Rimuovi
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400">
                  Non hai ancora amici. Inizia a cercare nuovi PixelPals!
                </p>
              )}
            </section>
          </div>
        </main>
      </div>
      <Footer />
      {showConfirmModal && (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
          <div className="bg-gray-800 p-6 rounded-lg shadow-xl border border-red-700 text-white max-w-sm w-full">
            <h3 className="text-xl font-bold mb-4 text-red-400">
              Conferma Rimozione Amico
            </h3>
            <p className="mb-6">
              Sei sicuro di voler rimuovere{' '}
              <span className="font-semibold text-red-300">
                {friendToRemove?.username}
              </span>{' '}
              dai tuoi amici? Questa azione è irreversibile.
            </p>
            <div className="flex justify-end space-x-4">
              <button
                onClick={cancelRemoveFriend}
                className="px-4 py-2 bg-gray-600 hover:bg-gray-700 rounded-md transition duration-200"
              >
                Annulla
              </button>
              <button
                onClick={confirmRemoveFriend}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-md transition duration-200"
              >
                Rimuovi
              </button>
            </div>
          </div>
        </div>
      )}
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

export default FriendsPage;
