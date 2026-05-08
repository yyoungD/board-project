import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getErrorMessage, signup } from '../../api/members.js';

const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;

const emptyForm = {
  loginId: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: ''
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

function SignupPage() {
  const navigate = useNavigate();
  const [form, setForm] = React.useState(emptyForm);
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

    if (!form.loginId || !form.password || !form.passwordConfirm || !form.name || !form.phone) {
      setMessage('모든 항목을 입력해 주세요.');
      setIsSaving(false);
      return;
    }

    if (passwordError || form.password !== form.passwordConfirm) {
      setIsSaving(false);
      return;
    }

    try {
      await signup({
        loginId: form.loginId,
        password: form.password,
        name: form.name,
        phone: form.phone
      });
      navigate('/', { state: { message: '회원가입이 완료되었습니다.' } });
    } catch (error) {
      setMessage(getErrorMessage(error, '회원가입에 실패했습니다.'));
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="page-section narrow-page">
      <div className="page-title-row">
        <div>
          <p className="eyebrow">회원가입</p>
          <h1>새 회원 등록</h1>
        </div>
        <Link className="secondary-link" to="/">
          목록
        </Link>
      </div>

      <form className="post-form" onSubmit={handleSubmit} noValidate>
        {message && <p className="status-message">{message}</p>}

        <label>
          아이디
          <input
            name="loginId"
            value={form.loginId}
            onChange={handleChange}
            minLength="4"
            maxLength="50"
            pattern="[A-Za-z0-9_]+"
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
            minLength="10"
            pattern="^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$"
            required
          />
          {passwordError ? (
            <span className="field-error">{passwordError}</span>
          ) : (
            <span className="field-help">10자 이상의 영어/숫자/특수문자를 각각 하나 이상 사용해 주세요.</span>
          )}
        </label>

        <label>
          비밀번호 확인
          <input
            name="passwordConfirm"
            type="password"
            value={form.passwordConfirm}
            onChange={handleChange}
            minLength="10"
            required
          />
          {passwordConfirmError && <span className="field-error">{passwordConfirmError}</span>}
        </label>

        <label>
          이름
          <input
            name="name"
            value={form.name}
            onChange={handleChange}
            maxLength="100"
            required
          />
        </label>

        <label>
          연락처
          <input
            name="phone"
            value={form.phone}
            onChange={handleChange}
            inputMode="numeric"
            maxLength="13"
            pattern="010-[0-9]{4}-[0-9]{4}"
            placeholder="010-0000-0000"
            required
          />
        </label>

        <div className="form-actions">
          <Link className="secondary-link" to="/">
            취소
          </Link>
          <button className="primary-button" type="submit" disabled={isSaving}>
            {isSaving ? '가입 중' : '회원가입'}
          </button>
        </div>
      </form>
    </section>
  );
}

export default SignupPage;
