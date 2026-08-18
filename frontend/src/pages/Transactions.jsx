import { useState, useEffect } from 'react';
import {
  Button,
  Container,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Toolbar,
  Typography,
  Select,
  Stack,
  ToggleButtonGroup,
  ToggleButton,
  Chip,
  Box,
  CircularProgress,
  Alert,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import Navbar from '../components/Navbar';
import {
  getTransactions,
  updateTransaction,
  deleteTransaction,
  createTransaction
} from '../api/transactions';
import { getCategories } from '../api/categories';
import { getDateRange, formatCurrency, kindLabel, kindColor } from '../utils/finance';
import SavingsIcon from '@mui/icons-material/Savings';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';

export default function Transactions() {
  // Estados para filtros
  const [filterType, setFilterType] = useState('all');
  const [dateFilter, setDateFilter] = useState('current_month');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [categories, setCategories] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [paginationModel, setPaginationModel] = useState({
    pageSize: 10,
    page: 0,
  });

  // Estados para o diálogo de nova transação
  const [dialogOpen, setDialogOpen] = useState(false);
  const [newTransaction, setNewTransaction] = useState({
    description: '',
    amount: '',
    date: new Date().toISOString().split('T')[0], // hoje
    categoryId: ''
  });
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [formError, setFormError] = useState('');

  // Carregar categorias para os selects
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await getCategories();
        setCategories(response.data);
      } catch (err) {
        console.error('Erro ao carregar categorias', err);
      }
    };
    fetchCategories();
  }, []);

  // Buscar transações
  useEffect(() => {
    const fetchTransactions = async () => {
      setLoading(true);
      setError(null);

      try {
        const { start, end } = getDateRange(dateFilter);
        const response = await getTransactions({
          type: filterType !== 'all' ? filterType : undefined,
          categoryId: categoryFilter !== 'all' ? categoryFilter : undefined,
          start,
          end,
        });

        setTransactions(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Erro ao carregar transações');
      } finally {
        setLoading(false);
      }
    };

    fetchTransactions();
  }, [filterType, dateFilter, categoryFilter]);

  // Handlers para o diálogo
  const handleOpenCreate = () => {
    setEditingTransaction(null);
    setNewTransaction({
      description: '',
      amount: '',
      date: new Date().toISOString().split('T')[0],
      categoryId: ''
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleOpenEdit = (transaction) => {
    setEditingTransaction(transaction);
    setNewTransaction({
      description: transaction.description,
      amount: transaction.amount,
      date: transaction.date,
      categoryId: transaction.categoryId
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingTransaction(null);
  };

  const handleSubmitTransaction = async () => {
    if (!newTransaction.description || !newTransaction.amount || !newTransaction.date || !newTransaction.categoryId) {
      setFormError('Todos os campos são obrigatórios');
      return;
    }

    try {
      if (editingTransaction) {
        await updateTransaction(editingTransaction.id, {
          description: newTransaction.description,
          amount: parseFloat(newTransaction.amount),
          date: newTransaction.date,
          categoryId: parseInt(newTransaction.categoryId)
        });
      } else {
        await createTransaction({
          description: newTransaction.description,
          amount: parseFloat(newTransaction.amount),
          date: newTransaction.date,
          categoryId: parseInt(newTransaction.categoryId)
        });
      }
      handleCloseDialog();
      // Refresh list
      const response = await getTransactions();
      setTransactions(response.data);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Erro ao salvar transação');
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewTransaction(prev => ({ ...prev, [name]: value }));
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Tem certeza que deseja excluir esta transação?')) return;

    try {
      await deleteTransaction(id);
      setTransactions(prev => prev.filter(t => t.id !== id));
    } catch {
      alert('Erro ao excluir transação');
    }
  };

  // Colunas da DataGrid
  const columns = [
    { field: 'description', headerName: 'Description', flex: 2, minWidth: 150 },
    {
      field: 'categoryName',
      headerName: 'Category',
      flex: 1,
      minWidth: 150,
      valueGetter: (value, row) => row.categoryName || 'Sem categoria',
    },
    {
      field: 'date',
      headerName: 'Date',
      width: 150,
      valueFormatter: (value) => new Date(value).toLocaleDateString('pt-PT'),
    },
    {
      field: 'amount',
      headerName: 'Amount',
      type: 'number',
      width: 150,
      renderCell: ({ value, row }) => {
        const color = row.type === 'INCOME' ? 'green' : row.type === 'SAVING' ? '#ed6c02' : 'red';
        const sign = row.type === 'INCOME' ? '+' : '-';
        return (
          <span style={{ color, fontWeight: 'bold' }}>
            {sign}{formatCurrency(value)}
          </span>
        );
      },
    },
    {
      field: 'type',
      headerName: 'Type',
      width: 150,
      renderCell: ({ value }) => (
        <Chip
          label={kindLabel(value)}
          color={kindColor(value)}
          size="small"
        />
      ),
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 150,
      sortable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={1}>
          <IconButton size="small" onClick={() => handleOpenEdit(params.row)}>
            <EditIcon />
          </IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.id)}>
            <DeleteIcon />
          </IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <>
      <Navbar />
      <Toolbar />

      <Container maxWidth={false} disableGutters sx={{ mt: 4, mb: 4, px: 3 }}>
        <Paper sx={{ p: 3 }}>
          {/* Cabeçalho com título e botão */}
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
            <Typography variant="h5">Transactions</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleOpenCreate}
            >
              New Transaction
            </Button>
          </Stack>

          {/* Filtros */}
          <Stack direction="row" justifyContent="space-between" sx={{ mb: 2 }}>
            <ToggleButtonGroup
              value={filterType}
              exclusive
              onChange={(e, val) => val && setFilterType(val)}
            >
              <ToggleButton value="all">All</ToggleButton>
              <ToggleButton value="INCOME">Income</ToggleButton>
              <ToggleButton value="EXPENSE">Expense</ToggleButton>
            </ToggleButtonGroup>

            <Stack direction="row" spacing={2}>
              <FormControl sx={{ minWidth: 150 }}>
                <InputLabel>Period</InputLabel>
                <Select
                  value={dateFilter}
                  label="Period"
                  onChange={(e) => setDateFilter(e.target.value)}
                >
                  <MenuItem value="current_month">Current Month</MenuItem>
                  <MenuItem value="past_month">Past Month</MenuItem>
                  <MenuItem value="current_year">Current Year</MenuItem>
                  <MenuItem value="past_year">Past Year</MenuItem>
                </Select>
              </FormControl>

              <FormControl sx={{ minWidth: 150 }}>
                <InputLabel>Category</InputLabel>
                <Select
                  value={categoryFilter}
                  label="Category"
                  onChange={(e) => setCategoryFilter(e.target.value)}
                >
                  <MenuItem value="all">All Categories</MenuItem>
                  {categories.map(cat => (
                    <MenuItem key={cat.id} value={cat.id}>{cat.name}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Stack>
          </Stack>

          {/* Conteúdo */}
          {loading && (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 5 }}>
              <CircularProgress />
            </Box>
          )}

          {error && <Alert severity="error">{error}</Alert>}

          {!loading && !error && (
            <Box sx={{ height: 500 }}>
              <DataGrid
                rows={transactions}
                columns={columns}
                paginationModel={paginationModel}
                onPaginationModelChange={setPaginationModel}
                pageSizeOptions={[5, 10, 20]}
                checkboxSelection
              />
            </Box>
          )}
        </Paper>
      </Container>

      {/* Diálogo de Nova Transação */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingTransaction ? 'Edit Transaction' : 'New Transaction'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Description"
              name="description"
              value={newTransaction.description}
              onChange={handleInputChange}
              fullWidth
              required
            />
            <TextField
              label="Amount"
              name="amount"
              type="number"
              value={newTransaction.amount}
              onChange={handleInputChange}
              fullWidth
              required
              inputProps={{ step: '0.01' }}
            />
            <TextField
              label="Date"
              name="date"
              type="date"
              value={newTransaction.date}
              onChange={handleInputChange}
              fullWidth
              required
              InputLabelProps={{ shrink: true }}
            />
            <FormControl fullWidth required>
              <InputLabel>Category</InputLabel>
              <Select
                name="categoryId"
                value={newTransaction.categoryId}
                onChange={handleInputChange}
                label="Category"
              >
              {categories.map(cat => (
                <MenuItem key={cat.id} value={cat.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {cat.kind === 'SAVING' && <SavingsIcon fontSize="small" sx={{ mr: 1 }} />}
                    {cat.name} ({kindLabel(cat.kind)})
                  </Box>
                </MenuItem>
              ))}
              </Select>
            </FormControl>
            {formError && <Alert severity="error">{formError}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmitTransaction} variant="contained">
            {editingTransaction ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}