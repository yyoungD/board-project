import axios from 'axios';
import React from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const emptyForm = {
  title: '',
  author: '',
  content: ''
};

function formatDateTime(value) {
  if (!value) {
    return '-';
  }

  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value));
}

function getErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message || error.message || fallbackMessage;
}

function App() {
  const [posts, setPosts] = React.useState([]);
  const [form, setForm] = React.useState(emptyForm);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isSaving, setIsSaving] = React.useState(false);
  const [message, setMessage] = React.useState('');

  async function loadPosts() {
    setIsLoading(true);
    setMessage('');

    try {
      const response = await axios.get('/api/posts');
      setPosts(response.data);
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 목록을 불러오지 못했습니다.'));
    } finally {
      setIsLoading(false);
    }
  }

  React.useEffect(() => {
    loadPosts();
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((currentForm) => ({
      ...currentForm,
      [name]: value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    try {
      await axios.post('/api/posts', form);
      setForm(emptyForm);
      await loadPosts();
      setMessage('게시글이 저장되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 저장에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <main className="app-shell">
      <section className="page-header">
        <div>
          <h1>게시판</h1>
        </div>
        <button className="secondary-button" type="button" onClick={loadPosts}>
          새로고침
        </button>
      </section>

      <section className="board-layout">
        <form className="post-form" onSubmit={handleSubmit}>
          <h2>글 작성</h2>

          <label>
            제목
            <input
              name="title"
              value={form.title}
              onChange={handleChange}
              maxLength="200"
              required
            />
          </label>

          <label>
            작성자
            <input
              name="author"
              value={form.author}
              onChange={handleChange}
              maxLength="100"
              required
            />
          </label>

          <label>
            내용
            <textarea
              name="content"
              value={form.content}
              onChange={handleChange}
              rows="8"
              required
            />
          </label>

          <button className="primary-button" type="submit" disabled={isSaving}>
            {isSaving ? '저장 중' : '저장'}
          </button>
        </form>

        <section className="post-list" aria-live="polite">
          <div className="list-toolbar">
            <h2>게시글 목록</h2>
            <span>{posts.length}개</span>
          </div>

          {message && <p className="status-message">{message}</p>}

          {isLoading ? (
            <p className="empty-message">불러오는 중입니다.</p>
          ) : posts.length === 0 ? (
            <p className="empty-message">아직 등록된 게시글이 없습니다.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>제목</th>
                    <th>작성자</th>
                    <th>내용</th>
                    <th>작성일시</th>
                  </tr>
                </thead>
                <tbody>
                  {posts.map((post) => (
                    <tr key={post.id}>
                      <td>{post.title}</td>
                      <td>{post.author}</td>
                      <td>{post.content}</td>
                      <td>{formatDateTime(post.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
