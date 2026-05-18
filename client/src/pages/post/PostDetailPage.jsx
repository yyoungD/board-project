import React from 'react';
import { MoreVertical } from 'lucide-react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { deletePost, getErrorMessage, getPost } from '../../api/posts.js';
import AttachmentFileIcon from '../../components/AttachmentFileIcon.jsx';
import CommentSection from '../../components/CommentSection.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import { getApiLoadError } from '../../utils/apiError.js';
import { formatDateTime } from '../../utils/date.js';

function PostDetailPage({ member }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = React.useState(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isDeleting, setIsDeleting] = React.useState(false);
  const [isPostMenuOpen, setIsPostMenuOpen] = React.useState(false);
  const [message, setMessage] = React.useState('');
  const [loadError, setLoadError] = React.useState(null);

  const isOwner = Boolean(member && post && post.author === member.loginId);

  React.useEffect(() => {
    async function loadPost() {
      setIsLoading(true);
      setMessage('');
      setLoadError(null);

      try {
        const data = await getPost(id);
        setPost(data);
      } catch (error) {
        setPost(null);
        setLoadError(getPostLoadError(error));
      } finally {
        setIsLoading(false);
      }
    }

    loadPost();
  }, [id]);

  function togglePostMenu() {
    setIsPostMenuOpen((currentValue) => !currentValue);
  }

  async function handleDelete() {
    const confirmed = window.confirm('게시글을 삭제하시겠습니까?');
    if (!confirmed) {
      return;
    }

    setIsDeleting(true);
    setIsPostMenuOpen(false);
    setMessage('');

    try {
      await deletePost(id);
      navigate('/');
    } catch (error) {
      setMessage(getErrorMessage(error, '게시글 삭제에 실패했습니다.'));
      setIsDeleting(false);
    }
  }

  if (!isLoading && loadError?.status === 404) {
    return (
      <section className="page-section">
        <ErrorState
          eyebrow="404 Not Found"
          title={loadError.title}
          message={loadError.message}
          actionLabel="목록으로 이동"
        />
      </section>
    );
  }

  return (
    <section className="page-section">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 상세</p>
          <h1>{post?.title || '게시글'}</h1>
        </div>
        <div className="page-actions">
          {isOwner && (
            <div className="post-menu">
              <button
                className="post-menu-button"
                type="button"
                aria-label="게시글 메뉴"
                aria-expanded={isPostMenuOpen}
                onClick={togglePostMenu}
              >
                <MoreVertical size={20} aria-hidden="true" />
              </button>
              {isPostMenuOpen && (
                <div className="post-menu-dropdown">
                  <Link to={`/posts/${id}/edit`} onClick={() => setIsPostMenuOpen(false)}>
                    수정
                  </Link>
                  <button type="button" onClick={handleDelete} disabled={isDeleting}>
                    {isDeleting ? '삭제 중' : '삭제'}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {message && <p className="status-message">{message}</p>}

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : loadError ? (
        <ErrorState
          title={loadError.title}
          message={loadError.message}
          className="post-error-state"
        />
      ) : (
        <>
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
              <div className="post-meta-views">
                <dt>조회수</dt>
                <dd>{post.viewCount}</dd>
              </div>
            </dl>
            <div
              className="post-content rich-content"
              dangerouslySetInnerHTML={{ __html: post.content }}
            />
            {post.files?.length > 0 && (
              <section className="post-attachments">
                <h2>첨부파일</h2>
                <ul className="attachment-list">
                  {post.files.map((file) => (
                    <li key={file.id}>
                      <AttachmentFileIcon file={file} />
                      <a href={`/api/uploads/files/${file.id}`}>{file.originalName}</a>
                      <small>{formatFileSize(file.fileSize || 0)}</small>
                    </li>
                  ))}
                </ul>
              </section>
            )}
          </article>
          <CommentSection postId={id} member={member} />
        </>
      )}

      {!loadError && (
        <Link className="wide-list-link detail-list-link" to="/">
          목록
        </Link>
      )}
    </section>
  );
}

function getPostLoadError(error) {
  return getApiLoadError(error, {
    notFound: {
      status: 404,
      title: '게시글을 찾을 수 없습니다.',
      message: '삭제되었거나 존재하지 않는 게시글입니다.'
    },
    server: {
      title: '게시글을 불러오지 못했습니다.',
      message: '서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.'
    },
    default: {
      title: '게시글을 불러오지 못했습니다.',
      message: getErrorMessage(error, '요청을 처리하는 중 문제가 발생했습니다.')
    }
  });
}

function formatFileSize(size) {
  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

export default PostDetailPage;
