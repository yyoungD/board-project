import apiClient from './client.js';
import { getErrorMessage } from './posts.js';

export async function signup(member) {
  const response = await apiClient.post('/api/members/signup', member, { skipAuth: true });
  return response.data;
}

export async function login(credentials) {
  const response = await apiClient.post('/api/members/login', credentials, { skipAuth: true });
  return response.data;
}

export async function getMe() {
  const response = await apiClient.get('/api/members/me');
  return response.data;
}

export async function updateMe(member) {
  const response = await apiClient.put('/api/members/me', member);
  return response.data;
}

export async function deleteMe() {
  await apiClient.delete('/api/members/me');
}

export async function logout() {
  await apiClient.post('/api/members/logout', null, { skipAuth: true });
}

export { getErrorMessage };
