import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { deleteMe, getErrorMessage, getMe, updateMe } from '../api/members.js';

const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;

const emptyForm = {
  loginId: '',
  name: '',
  phone: '',
  password: '',
  passwordConfirm: ''
};

function formatPhone(value) {
  const numbers = value.replace(/\D/g, '').slice(0, 11);

  if (numbers.length <= 3) {
    return numbers;
  }

  if (numbers.length <= 7) {
    return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
  }

  return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`;
}

function MyPage({ member, onAuthChange, onLogout }) {
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
  const [isLoading, setIsLoading] = React.useState(true);
  const [isSaving, setIsSaving] = React.useState(false);
  const [message, setMessage] = React.useState('');

  const passwordError =
    form.password && !passwordPattern.test(form.password)
      ? '10자 이상의 영어/숫자/특수문자를 각각 하나 이상 사용해 주세요.'
      : '';

  const passwordConfirmError =
    form.passwordConfirm && form.password !== form.passwordConfirm
      ? '비밀번호가 일치하지 않습니다.'
      : '';

  React.useEffect(() => {
    async function loadMe() {
      if (!member) {
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setMessage('');

      try {
        const data = await getMe();
        setForm({
          loginId: data.loginId,
          name: data.name,
          phone: data.phone,
          password: '',
          passwordConfirm: ''
        });
      } catch (error) {
        setMessage(getErrorMessage(error, '회원 정보를 불러오지 못했습니다.'));
      } finally {
        setIsLoading(false);
      }
    }

    loadMe();
  }, [member]);

  function handleChange(event) {
    const { name, value } = event.target;
    const nextValue = name === 'phone' ? formatPhone(value) : value;

    setForm((currentForm) => ({
      ...currentForm,
      [name]: nextValue
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');

    if (!form.phone) {
      setMessage('연락처를 입력해 주세요.');
      setIsSaving(false);
      return;
    }

    if (passwordError || form.password !== form.passwordConfirm) {
      setIsSaving(false);
      return;
    }

    try {
      const authResponse = await updateMe({
        phone: form.phone,
        password: form.password || null
      });
      onAuthChange(authResponse);
      setForm((currentForm) => ({
        ...currentForm,
        name: authResponse.member.name,
        phone: authResponse.member.phone,
        password: '',
        passwordConfirm: ''
      }));
      setMessage('회원 정보가 수정되었습니다.');
    } catch (error) {
      setMessage(getErrorMessage(error, '회원 정보 수정에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete() {
    const confirmed = window.confirm('정말 탈퇴하시겠습니까? 작성한 게시글은 그대로 남습니다.');
    if (!confirmed) {
      return;
    }

    try {
      await deleteMe();
      onLogout();
      navigate('/');
    } catch (error) {
      setMessage(getErrorMessage(error, '회원 탈퇴에 실패했습니다.'));
    }
  }

  if (!member) {
    return (
      <section className="page-section narrow-page">
        <p className="empty-message">로그인 후 마이페이지를 사용할 수 있습니다.</p>
        <div className="form-actions">
          <Link className="primary-link" to="/login">
            로그인
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">마이페이지</p>
          <h1>회원 정보</h1>
        </div>
        <button className="danger-button" type="button" onClick={handleDelete}>
          회원탈퇴
        </button>
      </div>

      {isLoading ? (
        <p className="empty-message">불러오는 중입니다.</p>
      ) : (
        <form className="post-form" onSubmit={handleSubmit} noValidate>
          {message && <p className="status-message">{message}</p>}

          <div className="field-block">
            <span className="field-label">아이디</span>
            <span className="author-display">{form.loginId}</span>
          </div>

          <div className="field-block">
            <span className="field-label">이름</span>
            <span className="author-display">{form.name}</span>
          </div>

          <label>
            연락처
            <input
              name="phone"
              value={form.phone}
              onChange={handleChange}
              inputMode="numeric"
              maxLength="13"
              placeholder="010-0000-0000"
              required
            />
          </label>

          <label>
            새 비밀번호
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
            />
            {passwordError ? (
              <span className="field-error">{passwordError}</span>
            ) : (
              <span className="field-help">변경하지 않으려면 비워두세요.</span>
            )}
          </label>

          <label>
            새 비밀번호 확인
            <input
              name="passwordConfirm"
              type="password"
              value={form.passwordConfirm}
              onChange={handleChange}
            />
            {passwordConfirmError && <span className="field-error">{passwordConfirmError}</span>}
          </label>

          <div className="form-actions">
            <Link className="secondary-link" to="/">
              취소
            </Link>
            <button className="primary-button" type="submit" disabled={isSaving}>
              {isSaving ? '수정 중' : '수정'}
            </button>
          </div>
        </form>
      )}
    </section>
  );
}

export default MyPage;
