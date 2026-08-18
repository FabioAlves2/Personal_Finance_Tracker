import api from './axiosConfig';

// Buscar transações do usuário, opcionalmente filtradas por tipo, categoria e/ou intervalo de datas.
// O backend combina livremente qualquer conjunto destes filtros.
export const getTransactions = ({ type, categoryId, start, end } = {}) => {
  const params = {};
  if (type) params.type = type;
  if (categoryId) params.categoryId = categoryId;
  if (start) params.start = start;
  if (end) params.end = end;
  return api.get('/transactions', { params });
};

// Criar nova transação
export const createTransaction = (data) => api.post('/transactions', data);

// Atualizar transação
export const updateTransaction = (id, data) => api.patch(`/transactions/${id}`, data);

// Deletar transação
export const deleteTransaction = (id) => api.delete(`/transactions/${id}`);