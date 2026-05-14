import React from 'react';
import { User } from 'lucide-react';
import { Link, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import LoginPage from './pages/member/LoginPage.jsx';
import MyPage from './pages/member/MyPage.jsx';
import SignupPage from './pages/member/SignupPage.jsx';
import { logout } from './api/members.js';
import CreatePostPage from './pages/post/CreatePostPage.jsx';
import EditPostPage from './pages/post/EditPostPage.jsx';
import PostDetailPage from './pages/post/PostDetailPage.jsx';
import PostListPage from './pages/post/PostListPage.jsx';

function getSavedMember() {
  const savedMember = localStorage.getItem('member');
  return savedMember ? JSON.parse(savedMember) : null;
}

function App() {
  const location = useLocation();
  const navigate = useNavigate();
  const [member, setMember] = React.useState(getSavedMember);

  function saveAuth(authResponse) {
    localStorage.setItem('member', JSON.stringify(authResponse.member));
    localStorage.setItem('token', authResponse.token);
    setMember(authResponse.member);
  }

  React.useEffect(() => {
    function handleAuthChange(event) {
      setMember(event.detail);
    }

    function handleAuthLogout() {
      setMember(null);
    }

    window.addEventListener('auth:change', handleAuthChange);
    window.addEventListener('auth:logout', handleAuthLogout);

    return () => {
      window.removeEventListener('auth:change', handleAuthChange);
      window.removeEventListener('auth:logout', handleAuthLogout);
    };
  }, []);

  async function handleLogout() {
    try {
      await logout();
    } catch {
      // Local logout should still happen even if the server session is already gone.
    }
    localStorage.removeItem('member');
    localStorage.removeItem('token');
    setMember(null);
    navigate('/');
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <Link className="brand" to="/">
          게시판
        </Link>
        <nav>
          {member ? (
            <>
              <Link className="nav-icon-link" to="/mypage">
                <User size={17} aria-hidden="true" />
                <span>My Page</span>
              </Link>
              <button className="nav-text-button" type="button" onClick={handleLogout}>
                로그아웃
              </button>
            </>
          ) : (
            <>
              <Link to="/login">로그인</Link>
              <Link to="/signup">회원가입</Link>
            </>
          )}
        </nav>
      </header>

      <div key={location.pathname} className="route-view">
        <Routes>
          <Route path="/" element={<PostListPage member={member} />} />
          <Route path="/login" element={<LoginPage onLogin={saveAuth} />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/mypage" element={<MyPage member={member} onAuthChange={saveAuth} onLogout={handleLogout} />} />
          <Route path="/posts/new" element={<CreatePostPage member={member} />} />
          <Route path="/posts/:id" element={<PostDetailPage member={member} />} />
          <Route path="/posts/:id/edit" element={<EditPostPage member={member} />} />
        </Routes>
      </div>
    </main>
  );
}

export default App;
