import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosWithAuth from '../services/axiosWithAuth';
import Swal from 'sweetalert2';

function AuthPages() {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

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
      const url = mode === 'login' ? '/auth/login' : '/auth/register';
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
        localStorage.setItem('userRole', data.role);
        localStorage.setItem('username', data.username);

        const userProfileRes = await axiosWithAuth.get('/auth/me');
        const userProfile = userProfileRes.data;

        const hasPreferredGames =
          userProfile.preferredGames && userProfile.preferredGames.length > 0;
        const hasPlatforms =
          userProfile.platforms && userProfile.platforms.length > 0;

        if (data.role === 'ROLE_ADMIN') {
          navigate('/admin-dashboard');
        } else if (!hasPreferredGames || !hasPlatforms) {
          navigate('/setup-profile');
        } else {
          navigate('/home');
        }
      } else {
        Swal.fire({
          title: 'Registrazione Completata!',
          text: 'Controlla la tua email per la verifica. Verrai reindirizzato alla pagina di configurazione del profilo.',
          icon: 'success',
          confirmButtonText: 'OK',
          customClass: {
            popup:
              'bg-gray-800 border border-purple-700 text-white rounded-xl shadow-2xl',
            title: 'text-purple-400 font-bold',
            content: 'text-gray-300',
            confirmButton:
              'bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-2 px-4 rounded-md transition duration-300 transform hover:scale-105 shadow-lg',
          },
          buttonsStyling: false,
        }).then(() => {
          navigate('/setup-profile');
        });
      }
    } catch (err) {
      if (err.response?.data?.message) {
        setError(`Errore: ${err.response.data.message}`);
      } else {
        setError('Credenziali non valide o errore di rete.');
      }
    }
  };

  return (
    <>
      <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden pt-16 pb-24">
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>

        <div className="relative z-10 text-center mb-8 px-4">
          <h1 className="text-5xl md:text-6xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-purple-400 to-blue-400 drop-shadow-lg animate-pulse-light font-oxanium">
            PIXELPALS
          </h1>
          <p className="mt-3 text-lg md:text-xl font-semibold text-transparent bg-clip-text bg-gradient-to-r from-cyan-300 to-green-300 animate-fade-in max-w-2xl mx-auto font-inter">
            **Connettiti. Gioca. Cresci insieme.**
          </p>
        </div>

        <div className="relative z-10 bg-gray-800 bg-opacity-90 shadow-2xl rounded-xl p-8 w-full max-w-md space-y-6 border border-purple-700 transform transition-all duration-300 hover:scale-105">
          <h2 className="text-3xl font-bold text-center text-purple-400 drop-shadow-lg font-oxanium">
            {mode === 'login' ? 'ACCEDI' : 'CREA IL TUO ACCOUNT'}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <input
              name="username"
              placeholder="Username"
              value={form.username}
              onChange={handleChange}
              required
              className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 text-base"
            />
            {mode === 'register' && (
              <input
                name="email"
                type="email"
                placeholder="Email"
                value={form.email}
                onChange={handleChange}
                required
                className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 text-base"
              />
            )}
            <input
              name="password"
              type="password"
              placeholder="Password"
              value={form.password}
              onChange={handleChange}
              required
              className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 text-base"
            />

            {error && (
              <div
                className="bg-red-900 bg-opacity-70 border border-red-700 text-red-300 px-4 py-3 rounded relative text-sm"
                role="alert"
              >
                <strong className="font-bold">ERRORE: </strong>
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
              className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl text-lg tracking-wide"
            >
              {mode === 'login' ? 'ACCEDI' : 'REGISTRATI'}
            </button>
          </form>

          <div className="text-center text-gray-400 text-sm">
            {mode === 'login' ? (
              <>
                Non hai un account?{' '}
                <button
                  onClick={toggleMode}
                  className="text-blue-400 hover:text-blue-300 underline font-semibold transition duration-200"
                >
                  Registrati qui!
                </button>
              </>
            ) : (
              <>
                Hai già un account?{' '}
                <button
                  onClick={toggleMode}
                  className="text-blue-400 hover:text-blue-300 underline font-semibold transition duration-200"
                >
                  Accedi qui!
                </button>
              </>
            )}
          </div>
        </div>
      </div>
      <style>{`
        @keyframes pulse-light {
          0%, 100% {
            opacity: 1;
            transform: scale(1);
          }
          50% {
            opacity: 0.8;
            transform: scale(1.02);
          }
        }
        .animate-pulse-light {
          animation: pulse-light 2s infinite ease-in-out;
        }

        @keyframes fade-in {
          0% {
            opacity: 0;
            transform: translateY(10px);
          }
          100% {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fade-in {
          animation: fade-in 1s ease-out forwards;
        }
      `}</style>
    </>
  );
}

export default AuthPages;
