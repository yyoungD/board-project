import React from 'react';
import { Link, Route, Routes, useNavigate } from 'react-router-dom';
import CreatePostPage from './pages/CreatePostPage.jsx';
import EditPostPage from './pages/EditPostPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import MyPage from './pages/MyPage.jsx';
import PostDetailPage from './pages/PostDetailPage.jsx';
import PostListPage from './pages/PostListPage.jsx';
import SignupPage from './pages/SignupPage.jsx';

function getSavedMember() {
  const savedMember = localStorage.getItem('member');
  return savedMember ? JSON.parse(savedMember) : null;
}

function App() {
  const navigate = useNavigate();
  const [member, setMember] = React.useState(getSavedMember);

  function saveAuth(authResponse) {
    localStorage.setItem('member', JSON.stringify(authResponse.member));
    localStorage.setItem('token', authResponse.token);
    setMember(authResponse.member);
  }

  function handleLogout() {
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
          <Link to="/">목록</Link>
          {member ? (
            <>
              <Link to="/mypage">마이페이지</Link>
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

      <Routes>
        <Route path="/" element={<PostListPage member={member} />} />
        <Route path="/login" element={<LoginPage onLogin={saveAuth} />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/mypage" element={<MyPage member={member} onAuthChange={saveAuth} onLogout={handleLogout} />} />
        <Route path="/posts/new" element={<CreatePostPage member={member} />} />
        <Route path="/posts/:id" element={<PostDetailPage member={member} />} />
        <Route path="/posts/:id/edit" element={<EditPostPage member={member} />} />
      </Routes>
    </main>
  );
}

export default App;
