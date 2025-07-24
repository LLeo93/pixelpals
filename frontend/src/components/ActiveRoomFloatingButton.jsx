import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDoorOpen } from '@fortawesome/free-solid-svg-icons';

const ActiveRoomFloatingButton = () => {
  const [activeMatchId, setActiveMatchId] = useState(null);

  const checkActiveMatch = () => {
    const storedMatchId = localStorage.getItem('activeMatchId');
    setActiveMatchId(storedMatchId);
  };

  useEffect(() => {
    checkActiveMatch();

    const handleStorageChange = (event) => {
      if (event.key === 'activeMatchId') {
        setActiveMatchId(event.newValue);
      }
    };

    window.addEventListener('storage', handleStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  if (!activeMatchId) {
    return null;
  }

  return (
    <div className="fixed bottom-4 right-4 z-40">
      <Link
        to={`/match-room/${activeMatchId}`}
        className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-6 rounded-full shadow-lg transition duration-300 transform hover:scale-105 flex items-center justify-center text-base"
      >
        <FontAwesomeIcon icon={faDoorOpen} className="mr-2" /> Torna nella Room
      </Link>
    </div>
  );
};

export default ActiveRoomFloatingButton;
