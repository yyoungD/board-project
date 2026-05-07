import axios from 'axios';

export async function getPosts() {
  const response = await axios.get('/api/posts');
  return response.data;
}

export async function getPost(id) {
  const response = await axios.get(`/api/posts/${id}`);
  return response.data;
}

export async function createPost(post) {
  const response = await axios.post('/api/posts', post);
  return response.data;
}

export function getErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message || error.message || fallbackMessage;
}
