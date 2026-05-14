import React from 'react';
import { Camera, MoreVertical, X } from 'lucide-react';
import {
  createComment,
  deleteComment,
  getComments,
  updateComment
} from '../api/comments.js';
import { getErrorMessage } from '../api/posts.js';
import { deleteImage, uploadImage } from '../api/uploads.js';
import { formatDateTime } from '../utils/date.js';

const imageUrlPattern = /^\/api\/uploads\/images\/\d+$/;
const imageIdPattern = /\/api\/uploads\/images\/(\d+)$/;

function CommentSection({ postId, member }) {
  const [comments, setComments] = React.useState([]);
  const [message, setMessage] = React.useState('');
  const [isLoading, setIsLoading] = React.useState(true);
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const commentCount = countComments(comments);

  const loadComments = React.useCallback(async () => {
    setIsLoading(true);
    setMessage('');

    try {
      const data = await getComments(postId);
      setComments(data);
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글을 불러오지 못했습니다.'));
    } finally {
      setIsLoading(false);
    }
  }, [postId]);

  React.useEffect(() => {
    loadComments();
  }, [loadComments]);

  async function handleSubmit(nextContent) {
    if (!member || !nextContent.trim()) {
      return;
    }

    setIsSubmitting(true);
    setMessage('');

    try {
      await createComment(postId, { content: nextContent.trim() });
      await loadComments();
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글 작성에 실패했습니다.'));
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleReply(parentId, replyContent) {
    await createComment(postId, { parentId, content: replyContent.trim() });
    await loadComments();
  }

  async function handleUpdate(commentId, nextContent) {
    await updateComment(commentId, { content: nextContent.trim() });
    await loadComments();
  }

  async function handleDelete(commentId) {
    const confirmed = window.confirm('댓글을 삭제하시겠습니까?');
    if (!confirmed) {
      return;
    }

    await deleteComment(commentId);
    await loadComments();
  }

  return (
    <section className="comments-section">
      <div className="comments-header">
        <h2>댓글</h2>
        <span>{commentCount}개</span>
      </div>

      {message && <p className="status-message">{message}</p>}

      <CommentForm
        className="comment-form"
        disabled={!member || isSubmitting}
        placeholder="댓글을 입력하세요."
        readOnly={!member}
        readOnlyValue="로그인하면 댓글을 작성할 수 있습니다."
        submitLabel={isSubmitting ? '등록 중' : '등록'}
        clearOnSubmit
        onSubmit={handleSubmit}
        onError={setMessage}
      />

      {isLoading ? (
        <p className="empty-message">댓글을 불러오는 중입니다.</p>
      ) : comments.length === 0 ? (
        <p className="empty-message">아직 댓글이 없습니다.</p>
      ) : (
        <div className="comments-list">
          {comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              member={member}
              onReply={handleReply}
              onUpdate={handleUpdate}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </section>
  );
}

function CommentForm({
  className,
  disabled,
  initialContent = '',
  placeholder,
  readOnly = false,
  readOnlyValue = '',
  submitLabel,
  textareaRef,
  clearOnSubmit = false,
  onCancel,
  onError,
  onSubmit
}) {
  const fileInputRef = React.useRef(null);
  const [text, setText] = React.useState('');
  const [imageUrls, setImageUrls] = React.useState([]);
  const [initialImageUrls, setInitialImageUrls] = React.useState([]);
  const [isUploadingImage, setIsUploadingImage] = React.useState(false);
  const [deletingImageUrl, setDeletingImageUrl] = React.useState(null);

  React.useEffect(() => {
    const parsed = splitCommentImages(initialContent);
    setText(parsed.text);
    setImageUrls(parsed.imageUrls);
    setInitialImageUrls(parsed.imageUrls);
  }, [initialContent]);

  function openImagePicker() {
    fileInputRef.current?.click();
  }

  async function handleImageChange(event) {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      onError('이미지 파일만 선택할 수 있습니다.');
      return;
    }

    setIsUploadingImage(true);
    onError('');

    try {
      const uploadedImage = await uploadImage(file);
      setImageUrls((currentUrls) => [...currentUrls, uploadedImage.url]);
    } catch (error) {
      onError(getErrorMessage(error, '이미지 업로드에 실패했습니다.'));
    } finally {
      setIsUploadingImage(false);
    }
  }

  async function removeImage(imageUrl) {
    const isExistingImage = initialImageUrls.includes(imageUrl);

    if (isExistingImage) {
      setImageUrls((currentUrls) => currentUrls.filter((url) => url !== imageUrl));
      return;
    }

    const imageId = getImageId(imageUrl);
    if (!imageId) {
      setImageUrls((currentUrls) => currentUrls.filter((url) => url !== imageUrl));
      return;
    }

    setDeletingImageUrl(imageUrl);
    onError('');

    try {
      await deleteImage(imageId);
      setImageUrls((currentUrls) => currentUrls.filter((url) => url !== imageUrl));
    } catch (error) {
      onError(getErrorMessage(error, '이미지 삭제에 실패했습니다.'));
    } finally {
      setDeletingImageUrl(null);
    }
  }

  async function submitForm(event) {
    event.preventDefault();
    const nextContent = buildCommentContent(text, imageUrls);

    if (!nextContent.trim() || disabled || readOnly || isUploadingImage || deletingImageUrl) {
      return;
    }

    try {
      await onSubmit(nextContent);
      if (clearOnSubmit) {
        setText('');
        setImageUrls([]);
        setInitialImageUrls([]);
      }
    } catch {
      // The parent displays the error message.
    }
  }

  return (
    <form className={className} onSubmit={submitForm}>
      {!readOnly && imageUrls.length > 0 && (
        <div className="comment-image-preview-list" aria-live="polite">
          {imageUrls.map((imageUrl) => (
            <div className="comment-image-preview-item" key={imageUrl}>
              <img src={imageUrl} alt="댓글 첨부 이미지 미리보기" />
              <button
                type="button"
                aria-label="첨부 이미지 삭제"
                title="첨부 이미지 삭제"
                disabled={deletingImageUrl === imageUrl}
                onClick={() => removeImage(imageUrl)}
              >
                <X size={14} aria-hidden="true" />
              </button>
            </div>
          ))}
        </div>
      )}

      <textarea
        ref={textareaRef}
        value={readOnly ? readOnlyValue : text}
        onChange={(event) => setText(event.target.value)}
        placeholder={placeholder}
        readOnly={readOnly}
        aria-readonly={readOnly}
      />

      <div className="comment-form-actions">
        <input
          ref={fileInputRef}
          className="hidden-file-input"
          type="file"
          accept="image/*"
          onChange={handleImageChange}
        />
        <button
          className="comment-image-button"
          type="button"
          aria-label="이미지 첨부"
          title="이미지 첨부"
          disabled={disabled || readOnly || isUploadingImage || Boolean(deletingImageUrl)}
          onClick={openImagePicker}
        >
          <Camera size={18} aria-hidden="true" />
        </button>
        <div className="comment-submit-actions">
          {onCancel && (
            <button className="secondary-button" type="button" onClick={onCancel}>
              취소
            </button>
          )}
          <button
            className="primary-button"
            type="submit"
            disabled={disabled || isUploadingImage || Boolean(deletingImageUrl)}
          >
            {isUploadingImage ? '업로드 중' : submitLabel}
          </button>
        </div>
      </div>
    </form>
  );
}

function countComments(comments) {
  return comments.reduce((count, comment) => {
    return count + 1 + countComments(comment.replies || []);
  }, 0);
}

function CommentItem({ comment, member, onReply, onUpdate, onDelete }) {
  const [isReplying, setIsReplying] = React.useState(false);
  const [isEditing, setIsEditing] = React.useState(false);
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const [message, setMessage] = React.useState('');
  const replyTextareaRef = React.useRef(null);
  const isOwner = Boolean(member && member.loginId === comment.author);
  const canChange = isOwner && !comment.deleted;

  React.useEffect(() => {
    if (isReplying) {
      replyTextareaRef.current?.focus();
    }
  }, [isReplying]);

  async function submitReply(nextContent) {
    setMessage('');
    try {
      await onReply(comment.id, nextContent);
      setIsReplying(false);
    } catch (error) {
      setMessage(getErrorMessage(error, '답글 작성에 실패했습니다.'));
      throw error;
    }
  }

  async function submitEdit(nextContent) {
    setMessage('');
    try {
      await onUpdate(comment.id, nextContent);
      setIsEditing(false);
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글 수정에 실패했습니다.'));
      throw error;
    }
  }

  async function clickDelete() {
    setMessage('');
    try {
      await onDelete(comment.id);
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글 삭제에 실패했습니다.'));
    }
  }

  return (
    <div className={`comment-item ${comment.deleted ? 'deleted' : ''}`}>
      <div className="comment-body">
        <div className="comment-top-row">
          <div className="comment-meta">
            <strong>{comment.author}</strong>
            <span>{formatDateTime(comment.createdAt)}</span>
            {comment.edited && !comment.deleted && <em>수정됨</em>}
          </div>

          {canChange && (
            <div className="comment-menu">
              <button
                className="comment-menu-button"
                type="button"
                aria-label="댓글 메뉴"
                aria-expanded={isMenuOpen}
                onClick={() => setIsMenuOpen((value) => !value)}
              >
                <MoreVertical size={18} aria-hidden="true" />
              </button>
              {isMenuOpen && (
                <div className="comment-menu-dropdown">
                  <button
                    type="button"
                    onClick={() => {
                      setIsEditing(true);
                      setIsMenuOpen(false);
                    }}
                  >
                    수정
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsMenuOpen(false);
                      clickDelete();
                    }}
                  >
                    삭제
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {isEditing ? (
          <CommentForm
            className="comment-edit-form"
            initialContent={comment.content}
            submitLabel="저장"
            onCancel={() => setIsEditing(false)}
            onSubmit={submitEdit}
            onError={setMessage}
          />
        ) : (
          <div className="comment-content">{renderCommentContent(comment.content)}</div>
        )}

        {message && <p className="field-error">{message}</p>}

        <div className="comment-actions">
          {member && !comment.deleted && (
            <button
              className="reply-button"
              type="button"
              onClick={() => setIsReplying((value) => !value)}
            >
              답글
            </button>
          )}
        </div>

        {isReplying && (
          <CommentForm
            className="comment-reply-form"
            initialContent={`@${comment.author} `}
            placeholder="답글을 입력하세요."
            submitLabel="답글 등록"
            textareaRef={replyTextareaRef}
            clearOnSubmit
            onCancel={() => setIsReplying(false)}
            onSubmit={submitReply}
            onError={setMessage}
          />
        )}
      </div>

      {comment.replies?.length > 0 && (
        <div className="comment-replies">
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              member={member}
              onReply={onReply}
              onUpdate={onUpdate}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function CommentImage({ src }) {
  const [isExpanded, setIsExpanded] = React.useState(false);

  return (
    <button
      className={`comment-image-button-view ${isExpanded ? 'expanded' : ''}`}
      type="button"
      aria-label={isExpanded ? '댓글 이미지 축소' : '댓글 이미지 확대'}
      onClick={() => setIsExpanded((value) => !value)}
    >
      <img className="comment-image" src={src} alt="댓글 첨부 이미지" />
    </button>
  );
}

function splitCommentImages(content) {
  const textLines = [];
  const imageUrls = [];

  content.split('\n').forEach((line) => {
    const trimmedLine = line.trim();
    if (imageUrlPattern.test(trimmedLine)) {
      imageUrls.push(trimmedLine);
      return;
    }

    textLines.push(line);
  });

  return {
    text: textLines.join('\n').trimEnd(),
    imageUrls
  };
}

function buildCommentContent(text, imageUrls) {
  const trimmedText = text.trimEnd();
  const imageContent = imageUrls.join('\n');

  if (trimmedText && imageContent) {
    return `${trimmedText}\n${imageContent}`;
  }

  return trimmedText || imageContent;
}

function getImageId(imageUrl) {
  const match = imageIdPattern.exec(imageUrl);
  return match ? match[1] : null;
}

function renderCommentContent(content) {
  const lines = content.split('\n');

  return lines.map((line, lineIndex) => {
    const trimmedLine = line.trim();

    if (imageUrlPattern.test(trimmedLine)) {
      return <CommentImage key={`${trimmedLine}-${lineIndex}`} src={trimmedLine} />;
    }

    return (
      <React.Fragment key={`${line}-${lineIndex}`}>
        {renderCommentTextLine(line)}
        {lineIndex < lines.length - 1 && <br />}
      </React.Fragment>
    );
  });
}

function renderCommentTextLine(line) {
  return line.split(/(@[A-Za-z0-9_]+)/g).map((part, index) => {
    if (part.startsWith('@')) {
      return (
        <span key={`${part}-${index}`} className="mention-text">
          {part}
        </span>
      );
    }

    return part;
  });
}

export default CommentSection;
