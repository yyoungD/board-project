import React from 'react';
import { useNavigate } from 'react-router-dom';
import { createPost, getErrorMessage } from '../../api/posts.js';
import ErrorState from '../../components/ErrorState.jsx';
import PostForm from '../../components/PostForm.jsx';
import { normalizeHtmlLinks } from '../../utils/links.js';

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
        content: normalizeHtmlLinks(form.content),
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
        </div>
      </div>

      <PostForm
        form={form}
        author={member.loginId}
        files={files}
        onFieldChange={handleChange}
        onContentChange={handleContentChange}
        onFilesChange={setFiles}
        onError={setMessage}
        onSubmit={handleSubmit}
        message={message}
        isSaving={isSaving}
        cancelTo="/"
        submitLabel="저장"
        savingLabel="저장 중"
      />
    </section>
  );
}

export default CreatePostPage;
