import React from 'react';
import { getErrorMessage } from '../api/posts.js';
import { deleteFile, uploadFile } from '../api/uploads.js';

function AttachmentFileField({ files, onChange, disabled, onError }) {
  const [isUploading, setIsUploading] = React.useState(false);
  const [deletingFileIds, setDeletingFileIds] = React.useState([]);

  async function handleFileChange(event) {
    const selectedFiles = Array.from(event.target.files || []);
    event.target.value = '';

    if (selectedFiles.length === 0) {
      return;
    }

    setIsUploading(true);
    onError('');

    try {
      const uploadedFiles = await Promise.all(selectedFiles.map((file) => uploadFile(file)));
      onChange([...files, ...uploadedFiles]);
    } catch (error) {
      onError(getErrorMessage(error, '첨부파일 업로드에 실패했습니다.'));
    } finally {
      setIsUploading(false);
    }
  }

  async function handleRemoveFile(file) {
    if (file.postId) {
      onChange(files.filter((currentFile) => currentFile.id !== file.id));
      return;
    }

    setDeletingFileIds((currentIds) => [...currentIds, file.id]);
    onError('');

    try {
      await deleteFile(file.id);
      onChange(files.filter((currentFile) => currentFile.id !== file.id));
    } catch (error) {
      onError(getErrorMessage(error, '첨부파일 삭제에 실패했습니다.'));
    } finally {
      setDeletingFileIds((currentIds) => currentIds.filter((fileId) => fileId !== file.id));
    }
  }

  return (
    <div className="field-block">
      <div className="attachment-picker">
        <label className="file-upload-button">
          첨부파일
          <input
            className="hidden-file-input"
            type="file"
            multiple
            onChange={handleFileChange}
            disabled={disabled || isUploading}
          />
        </label>
        <div className="attachment-inline-list" aria-live="polite">
          {files.length > 0 ? (
            files.map((file) => (
              <span className="attachment-chip" key={file.id}>
                <span>{file.originalName}</span>
                <small>{formatFileSize(file.size ?? file.fileSize ?? 0)}</small>
                <button
                  type="button"
                  onClick={() => handleRemoveFile(file)}
                  disabled={disabled || deletingFileIds.includes(file.id)}
                >
                  삭제
                </button>
              </span>
            ))
          ) : (
            <span className="field-help">선택된 파일이 없습니다.</span>
          )}
          {isUploading && <span className="field-help">업로드 중입니다.</span>}
        </div>
      </div>
    </div>
  );
}

function formatFileSize(size) {
  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

export default AttachmentFileField;
