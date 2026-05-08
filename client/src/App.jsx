import { Link, Route, Routes } from 'react-router-dom';
import CreatePostPage from './pages/CreatePostPage.jsx';
import EditPostPage from './pages/EditPostPage.jsx';
import PostDetailPage from './pages/PostDetailPage.jsx';
import PostListPage from './pages/PostListPage.jsx';
import SignupPage from './pages/SignupPage.jsx';

function App() {
  return (
    <main className="app-shell">
      <header className="app-header">
        <Link className="brand" to="/">
          게시판
        </Link>
        <nav>
          <Link to="/">목록</Link>
          <Link to="/signup">회원가입</Link>
          <Link className="nav-button" to="/posts/new">
            글쓰기
          </Link>
        </nav>
      </header>

      <Routes>
        <Route path="/" element={<PostListPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/posts/new" element={<CreatePostPage />} />
        <Route path="/posts/:id" element={<PostDetailPage />} />
        <Route path="/posts/:id/edit" element={<EditPostPage />} />
      </Routes>
    </main>
  );
}

export default App;
