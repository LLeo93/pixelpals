import './App.css';
import './index.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthPages from './pages/AuthPages';
import ProfilePages from './pages/ProfilePage';
import HomePage from './pages/HomePage';
import VerificationPage from './pages/VerificationPage';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AuthPages />} />
        <Route path="/profile" element={<ProfilePages />} />
        <Route path="/home" element={<HomePage />} />
        <Route
          path="/dashboard"
          element={<div>Benvenuto nella Dashboard!</div>}
        />
        <Route path="/verify" element={<VerificationPage />} />
        <Route path="/admin-dashboard" element={<AdminDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
