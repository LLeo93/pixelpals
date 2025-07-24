import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosWithAuth from '../services/axiosWithAuth';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faEdit,
  faTrash,
  faPlus,
  faSync,
} from '@fortawesome/free-solid-svg-icons';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [msg, setMsg] = useState('');
  const [showAddEditModal, setShowAddEditModal] = useState(false);
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const userRole = localStorage.getItem('userRole');
    if (userRole !== 'ROLE_ADMIN') {
      navigate('/home');
      return;
    }
    fetchUsers();
  }, [navigate]);

  const fetchUsers = async () => {
    setLoading(true);
    setError('');
    setMsg('');
    try {
      const { data } = await axiosWithAuth.get('/users');
      setUsers(data);
    } catch (err) {
      setError(
        'Errore durante il recupero degli utenti. Potresti non avere i permessi.'
      );
      if (err.response?.status === 401 || err.response?.status === 403) {
        localStorage.clear();
        navigate('/');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAddUser = () => {
    setCurrentUser({
      username: '',
      email: '',
      password: '',
      role: 'ROLE_USER',
      verified: false,
      level: 0,
      rating: 0,
      online: undefined,
    });
    setShowAddEditModal(true);
  };

  const handleEditUser = (user) => {
    const userForEdit = { ...user, password: '' };
    if (userForEdit.online !== undefined) {
      delete userForEdit.online;
    }
    setCurrentUser(userForEdit);
    setShowAddEditModal(true);
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Sei sicuro di voler eliminare questo utente?')) return;

    try {
      await axiosWithAuth.delete(`/users/${userId}`);
      setMsg('Utente eliminato con successo!');
      fetchUsers();
    } catch (err) {
      setError("Errore durante l'eliminazione dell'utente.");
      if (err.response?.data?.message) {
        setError(`Errore: ${err.response.data.message}`);
      }
    }
  };

  const handleSaveUser = async (formData) => {
    setMsg('');
    setError('');
    try {
      const dataToSend = { ...formData };
      if (dataToSend.password === '') {
        delete dataToSend.password;
      }
      if (dataToSend.online !== undefined) {
        delete dataToSend.online;
      }

      if (formData.id) {
        await axiosWithAuth.put(`/users/${formData.id}`, dataToSend);
        setMsg('Utente aggiornato con successo!');
      } else {
        await axiosWithAuth.post('/users', dataToSend);
        setMsg('Utente creato con successo!');
      }
      setShowAddEditModal(false);
      fetchUsers();
    } catch (err) {
      throw err;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white text-3xl font-bold font-oxanium">
        Caricamento utenti...
      </div>
    );
  }

  return (
    <>
      <Navbar />
      <div className="min-h-screen flex flex-col bg-gradient-to-br from-gray-900 via-black to-blue-900 text-white font-inter relative overflow-hidden">
        <div
          className="absolute inset-0 z-0 opacity-10"
          style={{
            backgroundImage:
              "url(\"data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='%239C92AC' fill-opacity='0.1' fill-rule='evenodd'%3E%3Ccircle cx='3' cy='3' r='3'/%3E%3Ccircle cx='13' cy='13' r='3'/%3E%3C/g%3E%3C/svg%3E\")",
          }}
        ></div>
        <main className="flex-grow p-4 pt-16 pb-24 relative z-10">
          <div className="max-w-6xl mx-auto bg-gray-800 bg-opacity-90 shadow-2xl rounded-2xl p-8 text-gray-200 border border-purple-700">
            <div className="flex flex-col md:flex-row justify-between items-center mb-6">
              <h2 className="text-3xl font-bold text-purple-400 mb-4 md:mb-0 font-oxanium drop-shadow-lg">
                GESTIONE UTENTI
              </h2>
              <div className="flex flex-wrap justify-center gap-2 w-full md:w-auto">
                <button
                  onClick={fetchUsers}
                  className="bg-gradient-to-r from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 text-white font-medium py-1.5 px-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center text-sm whitespace-nowrap"
                >
                  <FontAwesomeIcon icon={faSync} className="mr-1" /> Aggiorna
                  Lista
                </button>
                <button
                  onClick={handleAddUser}
                  className="bg-gradient-to-r from-green-600 to-lime-600 hover:from-green-700 hover:to-lime-700 text-white font-medium py-1.5 px-3 rounded-md transition duration-300 transform hover:scale-105 shadow-lg flex items-center text-sm whitespace-nowrap"
                >
                  <FontAwesomeIcon icon={faPlus} className="mr-1" /> Aggiungi
                  Utente
                </button>
              </div>
            </div>
            {msg && <Alert type="success" message={msg} />}
            {error && <Alert type="error" message={error} />}
            <div className="overflow-x-auto">
              <table className="min-w-full bg-gray-700 rounded-lg shadow overflow-hidden border border-gray-600">
                <thead className="bg-gray-700 text-gray-300 uppercase text-xs leading-normal">
                  <tr>
                    <th className="py-3 px-6 text-left">Username</th>
                    <th className="py-3 px-6 text-left">Email</th>
                    <th className="py-3 px-6 text-left">Ruolo</th>
                    <th className="py-3 px-6 text-center">Verificato</th>
                    <th className="py-3 px-6 text-center">Azioni</th>
                  </tr>
                </thead>
                <tbody className="text-gray-300 text-sm font-light">
                  {users.map((user) => (
                    <tr
                      key={user.id}
                      className="border-b border-gray-600 hover:bg-gray-600 transition duration-150"
                    >
                      <td className="py-3 px-6 text-left whitespace-nowrap">
                        {user.username}
                      </td>
                      <td className="py-3 px-6 text-left">{user.email}</td>
                      <td className="py-3 px-6 text-left">{user.role}</td>
                      <td className="py-3 px-6 text-center">
                        {user.verified ? '✅ Sì' : '❌ No'}
                      </td>
                      <td className="py-3 px-6 text-center">
                        <div className="flex items-center justify-center space-x-2">
                          <button
                            onClick={() => handleEditUser(user)}
                            className="w-8 h-8 rounded-full bg-blue-800 text-blue-300 hover:bg-blue-700 flex items-center justify-center transition duration-150"
                            title="Modifica Utente"
                          >
                            <FontAwesomeIcon icon={faEdit} />
                          </button>
                          <button
                            onClick={() => handleDeleteUser(user.id)}
                            className="w-8 h-8 rounded-full bg-red-800 text-red-300 hover:bg-red-700 flex items-center justify-center transition duration-150"
                            title="Elimina Utente"
                          >
                            <FontAwesomeIcon icon={faTrash} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </main>

        {showAddEditModal && (
          <UserModal
            user={currentUser}
            onSave={handleSaveUser}
            onClose={() => setShowAddEditModal(false)}
            setModalError={setError}
          />
        )}
      </div>
      <Footer />
    </>
  );
};

const UserModal = ({ user, onSave, onClose, setModalError }) => {
  const [formData, setFormData] = useState(user);
  const [localModalError, setLocalModalError] = useState('');

  useEffect(() => {
    setFormData(user);
    setLocalModalError('');
  }, [user]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalModalError('');

    if (!formData.id && !formData.password) {
      setLocalModalError('La password è obbligatoria per i nuovi utenti.');
      return;
    }
    if (
      formData.level !== undefined &&
      !Number.isInteger(Number(formData.level))
    ) {
      setLocalModalError("Il campo 'Livello' deve essere un numero intero.");
      return;
    }
    if (formData.rating !== undefined && isNaN(Number(formData.rating))) {
      setLocalModalError("Il campo 'Rating' deve essere un numero valido.");
      return;
    }

    const dataToSave = {
      ...formData,
      level: Number(formData.level),
      rating: Number(formData.rating),
    };

    try {
      await onSave(dataToSave);
    } catch (err) {
      if (err.response?.data?.message) {
        setLocalModalError(`Errore: ${err.response.data.message}`);
      } else {
        setLocalModalError('Errore sconosciuto durante il salvataggio.');
      }
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-900 bg-opacity-70 flex justify-center items-center z-50">
      <div className="bg-gray-800 rounded-lg shadow-xl p-8 w-full max-w-md max-h-[90vh] overflow-y-auto text-gray-200 border border-blue-700">
        <h3 className="text-2xl font-bold mb-6 text-center text-blue-400 font-oxanium">
          {user.id ? 'MODIFICA UTENTE' : 'AGGIUNGI NUOVO UTENTE'}
        </h3>
        {localModalError && <Alert type="error" message={localModalError} />}
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            required
          />
          <Input
            label="Email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
          <Input
            label="Password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            placeholder={user.id ? 'Lascia vuoto per non cambiare' : ''}
            required={!user.id}
          />
          <div>
            <label className="block text-sm font-medium text-gray-300">
              Ruolo
            </label>
            <select
              name="role"
              value={formData.role}
              onChange={handleChange}
              className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 text-gray-200"
            >
              <option value="ROLE_USER">Utente</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>
          <Input
            label="Avatar URL"
            name="avatarUrl"
            value={formData.avatarUrl || ''}
            onChange={handleChange}
          />
          <Textarea
            label="Bio"
            name="bio"
            value={formData.bio || ''}
            onChange={handleChange}
          />
          <Input
            label="Livello"
            name="level"
            type="number"
            value={formData.level}
            onChange={handleChange}
            step="1"
            min="0"
          />
          <Input
            label="Rating"
            name="rating"
            type="number"
            value={formData.rating}
            onChange={handleChange}
            step="0.1"
            min="0"
            max="10"
          />
          <div className="flex items-center">
            <input
              type="checkbox"
              name="verified"
              checked={formData.verified}
              onChange={handleChange}
              className="h-4 w-4 text-purple-400 focus:ring-purple-500 border-gray-600 rounded bg-gray-700"
            />
            <label className="ml-2 block text-sm text-gray-200">
              Verificato
            </label>
          </div>
          <div className="flex justify-end space-x-4 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="bg-gray-700 hover:bg-gray-600 text-gray-200 font-semibold py-2 px-4 rounded-md transition duration-300"
            >
              Annulla
            </button>
            <button
              type="submit"
              className="bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white font-semibold py-2 px-4 rounded-md transition duration-300"
            >
              Salva
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

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
      className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
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
      className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition duration-200"
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

export default AdminDashboard;
