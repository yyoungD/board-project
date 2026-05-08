import apiClient from './client.js';

export async function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/api/uploads/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });

  return response.data;
}
