import './App.css';
import './index.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AuthPages from './pages/AuthPages';
import ProfilePages from './pages/ProfilePage';
import HomePage from './pages/HomePage';
import VerificationPage from './pages/VerificationPage';
import AdminDashboard from './pages/AdminDashboard';
import FriendsPage from './pages/FriendsPage';
import SetupProfilePage from './pages/SetupProfilePage';
import MatchmakingPage from './pages/MatchMakingPage';
import ChatPage from './pages/ChatPage';
import { UnreadMessagesProvider } from './components/UnreadMessagesContext';
import MatchRoomPage from './pages/MatchRoomPage';
import PendingMatchesPage from './pages/PendingMatchesPage';
import RateMatchPage from './pages/RateMatchPage';

function App() {
  // DEVI DEFINIRE currentUserUsername QUI PRIMA DI USARLO
  const currentUserUsername = localStorage.getItem('username');

  return (
    <Router>
      <UnreadMessagesProvider currentUserUsername={currentUserUsername}>
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
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="/setup-profile" element={<SetupProfilePage />} />
          <Route path="/matchmaking" element={<MatchmakingPage />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/pending-matches" element={<PendingMatchesPage />} />
          <Route path="/match-room/:matchId" element={<MatchRoomPage />} />
          <Route path="/rate-match/:matchId" element={<RateMatchPage />} />
        </Routes>
      </UnreadMessagesProvider>
    </Router>
  );
}

export default App;
