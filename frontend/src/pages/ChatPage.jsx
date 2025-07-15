// frontend/src/pages/ChatPage.jsx
import React, { useState, useEffect, useContext } from 'react'; // Aggiunto useContext
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import ChatComponent from '../components/ChatComponent';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments,
  faUsers,
  faUserCircle,
} from '@fortawesome/free-solid-svg-icons';
import { UnreadMessagesContext } from '../components/UnreadMessagesContext';

const ChatPage = () => {
  const navigate = useNavigate();
  const [currentUserProfile, setCurrentUserProfile] = useState(null);
  const [friends, setFriends] = useState([]);
  const [selectedFriend, setSelectedFriend] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const currentUsername = localStorage.getItem('username'); // Recupera l'username dell'utente corrente

  const { unreadCountsPerChat, markChatAsRead } = useContext(
    UnreadMessagesContext
  );

  // Function to fetch all chat-related data
  const fetchChatData = async () => {
    setLoading(true); // Set loading to true at the start of fetch
    setError(''); // Clear previous errors
    try {
      const userProfileRes = await axiosWithAuth.get('/auth/me');
      setCurrentUserProfile(userProfileRes.data);

      const friendsResponse = await axiosWithAuth.get('/friends');
      // Filtra la lista degli amici per assicurarti che l'utente corrente non sia incluso
      const filteredFriends = friendsResponse.data.filter(
        (friend) => friend.username !== currentUsername
      );
      // Utilizza un Set per rimuovere eventuali duplicati basati sull'ID dell'amico
      // Questo è un fallback nel caso il backend restituisca duplicati.
      const uniqueFriends = Array.from(
        new Set(filteredFriends.map((f) => f.id))
      ).map((id) => filteredFriends.find((f) => f.id === id));

      setFriends(uniqueFriends);
    } catch (err) {
      console.error('Errore nel recupero dati chat:', err);
      setError(
        'Errore durante il caricamento della lista amici o del profilo utente.'
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoading(false); // Set loading to false after fetch completes
    }
  };

  // MODIFIED useEffect for initial fetch and re-fetching on focus/visibility
  useEffect(() => {
    // Function to handle re-fetching on visibility change
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        console.log('Pagina visibile, re-fetching dati chat...');
        fetchChatData();
      }
    };

    // Initial call when the component mounts
    fetchChatData();

    // Add event listeners for window focus and document visibility change
    window.addEventListener('focus', fetchChatData);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    // Cleanup function: Remove listeners when the component unmounts
    return () => {
      window.removeEventListener('focus', fetchChatData);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [navigate, currentUsername]); // Dependencies for the effect

  const handleFriendSelect = (friend) => {
    setSelectedFriend(friend);
    if (currentUserProfile && friend) {
      const chatRoomId =
        currentUserProfile.id.localeCompare(friend.id) < 0
          ? `${currentUserProfile.id}_${friend.id}`
          : `${friend.id}_${currentUserProfile.id}`;
      markChatAsRead(chatRoomId);
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
            Caricamento chat...
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
          <div className="max-w-6xl mx-auto bg-gray-800 bg-opacity-90 p-8 rounded-xl shadow-2xl border border-blue-700 flex flex-col md:flex-row h-[70vh]">
            {/* Sidebar Amici */}
            <div className="w-full md:w-1/4 bg-gray-700 rounded-lg p-4 mr-4 flex flex-col overflow-y-auto border border-gray-600 shadow-md mb-4 md:mb-0">
              <h2 className="text-xl font-bold text-purple-400 mb-4 flex items-center">
                <FontAwesomeIcon icon={faUsers} className="mr-2" /> I MIEI AMICI
              </h2>
              {error && <Alert type="error" message={error} />}
              {friends.length === 0 ? (
                <p className="text-gray-400">
                  Nessun amico accettato. Vai alla pagina Amici per aggiungerne!
                </p>
              ) : (
                <ul className="space-y-2">
                  {friends.map((friend) => {
                    const friendChatRoomId =
                      currentUserProfile && friend
                        ? currentUserProfile.id.localeCompare(friend.id) < 0
                          ? `${currentUserProfile.id}_${friend.id}`
                          : `${friend.id}_${currentUserProfile.id}`
                        : null;
                    const unreadCount =
                      unreadCountsPerChat[friendChatRoomId] || 0;

                    return (
                      <li
                        key={friend.id} // Assicurati che l'ID dell'amico sia univoco e valido
                        className={`relative flex items-center p-2 rounded-md cursor-pointer transition duration-200
                          ${
                            selectedFriend && selectedFriend.id === friend.id
                              ? 'bg-blue-600 text-white shadow-lg'
                              : 'hover:bg-gray-600 text-gray-200'
                          }`}
                        onClick={() => handleFriendSelect(friend)}
                      >
                        <img
                          src={
                            friend.avatarUrl ||
                            `https://placehold.co/40x40/3B82F6/FFFFFF?text=${friend.username
                              .charAt(0)
                              .toUpperCase()}`
                          }
                          alt="Avatar"
                          className="w-10 h-10 rounded-full mr-3 object-cover border-2 border-blue-400"
                        />
                        <span className="font-semibold">{friend.username}</span>
                        {unreadCount > 0 && (
                          <span className="absolute top-1 right-1 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2 border-2 border-gray-700">
                            {unreadCount}
                          </span>
                        )}
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>

            {/* Area Chat */}
            <div className="flex-1 bg-gray-700 rounded-lg flex flex-col border border-gray-600 shadow-md">
              {selectedFriend ? (
                <ChatComponent
                  currentUserId={currentUserProfile?.id}
                  currentUserUsername={currentUsername}
                  otherUser={selectedFriend}
                />
              ) : (
                <div className="flex-1 flex flex-col items-center justify-center text-gray-400 text-lg">
                  <FontAwesomeIcon
                    icon={faComments}
                    className="text-5xl mb-4 text-blue-400"
                  />
                  <p>Seleziona un amico dalla lista per iniziare a chattare.</p>
                </div>
              )}
            </div>
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

export default ChatPage;
