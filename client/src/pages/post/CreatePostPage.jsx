import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { createPost, getErrorMessage } from '../../api/posts.js';
import AttachmentFileField from '../../components/AttachmentFileField.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import RichTextEditor from '../../components/RichTextEditor.jsx';

const emptyForm = {
  title: '',
  content: ''
};

function CreatePostPage({ member }) {
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [files, setFiles] = React.useState([]);
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
      setMessage('로그인해야 글을 작성할 수 있습니다.');
      setIsSaving(false);
      return;
    }

    try {
      const createdPost = await createPost({
        title: form.title,
        content: form.content,
        fileIds: files.map((file) => file.id)
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
      <section className="page-section">
        <ErrorState
          eyebrow="401 Unauthorized"
          title="로그인이 필요합니다."
          message="게시글을 작성하려면 먼저 로그인해주세요."
          actionLabel="로그인 페이지로 이동"
          actionTo="/login"
        />
      </section>
    );
  }

  return (
    <section className="page-section">
      <div className="page-title-row">
        <div>
          <h1>게시글 작성</h1>
        </div>...//////
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

        <AttachmentFileField
          files={files}
          onChange={setFiles}
          onError={setMessage}
          disabled={isSaving}
        />

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
