import axios from 'axios';

const API = axios.create({
  baseURL: '/api/auth',
});

export const login = async (email, password) => {
  const res = await API.post('/login', { email, password });
  localStorage.setItem('accessToken', res.data.token);
  localStorage.setItem('refreshToken', res.data.refreshToken);
  return res.data;
};

export const getUser = async () => {
  const token = localStorage.getItem('accessToken');
  try {
    const res = await API.get('/me', {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  } catch (err) {
    // Se token scaduto, prova refresh
    if (err.response?.status === 401) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        const retry = await API.get('/me', {
          headers: {
            Authorization: `Bearer ${newToken}`,
          },
        });
        return retry.data;
      }
    }
    throw err;
  }
};

export const refreshAccessToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return null;

  try {
    const res = await API.post('/refresh', { refreshToken });
    localStorage.setItem('accessToken', res.data.token);
    return res.data.token;
  } catch (err) {
    console.error('Errore nel refresh token:', err);
    return null;
  }
};
