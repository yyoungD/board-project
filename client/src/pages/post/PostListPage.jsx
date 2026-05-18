import React from 'react';
import { ImageIcon, Paperclip } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { getPosts } from '../../api/posts.js';
import Pagination from '../../components/Pagination.jsx';
import SearchBar from '../../components/SearchBar.jsx';
import { getApiLoadError } from '../../utils/apiError.js';
import { formatDateTime } from '../../utils/date.js';

const pageSize = 10;

function PostListPage({ member }) {
  const navigate = useNavigate();
  const [posts, setPosts] = React.useState([]);
  const [page, setPage] = React.useState(1);
  const [keyword, setKeyword] = React.useState('');
  const [totalPages, setTotalPages] = React.useState(0);
  const [totalElements, setTotalElements] = React.useState(0);
  const [isLoading, setIsLoading] = React.useState(true);
  const [message, setMessage] = React.useState('');
  const [loadError, setLoadError] = React.useState(null);

  React.useEffect(() => {
    async function loadPosts() {
      setIsLoading(true);
      setMessage('');
      setLoadError(null);

      try {
        const data = await getPosts(page, pageSize, keyword);
        setPosts(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (error) {
        setPosts([]);
        setTotalPages(0);
        setTotalElements(0);
        setLoadError(getPostListLoadError(error));
      } finally {
        setIsLoading(false);
      }
    }

    loadPosts();
  }, [page, keyword]);

  function handleSearch(nextKeyword) {
    setKeyword(nextKeyword);
    setPage(1);
  }

  function openPost(postId) {
    navigate(`/posts/${postId}`);
  }

  function handlePostRowKeyDown(event, postId) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      openPost(postId);
    }
  }

  return (
    <section className="page-section">
      <div className="page-title-row">
        <div>
          <h1 className="list-page-title">전체 게시글</h1>
        </div>
        <div className="list-actions">
          <SearchBar
            initialKeyword={keyword}
            placeholder="제목으로 검색"
            onSearch={handleSearch}
          />
          {member && (
            <Link className="primary-link" to="/posts/new">
              글쓰기
            </Link>
          )}
        </div>
      </div>

      {message && <p className="status-message">{message}</p>}

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : loadError ? (
        <div className="empty-state">
          <h2>{loadError.title}</h2>
          <p>{loadError.message}</p>
        </div>
      ) : posts.length === 0 ? (
        <>
          <div className="empty-state">
            <p>{keyword ? '검색 결과가 없습니다.' : '아직 등록된 게시글이 없습니다.'}</p>
            {member && !keyword && (
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
          <div key={`${keyword}-${page}`} className="table-wrap results-view">
            <table>
              <thead>
                <tr>
                  <th>제목</th>
                  <th>작성자</th>
                  <th>작성일시</th>
                  <th>조회수</th>
                </tr>
              </thead>
              <tbody>
                {posts.map((post) => (
                  <tr
                    className="post-row"
                    key={post.id}
                    role="link"
                    tabIndex={0}
                    onClick={() => openPost(post.id)}
                    onKeyDown={(event) => handlePostRowKeyDown(event, post.id)}
                  >
                    <td>
                      <span className="title-link">
                        <span className="title-text">{highlightSearchTerm(post.title, keyword)}</span>
                        {post.hasImage && (
                          <ImageIcon className="title-status-icon" size={15} aria-label="이미지 포함" />
                        )}
                        {post.hasFile && (
                          <Paperclip className="title-status-icon" size={15} aria-label="첨부파일 포함" />
                        )}
                        {post.commentCount > 0 && (
                          <span className="comment-count">({post.commentCount})</span>
                        )}
                      </span>
                    </td>
                    <td>{post.author}</td>
                    <td>{formatDateTime(post.createdAt)}</td>
                    <td>{post.viewCount}</td>
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

function highlightSearchTerm(text, keyword) {
  const trimmedKeyword = keyword.trim();
  if (!trimmedKeyword) {
    return text;
  }

  const escapedKeyword = trimmedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const pattern = new RegExp(`(${escapedKeyword})`, 'gi');

  return text.split(pattern).map((part, index) => {
    if (part.toLowerCase() === trimmedKeyword.toLowerCase()) {
      return (
        <mark key={`${part}-${index}`} className="search-highlight">
          {part}
        </mark>
      );
    }

    return part;
  });
}

function getPostListLoadError(error) {
  return getApiLoadError(error, {
    notFound: {
      title: '게시글 목록을 찾을 수 없습니다.',
      message: '요청한 목록 주소가 존재하지 않습니다.'
    },
    server: {
      title: '게시글 목록을 불러오지 못했습니다.',
      message: '서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.'
    },
    default: {
      title: '게시글 목록을 불러오지 못했습니다.',
      message: '요청을 처리하는 중 문제가 발생했습니다.'
    }
  });
}

export default PostListPage;
