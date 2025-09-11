import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { API_BASE_URL } from '../services/config';

const VerificationPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [message, setMessage] = useState(
    'Verifica del tuo account in corso...'
  );
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    const verifyEmail = async () => {
      const params = new URLSearchParams(location.search);
      const token = params.get('token');

      if (!token) {
        setMessage('Token di verifica mancante.');
        setIsSuccess(false);
        return;
      }

      try {
        const response = await axios.get(
          `${API_BASE_URL}/auth/verify-email?token=${token}`
        );

        if (response.status === 200) {
          setMessage(
            'La tua email è stata verificata con successo! Ora puoi effettuare il login.'
          );
          setIsSuccess(true);
        } else {
          setMessage(
            'Errore durante la verifica: ' +
              (response.data.message || 'Errore sconosciuto.')
          );
          setIsSuccess(false);
        }
      } catch (error) {
        console.error('Errore di verifica:', error);
        setMessage(
          'Impossibile verificare la tua email. Il link potrebbe essere scaduto o non valido.'
        );
        setIsSuccess(false);
      }
    };

    verifyEmail();
  }, [location.search]);

  return (
    <>
      <Navbar />

      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden pt-16 pb-24">
        {' '}
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>
        <div className="relative z-10 bg-gray-800 bg-opacity-90 shadow-2xl rounded-xl p-8 w-full max-w-md space-y-6 text-center border border-purple-700 transform transition-all duration-300 hover:scale-105">
          <h2 className="text-3xl font-bold text-purple-400 font-oxanium drop-shadow-lg">
            VERIFICA EMAIL
          </h2>
          <p
            className={`text-lg font-medium ${
              isSuccess ? 'text-green-400' : 'text-red-400'
            }`}
          >
            {message}
          </p>
          {isSuccess && (
            <button
              onClick={() => navigate('/')}
              className="mt-4 w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl text-lg tracking-wide"
            >
              VAI AL LOGIN
            </button>
          )}
          {!isSuccess && (
            <button
              onClick={() => navigate('/')}
              className="mt-4 w-full bg-gray-700 hover:bg-gray-600 text-gray-200 font-bold py-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl text-lg tracking-wide"
            >
              TORNA AL LOGIN
            </button>
          )}
        </div>
      </div>
      <Footer />
    </>
  );
};

export default VerificationPage;
