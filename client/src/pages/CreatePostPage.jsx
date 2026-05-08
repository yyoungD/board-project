import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createPost, getErrorMessage } from '../api/posts.js';
import RichTextEditor from '../components/RichTextEditor.jsx';

const emptyForm = {
  title: '',
  content: ''
};

function CreatePostPage({ member }) {
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [isSaving, setIsSaving] = React.useState(false);
  const [message, setMessage] = React.useState('');

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((currentForm) => ({
      ...currentForm,
      [name]: value
    }));
  }

  function handleContentChange(content) {
    setForm((currentForm) => ({
      ...currentForm,
      content
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    if (!member) {
      setMessage('로그인 후 글을 작성할 수 있습니다.');
      setIsSaving(false);
      return;
    }

    try {
      const createdPost = await createPost({
        title: form.title,
        author: member.loginId,
        content: form.content
      });
      navigate(`/posts/${createdPost.id}`);
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 저장에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  if (!member) {
    return (
      <section className="page-section narrow-page">
        <p className="empty-message">로그인 후 글을 작성할 수 있습니다.</p>
        <div className="form-actions">
          <Link className="secondary-link" to="/">
            목록
          </Link>
          <Link className="primary-link" to="/login">
            로그인
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 작성</p>
          <h1>새 글 쓰기</h1>
        </div>
      </div>

      <form className="post-form" onSubmit={handleSubmit}>
        {message && <p className="status-message">{message}</p>}

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

        <div className="field-block">
          <span className="field-label">작성자</span>
          <span className="author-display">{member.loginId}</span>
        </div>

        <div className="field-block">
          <span className="field-label">내용</span>
          <RichTextEditor value={form.content} onChange={handleContentChange} />
        </div>

        <div className="form-actions">
          <Link className="secondary-link" to="/">
            취소
          </Link>
          <button className="primary-button" type="submit" disabled={isSaving}>
            {isSaving ? '저장 중' : '저장'}
          </button>
        </div>
      </form>
    </section>
  );
}

export default CreatePostPage;
