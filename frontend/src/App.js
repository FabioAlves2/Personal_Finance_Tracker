import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider, createTheme } from "@mui/material";
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard'; // vamos criar agora
import Transactions from './pages/Transactions';
import Categories from './pages/Categories';
import Profile from './pages/Profile';
import TokenRefresher from './context/TokenRefresher';

// Componente para proteger rotas que exigem autenticação
const PrivateRoute = ({ children }) => {
  const { token } = useAuth();
  return token ? children : <Navigate to="/login" replace />;
};

const theme = createTheme({
  palette: {
    mode: "light",
    primary: {main: "#08567e"},
    secondary: {main: "#a1a1a1"}
  }
})

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
        <AuthProvider>
          <TokenRefresher />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>}/>
            <Route path="/transactions" element={<PrivateRoute><Transactions /></PrivateRoute>}/>
            <Route path="/categories" element={<PrivateRoute><Categories /></PrivateRoute>} />
            <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
            {/* Outras rotas protegidas podem ser adicionadas aqui */}
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
