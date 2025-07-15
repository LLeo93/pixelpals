import axios from 'axios';

// Crea un'istanza di Axios con la base URL della tua API.
// Questo deve puntare alla radice dell'API, non a un sotto-percorso come /api/auth.
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api', // <-- MODIFICATO QUI: rimosso "/auth"
});

// Interceptor per aggiungere il token JWT a ogni richiesta
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // Gestione errori durante la preparazione della richiesta (es. token non disponibile)
    return Promise.reject(error);
  }
);

export default axiosInstance;
