import React from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getErrorMessage, getPost, updatePost } from '../../api/posts.js';
import PostForm from '../../components/PostForm.jsx';
import { normalizeHtmlLinks } from '../../utils/links.js';

const emptyForm = {
  title: '',
  author: '',
  content: ''
};

function EditPostPage({ member }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [files, setFiles] = React.useState([]);
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
        setFiles(post.files || []);
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
        content: normalizeHtmlLinks(form.content),
        fileIds: files.map((file) => file.id)
      });
      navigate(`/posts/${updatedPost.id}`);
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 수정에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="page-section">
      <div className="page-title-row">
        <div>
          <h1>게시글 수정</h1>
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
        <PostForm
          form={form}
          author={form.author}
          files={files}
          onFieldChange={handleChange}
          onContentChange={handleContentChange}
          onFilesChange={setFiles}
          onError={setMessage}
          onSubmit={handleSubmit}
          message={message}
          isSaving={isSaving}
          cancelTo={`/posts/${id}`}
          submitLabel="수정"
          savingLabel="수정 중"
        />
      )}
    </section>
  );
}

export default EditPostPage;
