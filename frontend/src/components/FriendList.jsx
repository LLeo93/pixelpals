import React, { useEffect, useState, useRef } from 'react';
const FriendList = ({ userToken, username }) => {
  const [friendsData, setFriendsData] = useState([]);
  const [loading, setLoading] = useState(true);
  const isMounted = useRef(false);

  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);
  const fetchFriendsStatus = async () => {
    if (!userToken) {
      console.warn('⚠️ Token utente non disponibile per il polling amici.');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(
        'http://localhost:8080/api/auth/friends/status',
        {
          headers: {
            Authorization: `Bearer ${userToken}`,
            'Content-Type': 'application/json',
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error(
          `❌ Errore HTTP durante il fetching degli amici: ${response.status} - ${errorText}`
        );
        setFriendsData([]);
        setLoading(false);
        return;
      }

      const data = await response.json();
      if (isMounted.current) {
        setFriendsData(data);
        console.log('DEBUG: Dati amici ricevuti dal backend:', data);
      }
    } catch (error) {
      console.error(
        '❌ Errore di rete o parsing durante il fetching degli amici:',
        error
      );
      if (isMounted.current) {
        setFriendsData([]);
      }
    } finally {
      if (isMounted.current) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    console.log('--- FriendList useEffect (Polling) ---');

    fetchFriendsStatus();

    const pollingInterval = setInterval(() => {
      fetchFriendsStatus();
    }, 5000);

    return () => {
      console.log('🧹 Pulizia intervallo di polling FriendList...');
      clearInterval(pollingInterval);
    };
  }, [userToken]);
  const onlineFriends = friendsData.filter((f) => {
    const isFriendOnline = f.online === true;
    if (f.username) {
      console.log(
        `DEBUG: Amico ${f.username} (ID: ${f.id}) - online: ${
          f.online
        } (Tipo: ${typeof f.online}) -> Risultato filtro ONLINE: ${isFriendOnline}`
      );
    }
    return isFriendOnline;
  });

  const offlineFriends = friendsData.filter((f) => {
    const isFriendOffline = f.online === false;
    if (f.username) {
      console.log(
        `DEBUG: Amico ${f.username} (ID: ${f.id}) - online: ${
          f.online
        } (Tipo: ${typeof f.online}) -> Risultato filtro OFFLINE: ${isFriendOffline}`
      );
    }
    return isFriendOffline;
  });

  if (loading) {
    return (
      <div className="flex items-center gap-2 text-green-400">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5 animate-pulse"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path d="M2.94 7.94a10 10 0 0114.12 0 1 1 0 01-1.42 1.42 8 8 0 00-11.28 0 1 1 0 01-1.42-1.42zM5.76 10.76a6 6 0 018.48 0 1 1 0 01-1.42 1.42 4 4 0 00-5.64 0 1 1 0 01-1.42-1.42zM10 14a1 1 0 110 2 1 1 0 010-2z" />
        </svg>
        <span>Caricamento amici...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-green-400 font-bold text-lg mb-2">
          🟢 Amici Online ({onlineFriends.length})
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {onlineFriends.length > 0 ? (
            onlineFriends.map((friend) => (
              <div
                key={friend.id}
                className="bg-gray-700 p-3 rounded-lg flex items-center space-x-3 border border-gray-600 shadow-md"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="w-5 h-5 text-green-400 animate-pulse"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path d="M2.94 7.94a10 10 0 0114.12 0 1 1 0 01-1.42 1.42 8 8 0 00-11.28 0 1 1 0 01-1.42-1.42zM5.76 10.76a6 6 0 018.48 0 1 1 0 01-1.42 1.42 4 4 0 00-5.64 0 1 1 0 01-1.42-1.42zM10 14a1 1 0 110 2 1 1 0 010-2z" />
                </svg>
                <span className="text-white">{friend.username}</span>
              </div>
            ))
          ) : (
            <span className="text-gray-400">
              Nessun amico online al momento.
            </span>
          )}
        </div>
      </div>

      <div>
        <h2 className="text-gray-300 font-bold text-lg mb-2">
          ⚪ Amici Offline ({offlineFriends.length})
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {offlineFriends.length > 0 ? (
            offlineFriends.map((friend) => (
              <div
                key={friend.id}
                className="bg-gray-700 p-3 rounded-lg flex items-center space-x-3 border border-gray-600 shadow-md"
              >
                <div className="w-3 h-3 rounded-full bg-gray-500"></div>
                <span className="text-gray-300">{friend.username}</span>
              </div>
            ))
          ) : (
            <span className="text-gray-400">
              Tutti gli amici sono online o non ci sono amici offline.
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default FriendList;
