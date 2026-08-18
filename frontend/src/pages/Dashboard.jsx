import { useState, useEffect } from 'react';
import {
  Container,
  Paper,
  Toolbar,
  Typography,
  Stack,
  Box,
  ToggleButtonGroup,
  ToggleButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  List,
  ListItem,
  ListItemText,
  Divider,
  Chip,
  CircularProgress,
  Alert,
  Grid
} from '@mui/material';
import { PieChart } from '@mui/x-charts/PieChart';
import Navbar from '../components/Navbar';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import SavingsIcon from '@mui/icons-material/Savings';
import { getTransactions } from '../api/transactions';
import { getCategories } from '../api/categories';
import { getDateRange, formatCurrency } from '../utils/finance';

export default function Dashboard() {
  // Estados
  const [incomeExpense, setIncomeExpense] = useState('income'); // 'income' ou 'expense' para o gráfico
  const [dateFilter, setDateFilter] = useState('current_month');
  const [categories, setCategories] = useState([]);
  const [allTransactions, setAllTransactions] = useState([]); // todas as transações (sem filtro)
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Buscar dados iniciais
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [catsRes, transRes] = await Promise.all([
          getCategories(),
          getTransactions()
        ]);
        setCategories(catsRes.data);
        setAllTransactions(transRes.data);
      } catch (err) {
        setError('Error loading data. Please try again.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  // Filtrar transações por período
  const { start, end } = getDateRange(dateFilter);
  const periodTransactions = allTransactions.filter(t => {
    if (!start || !end) return true;
    const tDate = new Date(t.date);
    return tDate >= new Date(start) && tDate <= new Date(end);
  });


  // Replace the existing calculations with these:
  const savingsTransactions = allTransactions.filter(t => t.type === 'SAVING');

  // For period calculations, use regular (non-savings) transactions only
  const regularPeriodTransactions = periodTransactions.filter(t => t.type !== 'SAVING');
  const periodIncome = regularPeriodTransactions
    .filter(t => t.type === 'INCOME')
    .reduce((acc, t) => acc + t.amount, 0);
  const periodExpense = regularPeriodTransactions
    .filter(t => t.type === 'EXPENSE')
    .reduce((acc, t) => acc + t.amount, 0);
  const periodBalance = periodIncome - periodExpense;

  // All-time savings total (independent of period)
  const totalSavings = savingsTransactions.reduce((acc, t) => acc + t.amount, 0);

  // Dados para o gráfico (baseado no período e no tipo selecionado)
  const chartTransactions = periodTransactions.filter(t =>
    t.type === (incomeExpense === 'income' ? 'INCOME' : 'EXPENSE')
  );
  const categoryMap = {};
  chartTransactions.forEach(t => {
    const catName = t.categoryName || 'Other';
    categoryMap[catName] = (categoryMap[catName] || 0) + t.amount;
  });
  const pieData = Object.entries(categoryMap).map(([label, value], index) => {
    const category = categories.find(c => c.name === label);
    return {
      id: index,
      label,
      value,
      color: category?.color || `hsl(${index * 60}, 70%, 50%)`,
    };
  });
  const totalChart = chartTransactions.reduce((acc, t) => acc + t.amount, 0);

  // Últimas 5 transações do período (ordenadas por data)
  const recentTransactions = [...periodTransactions]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 5);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 20 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Container sx={{ mt: 10 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <>
      <Navbar />
      <Toolbar />

      <Container sx={{ mt: 10, mb: 4 }}>
        {/* Cards de estatísticas */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item size="grow">
            <Paper sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%' }}>
                <AccountBalanceWalletIcon color="primary" sx={{ fontSize: 40, width: 40 }} />
                <Stack direction="column" alignItems="center" sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" color="text.secondary">Balance</Typography>
                  <Typography variant="h4" sx={{ fontWeight: 'bold' }}>{formatCurrency(periodBalance)}</Typography>
                </Stack>
              </Stack>
            </Paper>
          </Grid>
          <Grid item size="grow">
            <Paper sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%' }}>
                <TrendingUpIcon color="success" sx={{ fontSize: 40, width: 40 }} />
                <Stack direction="column" alignItems="center" sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" color="text.secondary">Income</Typography>
                  <Typography variant="h4" sx={{ fontWeight: 'bold' }}>{formatCurrency(periodIncome)}</Typography>
                </Stack>
              </Stack>
            </Paper>
          </Grid>
          <Grid item size="grow">
            <Paper sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%' }}>
                <TrendingDownIcon color="error" sx={{ fontSize: 40, width: 40 }} />
                <Stack direction="column" alignItems="center" sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" color="text.secondary">Expense</Typography>
                  <Typography variant="h4" sx={{ fontWeight: 'bold' }}>{formatCurrency(periodExpense)}</Typography>
                </Stack>
              </Stack>
            </Paper>
          </Grid>
          <Grid item size="grow">
            <Paper sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
              <Stack direction="row" alignItems="center" spacing={1} sx={{ width: '100%' }}>
                <SavingsIcon color="warning" sx={{ fontSize: 40, width: 40 }} />
                  <Stack direction="column" alignItems="center" sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" color="text.secondary">Savings (All time)</Typography>
                    <Typography variant="h4" sx={{ fontWeight: 'bold' }}>{formatCurrency(totalSavings)}</Typography>
                </Stack>
              </Stack>
            </Paper>
          </Grid>
        </Grid>

        {/* Container inferior com dois painéis */}
        <Box sx={{ display: 'flex', gap: 3, mt: 5, flexWrap: 'wrap' }}>
          {/* Painel esquerdo flexível: Finance Overview */}
          <Box sx={{ flex: 1, minWidth: '300px' }}>
            <Paper sx={{ p: 4, display: 'flex', flexDirection: 'column' }}>
              <Typography variant="h5" sx={{ mb: 2 }}>Finance Overview</Typography>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3, flexWrap: 'wrap', gap: 2 }}>
                <ToggleButtonGroup
                  color="primary"
                  value={incomeExpense}
                  exclusive
                  onChange={(e, newValue) => newValue && setIncomeExpense(newValue)}
                  size="medium"
                >
                  <ToggleButton value="income">Income</ToggleButton>
                  <ToggleButton value="expense">Expense</ToggleButton>
                </ToggleButtonGroup>

                <FormControl size="medium" sx={{ minWidth: 150 }}>
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
              </Stack>

              {chartTransactions.length === 0 ? (
                <Alert severity="info">Add transactions of this type to see your statistics.</Alert>
              ) : (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                  <Box sx={{ flexShrink: 0 }}>
                    <PieChart
                      series={[
                        {
                          data: pieData,
                          highlightScope: { faded: 'global', highlighted: 'item' },
                          faded: { innerRadius: 30, additionalRadius: -30, color: 'gray' },
                        },
                      ]}
                      width={300}
                      height={300}
                      slotProps={{ legend: { hidden: true } }}
                    />
                  </Box>

                  <Box sx={{ flexGrow: 1, minWidth: 200 }}>
                    <Typography variant="h6" gutterBottom>Categories</Typography>
                    <List dense>
                      {pieData.map((cat) => {
                        const percentage = ((cat.value / totalChart) * 100).toFixed(1);
                        return (
                          <ListItem key={cat.id} disableGutters>
                            <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
                              <Box sx={{ width: 20, height: 20, borderRadius: '50%', bgcolor: cat.color, mr: 1.5 }} />
                              <Typography variant="body2" sx={{ flexGrow: 1 }}>{cat.label}</Typography>
                              <Typography variant="body2" fontWeight="bold">{percentage}%</Typography>
                            </Box>
                          </ListItem>
                        );
                      })}
                    </List>
                  </Box>
                </Box>
              )}
            </Paper>
          </Box>

          {/* Painel direito: Últimas Transações do período */}
          <Box sx={{ width: 320, minWidth: '280px' }}>
            <Paper sx={{ p: 3, display: 'flex', flexDirection: 'column' }}>
              <Typography variant="h5" gutterBottom>Last Transactions</Typography>
              {recentTransactions.length === 0 ? (
                <Alert severity="info">No transactions in this period.</Alert>
              ) : (
                <List>
                  {recentTransactions.map((tx, index) => (
                    <Box key={tx.id}>
                      <ListItem alignItems="flex-start" disableGutters>
                        <ListItemText
                          primary={tx.description}
                          secondary={new Date(tx.date).toLocaleDateString('pt-PT')}
                        />
                        <Chip
                          label={formatCurrency(tx.amount)}
                          color={tx.type === 'INCOME' ? 'success' : 'error'}
                          size="medium"
                          variant="outlined"
                        />
                      </ListItem>
                      {index < recentTransactions.length - 1 && <Divider component="li" sx={{ my: 1 }} />}
                    </Box>
                  ))}
                </List>
              )}
            </Paper>
          </Box>
        </Box>
      </Container>
    </>
  );
}