import { Home, SearchX } from 'lucide-react';
import { Link } from 'react-router-dom';

function ErrorState({
  eyebrow,
  title,
  message,
  actionLabel,
  actionTo = '/',
  icon = <SearchX size={38} />,
  className = ''
}) {
  const classes = ['error-state', className].filter(Boolean).join(' ');

  return (
    <div className={classes} aria-labelledby="error-state-title">
      {icon && (
        <div className="error-state-icon" aria-hidden="true">
          {icon}
        </div>
      )}
      {eyebrow && <p className="eyebrow">{eyebrow}</p>}
      <h1 id="error-state-title">{title}</h1>
      {message && <p className="error-state-message">{message}</p>}
      {actionLabel && (
        <Link className="primary-link error-state-link" to={actionTo}>
          <Home size={17} aria-hidden="true" />
          <span>{actionLabel}</span>
        </Link>
      )}
    </div>
  );
}

export default ErrorState;
