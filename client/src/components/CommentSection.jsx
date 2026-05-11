import React from 'react';
import { MoreVertical } from 'lucide-react';
import {
  createComment,
  deleteComment,
  getComments,
  updateComment
} from '../api/comments.js';
import { getErrorMessage } from '../api/posts.js';
import { formatDateTime } from '../utils/date.js';

function CommentSection({ postId, member }) {
  const [comments, setComments] = React.useState([]);
  const [content, setContent] = React.useState('');
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

  async function handleSubmit(event) {
    event.preventDefault();
    if (!member) {
      return;
    }

    if (!content.trim()) {
      return;
    }

    setIsSubmitting(true);
    setMessage('');

    try {
      await createComment(postId, { content: content.trim() });
      setContent('');
      await loadComments();
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글 작성에 실패했습니다.'));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleReply(parentId, replyContent) {
    await createComment(postId, { parentId, content: replyContent });
    await loadComments();
  }

  async function handleUpdate(commentId, nextContent) {
    await updateComment(commentId, { content: nextContent });
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

      <form className="comment-form" onSubmit={handleSubmit}>
        <textarea
          value={member ? content : '로그인하면 댓글을 작성할 수 있습니다.'}
          onChange={(event) => setContent(event.target.value)}
          placeholder="댓글을 입력하세요"
          readOnly={!member}
          aria-readonly={!member}
        />
        <div className="comment-form-actions">
          <button className="primary-button" type="submit" disabled={!member || isSubmitting}>
            {isSubmitting ? '등록 중' : '등록'}
          </button>
        </div>
      </form>

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

function countComments(comments) {
  return comments.reduce((count, comment) => {
    return count + 1 + countComments(comment.replies || []);
  }, 0);
}

function CommentItem({ comment, member, onReply, onUpdate, onDelete }) {
  const [isReplying, setIsReplying] = React.useState(false);
  const [isEditing, setIsEditing] = React.useState(false);
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const [replyContent, setReplyContent] = React.useState('');
  const [editContent, setEditContent] = React.useState(comment.content);
  const [message, setMessage] = React.useState('');
  const isOwner = Boolean(member && member.loginId === comment.author);
  const canChange = isOwner && !comment.deleted;

  React.useEffect(() => {
    setEditContent(comment.content);
  }, [comment.content]);

  async function submitReply(event) {
    event.preventDefault();
    if (!replyContent.trim()) {
      return;
    }

    setMessage('');
    try {
      await onReply(comment.id, replyContent.trim());
      setReplyContent('');
      setIsReplying(false);
    } catch (error) {
      setMessage(getErrorMessage(error, '답글 작성에 실패했습니다.'));
    }
  }

  async function submitEdit(event) {
    event.preventDefault();
    if (!editContent.trim()) {
      return;
    }

    setMessage('');
    try {
      await onUpdate(comment.id, editContent.trim());
      setIsEditing(false);
    } catch (error) {
      setMessage(getErrorMessage(error, '댓글 수정에 실패했습니다.'));
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
          <form className="comment-edit-form" onSubmit={submitEdit}>
            <textarea
              value={editContent}
              onChange={(event) => setEditContent(event.target.value)}
            />
            <div className="comment-inline-actions">
              <button className="primary-button" type="submit">
                저장
              </button>
              <button
                className="secondary-button"
                type="button"
                onClick={() => setIsEditing(false)}
              >
                취소
              </button>
            </div>
          </form>
        ) : (
          <p className="comment-content">{comment.content}</p>
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
          <form className="comment-reply-form" onSubmit={submitReply}>
            <textarea
              value={replyContent}
              onChange={(event) => setReplyContent(event.target.value)}
              placeholder="답글을 입력하세요"
            />
            <div className="comment-inline-actions">
              <button className="primary-button" type="submit">
                답글 등록
              </button>
              <button
                className="secondary-button"
                type="button"
                onClick={() => setIsReplying(false)}
              >
                취소
              </button>
            </div>
          </form>
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

export default CommentSection;
