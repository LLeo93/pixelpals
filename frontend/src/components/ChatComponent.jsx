// frontend/src/components/ChatComponent.jsx
import React, { useState, useEffect, useRef } from 'react';
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faPaperPlane,
  faTimesCircle,
  faSpinner,
  faComments, // Assicurati che questo sia importato se usato nel JSX
} from '@fortawesome/free-solid-svg-icons';

const WS_URL = 'http://localhost:8080/ws';

const ChatComponent = ({ currentUserId, currentUserUsername, otherUser }) => {
  const [stompClient, setStompClient] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  // Calcola l'ID della chat room in modo consistente tra due utenti
  const chatRoomId =
    currentUserId && otherUser
      ? currentUserId.localeCompare(otherUser.id) < 0
        ? `${currentUserId}_${otherUser.id}`
        : `${otherUser.id}_${currentUserId}`
      : null;

  // Effetto per la connessione WebSocket e le sottoscrizioni
  useEffect(() => {
    // Non procedere se mancano gli ID utente o l'altro utente
    if (!currentUserId || !otherUser) return;

    setError('');
    setMessages([]); // Pulisci i messaggi quando cambia l'amico selezionato o l'utente corrente

    // Crea una nuova connessione SockJS e un client STOMP
    const socket = new SockJS(WS_URL);
    const client = Stomp.over(socket);

    // Recupera il token JWT dal localStorage per l'autenticazione WebSocket
    const token = localStorage.getItem('accessToken');
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    // Tenta di connettersi al WebSocket
    client.connect(
      headers, // Passa le intestazioni con il token JWT per l'autenticazione
      (frame) => {
        console.log('Connesso al WebSocket:', frame);
        setIsConnected(true);
        setStompClient(client);

        // Sottoscrivi alla coda privata dell'utente corrente per ricevere messaggi diretti
        // (es. notifiche o messaggi specifici per l'utente, non necessariamente di chatroom)
        client.subscribe(
          `/user/${currentUserUsername}/queue/messages`,
          (message) => {
            const receivedMessage = JSON.parse(message.body);
            console.log('Messaggio privato ricevuto (queue):', receivedMessage);
            // Questa coda potrebbe essere usata per messaggi che non sono parte della chatroom principale
            // o per una logica di notifica. Se i messaggi della chatroom arrivano anche qui,
            // la deduplicazione è gestita nella sottoscrizione del topic.
          }
        );

        // Sottoscrivi al topic specifico della chat room per i messaggi broadcast
        // Questa è la sottoscrizione principale per i messaggi di chat 1-a-1
        client.subscribe(`/topic/chatRoom/${chatRoomId}`, (message) => {
          const receivedMessage = JSON.parse(message.body);
          console.log('Messaggio ricevuto (topic chatRoom):', receivedMessage);

          // LOGICA DI DEDUPLICAZIONE:
          // Aggiorna lo stato dei messaggi per evitare duplicati.
          // Cerca un messaggio esistente con lo stesso ID definitivo
          // o un messaggio temporaneo che può essere rimpiazzato dal messaggio ufficiale.
          setMessages((prevMessages) => {
            // Controlla se il messaggio ufficiale (con ID definitivo) è già presente
            const isOfficialDuplicate = prevMessages.some(
              (msg) => msg.id === receivedMessage.id
            );

            if (isOfficialDuplicate) {
              // Se il messaggio ufficiale è già presente, non fare nulla per evitare duplicati
              return prevMessages;
            }

            // Cerca un messaggio temporaneo corrispondente da rimpiazzare
            const updatedMessages = prevMessages.map((msg) => {
              // Se è un messaggio temporaneo e corrisponde al contenuto e al mittente
              // del messaggio ricevuto, rimpiazzalo con la versione ufficiale.
              if (
                msg.isTemporary &&
                msg.content === receivedMessage.content &&
                msg.senderId === receivedMessage.senderId
              ) {
                return { ...receivedMessage, isTemporary: false }; // Rimpiazza con l'ufficiale
              }
              return msg;
            });

            // Se nessun messaggio temporaneo è stato rimpiazzato (o se non c'erano temporanei),
            // aggiungi il messaggio ricevuto come nuovo.
            // Questo gestisce anche il caso in cui il messaggio ricevuto è il primo ad arrivare.
            const wasTemporaryReplaced = updatedMessages.some(
              (msg) => msg.id === receivedMessage.id && !msg.isTemporary
            );

            if (!wasTemporaryReplaced) {
              return [
                ...prevMessages,
                { ...receivedMessage, isTemporary: false },
              ];
            }

            return updatedMessages;
          });
        });

        // Dopo la connessione e le sottoscrizioni, carica la cronologia della chat
        fetchChatHistory(currentUserId, otherUser.id);
      },
      (err) => {
        // Gestione degli errori di connessione WebSocket
        console.error('Errore di connessione WebSocket:', err);
        setError('Impossibile connettersi alla chat. Riprova più tardi.');
        setIsConnected(false);
      }
    );

    // Funzione di pulizia: disconnetti il client STOMP quando il componente viene smontato
    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log('Disconnesso dal WebSocket.');
          setIsConnected(false);
        });
      }
    };
  }, [currentUserId, currentUserUsername, otherUser, chatRoomId]); // Dipendenze per ricollegarsi se l'utente o l'amico cambiano

  // Effetto per lo scroll automatico dei messaggi
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Funzione per recuperare la cronologia della chat via REST
  const fetchChatHistory = async (user1Id, user2Id) => {
    try {
      const response = await axiosWithAuth.get(`/messages/history/${user2Id}`);
      setMessages(response.data);
    } catch (err) {
      console.error('Errore nel recupero cronologia chat:', err);
      setError('Errore durante il caricamento della cronologia chat.');
    }
  };

  // Funzione per inviare un messaggio tramite WebSocket
  const sendMessage = () => {
    if (stompClient && isConnected && messageInput.trim() !== '' && otherUser) {
      const chatMessage = {
        senderId: currentUserId,
        senderUsername: currentUserUsername,
        receiverId: otherUser.id,
        receiverUsername: otherUser.username,
        content: messageInput.trim(),
        chatRoomId: chatRoomId,
        // Il timestamp definitivo e l'ID del messaggio saranno impostati dal backend
      };

      console.log('Invio messaggio:', chatMessage);
      stompClient.send(
        '/app/chat.sendMessage',
        {},
        JSON.stringify(chatMessage)
      );
      setMessageInput(''); // Pulisci l'input dopo l'invio

      // Aggiorna localmente i messaggi per una visualizzazione immediata (ottimistica)
      setMessages((prevMessages) => [
        ...prevMessages,
        {
          ...chatMessage,
          // Genera un ID temporaneo robusto per la key di React e la deduplicazione
          id: `temp_${Date.now()}_${Math.random()
            .toString(36)
            .substring(2, 9)}`,
          timestamp: new Date().toISOString(), // Timestamp provvisorio per la visualizzazione
          isTemporary: true, // Flag per indicare che è un messaggio temporaneo locale
        },
      ]);
    }
  };

  // Renderizza un messaggio di placeholder se non è selezionato nessun amico
  if (!currentUserId || !otherUser) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-gray-400 text-lg">
        <FontAwesomeIcon
          icon={faComments}
          className="text-5xl mb-4 text-blue-400"
        />
        <p>Seleziona un amico per iniziare a chattare.</p>
      </div>
    );
  }

  // Renderizza l'interfaccia della chat
  return (
    <div className="flex-1 flex flex-col h-full">
      {/* Intestazione Chat */}
      <div className="bg-gray-800 p-4 rounded-t-lg flex items-center border-b border-gray-600 shadow-md">
        <img
          src={
            otherUser.avatarUrl ||
            `https://placehold.co/40x40/3B82F6/FFFFFF?text=${otherUser.username
              .charAt(0)
              .toUpperCase()}`
          }
          alt="Avatar Amico"
          className="w-10 h-10 rounded-full mr-3 object-cover border-2 border-blue-400"
        />
        <h3 className="text-xl font-bold text-white">{otherUser.username}</h3>
        {!isConnected && (
          <FontAwesomeIcon
            icon={faSpinner}
            spin
            className="ml-auto text-blue-400 text-lg"
            title="Connessione in corso..."
          />
        )}
      </div>

      {/* Area Messaggi */}
      <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-700">
        {error && <Alert type="error" message={error} />}
        {messages.map((msg, index) => (
          <div
            key={msg.id || index} // Usa msg.id se disponibile, altrimenti index
            className={`flex ${
              msg.senderId === currentUserId ? 'justify-end' : 'justify-start'
            }`}
          >
            <div
              className={`max-w-[70%] p-3 rounded-lg shadow-md ${
                msg.senderId === currentUserId
                  ? 'bg-blue-600 text-white rounded-br-none'
                  : 'bg-gray-600 text-gray-100 rounded-bl-none'
              }`}
            >
              <p className="font-semibold text-sm mb-1">
                {msg.senderId === currentUserId ? 'Tu' : msg.senderUsername}
              </p>
              <p className="text-base">{msg.content}</p>
              <span className="text-xs text-gray-300 block text-right mt-1">
                {new Date(msg.timestamp).toLocaleTimeString([], {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </span>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} /> {/* Elemento vuoto per lo scroll */}
      </div>

      {/* Input Messaggio */}
      <div className="bg-gray-800 p-4 rounded-b-lg flex items-center border-t border-gray-600 shadow-md">
        <input
          type="text"
          value={messageInput}
          onChange={(e) => setMessageInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Scrivi un messaggio..."
          className="flex-1 px-4 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200 mr-3"
          disabled={!isConnected}
        />
        <button
          onClick={sendMessage}
          className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition duration-200 flex items-center"
          disabled={!isConnected || messageInput.trim() === ''}
        >
          <FontAwesomeIcon icon={faPaperPlane} className="mr-2" /> Invia
        </button>
      </div>
    </div>
  );
};

// Componente Alert (copiato da altre pagine per coerenza)
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

export default ChatComponent;
