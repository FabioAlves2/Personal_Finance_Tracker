import api from './axiosConfig';

export const updateProfile = (name) => api.post('/user/update-profile', { name });
export const changePassword = (currentPassword, newPassword) => 
    api.post('/user/change-password', { currentPassword, newPassword });