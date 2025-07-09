import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosWithAuth from '../services/axiosWhitAuth';

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
        const { data } = await axiosWithAuth.get('/me');
        // Converti level e rating a numeri quando carichi i dati iniziali
        setForm({
          ...data,
          level: Number(data.level), // Assicura che sia un numero
          rating: Number(data.rating), // Assicura che sia un numero
        });
        setOriginal({
          ...data,
          level: Number(data.level),
          rating: Number(data.rating),
        });
      } catch (err) {
        console.error('Errore caricamento profilo:', err);
        setError('Errore durante il caricamento del profilo.');
        if (err.response?.status === 401) {
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
    const { name, value, type } = e.target; // Ottieni anche il tipo dell'input

    setForm((prev) => {
      let newValue = value;
      // Converti in numero se il tipo è 'number'
      if (type === 'number') {
        // Usa Number() per convertire in numero (float o integer)
        // Se il campo è vuoto, imposta a 0 o null a seconda delle tue esigenze
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
      // Per 'level' e 'rating', assicurati che i valori siano numeri validi
      // e confrontali con gli originali per vedere se sono cambiati.
      // Se il valore è un numero e non è cambiato, non includerlo.
      // Se è cambiato, includilo.
      if (key === 'level' || key === 'rating') {
        const parsedValue = Number(form[key]); // Converti esplicitamente in numero
        if (!isNaN(parsedValue) && parsedValue !== original[key]) {
          acc[key] = parsedValue;
        } else if (isNaN(parsedValue) && form[key] !== original[key]) {
          // Se il valore è NaN (es. l'utente ha svuotato il campo numerico)
          // e l'originale non era NaN (cioè era un numero),
          // puoi decidere se inviare null, 0 o lasciare invariato.
          // Per ora, lo inviamo come null o 0, a seconda di come lo gestisce il backend.
          // L'errore del backend suggerisce che non accetta "non-numerici".
          // Quindi, potremmo voler non inviare il campo se non è un numero valido.
          // Oppure validare a livello di frontend.
          if (form[key] === '') {
            // Se il campo è stato svuotato
            acc[key] = 0; // O null, se il backend accetta null per int
          } else {
            // Se non è un numero valido e non è vuoto, gestisci l'errore o non includere il campo
            console.warn(
              `Campo ${key} non numerico valido: ${form[key]}. Non verrà inviato.`
            );
            // Potresti voler mostrare un errore all'utente qui
          }
        }
      } else if (form[key] !== original[key]) {
        // Per tutti gli altri campi, mantieni la logica esistente
        acc[key] = form[key];
      }
      return acc;
    }, {});

    // Aggiungi qui una validazione frontend per level e rating
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
      const res = await axiosWithAuth.put('/me/update', updatedFields);
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
      // Puoi migliorare la gestione degli errori qui
      if (err.response && err.response.data && err.response.data.message) {
        setError(`Errore: ${err.response.data.message}`);
      } else {
        setError("Errore durante l'aggiornamento del profilo.");
      }
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
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
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold text-purple-700">Il mio profilo</h2>
          <button
            onClick={handleLogout}
            className="text-red-600 text-sm underline"
          >
            Logout
          </button>
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
            value={form.level} // Il valore per l'input di tipo "number" può essere una stringa vuota per indicare che non c'è valore
            onChange={handleChange}
            type="number"
            min="0" // Aggiungi min/max se appropriato
            step="1" // Forse vuoi che il level sia sempre un intero
          />
          <Input
            label="Rating"
            name="rating"
            value={form.rating}
            onChange={handleChange}
            type="number"
            step="0.1"
            min="0" // Aggiungi min/max se appropriato
            max="10" // Ad esempio, se il rating va da 0 a 10
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

// I componenti Input, Textarea e Alert rimangono invariati,
// a parte l'aggiunta di `step`, `min`, `max` nel componente Input.
const Input = ({
  label,
  name,
  value,
  onChange,
  type = 'text',
  step,
  min,
  max,
}) => (
  <div>
    <label className="block text-sm font-medium text-gray-700">{label}</label>
    <input
      name={name}
      value={value === 0 ? 0 : value || ''} // Gestisci il caso in cui il valore sia 0 (number) o null/undefined
      onChange={onChange}
      type={type}
      step={step}
      min={min} // Passa min
      max={max} // Passa max
      required={name === 'username' || name === 'email'}
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
      rows={4}
      className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-purple-500"
    />
  </div>
);

const Alert = ({ type, message }) => {
  const base =
    type === 'success'
      ? 'bg-green-100 border-green-400 text-green-700'
      : 'bg-red-100 border-red-400 text-red-700';
  return (
    <div
      className={`border px-4 py-3 rounded mb-4 ${base}`}
      role="alert"
      aria-live="polite"
    >
      {type === 'success' ? '✅' : '❌'} {message}
    </div>
  );
};

export default ProfilePages;
