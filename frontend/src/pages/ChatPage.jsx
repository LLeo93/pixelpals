import React, { useState, useEffect, useContext, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import ChatComponent from '../components/ChatComponent';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faComments,
  faUsers,
  faSpinner,
} from '@fortawesome/free-solid-svg-icons';
import { UnreadMessagesContext } from '../components/UnreadMessagesContext';
import { useSocket } from '../hooks/UseSocket';

const ChatPage = () => {
  const navigate = useNavigate();
  const [currentUserProfile, setCurrentUserProfile] = useState(null);
  const [friends, setFriends] = useState([]);
  const [selectedFriend, setSelectedFriend] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingFriends, setLoadingFriends] = useState(true);
  const [profileError, setProfileError] = useState('');
  const [friendsError, setFriendsError] = useState('');
  const currentUsername = localStorage.getItem('username');
  const token = localStorage.getItem('accessToken');

  const { unreadCountsPerChat, markChatAsRead } = useContext(
    UnreadMessagesContext
  );

  const stompClient = useSocket(token);
  const isStompConnected = stompClient?.connected;

  const fetchUserProfile = useCallback(async () => {
    setLoadingProfile(true);
    setProfileError('');
    try {
      const userRes = await axiosWithAuth.get('/auth/me');
      setCurrentUserProfile(userRes.data);
    } catch (err) {
      setProfileError('Errore durante il caricamento del profilo utente.');
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoadingProfile(false);
    }
  }, [navigate]);

  const fetchFriendsStatus = useCallback(async () => {
    setLoadingFriends(true);
    setFriendsError('');
    try {
      const friendsResponse = await axiosWithAuth.get('/auth/friends/status');
      const filteredFriends = friendsResponse.data.filter(
        (friend) => friend.username !== currentUsername
      );
      const friendsWithInitialStatus = filteredFriends.map((friend) => ({
        ...friend,
        Online: friend.online,
      }));
      const uniqueFriends = Array.from(
        new Set(friendsWithInitialStatus.map((f) => f.id))
      ).map((id) => friendsWithInitialStatus.find((f) => f.id === id));
      setFriends(uniqueFriends);
    } catch (err) {
      setFriendsError('Errore durante il caricamento della lista amici.');
    } finally {
      setLoadingFriends(false);
    }
  }, [currentUsername]);

  useEffect(() => {
    if (!currentUsername) {
      setLoadingProfile(false);
      setProfileError('Utente non autenticato. Effettua il login.');
      return;
    }
    fetchUserProfile();
  }, [currentUsername, fetchUserProfile]);

  useEffect(() => {
    if (!currentUsername) return;
    fetchFriendsStatus();
  }, [currentUsername, fetchFriendsStatus]);

  useEffect(() => {
    if (!stompClient || !isStompConnected || !currentUsername) return;
    const statusSub = stompClient.subscribe('/topic/status', (message) => {
      try {
        const { userId, online } = JSON.parse(message.body);
        setFriends((prev) =>
          prev.map((f) => (f.id === userId ? { ...f, Online: online } : f))
        );
      } catch (_) {}
    });
    return () => {
      if (statusSub) statusSub.unsubscribe();
    };
  }, [stompClient, isStompConnected, currentUsername]);

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

  const overallLoading = loadingProfile || loadingFriends;
  const overallError = profileError || friendsError;

  if (overallLoading) {
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
        <style>
          {`
            .custom-scrollbar::-webkit-scrollbar {
              width: 8px;
              height: 8px;
            }
            .custom-scrollbar::-webkit-scrollbar-track {
              background: #2d3748;
              border-radius: 10px;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb {
              background-color: #4a5568;
              border-radius: 10px;
              border: 2px solid #2d3748;
            }
            .custom-scrollbar::-webkit-scrollbar-thumb:hover {
              background-color: #6366f1;
            }
            .custom-scrollbar {
              scrollbar-width: thin;
              scrollbar-color: #4a5568 #2d3748;
            }
          `}
        </style>

        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>

        <main className="flex-grow p-4 pt-20 pb-24 relative z-10">
          <div className="max-w-6xl mx-auto bg-gray-800 bg-opacity-90 p-8 rounded-xl shadow-2xl border border-blue-700 flex flex-col md:flex-row h-[70vh] md:h-[70vh]">
            <div className="w-full md:w-1/3 flex-shrink-0 bg-gray-700 rounded-lg p-4 mr-4 flex flex-col overflow-y-auto border border-gray-600 shadow-md mb-4 md:mb-0 custom-scrollbar">
              <h2 className="text-xl font-bold text-purple-400 mb-4 flex items-center">
                <FontAwesomeIcon icon={faUsers} className="mr-2" /> I MIEI AMICI
              </h2>
              {loadingFriends && (
                <div className="flex justify-center items-center h-full">
                  <FontAwesomeIcon
                    icon={faSpinner}
                    spin
                    className="text-blue-400 text-3xl"
                  />
                </div>
              )}
              {overallError && <Alert type="error" message={overallError} />}
              {friends.length === 0 && !loadingFriends && !overallError ? (
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
                        key={friend.id}
                        className={`relative flex items-center p-2 rounded-md cursor-pointer transition duration-200 ${
                          selectedFriend?.id === friend.id
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
                        <span
                          className={`w-3 h-3 rounded-full ml-2 ${
                            friend.Online ? 'bg-green-500' : 'bg-red-500'
                          }`}
                          title={friend.Online ? 'Online' : 'Offline'}
                        ></span>
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

            <div className="flex-1 bg-gray-700 rounded-lg flex flex-col border border-gray-600 shadow-md h-4/5 mb-4">
              {selectedFriend ? (
                <ChatComponent
                  currentUserId={currentUserProfile?.id}
                  currentUserUsername={currentUsername}
                  otherUser={selectedFriend}
                  stompClient={stompClient}
                  isConnected={isStompConnected}
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
