import axiosWithAuth from './axiosWithAuth';

export const login = async (username, password) => {
  const res = await axiosWithAuth.post('/login', { username, password });
  localStorage.setItem('accessToken', res.data.token);
  return res.data;
};

export const getUser = async () => {
  const res = await axiosWithAuth.get('/me');
  return res.data;
};
