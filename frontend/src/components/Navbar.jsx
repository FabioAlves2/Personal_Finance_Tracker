import { AppBar, Button, Toolbar, Typography, Stack } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <AppBar position="fixed">
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          {user?.name ? `Hello, ${user.name}` : 'My Personal Finance'}
        </Typography>
        <Stack direction="row" spacing={1}>
          <Button sx={{ color: 'white' }} onClick={() => navigate('/')}>
            Dashboard
          </Button>
          <Button sx={{ color: 'white' }} onClick={() => navigate('/transactions')}>
            Transactions
          </Button>
          <Button sx={{ color: 'white' }} onClick={() => navigate('/categories')}>
            Categories
          </Button>
          <Button sx={{ color: 'white' }} onClick={() => navigate('/profile')}>
            Profile
          </Button>
          <Button sx={{ color: 'white' }} onClick={logout}>
            Logout
          </Button>
        </Stack>
      </Toolbar>
    </AppBar>
  );
}