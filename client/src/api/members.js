import axios from 'axios';
import { getErrorMessage } from './posts.js';

export async function signup(member) {
  const response = await axios.post('/api/members/signup', member);
  return response.data;
}

export async function login(credentials) {
  const response = await axios.post('/api/members/login', credentials);
  return response.data;
}

export { getErrorMessage };
