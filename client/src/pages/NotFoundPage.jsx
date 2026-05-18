import { useLocation } from 'react-router-dom';
import ErrorState from '../components/ErrorState.jsx';

function NotFoundPage() {
  const location = useLocation();

  return (
    <section className="page-section">
      <ErrorState
        eyebrow="404 Not Found"
        title="페이지를 찾을 수 없습니다."
        message={
          <>
            요청하신 주소 <strong>{location.pathname}</strong> 는 존재하지 않거나 이동되었습니다.
          </>
        }
        actionLabel="게시글 목록으로 이동"
      />
    </section>
  );
}

export default NotFoundPage;
