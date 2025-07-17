import React, { createContext, useState, useEffect, useContext } from 'react';
import axiosWithAuth from '../services/axiosWithAuth';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import { useNavigate } from 'react-router-dom';
import Swal from 'sweetalert2'; // Importa SweetAlert2

// Crea il Context
export const UnreadMessagesContext = createContext();

// URL del WebSocket (assicurati che sia lo stesso del ChatComponent)
const WS_URL = 'http://localhost:8080/ws';

// Custom hook per semplificare l'accesso al contesto
export const useUnreadMessages = () => useContext(UnreadMessagesContext);

export const UnreadMessagesProvider = ({ children }) => {
  // Stato per il conteggio totale dei messaggi non letti della chat
  const [totalUnreadCount, setTotalUnreadCount] = useState(0);
  // Stato per i conteggi non letti per ogni chatroom/amico (mappa: chatRoomId -> count)
  const [unreadCountsPerChat, setUnreadCountsPerChat] = useState({});
  // NUOVO: Stato per le notifiche di partita (es. richieste, chiusure, rifiuti)
  const [matchNotifications, setMatchNotifications] = useState([]);
  // Stato per il client STOMP specifico per le notifiche
  const [notificationStompClient, setNotificationStompClient] = useState(null);

  const currentUserUsername = localStorage.getItem('username');
  const navigate = useNavigate();

  // Funzione per recuperare i conteggi non letti iniziali della chat
  const fetchInitialUnreadCounts = async () => {
    try {
      const totalRes = await axiosWithAuth.get('/messages/unread/total');
      setTotalUnreadCount(totalRes.data);

      const perChatRes = await axiosWithAuth.get('/messages/unread/per-chat');
      setUnreadCountsPerChat(perChatRes.data);
    } catch (err) {
      console.error(
        'UnreadMessagesContext: Errore nel recupero dei conteggi non letti:',
        err
      );
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    }
  };

  // Funzione per recuperare le richieste di partita pendenti (per la navbar)
  const fetchPendingMatchRequests = async () => {
    try {
      const response = await axiosWithAuth.get('/match/pending-game-match');
      // Qui potremmo filtrare solo le richieste in entrata se necessario
      // Per ora, consideriamo tutte le pending come "notifiche" per la navbar
      const newPendingNotifications = response.data.map((match) => ({
        type: 'MATCH_REQUEST',
        matchId: match.id,
        senderUsername: match.userAUsername,
        gameName: match.gameName,
        message: `Nuova richiesta di partita da ${match.userAUsername} per ${match.gameName}!`,
        timestamp: new Date(match.matchedAt).toISOString(),
      }));
      setMatchNotifications(newPendingNotifications);
    } catch (err) {
      console.error(
        'UnreadMessagesContext: Errore nel recupero delle richieste di partita pendenti:',
        err
      );
      setMatchNotifications([]);
    }
  };

  useEffect(() => {
    if (!currentUserUsername) {
      console.log(
        'UnreadMessagesContext: Nessun utente corrente, salto la connessione WebSocket.'
      );
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
      setMatchNotifications([]); // Resetta anche le notifiche di partita
      if (notificationStompClient && notificationStompClient.connected) {
        notificationStompClient.disconnect(() => {
          console.log(
            'UnreadMessagesContext: Disconnesso dal WebSocket per notifiche (utente non loggato).'
          );
        });
      }
      return;
    }

    console.log(
      'UnreadMessagesContext: Apertura connessione WebSocket per utente:',
      currentUserUsername
    );
    const socket = new SockJS(WS_URL);
    const client = Stomp.over(socket);

    const token = localStorage.getItem('accessToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    client.connect(
      headers,
      (frame) => {
        console.log(
          'UnreadMessagesContext: Connesso al WebSocket per notifiche:',
          frame
        );
        setNotificationStompClient(client);

        client.subscribe(
          `/user/${currentUserUsername}/queue/unread-updates`,
          (notificationMessage) => {
            const update = JSON.parse(notificationMessage.body);
            console.log(
              'UnreadMessagesContext: Aggiornamento notifica ricevuto:',
              update
            );

            // Gestione dei conteggi non letti della chat
            if (update.totalUnreadCount !== undefined) {
              setTotalUnreadCount(update.totalUnreadCount);
            }
            if (update.unreadCountsPerChat !== undefined) {
              setUnreadCountsPerChat(update.unreadCountsPerChat);
            } else if (update.chatRoomId && update.unreadCount !== undefined) {
              setUnreadCountsPerChat((prevCounts) => ({
                ...prevCounts,
                [update.chatRoomId]: update.unreadCount,
              }));
            }

            // Gestione della notifica di accettazione partita
            if (update.type === 'MATCH_ACCEPTED' && update.matchId) {
              console.log(
                `UnreadMessagesContext: Match ${update.matchId} accettato. Reindirizzamento in corso...`
              );
              localStorage.setItem('activeMatchId', update.matchId); // Assicurati che sia salvato
              Swal.fire({
                title: 'Partita Accettata!',
                text: `${update.opponentUsername} ha accettato la tua richiesta per ${update.gameName}. Verrai reindirizzato alla stanza della partita.`,
                icon: 'success',
                timer: 3000,
                timerProgressBar: true,
                showConfirmButton: false,
                customClass: {
                  popup:
                    'bg-gray-800 text-white rounded-lg shadow-xl border border-blue-700',
                  title: 'text-green-400',
                },
              }).then(() => {
                navigate(`/match-room/${update.matchId}`);
              });
            }

            // Gestione della notifica di chiusura partita
            if (update.type === 'MATCH_CLOSED' && update.matchId) {
              console.log(
                `UnreadMessagesContext: Match ${update.matchId} chiuso da ${update.closerUsername}. Reindirizzamento alla pagina di rating...`
              );
              localStorage.removeItem('activeMatchId'); // Rimuovi l'ID della partita attiva
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

            // Gestione della notifica di richiesta partita (per la navbar)
            if (update.type === 'MATCH_REQUEST' && update.matchId) {
              console.log(
                `UnreadMessagesContext: Nuova richiesta di partita da ${update.senderUsername} per ${update.gameName}.`
              );
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

            // NUOVO: Gestione della notifica di rifiuto partita
            if (update.type === 'MATCH_DECLINED' && update.matchId) {
              console.log(
                `UnreadMessagesContext: Richiesta partita ${update.matchId} rifiutata da ${update.declinerUsername}.`
              );
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

        // Dopo la connessione WebSocket, recupera i conteggi iniziali
        fetchInitialUnreadCounts();
        fetchPendingMatchRequests(); // Recupera anche le richieste di partita pendenti
      },
      (err) => {
        console.error(
          'UnreadMessagesContext: Errore di connessione WebSocket per notifiche:',
          err
        );
        setNotificationStompClient(null);
        if (
          err.headers &&
          err.headers['message'] &&
          err.headers['message'].includes('Unauthorized')
        ) {
          localStorage.clear();
          navigate('/');
        }
      }
    );

    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log(
            'UnreadMessagesContext: Disconnesso dal WebSocket per notifiche.'
          );
        });
      }
    };
  }, [currentUserUsername, navigate]);

  // Funzione per marcare una chat come letta
  const markChatAsRead = async (chatRoomId) => {
    if (!currentUserUsername) return;

    try {
      await axiosWithAuth.post(`/messages/mark-read/${chatRoomId}`);
      console.log(
        `UnreadMessagesContext: Chat room ${chatRoomId} marcata come letta.`
      );

      setUnreadCountsPerChat((prevCounts) => {
        const newCounts = { ...prevCounts };
        delete newCounts[chatRoomId];
        return newCounts;
      });
      setTotalUnreadCount((prevTotal) => {
        const currentChatCount = unreadCountsPerChat[chatRoomId] || 0;
        return Math.max(0, prevTotal - currentChatCount);
      });
    } catch (err) {
      console.error(
        'UnreadMessagesContext: Errore nel marcare la chat come letta:',
        err
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    }
  };

  // Funzione per marcare una notifica di partita come letta (o rimuoverla)
  const clearMatchNotification = (matchId, type) => {
    setMatchNotifications((prev) =>
      prev.filter(
        (notif) => !(notif.matchId === matchId && notif.type === type)
      )
    );
    // Potresti voler inviare una richiesta al backend per marcare la notifica come letta persistente
  };

  return (
    <UnreadMessagesContext.Provider
      value={{
        totalUnreadCount,
        unreadCountsPerChat,
        matchNotifications, // Esponi le notifiche di partita
        markChatAsRead,
        refreshUnreadCounts: () => {
          // Ricarica sia chat che match notifications
          fetchInitialUnreadCounts();
          fetchPendingMatchRequests();
        },
        clearMatchNotification, // Esponi la funzione per pulire le notifiche di partita
      }}
    >
      {children}
    </UnreadMessagesContext.Provider>
  );
};
