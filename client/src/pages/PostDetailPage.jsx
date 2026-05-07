import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { getErrorMessage, getPost } from '../api/posts.js';
import { formatDateTime } from '../utils/date.js';

function PostDetailPage() {
  const { id } = useParams();
  const [post, setPost] = React.useState(null);
  const [isLoading, setIsLoading] = React.useState(true);
  const [message, setMessage] = React.useState('');

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

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 상세</p>
          <h1>{post?.title || '게시글'}</h1>
        </div>
        <Link className="secondary-link" to="/">
          목록
        </Link>
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
          <div className="post-content">{post.content}</div>
        </article>
      ) : (
        <p className="empty-message">게시글을 찾을 수 없습니다.</p>
      )}
    </section>
  );
}

export default PostDetailPage;
