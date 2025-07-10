import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosWithAuth from '../services/axiosWithAuth';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

const ProfilePages = () => {
  const [form, setForm] = useState({});
  const [original, setOriginal] = useState({});
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    const fetchUser = async () => {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        navigate('/');
        return;
      }

      try {
        const { data } = await axiosWithAuth.get('/auth/me');
        setForm({
          ...data,
          level: Number(data.level),
          rating: Number(data.rating),
        });
        setOriginal({
          ...data,
          level: Number(data.level),
          rating: Number(data.rating),
        });
      } catch (err) {
        console.error('Errore caricamento profilo:', err);
        setError('Errore durante il caricamento del profilo.');
        if (
          err.response?.status === 401 ||
          err.response?.status === 403 ||
          err.response?.status === 404
        ) {
          localStorage.clear();
          navigate('/');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value, type } = e.target;

    setForm((prev) => {
      let newValue = value;
      if (type === 'number') {
        newValue = value === '' ? '' : Number(value);
      }
      return { ...prev, [name]: newValue };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMsg('');
    setError('');

    const updatedFields = Object.keys(form).reduce((acc, key) => {
      if (key === 'level' || key === 'rating') {
        const parsedValue = Number(form[key]);
        if (!isNaN(parsedValue) && parsedValue !== original[key]) {
          acc[key] = parsedValue;
        } else if (isNaN(parsedValue) && form[key] !== original[key]) {
          if (form[key] === '') {
            acc[key] = 0;
          } else {
            console.warn(
              `Campo ${key} non numerico valido: ${form[key]}. Non verrà inviato.`
            );
          }
        }
      } else if (form[key] !== original[key]) {
        acc[key] = form[key];
      }
      return acc;
    }, {});

    if (
      updatedFields.level !== undefined &&
      !Number.isInteger(updatedFields.level)
    ) {
      setError("Il campo 'Livello' deve essere un numero intero.");
      return;
    }
    if (updatedFields.rating !== undefined && isNaN(updatedFields.rating)) {
      setError("Il campo 'Rating' deve essere un numero valido.");
      return;
    }

    if (!Object.keys(updatedFields).length) {
      setMsg('Nessuna modifica da salvare.');
      return;
    }

    try {
      const res = await axiosWithAuth.put('/auth/me/update', updatedFields);
      setForm({
        ...res.data,
        level: Number(res.data.level),
        rating: Number(res.data.rating),
      });
      setOriginal({
        ...res.data,
        level: Number(res.data.level),
        rating: Number(res.data.rating),
      });
      setMsg('Profilo aggiornato con successo.');
    } catch (err) {
      console.error('Errore aggiornamento:', err);
      if (err.response && err.response.data && err.response.data.message) {
        setError(`Errore: ${err.response.data.message}`);
      } else {
        setError("Errore durante l'aggiornamento del profilo.");
      }
    }
  };

  if (loading) {
    return (
      <div className="text-center py-10 text-purple-400 font-medium text-lg font-oxanium">
        Caricamento profilo...
      </div>
    );
  }

  return (
    <>
      <Navbar />
      {/* Sfondo più scuro e "techy" con gradiente e effetto noise */}
      <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden">
        {/* Effetto noise/griglia di sfondo */}
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>

        {/* Contenitore del form con stile gaming */}
        <div className="flex-grow max-w-2xl mx-auto bg-gray-800 bg-opacity-90 shadow-2xl rounded-2xl p-8 mt-24 mb-24 w-full px-4 text-gray-200 border border-purple-700 transform transition-all duration-300 hover:scale-105 relative z-10">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-3xl font-bold text-purple-400 font-oxanium drop-shadow-lg">
              IL MIO PROFILO
            </h2>
          </div>

          {msg && <Alert type="success" message={msg} />}
          {error && <Alert type="error" message={error} />}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Username"
              name="username"
              value={form.username}
              onChange={handleChange}
            />
            <Input
              label="Email"
              name="email"
              value={form.email}
              onChange={handleChange}
              type="email"
            />
            <Input
              label="Avatar URL"
              name="avatarUrl"
              value={form.avatarUrl}
              onChange={handleChange}
            />
            <Textarea
              label="Bio"
              name="bio"
              value={form.bio}
              onChange={handleChange}
            />
            <Input
              label="Livello"
              name="level"
              value={form.level}
              onChange={handleChange}
              type="number"
              min="0"
              step="1"
            />
            <Input
              label="Rating"
              name="rating"
              value={form.rating}
              onChange={handleChange}
              type="number"
              step="0.1"
              min="0"
              max="10"
            />
            <Input
              label="Nuova Password"
              name="password"
              value={form.password || ''}
              onChange={handleChange}
              type="password"
            />

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-bold py-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl text-lg tracking-wide"
            >
              SALVA MODIFICHE
            </button>
          </form>
        </div>
      </div>
      <Footer />
    </>
  );
};

// I componenti Input, Textarea e Alert sono stati aggiornati per il tema scuro
const Input = ({
  label,
  name,
  value,
  onChange,
  type = 'text',
  step,
  min,
  max,
  placeholder,
  required = false,
}) => (
  <div>
    <label className="block text-sm font-medium text-gray-300">{label}</label>
    <input
      name={name}
      value={value === 0 ? 0 : value || ''}
      onChange={onChange}
      type={type}
      step={step}
      min={min}
      max={max}
      placeholder={placeholder}
      required={required}
      className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 text-base"
    />
  </div>
);

const Textarea = ({ label, name, value, onChange }) => (
  <div>
    <label className="block text-sm font-medium text-gray-300">{label}</label>
    <textarea
      name={name}
      value={value || ''}
      onChange={onChange}
      rows={4}
      className="w-full px-4 py-3 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 text-base"
    />
  </div>
);

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

export default ProfilePages;
