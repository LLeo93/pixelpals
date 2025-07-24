import { useEffect, useRef, useState, useCallback } from 'react';
import { Stomp } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const useSocket = (token) => {
  const [client, setClient] = useState(null);
  const socketRef = useRef(null);

  const connectWebSocket = useCallback(() => {
    if (!token) {
      if (socketRef.current) {
        socketRef.current.deactivate();
        socketRef.current = null;
      }
      setClient(null);
      return;
    }

    if (
      socketRef.current &&
      (socketRef.current.connected || socketRef.current.connecting)
    ) {
      setClient(socketRef.current);
      return;
    }

    const stompClient = Stomp.over(
      () => new SockJS('http://localhost:8080/ws')
    );
    stompClient.debug = () => {};
    stompClient.connectHeaders = {
      Authorization: `Bearer ${token}`,
    };
    stompClient.heartbeatIncoming = 120000;
    stompClient.heartbeatOutgoing = 120000;
    stompClient.reconnectDelay = 1000;

    stompClient.onStompError = () => {};
    stompClient.onConnect = () => {
      setClient(stompClient);
      stompClient.reconnectDelay = 1000;
    };
    stompClient.onDisconnect = () => {};
    stompClient.onWebSocketClose = () => {};

    stompClient.activate();
    socketRef.current = stompClient;
  }, [token]);

  useEffect(() => {
    if (token) {
      connectWebSocket();
    } else {
      if (socketRef.current) {
        socketRef.current.deactivate();
      }
      setClient(null);
      socketRef.current = null;
    }

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        if (
          !socketRef.current ||
          (!socketRef.current.connected && !socketRef.current.connecting)
        ) {
          connectWebSocket();
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      if (socketRef.current) {
        socketRef.current.deactivate();
      }
      setClient(null);
      socketRef.current = null;
    };
  }, [token, connectWebSocket]);

  return client;
};
