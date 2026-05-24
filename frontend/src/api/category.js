import service from './index';

export const getAllCategories = () => service.get('/category/all');
export const getCategoryById = (id) => service.get(`/category/${id}`);
export const addCategory = (data) => service.post('/category', data);
export const updateCategory = (data) => service.put('/category', data);
export const deleteCategory = (id) => service.delete(`/category/${id}`);
