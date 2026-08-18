import api from "./axiosConfig";

// GET all categories
export const getCategories = () => api.get("/category");

// Create a new category
export const createCategory = (data) => api.post("/category", data);

// Update an existing category (full update)
export const updateCategory = (id, data) => api.patch(`/category/${id}`, data);

// Delete a category
export const deleteCategory = (id) => api.delete(`/category/${id}`);