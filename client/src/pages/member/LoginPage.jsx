import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getErrorMessage, login } from '../../api/members.js';

const emptyForm = {
  loginId: '',
  password: ''
};

function LoginPage({ onLogin }) {
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [isSaving, setIsSaving] = React.useState(false);
  const [message, setMessage] = React.useState('');

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((currentForm) => ({
      ...currentForm,
      [name]: value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    if (!form.loginId || !form.password) {
      setMessage('아이디와 비밀번호를 입력해 주세요.');
      setIsSaving(false);
      return;
    }

    try {
      const authResponse = await login(form);
      onLogin(authResponse);
      navigate('/');
    } catch (error) {
      setMessage(getErrorMessage(error, '로그인에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">로그인</p>
          <h1>회원 로그인</h1>
        </div>
      </div>

      <form className="post-form" onSubmit={handleSubmit} noValidate>
        <label>
          아이디
          <input
            name="loginId"
            value={form.loginId}
            onChange={handleChange}
            required
          />
        </label>

        <label>
          비밀번호
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
          />
        </label>
        {message && <p className="status-message">{message}</p>}

        <div className="form-actions">
          <Link className="secondary-link" to="/signup">
            회원가입
          </Link>
          <button className="primary-button" type="submit" disabled={isSaving}>
            {isSaving ? '로그인 중' : '로그인'}
          </button>
        </div>
      </form>

      <div className="oauth-login-section">
        <a className="secondary-link oauth-login-link" href="/oauth2/authorization/google">
          <svg className="oauth-login-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84C6.71 7.31 9.14 5.38 12 5.38z"
            />
          </svg>
          <span>Google로 계속하기</span>
          Google로 로그인
        </a>
      </div>
    </section>
  );
}

export default LoginPage;
