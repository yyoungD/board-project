import apiClient from './client.js';

export async function getComments(postId) {
  const response = await apiClient.get(`/api/posts/${postId}/comments`);
  return response.data;
}

export async function createComment(postId, comment) {
  const response = await apiClient.post(`/api/posts/${postId}/comments`, comment);
  return response.data;
}

export async function updateComment(id, comment) {
  const response = await apiClient.put(`/api/comments/${id}`, comment);
  return response.data;
}

export async function deleteComment(id) {
  const response = await apiClient.delete(`/api/comments/${id}`);
  return response.data;
}
