import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createPost, getErrorMessage } from '../api/posts.js';

const emptyForm = {
  title: '',
  author: '',
  content: ''
};

function CreatePostPage() {
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

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    try {
      const createdPost = await createPost(form);
      navigate(`/posts/${createdPost.id}`);
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 저장에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 작성</p>
          <h1>새 글 쓰기</h1>
        </div>
        <Link className="secondary-link" to="/">
          목록
        </Link>
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
            rows="12"
            required
          />
        </label>

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
