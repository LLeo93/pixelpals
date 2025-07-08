import './App.css';
import './index.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthPage from './pages/AuthPages';
import ProfilePage from './pages/ProfilePage'; // importa la pagina profilo

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AuthPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/dashboard" element={<div>Benvenuto!</div>} />
      </Routes>
    </Router>
  );
}

export default App;
