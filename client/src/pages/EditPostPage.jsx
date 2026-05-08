import React from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getErrorMessage, getPost, updatePost } from '../api/posts.js';
import RichTextEditor from '../components/RichTextEditor.jsx';

const emptyForm = {
  title: '',
  author: '',
  content: ''
};

function EditPostPage({ member }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isSaving, setIsSaving] = React.useState(false);
  const [message, setMessage] = React.useState('');
  const [canEdit, setCanEdit] = React.useState(false);

  React.useEffect(() => {
    async function loadPost() {
      setIsLoading(true);
      setMessage('');

      try {
        const post = await getPost(id);
        setForm({
          title: post.title,
          author: post.author,
          content: post.content
        });
        setCanEdit(Boolean(member && post.author === member.loginId));
      } catch (error) {
        setMessage(getErrorMessage(error, '게시글을 불러오지 못했습니다.'));
      } finally {
        setIsLoading(false);
      }
    }

    loadPost();
  }, [id, member]);

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

    if (!canEdit) {
      setMessage('본인이 작성한 글만 수정할 수 있습니다.');
      setIsSaving(false);
      return;
    }

    try {
      const updatedPost = await updatePost(id, {
        title: form.title,
        content: form.content
      });
      navigate(`/posts/${updatedPost.id}`);
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 수정에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 수정</p>
          <h1>글 수정</h1>
        </div>
        <Link className="secondary-link" to={`/posts/${id}`}>
          상세
        </Link>
      </div>

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : !canEdit ? (
        <p className="empty-message">본인이 작성한 글만 수정할 수 있습니다.</p>
      ) : (
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
            <span className="author-display">{form.author}</span>
          </div>

          <div className="field-block">
            <span className="field-label">내용</span>
            <RichTextEditor value={form.content} onChange={handleContentChange} />
          </div>

          <div className="form-actions">
            <Link className="secondary-link" to={`/posts/${id}`}>
              취소
            </Link>
            <button className="primary-button" type="submit" disabled={isSaving}>
              {isSaving ? '수정 중' : '수정'}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}

export default EditPostPage;
