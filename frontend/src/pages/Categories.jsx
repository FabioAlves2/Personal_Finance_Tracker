import React, { useState, useEffect, useCallback } from 'react';
import {
  Button,
  Container,
  Paper,
  Toolbar,
  Typography,
  Stack,
  Box,
  CircularProgress,
  Alert,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Grid
} from '@mui/material';
import Navbar from '../components/Navbar';
import { DataGrid } from '@mui/x-data-grid';
import { getCategories, createCategory, updateCategory, deleteCategory } from '../api/categories';
import { kindLabel, kindColor } from '../utils/finance';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';

// Lista de ícones disponíveis
const ICON_OPTIONS = [
  'gift',
  'home',
  'shopping-basket',
  'bus',
  'piggy-bank',
  'restaurant',
  'car',
  'health-and-safety',
  'school',
  'work',
  'savings',
  'account-balance',
  'credit-card',
  'attach-money',
  'money-off'
];

// Componente memoizado para o campo de cor (evita re-renders ao arrastar)
const ColorPickerField = React.memo(({ value, onChange }) => {
  return (
    <TextField
      label="Color"
      name="color"
      type="color"
      value={value}
      onChange={onChange}
      fullWidth
      sx={{ '& input': { height: 50 } }}
    />
  );
});

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    kind: 'INCOME',
    color: '#000000',
    icon: ''
  });
  const [formError, setFormError] = useState('');

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const response = await getCategories();
      setCategories(response.data);
    } catch (err) {
      setError('Error loading categories');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreate = () => {
    setEditingCategory(null);
    setFormData({ name: '', kind: 'INCOME', color: '#000000', icon: '' });
    setFormError('');
    setDialogOpen(true);
  };

  const handleOpenEdit = (cat) => {
    if (cat.isDefault) {
      alert('Global categories cannot be edited.');
      return;
    }
    setEditingCategory(cat);
    setFormData({
      name: cat.name,
      kind: cat.kind,
      color: cat.color || '#000000',
      icon: cat.icon || ''
    });
    setFormError('');
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setEditingCategory(null);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // Handler memoizado para o campo de cor
  const handleColorChange = useCallback((e) => {
    setFormData(prev => ({ ...prev, color: e.target.value }));
  }, []);

  const handleSubmit = async () => {
    if (!formData.name.trim()) {
      setFormError('Name is required');
      return;
    }

    try {
      if (editingCategory) {
        await updateCategory(editingCategory.id, {
          name: formData.name,
          kind: formData.kind,
          color: formData.color,
          icon: formData.icon
        });
      } else {
        await createCategory({
          name: formData.name,
          kind: formData.kind,
          color: formData.color,
          icon: formData.icon
        });
      }
      handleCloseDialog();
      fetchCategories();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Error saving category');
    }
  };

  const handleDelete = async (id, isGlobal) => {
    if (isGlobal) {
      alert('Global categories cannot be deleted.');
      return;
    }
    if (!window.confirm('Are you sure you want to delete this category?')) return;
    try {
      await deleteCategory(id);
      fetchCategories();
    } catch {
      alert('Error deleting category');
    }
  };

  // Separar categorias globais e do utilizador
  const globalCategories = categories.filter(cat => cat.isDefault === true);
  const userCategories = categories.filter(cat => cat.isDefault === false);

  // Colunas para a DataGrid de categorias do utilizador
  const columns = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'name', headerName: 'Name', flex: 1 },
    {
      field: 'kind',
      headerName: 'Type',
      width: 100,
      renderCell: (params) => (
        <Chip
          label={kindLabel(params.value)}
          color={kindColor(params.value)}
          size="small"
        />
      ),
    },
    {
      field: 'color',
      headerName: 'Color',
      width: 80,
      renderCell: (params) => (
        <Box sx={{ width: 20, height: 20, borderRadius: '50%', bgcolor: params.value }} />
      ),
    },
    {
      field: 'icon',
      headerName: 'Icon',
      width: 100,
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 120,
      sortable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={1}>
          <IconButton size="small" onClick={() => handleOpenEdit(params.row)} title="Edit">
            <EditIcon />
          </IconButton>
          <IconButton size="small" color="error" onClick={() => handleDelete(params.id, false)} title="Delete">
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

      <Container maxWidth="xl" sx={{ mt: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
            <Typography variant="h5">Categories</Typography>
            <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenCreate}>
              New Category
            </Button>
          </Stack>

          {loading && <CircularProgress />}
          {error && <Alert severity="error">{error}</Alert>}
          {!loading && !error && (
            <>
              {/* Categorias globais (read‑only) em grid de cards */}
              {globalCategories.length > 0 && (
                <Box sx={{ mb: 4 }}>
                  <Typography variant="h6" gutterBottom>Global Categories (read‑only)</Typography>
                  <Grid container spacing={2}>
                    {globalCategories.map(cat => (
                      <Grid item xs={6} sm={4} md={3} key={cat.id}>
                        <Paper elevation={1} sx={{ p: 1.5, display: 'flex', alignItems: 'center' }}>
                          <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: cat.color, mr: 1.5 }} />
                          <Box sx={{ flex: 1 }}>
                            <Typography variant="body2" fontWeight="medium">{cat.name}</Typography>
                            <Chip
                              label={kindLabel(cat.kind)}
                              color={kindColor(cat.kind)}
                              size="small"
                              sx={{ mt: 0.5 }}
                            />
                          </Box>
                        </Paper>
                      </Grid>
                    ))}
                  </Grid>
                </Box>
              )}

              {/* Categorias do utilizador (editáveis) */}
              <Typography variant="h6" gutterBottom>Your Categories</Typography>
              <Box sx={{ height: 400 }}>
                <DataGrid
                  rows={userCategories}
                  columns={columns}
                  pageSizeOptions={[10, 20]}
                  disableRowSelectionOnClick
                />
              </Box>
            </>
          )}
        </Paper>
      </Container>

      {/* Diálogo de criação/edição */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingCategory ? 'Edit Category' : 'New Category'}</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              fullWidth
              required
            />
            <FormControl fullWidth>
              <InputLabel>Type</InputLabel>
              <Select name="kind" value={formData.kind} onChange={handleChange} label="Type">
                <MenuItem value="INCOME">Income</MenuItem>
                <MenuItem value="EXPENSE">Expense</MenuItem>
                <MenuItem value="SAVING">Savings</MenuItem>
              </Select>
            </FormControl>
            {/* Campo de cor memoizado */}
            <ColorPickerField value={formData.color} onChange={handleColorChange} />
            <FormControl fullWidth>
              <InputLabel>Icon</InputLabel>
              <Select name="icon" value={formData.icon} onChange={handleChange} label="Icon">
                <MenuItem value=""><em>None</em></MenuItem>
                {ICON_OPTIONS.map(icon => (
                  <MenuItem key={icon} value={icon}>{icon}</MenuItem>
                ))}
              </Select>
            </FormControl>
            {formError && <Alert severity="error">{formError}</Alert>}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained">
            {editingCategory ? 'Save' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}