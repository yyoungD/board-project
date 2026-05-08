import React from 'react';
import { Link } from 'react-router-dom';
import { getErrorMessage, getPosts } from '../../api/posts.js';
import Pagination from '../../components/Pagination.jsx';
import { formatDateTime } from '../../utils/date.js';

const pageSize = 10;

function PostListPage({ member }) {
  const [posts, setPosts] = React.useState([]);
  const [page, setPage] = React.useState(1);
  const [totalPages, setTotalPages] = React.useState(0);
  const [totalElements, setTotalElements] = React.useState(0);
  const [isLoading, setIsLoading] = React.useState(true);
  const [message, setMessage] = React.useState('');

  React.useEffect(() => {
    async function loadPosts() {
      setIsLoading(true);
      setMessage('');

      try {
        const data = await getPosts(page, pageSize);
        setPosts(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (error) {
        setMessage(getErrorMessage(error, '게시글 목록을 불러오지 못했습니다.'));
      } finally {
        setIsLoading(false);
      }
    }

    loadPosts();
  }, [page]);

  return (
    <section className="page-section">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">게시글 목록</p>
          <h1>전체 게시글</h1>
        </div>
        {member && (
          <Link className="primary-link" to="/posts/new">
            글쓰기
          </Link>
        )}
      </div>

      {message && <p className="status-message">{message}</p>}

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : posts.length === 0 ? (
        <>
          <div className="empty-state">
            <p>아직 등록된 게시글이 없습니다.</p>
            {member && (
              <Link className="primary-link" to="/posts/new">
                첫 글 작성
              </Link>
            )}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      ) : (
        <>
          <div className="list-summary">총 {totalElements}개</div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>제목</th>
                  <th>작성자</th>
                  <th>작성일시</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post) => (
                  <tr key={post.id}>
                    <td>
                      <Link className="title-link" to={`/posts/${post.id}`}>
                        {post.title}
                      </Link>
                    </td>
                    <td>{post.author}</td>
                    <td>{formatDateTime(post.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </section>
  );
}

export default PostListPage;
