import { Link } from 'react-router-dom';
import AttachmentFileField from './AttachmentFileField.jsx';
import RichTextEditor from './RichTextEditor.jsx';

function PostForm({
  form,
  author,
  files,
  onFieldChange,
  onContentChange,
  onFilesChange,
  onError,
  onSubmit,
  message,
  isSaving,
  cancelTo,
  submitLabel,
  savingLabel
}) {
  return (
    <form className="post-form" onSubmit={onSubmit}>
      {message && <p className="status-message">{message}</p>}

      <label>
        제목
        <input
          name="title"
          value={form.title}
          onChange={onFieldChange}
          maxLength="200"
          required
        />
      </label>

      <div className="field-block">
        <span className="field-label">작성자</span>
        <span className="author-display">{author}</span>
      </div>

      <div className="field-block">
        <span className="field-label">내용</span>
        <RichTextEditor value={form.content} onChange={onContentChange} />
      </div>

      <AttachmentFileField
        files={files}
        onChange={onFilesChange}
        onError={onError}
        disabled={isSaving}
      />

      <div className="form-actions">
        <Link className="secondary-link" to={cancelTo}>
          취소
        </Link>
        <button className="primary-button" type="submit" disabled={isSaving}>
          {isSaving ? savingLabel : submitLabel}
        </button>
      </div>
    </form>
  );
}

export default PostForm;
