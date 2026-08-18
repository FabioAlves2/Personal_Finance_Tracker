import { useState } from 'react';
import {
  Button,
  Container,
  Paper,
  Toolbar,
  Typography,
  Stack,
  Box,
  TextField,
  Alert,
  CircularProgress,
  Divider,
  Switch,
  FormControlLabel,
  Grid
} from '@mui/material';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { updateProfile, changePassword } from '../api/user';

export default function Profile() {
  const { user, updateUser } = useAuth();

  // Estados para atualização do nome
  const [name, setName] = useState(user?.name || '');
  const [nameLoading, setNameLoading] = useState(false);
  const [nameSuccess, setNameSuccess] = useState(false);
  const [nameError, setNameError] = useState('');

  // Estados para alteração da palavra‑passe
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState(false);
  const [passwordError, setPasswordError] = useState('');

  // Preferências 
  const [darkMode, setDarkMode] = useState(false);
  const [emailNotifications, setEmailNotifications] = useState(true);

    const handleNameSubmit = async (e) => {
    e.preventDefault();
    setNameLoading(true);
    setNameError('');
    setNameSuccess(false);
    try {
        await updateProfile(name);
        updateUser({ ...user, name });
        setNameSuccess(true);
    } catch (err) {
        setNameError(err.response?.data?.message || 'Error updating name');
    } finally {
        setNameLoading(false);
    }
    };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setPasswordError('Passwords do not match');
      return;
    }
    setPasswordLoading(true);
    setPasswordError('');
    setPasswordSuccess(false);
    try {
      await changePassword(currentPassword, newPassword);
      setPasswordSuccess(true);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Error changing password');
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <>
      <Navbar />
      <Toolbar />

      <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
        <Paper sx={{ p: 4 }}>
          <Typography variant="h4" gutterBottom>Profile Settings</Typography>

          {/* Informação da conta (apenas leitura) */}
          <Box sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>Account Information</Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Email"
                  value={user?.email || ''}
                  fullWidth
                  disabled
                  variant="filled"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Member since"
                  value="Not available"
                  fullWidth
                  disabled
                  variant="filled"
                />
              </Grid>
            </Grid>
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* Atualizar nome */}
          <Box component="form" onSubmit={handleNameSubmit} sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>Update Name</Typography>
            <Stack direction="row" spacing={2} alignItems="flex-start">
              <TextField
                label="Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                fullWidth
                required
                disabled={nameLoading}
              />
              <Button
                type="submit"
                variant="contained"
                disabled={nameLoading || name === user?.name}
                sx={{ height: 56 }}
              >
                {nameLoading ? <CircularProgress size={24} /> : 'Update'}
              </Button>
            </Stack>
            {nameSuccess && <Alert severity="success" sx={{ mt: 1 }}>Name updated successfully!</Alert>}
            {nameError && <Alert severity="error" sx={{ mt: 1 }}>{nameError}</Alert>}
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* Alterar palavra‑passe */}
          <Box component="form" onSubmit={handlePasswordSubmit} sx={{ mb: 4 }}>
            <Typography variant="h6" gutterBottom>Change Password</Typography>
            <Stack spacing={2}>
              <TextField
                label="Current Password"
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                fullWidth
                required
                disabled={passwordLoading}
              />
              <TextField
                label="New Password"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                fullWidth
                required
                disabled={passwordLoading}
              />
              <TextField
                label="Confirm New Password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                fullWidth
                required
                disabled={passwordLoading}
              />
              <Button
                type="submit"
                variant="contained"
                disabled={passwordLoading}
                sx={{ alignSelf: 'flex-start' }}
              >
                {passwordLoading ? <CircularProgress size={24} /> : 'Change Password'}
              </Button>
              {passwordSuccess && <Alert severity="success">Password changed successfully!</Alert>}
              {passwordError && <Alert severity="error">{passwordError}</Alert>}
            </Stack>
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* Preferências (exemplo) */}
          <Box>
            <Typography variant="h6" gutterBottom>Preferences</Typography>
            <FormControlLabel
              control={<Switch checked={darkMode} onChange={(e) => setDarkMode(e.target.checked)} />}
              label="Dark Mode (coming soon)"
              disabled
            />
            <FormControlLabel
              control={<Switch checked={emailNotifications} onChange={(e) => setEmailNotifications(e.target.checked)} />}
              label="Email Notifications (coming soon)"
              disabled
            />
          </Box>
        </Paper>
      </Container>
    </>
  );
}