import React from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePost, getErrorMessage, getPost } from '../../api/posts.js';
import { formatDateTime } from '../../utils/date.js';

function PostDetailPage({ member }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = React.useState(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isDeleting, setIsDeleting] = React.useState(false);
  const [message, setMessage] = React.useState('');

  const isOwner = Boolean(member && post && post.author === member.loginId);

  React.useEffect(() => {
    async function loadPost() {
      setIsLoading(true);
      setMessage('');

      try {
        const data = await getPost(id);
        setPost(data);
      } catch (error) {
        setMessage(getErrorMessage(error, '게시글을 불러오지 못했습니다.'));
      } finally {
        setIsLoading(false);
      }
    }

    loadPost();
  }, [id]);

  async function handleDelete() {
    const confirmed = window.confirm('게시글을 삭제하시겠습니까?');
    if (!confirmed) {
      return;
    }

    setIsDeleting(true);
    setMessage('');

    try {
      await deletePost(id);
      navigate('/');
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 삭제에 실패했습니다.'));
      setIsDeleting(false);
    }
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 상세</p>
          <h1>{post?.title || '게시글'}</h1>
        </div>
        <div className="page-actions">
          {isOwner && (
            <>
              <Link className="secondary-link" to={`/posts/${id}/edit`}>
                수정
              </Link>
              <button
                className="danger-button"
                type="button"
                onClick={handleDelete}
                disabled={isDeleting}
              >
                {isDeleting ? '삭제 중' : '삭제'}
              </button>
            </>
          )}
        </div>
      </div>

      {message && <p className="status-message">{message}</p>}

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : post ? (
        <article className="post-detail">
          <dl className="post-meta">
            <div>
              <dt>작성자</dt>
              <dd>{post.author}</dd>
            </div>
            <div>
              <dt>작성일시</dt>
              <dd>{formatDateTime(post.createdAt)}</dd>
            </div>
          </dl>
          <div
            className="post-content rich-content"
            dangerouslySetInnerHTML={{ __html: post.content }}
          />
        </article>
      ) : (
        <p className="empty-message">게시글을 찾을 수 없습니다.</p>
      )}

      <Link className="wide-list-link detail-list-link" to="/">
        목록
      </Link>
    </section>
  );
}

export default PostDetailPage;
