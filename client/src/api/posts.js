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

export async function updatePost(id, post) {
  const response = await axios.put(`/api/posts/${id}`, post);
  return response.data;
}

export async function deletePost(id) {
  await axios.delete(`/api/posts/${id}`);
}

export function getErrorMessage(error, fallbackMessage) {
  const responseData = error.response?.data;

  return (
    responseData?.message ||
    responseData?.error ||
    responseData?.detail ||
    fallbackMessage ||
    error.message
  );
}
