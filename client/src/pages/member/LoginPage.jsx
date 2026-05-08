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
    </section>
  );
}

export default LoginPage;
