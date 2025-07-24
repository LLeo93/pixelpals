import React, { useState, useEffect, useRef } from 'react';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faPaperPlane,
  faSpinner,
  faComments,
} from '@fortawesome/free-solid-svg-icons';

const ChatComponent = ({
  currentUserId,
  currentUserUsername,
  otherUser,
  stompClient,
  isConnected,
}) => {
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [error, setError] = useState('');
  const [loadingHistory, setLoadingHistory] = useState(false);
  const messagesEndRef = useRef(null);

  const chatRoomId =
    currentUserId && otherUser
      ? currentUserId.localeCompare(otherUser.id) < 0
        ? `${currentUserId}_${otherUser.id}`
        : `${otherUser.id}_${currentUserId}`
      : null;

  useEffect(() => {
    if (!currentUserId || !otherUser || !stompClient || !isConnected) return;

    setError('');
    setMessages([]);

    const privateSub = stompClient.subscribe(
      `/user/${currentUserUsername}/queue/messages`,
      (message) => {
        const receivedMessage = JSON.parse(message.body);
      }
    );

    const chatRoomSub = stompClient.subscribe(
      `/topic/chatRoom/${chatRoomId}`,
      (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages((prevMessages) => {
          const isOfficialDuplicate = prevMessages.some(
            (msg) => msg.id === receivedMessage.id
          );

          if (isOfficialDuplicate) return prevMessages;

          const updatedMessages = prevMessages.map((msg) => {
            if (
              msg.isTemporary &&
              msg.content === receivedMessage.content &&
              msg.senderId === receivedMessage.senderId
            ) {
              return { ...receivedMessage, isTemporary: false };
            }
            return msg;
          });

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
      }
    );

    fetchChatHistory(currentUserId, otherUser.id);

    return () => {
      if (privateSub) privateSub.unsubscribe();
      if (chatRoomSub) chatRoomSub.unsubscribe();
    };
  }, [
    currentUserId,
    currentUserUsername,
    otherUser,
    chatRoomId,
    stompClient,
    isConnected,
  ]);

  const fetchChatHistory = async (user1Id, user2Id) => {
    setLoadingHistory(true);
    try {
      const response = await axiosWithAuth.get(`/messages/history/${user2Id}`);
      setMessages(response.data);
    } catch (err) {
      setError('Errore durante il caricamento della cronologia chat.');
    } finally {
      setLoadingHistory(false);
    }
  };

  const sendMessage = () => {
    if (stompClient && isConnected && messageInput.trim() !== '' && otherUser) {
      const chatMessage = {
        senderId: currentUserId,
        senderUsername: currentUserUsername,
        receiverId: otherUser.id,
        receiverUsername: otherUser.username,
        content: messageInput.trim(),
        chatRoomId: chatRoomId,
      };

      stompClient.send(
        '/app/chat.sendMessage',
        {},
        JSON.stringify(chatMessage)
      );
      setMessageInput('');
      setMessages((prevMessages) => [
        ...prevMessages,
        {
          ...chatMessage,
          id: `temp_${Date.now()}_${Math.random()
            .toString(36)
            .substring(2, 9)}`,
          timestamp: new Date().toISOString(),
          isTemporary: true,
        },
      ]);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      sendMessage();
    }
  };

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

  return (
    <div className="flex-1 flex flex-col h-full">
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

      <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-700 custom-scrollbar">
        {error && <Alert type="error" message={error} />}
        {loadingHistory ? (
          <div className="flex justify-center items-center h-full">
            <FontAwesomeIcon
              icon={faSpinner}
              spin
              className="text-blue-400 text-3xl"
            />
          </div>
        ) : messages.length === 0 ? (
          <div className="flex justify-center items-center h-full text-gray-400">
            <p>Nessun messaggio in questa conversazione.</p>
          </div>
        ) : (
          messages.map((msg, index) => (
            <div
              key={msg.id || index}
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
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="bg-gray-800 p-4 rounded-b-lg flex items-center border-t border-gray-600 shadow-md">
        <input
          type="text"
          value={messageInput}
          onChange={(e) => setMessageInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Scrivi un messaggio..."
          className="min-w-0 flex-1 px-4 py-2 bg-gray-700 sm:px-3 sm:py-1 sm:text-sm border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200 mr-3"
          disabled={!isConnected}
        />
        <button
          onClick={sendMessage}
          className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition duration-200 flex items-center sm:py-1 sm:px-3 sm:text-sm"
          disabled={!isConnected || messageInput.trim() === ''}
        >
          <FontAwesomeIcon icon={faPaperPlane} className="mr-2" /> Invia
        </button>
      </div>
    </div>
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

export default ChatComponent;
