import apiClient from './client.js';

export async function getPosts() {
  const response = await apiClient.get('/api/posts');
  return response.data;
}

export async function getPost(id) {
  const response = await apiClient.get(`/api/posts/${id}`);
  return response.data;
}

export async function createPost(post) {
  const response = await apiClient.post('/api/posts', post);
  return response.data;
}

export async function updatePost(id, post) {
  const response = await apiClient.put(`/api/posts/${id}`, post);
  return response.data;
}

export async function deletePost(id) {
  await apiClient.delete(`/api/posts/${id}`);
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
