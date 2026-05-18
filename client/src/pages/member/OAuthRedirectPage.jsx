import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import ErrorState from '../../components/ErrorState.jsx';

function OAuthRedirectPage({ onLogin }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [hasError, setHasError] = React.useState(false);
  const handledRef = React.useRef(false);

  React.useEffect(() => {
    if (handledRef.current) {
      return;
    }
    handledRef.current = true;

    const token = searchParams.get('token');
    const memberId = searchParams.get('memberId');
    const loginId = searchParams.get('loginId');
    const name = searchParams.get('name');

    if (!token || !memberId || !loginId || !name) {
      setHasError(true);
      return;
    }

    try {
      onLogin({
        token,
        member: {
          id: Number(memberId),
          loginId,
          name,
          phone: searchParams.get('phone') || '',
          createdAt: searchParams.get('createdAt')
        }
      });
      navigate('/', { replace: true });

      window.setTimeout(() => {
        if (window.location.pathname === '/oauth2/redirect') {
          window.location.replace('/');
        }
      }, 0);
    } catch {
      setHasError(true);
    }
  }, [navigate, onLogin, searchParams]);

  if (hasError) {
    return (
      <section className="page-section">
        <ErrorState
          title="Google 로그인에 실패했습니다."
          message="로그인 응답을 확인할 수 없습니다. 다시 시도해주세요."
          actionLabel="로그인으로 이동"
          actionTo="/login"
        />
      </section>
    );
  }

  return (
    <section className="page-section">
      <p className="empty-message">Google 로그인 처리 중입니다.</p>
    </section>
  );
}

export default OAuthRedirectPage;
