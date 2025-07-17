import React, { createContext, useState, useEffect, useContext } from 'react';
import axiosWithAuth from '../services/axiosWithAuth';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import { useNavigate } from 'react-router-dom'; // Importa useNavigate

// Crea il Context
export const UnreadMessagesContext = createContext();

// URL del WebSocket (assicurati che sia lo stesso del ChatComponent)
const WS_URL = 'http://localhost:8080/ws';

export const UnreadMessagesProvider = ({ children }) => {
  // Stato per il conteggio totale dei messaggi non letti
  const [totalUnreadCount, setTotalUnreadCount] = useState(0);
  // Stato per i conteggi non letti per ogni chatroom/amico (mappa: chatRoomId -> count)
  const [unreadCountsPerChat, setUnreadCountsPerChat] = useState({});
  // Stato per il client STOMP specifico per le notifiche
  const [notificationStompClient, setNotificationStompClient] = useState(null);

  // Recupera l'username dell'utente corrente (necessario per le sottoscrizioni private)
  const currentUserUsername = localStorage.getItem('username');
  const navigate = useNavigate(); // Ottieni la funzione navigate

  // Effetto per la connessione WebSocket e il recupero iniziale dei conteggi
  useEffect(() => {
    // Non procedere se l'utente non è loggato
    if (!currentUserUsername) {
      console.log(
        'UnreadMessagesContext: Nessun utente corrente, salto la connessione WebSocket.'
      );
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
      // Disconnetti se c'era una connessione attiva e l'utente non è loggato
      if (notificationStompClient && notificationStompClient.connected) {
        notificationStompClient.disconnect(() => {
          console.log(
            'UnreadMessagesContext: Disconnesso dal WebSocket per notifiche (utente non loggato).'
          );
        });
      }
      return;
    }

    // Funzione per recuperare i conteggi non letti iniziali
    const fetchInitialUnreadCounts = async () => {
      try {
        const totalRes = await axiosWithAuth.get('/messages/unread/total');
        setTotalUnreadCount(totalRes.data); // Assumi che il backend restituisca un numero direttamente

        const perChatRes = await axiosWithAuth.get('/messages/unread/per-chat');
        setUnreadCountsPerChat(perChatRes.data); // Assumi che il backend restituisca una mappa
      } catch (err) {
        console.error(
          'UnreadMessagesContext: Errore nel recupero dei conteggi non letti:',
          err
        );
        // Gestisci l'errore, es. resetta i conteggi
        setTotalUnreadCount(0);
        setUnreadCountsPerChat({});
        // Se l'errore è 401/403, reindirizza al login
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      }
    };

    console.log(
      'UnreadMessagesContext: Apertura connessione WebSocket per utente:',
      currentUserUsername
    );
    // Connessione WebSocket per aggiornamenti in tempo reale
    const socket = new SockJS(WS_URL);
    const client = Stomp.over(socket);

    // Disabilita i log di debug di StompJS per evitare spam eccessivo nella console
    // client.debug = null;

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

        // Sottoscrivi alla coda privata per gli aggiornamenti dei conteggi non letti e notifiche match
        client.subscribe(
          `/user/${currentUserUsername}/queue/unread-updates`,
          (notificationMessage) => {
            const update = JSON.parse(notificationMessage.body);
            console.log(
              'UnreadMessagesContext: Aggiornamento notifica ricevuto:',
              update
            );

            // Gestione dei conteggi non letti
            if (update.totalUnreadCount !== undefined) {
              setTotalUnreadCount(update.totalUnreadCount);
            }
            if (update.unreadCountsPerChat !== undefined) {
              // Modificato per corrispondere al payload del backend
              setUnreadCountsPerChat(update.unreadCountsPerChat);
            } else if (update.chatRoomId && update.unreadCount !== undefined) {
              // Vecchia logica, mantenuta per compatibilità
              setUnreadCountsPerChat((prevCounts) => ({
                ...prevCounts,
                [update.chatRoomId]: update.unreadCount,
              }));
            }

            // NUOVO: Gestione della notifica di accettazione partita
            console.log(
              `UnreadMessagesContext: Controllo notifica match: type=${update.type}, matchId=${update.matchId}`
            );
            if (update.type === 'MATCH_ACCEPTED' && update.matchId) {
              console.log(
                `UnreadMessagesContext: Match ${update.matchId} accettato. Reindirizzamento in corso...`
              );
              navigate(`/match-room/${update.matchId}`);
            }
          }
        );

        // Dopo la connessione WebSocket, recupera i conteggi iniziali
        fetchInitialUnreadCounts();
      },
      (err) => {
        console.error(
          'UnreadMessagesContext: Errore di connessione WebSocket per notifiche:',
          err
        );
        setNotificationStompClient(null);
        // Se l'errore è dovuto a token scaduto/non valido, reindirizza al login
        // Questa parte potrebbe essere gestita meglio a livello di axiosWithAuth o un intercettore
        // ma per ora, un reindirizzamento generico può aiutare
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

    // Funzione di pulizia: disconnetti il client STOMP
    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log(
            'UnreadMessagesContext: Disconnesso dal WebSocket per notifiche.'
          );
        });
      }
    };
  }, [currentUserUsername, navigate]); // Aggiungi navigate alle dipendenze

  // Funzione per aggiornare manualmente i conteggi (es. dopo aver letto un messaggio)
  const refreshUnreadCounts = () => {
    // Chiama la funzione interna per recuperare i conteggi più recenti
    const fetchInitialUnreadCounts = async () => {
      try {
        const totalRes = await axiosWithAuth.get('/messages/unread/total');
        setTotalUnreadCount(totalRes.data);

        const perChatRes = await axiosWithAuth.get('/messages/unread/per-chat');
        setUnreadCountsPerChat(perChatRes.data);
      } catch (err) {
        console.error(
          'UnreadMessagesContext: Errore nel recupero dei conteggi non letti durante il refresh:',
          err
        );
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
          navigate('/');
        }
      }
    };
    fetchInitialUnreadCounts();
  };

  // Funzione per marcare una chat come letta (chiamata dal ChatComponent)
  const markChatAsRead = async (chatRoomId) => {
    if (!currentUserUsername) return; // Non fare nulla se non loggato

    try {
      // Chiama l'endpoint backend per marcare i messaggi come letti
      await axiosWithAuth.post(`/messages/mark-read/${chatRoomId}`);
      console.log(
        `UnreadMessagesContext: Chat room ${chatRoomId} marcata come letta.`
      );

      // Aggiorna localmente i conteggi dopo aver marcato come letto
      setUnreadCountsPerChat((prevCounts) => {
        const newCounts = { ...prevCounts };
        delete newCounts[chatRoomId]; // Rimuovi il conteggio per questa chat
        return newCounts;
      });
      // Ricalcola il totale o recuperalo dal backend se preferisci
      setTotalUnreadCount((prevTotal) => {
        const currentChatCount = unreadCountsPerChat[chatRoomId] || 0;
        return Math.max(0, prevTotal - currentChatCount);
      });

      // Potresti anche voler inviare un messaggio WebSocket al backend
      // per notificare che la chat è stata letta, in modo che altri client
      // (es. se l'utente ha la chat aperta su più dispositivi) si aggiornino.
      // Questo dipenderebbe dall'implementazione backend.
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

  return (
    <UnreadMessagesContext.Provider
      value={{
        totalUnreadCount,
        unreadCountsPerChat,
        markChatAsRead,
        refreshUnreadCounts,
      }}
    >
      {children}
    </UnreadMessagesContext.Provider>
  );
};
