import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api/auth',
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (res) => res,
  async (err) => {
    const originalRequest = err.config;

    if (err.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem('refreshToken');

      try {
        const res = await axios.post('/api/auth/refresh', { refreshToken });
        localStorage.setItem('accessToken', res.data.token);

        axiosInstance.defaults.headers.Authorization = `Bearer ${res.data.token}`;
        originalRequest.headers.Authorization = `Bearer ${res.data.token}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh fallito', refreshError);
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(err);
  }
);

export default axiosInstance;
