import { FileImage, FileSpreadsheet, FileText, FileType } from 'lucide-react';

function AttachmentFileIcon({ file, size = 18 }) {
  const type = getAttachmentType(file);

  const iconProps = {
    className: `attachment-file-icon ${type}`,
    size,
    'aria-hidden': 'true'
  };

  if (type === 'pdf') {
    return <FileText {...iconProps} />;
  }

  if (type === 'excel') {
    return <FileSpreadsheet {...iconProps} />;
  }

  if (type === 'hwp') {
    return <FileType {...iconProps} />;
  }

  if (type === 'image') {
    return <FileImage {...iconProps} />;
  }

  return <FileText {...iconProps} />;
}

function getAttachmentType(file) {
  const contentType = file.contentType || '';
  const extension = getFileExtension(file.originalName);

  if (contentType === 'application/pdf' || extension === 'pdf') {
    return 'pdf';
  }

  if (
    contentType.includes('spreadsheet') ||
    contentType.includes('excel') ||
    ['xls', 'xlsx', 'csv'].includes(extension)
  ) {
    return 'excel';
  }

  if (
    contentType.includes('haansofthwp') ||
    contentType.includes('hwp') ||
    ['hwp', 'hwpx'].includes(extension)
  ) {
    return 'hwp';
  }

  if (contentType.startsWith('image/') || ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(extension)) {
    return 'image';
  }

  return 'file';
}

function getFileExtension(filename) {
  const dotIndex = filename?.lastIndexOf('.') ?? -1;
  if (dotIndex < 0) {
    return '';
  }

  return filename.slice(dotIndex + 1).toLowerCase();
}

export default AttachmentFileIcon;
