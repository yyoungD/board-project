import axios from 'axios';

const apiClient = axios.create({
  withCredentials: true
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  if (token && !config.skipAuth) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest || originalRequest._retry || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    if (originalRequest.url === '/api/members/refresh') {
      clearSavedAuth();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const response = await axios.post('/api/members/refresh', null, {
        withCredentials: true
      });
      const authResponse = response.data;

      localStorage.setItem('member', JSON.stringify(authResponse.member));
      localStorage.setItem('token', authResponse.token);
      window.dispatchEvent(new CustomEvent('auth:change', { detail: authResponse.member }));

      originalRequest.headers.Authorization = `Bearer ${authResponse.token}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      clearSavedAuth();
      window.dispatchEvent(new Event('auth:logout'));
      return Promise.reject(refreshError);
    }
  }
);

function clearSavedAuth() {
  localStorage.removeItem('member');
  localStorage.removeItem('token');
}

export default apiClient;
