// Calcula o intervalo de datas [start, end] (ISO 'yyyy-MM-dd') para um filtro de período
export const getDateRange = (period) => {
  const now = new Date();
  let start, end;

  switch (period) {
    case 'current_month':
      start = new Date(now.getFullYear(), now.getMonth(), 1);
      end = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      break;
    case 'past_month':
      start = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      end = new Date(now.getFullYear(), now.getMonth(), 0);
      break;
    case 'current_year':
      start = new Date(now.getFullYear(), 0, 1);
      end = new Date(now.getFullYear(), 11, 31);
      break;
    case 'past_year':
      start = new Date(now.getFullYear() - 1, 0, 1);
      end = new Date(now.getFullYear() - 1, 11, 31);
      break;
    default:
      start = null;
      end = null;
  }

  return {
    start: start ? start.toISOString().split('T')[0] : null,
    end: end ? end.toISOString().split('T')[0] : null,
  };
};

export const formatCurrency = (value) =>
  new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' }).format(value);

// Rótulo e cor de UI para os 3 "kinds" de categoria / tipos de transação
const KIND_LABELS = { INCOME: 'Income', EXPENSE: 'Expense', SAVING: 'Savings' };
const KIND_COLORS = { INCOME: 'success', EXPENSE: 'error', SAVING: 'warning' };

export const kindLabel = (kind) => KIND_LABELS[kind] || kind;
export const kindColor = (kind) => KIND_COLORS[kind] || 'default';
