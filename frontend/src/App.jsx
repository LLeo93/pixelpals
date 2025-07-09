import './App.css';
import './index.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthPages from './pages/AuthPages';
import ProfilePages from './pages/ProfilePage';
import HomePage from './pages/HomePage';
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AuthPages />} />
        <Route path="/profile" element={<ProfilePages />} />
        <Route path="/dashboard" element={<div>Benvenuto!</div>} />
        <Route path="/home" element={<HomePage />} />
      </Routes>
    </Router>
  );
}

export default App;
