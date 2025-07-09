import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosWithAuth from '../services/axiosWhitAuth';

function AuthPages() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');

  const navigate = useNavigate(); // Inizializza useNavigate

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const toggleMode = () => {
    setMode(mode === 'login' ? 'register' : 'login');
    setForm({ username: '', email: '', password: '' });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const url = mode === 'login' ? '/login' : '/register';
      const payload =
        mode === 'login'
          ? { username: form.username, password: form.password }
          : {
              username: form.username,
              email: form.email,
              password: form.password,
            };

      const { data } = await axiosWithAuth.post(url, payload);

      if (mode === 'login') {
        localStorage.setItem('accessToken', data.token);
        // Reindirizza l'utente alla homepage dopo il login riuscito
        navigate('/home'); // Modificato da window.location.href = '/profile';
      } else {
        // Dopo la registrazione, reindirizza alla pagina di login
        setMode('login');
        // Potresti anche voler mostrare un messaggio di successo qui
      }
    } catch (err) {
      console.error('Errore autenticazione:', err);
      // Migliora il messaggio di errore se disponibile dalla risposta del backend
      if (err.response && err.response.data && err.response.data.message) {
        setError(`Errore: ${err.response.data.message}`);
      } else {
        setError('Credenziali non valide o errore di rete.');
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-700 to-purple-700 px-4">
      <div className="bg-white shadow-lg rounded-2xl p-8 w-full max-w-md space-y-6">
        <h2 className="text-2xl font-bold text-center text-purple-700">
          {mode === 'login' ? 'Accedi al tuo account' : 'Crea un nuovo account'}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            name="username"
            placeholder="Username"
            value={form.username}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-purple-500"
          />
          {mode === 'register' && (
            <input
              name="email"
              type="email"
              placeholder="Email"
              value={form.email}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-purple-500"
            />
          )}
          <input
            name="password"
            type="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-purple-500"
          />

          {error && (
            <div
              className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
              role="alert"
            >
              <strong className="font-bold">Errore: </strong>
              <span className="block sm:inline">{error}</span>
              <span
                onClick={() => setError('')}
                className="absolute top-0 bottom-0 right-0 px-4 py-3 cursor-pointer"
              >
                <svg
                  className="fill-current h-6 w-6 text-red-500"
                  viewBox="0 0 20 20"
                >
                  <path d="M14.348 5.652a1 1 0 00-1.414 0L10 8.586 7.066 5.652a1 1 0 00-1.414 1.414L8.586 10l-2.934 2.934a1 1 0 001.414 1.414L10 11.414l2.934 2.934a1 1 0 001.414-1.414L11.414 10l2.934-2.934a1 1 0 000-1.414z" />
                </svg>
              </span>
            </div>
          )}

          <button
            type="submit"
            className="w-full bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 rounded-md transition"
          >
            {mode === 'login' ? 'Login' : 'Registrati'}
          </button>
        </form>

        <div className="text-center text-sm">
          {mode === 'login' ? (
            <>
              Non hai un account?{' '}
              <button onClick={toggleMode} className="text-blue-600 underline">
                Registrati
              </button>
            </>
          ) : (
            <>
              Hai già un account?{' '}
              <button onClick={toggleMode} className="text-blue-600 underline">
                Accedi
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default AuthPages;
