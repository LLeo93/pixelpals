// frontend/src/context/UnreadMessagesContext.jsx
import React, { createContext, useState, useEffect, useContext } from 'react';
import axiosWithAuth from '../services/axiosWithAuth';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

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

  // Effetto per la connessione WebSocket e il recupero iniziale dei conteggi
  useEffect(() => {
    // Non procedere se l'utente non è loggato
    if (!currentUserUsername) {
      setTotalUnreadCount(0);
      setUnreadCountsPerChat({});
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
        console.error('Errore nel recupero dei conteggi non letti:', err);
        // Gestisci l'errore, es. resetta i conteggi
        setTotalUnreadCount(0);
        setUnreadCountsPerChat({});
      }
    };

    // Connessione WebSocket per aggiornamenti in tempo reale
    const socket = new SockJS(WS_URL);
    const client = Stomp.over(socket);

    const token = localStorage.getItem('accessToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    client.connect(
      headers,
      (frame) => {
        console.log('Connesso al WebSocket per notifiche:', frame);
        setNotificationStompClient(client);

        // Sottoscrivi alla coda privata per gli aggiornamenti dei conteggi non letti
        client.subscribe(
          `/user/${currentUserUsername}/queue/unread-updates`,
          (notificationMessage) => {
            const update = JSON.parse(notificationMessage.body);
            console.log('Aggiornamento notifica ricevuto:', update);

            // L'update dovrebbe contenere il nuovo conteggio totale e/o per chat
            if (update.totalUnreadCount !== undefined) {
              setTotalUnreadCount(update.totalUnreadCount);
            }
            if (update.chatRoomId && update.unreadCount !== undefined) {
              setUnreadCountsPerChat((prevCounts) => ({
                ...prevCounts,
                [update.chatRoomId]: update.unreadCount,
              }));
            }
          }
        );

        // Dopo la connessione WebSocket, recupera i conteggi iniziali
        fetchInitialUnreadCounts();
      },
      (err) => {
        console.error('Errore di connessione WebSocket per notifiche:', err);
        setNotificationStompClient(null);
      }
    );

    // Funzione di pulizia: disconnetti il client STOMP
    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log('Disconnesso dal WebSocket per notifiche.');
        });
      }
    };
  }, [currentUserUsername]); // Dipende dall'username corrente

  // Funzione per marcare una chat come letta (chiamata dal ChatComponent)
  const markChatAsRead = async (chatRoomId) => {
    if (!currentUserUsername) return; // Non fare nulla se non loggato

    try {
      // Chiama l'endpoint backend per marcare i messaggi come letti
      await axiosWithAuth.post(`/messages/mark-read/${chatRoomId}`);
      console.log(`Chat room ${chatRoomId} marcata come letta.`);

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
      console.error('Errore nel marcare la chat come letta:', err);
    }
  };

  return (
    <UnreadMessagesContext.Provider
      value={{ totalUnreadCount, unreadCountsPerChat, markChatAsRead }}
    >
      {children}
    </UnreadMessagesContext.Provider>
  );
};
