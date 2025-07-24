import React, {
  createContext,
  useState,
  useEffect,
  useContext,
  useCallback,
} from 'react';
import axiosWithAuth from '../services/axiosWithAuth';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2';

export const UnreadMessagesContext = createContext();
const WS_URL = 'http://localhost:8080/ws';

export const useUnreadMessages = () => useContext(UnreadMessagesContext);

export const UnreadMessagesProvider = ({ children }) => {
  const [totalUnreadCount, setTotalUnreadCount] = useState(0);
  const [unreadCountsPerChat, setUnreadCountsPerChat] = useState({});
  const [matchNotifications, setMatchNotifications] = useState([]);
  const [notificationStompClient, setNotificationStompClient] = useState(null);
  const [friendChatUnreadCount, setFriendChatUnreadCount] = useState(0);
  const [matchChatUnreadCount, setMatchChatUnreadCount] = useState(0);
  const [pendingFriendRequestCount, setPendingFriendRequestCount] = useState(0);

  const incrementFriendChat = () => {
    setFriendChatUnreadCount((prev) => prev + 1);
  };

  const incrementMatchChat = () => setMatchChatUnreadCount((prev) => prev + 1);
  const resetFriendChat = () => setFriendChatUnreadCount(0);
  const resetMatchChat = () => setMatchChatUnreadCount(0);

  const currentUserUsername = localStorage.getItem('username');
  const navigate = useNavigate();

  const fetchInitialUnreadCounts = async () => {
    try {
      const totalRes = await axiosWithAuth.get('/messages/unread/total');
      setTotalUnreadCount(totalRes.data);

      const perChatRes = await axiosWithAuth.get('/messages/unread/per-chat');
      setUnreadCountsPerChat(perChatRes.data);
    } catch (err) {
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    }
  };

  const fetchPendingMatchRequests = async () => {
    try {
      const response = await axiosWithAuth.get('/match/pending-game-match');
      const newPendingNotifications = response.data.map((match) => ({
        type: 'MATCH_REQUEST',
        matchId: match.id,
        senderUsername: match.userAUsername,
        gameName: match.gameName,
        message: `Nuova richiesta di partita da ${match.userAUsername} per ${match.gameName}!`,
        timestamp: new Date(match.matchedAt).toISOString(),
      }));
      setMatchNotifications(newPendingNotifications);
    } catch {
      setMatchNotifications([]);
    }
  };

  const fetchPendingFriendRequests = useCallback(async () => {
    try {
      const response = await axiosWithAuth.get('/friends/pending');
      setPendingFriendRequestCount(response.data.length);
    } catch {
      setPendingFriendRequestCount(0);
    }
  }, []);

  useEffect(() => {
    if (!currentUserUsername) {
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
      setMatchNotifications([]);
      setPendingFriendRequestCount(0);
      if (notificationStompClient?.connected) {
        notificationStompClient.disconnect();
      }
      return;
    }

    const socket = new SockJS(WS_URL);
    const client = Stomp.over(socket);
    const token = localStorage.getItem('accessToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    client.connect(
      headers,
      () => {
        setNotificationStompClient(client);

        client.subscribe(
          `/user/${currentUserUsername}/queue/unread-updates`,
          (msg) => {
            const update = JSON.parse(msg.body);

            if (update.totalUnreadCount !== undefined) {
              setTotalUnreadCount(update.totalUnreadCount);
            }

            if (update.unreadCountsPerChat !== undefined) {
              setUnreadCountsPerChat(update.unreadCountsPerChat);
            } else if (update.chatRoomId && update.unreadCount !== undefined) {
              setUnreadCountsPerChat((prev) => ({
                ...prev,
                [update.chatRoomId]: update.unreadCount,
              }));
            }

            if (update.type === 'CHAT_FRIEND') {
              incrementFriendChat();
            }

            if (update.type === 'CHAT_MATCH') {
              incrementMatchChat();
            }
          }
        );

        client.subscribe(
          `/user/${currentUserUsername}/queue/match-notifications`,
          (msg) => {
            const update = JSON.parse(msg.body);

            if (update.type === 'MATCH_ACCEPTED' && update.matchId) {
              localStorage.setItem('activeMatchId', update.matchId);
              Swal.fire({
                title: 'Partita Accettata!',
                text: `${update.opponentUsername} ha accettato la tua richiesta per ${update.gameName}. Verrai reindirizzato alla stanza della partita.`,
                icon: 'success',
                timer: 3000,
                showConfirmButton: false,
                timerProgressBar: true,
                customClass: {
                  popup:
                    'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
                  title: 'text-green-400',
                  confirmButton:
                    'bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md',
                },
              }).then(() => {
                navigate(`/match-room/${update.matchId}`);
              });
            }

            if (update.type === 'MATCH_CLOSED' && update.matchId) {
              localStorage.removeItem('activeMatchId');
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
                navigate(`/rate-match/${update.matchId}`);
              });
            }

            if (update.type === 'MATCH_REQUEST' && update.matchId) {
              setMatchNotifications((prev) => [
                ...prev,
                {
                  type: 'MATCH_REQUEST',
                  matchId: update.matchId,
                  senderUsername: update.senderUsername,
                  gameName: update.gameName,
                  message: `Nuova richiesta da ${update.senderUsername} per ${update.gameName}`,
                  timestamp: new Date().toISOString(),
                },
              ]);
            }

            if (update.type === 'MATCH_DECLINED' && update.matchId) {
              setMatchNotifications((prev) => [
                ...prev,
                {
                  type: 'MATCH_DECLINED',
                  matchId: update.matchId,
                  declinerUsername: update.declinerUsername,
                  gameName: update.gameName,
                  message: `${update.declinerUsername} ha rifiutato la tua richiesta per ${update.gameName}.`,
                  timestamp: new Date().toISOString(),
                },
              ]);
            }
          }
        );

        client.subscribe(
          `/user/${currentUserUsername}/queue/friend-requests/new`,
          () => {
            setPendingFriendRequestCount((prev) => prev + 1);
          }
        );

        client.subscribe(
          `/user/${currentUserUsername}/queue/friend-requests/update`,
          () => {
            fetchPendingFriendRequests();
          }
        );

        fetchInitialUnreadCounts();
        fetchPendingMatchRequests();
        fetchPendingFriendRequests();
      },
      (err) => {
        setNotificationStompClient(null);
        if (err?.headers?.message?.includes('Unauthorized')) {
          localStorage.clear();
          navigate('/');
        }
      }
    );

    return () => {
      if (client?.connected) {
        client.disconnect();
      }
    };
  }, [currentUserUsername, navigate, fetchPendingFriendRequests]);

  const markChatAsRead = async (chatRoomId) => {
    if (!currentUserUsername) return;

    try {
      await axiosWithAuth.post(`/messages/mark-read/${chatRoomId}`);
      const count = unreadCountsPerChat[chatRoomId] || 0;

      setUnreadCountsPerChat((prev) => {
        const updated = { ...prev };
        delete updated[chatRoomId];
        return updated;
      });

      setTotalUnreadCount((prevTotal) => Math.max(0, prevTotal - count));
      setFriendChatUnreadCount((prev) => Math.max(0, prev - count));
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    }
  };

  const clearMatchNotification = (matchId, type) => {
    setMatchNotifications((prev) =>
      prev.filter((n) => !(n.matchId === matchId && n.type === type))
    );
  };

  return (
    <UnreadMessagesContext.Provider
      value={{
        totalUnreadCount,
        unreadCountsPerChat,
        matchNotifications,
        friendChatUnreadCount,
        matchChatUnreadCount,
        pendingFriendRequestCount,
        incrementFriendChat,
        incrementMatchChat,
        resetFriendChat,
        resetMatchChat,
        markChatAsRead,
        refreshUnreadCounts: () => {
          fetchInitialUnreadCounts();
          fetchPendingMatchRequests();
          fetchPendingFriendRequests();
        },
        clearMatchNotification,
        fetchPendingFriendRequests,
      }}
    >
      {children}
    </UnreadMessagesContext.Provider>
  );
};
