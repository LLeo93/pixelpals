import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { getUser } from '../services/authservice'; // Assicurati che funzioni correttamente

const ProfilePage = () => {
  const [form, setForm] = useState({});
  const [original, setOriginal] = useState({});
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const token = localStorage.getItem('accessToken');

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const userData = await getUser();
        setForm(userData);
        setOriginal(userData);
      } catch (err) {
        console.error('Errore caricamento profilo:', err);
        setError('Errore durante il caricamento del profilo.');
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMsg('');
    setError('');

    const updatedFields = {};
    for (let key in form) {
      if (form[key] !== original[key]) {
        updatedFields[key] = form[key];
      }
    }

    if (!Object.keys(updatedFields).length) {
      setMsg('Nessuna modifica da salvare.');
      return;
    }

    try {
      const res = await axios.put('/api/auth/me/update', updatedFields, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setOriginal(res.data);
      setForm(res.data);
      setMsg('Profilo aggiornato con successo.');
    } catch (err) {
      console.error('Errore aggiornamento profilo:', err);
      setError("Errore durante l'aggiornamento.");
    }
  };

  if (loading) {
    return (
      <div className="text-center py-10 text-purple-600 font-medium text-lg">
        Caricamento profilo...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-700 to-purple-700 py-12 px-4">
      <div className="max-w-2xl mx-auto bg-white shadow-xl rounded-2xl p-8">
        <h2 className="text-3xl font-bold text-center text-purple-700 mb-6">
          Il mio profilo
        </h2>

        {msg && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            ✅ {msg}
          </div>
        )}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            ❌ {error}
          </div>
        )}

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
          />
          <Input
            label="Rating"
            name="rating"
            value={form.rating}
            onChange={handleChange}
            type="number"
            step="0.1"
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
            className="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-2 rounded-md transition"
          >
            Salva modifiche
          </button>
        </form>
      </div>
    </div>
  );
};

const Input = ({ label, name, value, onChange, type = 'text', step }) => (
  <div>
    <label className="block text-sm font-medium text-gray-700">{label}</label>
    <input
      name={name}
      value={value || ''}
      onChange={onChange}
      type={type}
      step={step}
      className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
    />
  </div>
);

const Textarea = ({ label, name, value, onChange }) => (
  <div>
    <label className="block text-sm font-medium text-gray-700">{label}</label>
    <textarea
      name={name}
      value={value || ''}
      onChange={onChange}
      className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
    />
  </div>
);

export default ProfilePage;
