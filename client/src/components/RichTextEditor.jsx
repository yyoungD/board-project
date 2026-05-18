import { Mark, mergeAttributes } from '@tiptap/core';
import Highlight from '@tiptap/extension-highlight';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import TextAlign from '@tiptap/extension-text-align';
import Underline from '@tiptap/extension-underline';
import { EditorContent, useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import {
  AlignCenter,
  AlignLeft,
  AlignRight,
  Bold,
  Highlighter,
  ImagePlus,
  Italic,
  Link as LinkIcon,
  List,
  ListOrdered,
  Quote,
  Redo2,
  Underline as UnderlineIcon,
  Undo2,
  Unlink
} from 'lucide-react';
import { normalizeLinkUrl } from '../utils/links.js';
import React from 'react';
import { uploadImage } from '../api/uploads.js';

const FontSize = Mark.create({
  name: 'fontSize',

  addAttributes() {
    return {
      size: {
        default: null,
        parseHTML: (element) => element.style.fontSize?.replace(/['"]+/g, ''),
        renderHTML: (attributes) => {
          if (!attributes.size) {
            return {};
          }

          return {
            style: `font-size: ${attributes.size}`
          };
        }
      }
    };
  },

  parseHTML() {
    return [
      {
        tag: 'span[style*="font-size"]'
      }
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ['span', mergeAttributes(HTMLAttributes), 0];
  },

  addCommands() {
    return {
      setFontSize:
        (size) =>
        ({ commands }) =>
          commands.setMark(this.name, { size }),
      unsetFontSize:
        () =>
        ({ commands }) =>
          commands.unsetMark(this.name)
    };
  }
});

const fontSizeOptions = [
  { label: '기본', value: '' },
  { label: '14 px', value: '14px' },
  { label: '16 px', value: '16px' },
  { label: '20 px', value: '20px' },
  { label: '26 px', value: '26px' }
];

function ToolbarButton({ active, children, disabled = false, label, onClick }) {
  return (
    <button
      aria-label={label}
      className={active ? 'editor-toolbar-button active' : 'editor-toolbar-button'}
      disabled={disabled}
      title={label}
      type="button"
      onClick={onClick}
    >
      {children}
    </button>
  );
}

function RichTextEditor({ value, onChange }) {
  const fileInputRef = React.useRef(null);
  const [isUploadingImage, setIsUploadingImage] = React.useState(false);
  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      Highlight,
      FontSize,
      Image.configure({
        inline: false,
        allowBase64: false
      }),
      Link.configure({
        openOnClick: false,
        autolink: true,
        defaultProtocol: 'https',
        HTMLAttributes: {
          target: '_blank',
          rel: 'noopener noreferrer'
        }
      }),
      TextAlign.configure({
        types: ['heading', 'paragraph']
      })
    ],
    content: value,
    editorProps: {
      attributes: {
        class: 'rich-editor-content'
      }
    },
    onUpdate({ editor }) {
      onChange(editor.getHTML());
    }
  });

  React.useEffect(() => {
    if (editor && value !== editor.getHTML()) {
      editor.commands.setContent(value || '', false);
    }
  }, [editor, value]);

  if (!editor) {
    return null;
  }

  function setLink() {
    const previousUrl = editor.getAttributes('link').href || '';
    const { empty, from, to } = editor.state.selection;

    if (empty && !previousUrl) {
      window.alert('링크를 걸 텍스트를 먼저 선택해 주세요.');
      return;
    }

    const selectedText = editor.state.doc.textBetween(from, to, ' ').trim();
    const defaultUrl = previousUrl || (looksLikeUrl(selectedText) ? selectedText : '');
    const url = window.prompt('링크 URL을 입력하세요.', defaultUrl);

    if (url === null) {
      return;
    }

    if (url.trim() === '') {
      editor.chain().focus().unsetLink().run();
      return;
    }

    editor.chain().focus().extendMarkRange('link').setLink({ href: normalizeLinkUrl(url) }).run();
  }

  function handleFontSizeChange(event) {
    const nextSize = event.target.value;

    if (!nextSize) {
      editor.chain().focus().unsetFontSize().run();
      return;
    }

    editor.chain().focus().setFontSize(nextSize).run();
  }

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
      window.alert('이미지 파일만 선택할 수 있습니다.');
      return;
    }

    setIsUploadingImage(true);
    try {
      const uploadedImage = await uploadImage(file);
      editor.chain().focus().setImage({ src: uploadedImage.url }).run();
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.response?.data?.error ||
        '이미지 업로드에 실패했습니다.';
      window.alert(message);
    } finally {
      setIsUploadingImage(false);
    }
  }

  const currentFontSize = editor.getAttributes('fontSize').size || '';

  return (
    <div className="rich-editor">
      <input
        ref={fileInputRef}
        className="hidden-file-input"
        type="file"
        accept="image/*"
        onChange={handleImageChange}
      />
      <div className="editor-toolbar">
        <select
          className="editor-toolbar-select"
          aria-label="글씨 크기"
          title="글씨 크기"
          value={currentFontSize}
          onChange={handleFontSizeChange}
        >
          {fontSizeOptions.map((option) => (
            <option key={option.label} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        <ToolbarButton label="굵게" active={editor.isActive('bold')} onClick={() => editor.chain().focus().toggleBold().run()}>
          <Bold size={16} />
        </ToolbarButton>
        <ToolbarButton label="기울임" active={editor.isActive('italic')} onClick={() => editor.chain().focus().toggleItalic().run()}>
          <Italic size={16} />
        </ToolbarButton>
        <ToolbarButton label="밑줄" active={editor.isActive('underline')} onClick={() => editor.chain().focus().toggleUnderline().run()}>
          <UnderlineIcon size={16} />
        </ToolbarButton>
        <ToolbarButton label="형광펜" active={editor.isActive('highlight')} onClick={() => editor.chain().focus().toggleHighlight().run()}>
          <Highlighter size={16} />
        </ToolbarButton>
        <ToolbarButton label="글머리 목록" active={editor.isActive('bulletList')} onClick={() => editor.chain().focus().toggleBulletList().run()}>
          <List size={16} />
        </ToolbarButton>
        <ToolbarButton label="번호 목록" active={editor.isActive('orderedList')} onClick={() => editor.chain().focus().toggleOrderedList().run()}>
          <ListOrdered size={16} />
        </ToolbarButton>
        <ToolbarButton label="인용" active={editor.isActive('blockquote')} onClick={() => editor.chain().focus().toggleBlockquote().run()}>
          <Quote size={16} />
        </ToolbarButton>
        <ToolbarButton label="왼쪽 정렬" active={editor.isActive({ textAlign: 'left' })} onClick={() => editor.chain().focus().setTextAlign('left').run()}>
          <AlignLeft size={16} />
        </ToolbarButton>
        <ToolbarButton label="가운데 정렬" active={editor.isActive({ textAlign: 'center' })} onClick={() => editor.chain().focus().setTextAlign('center').run()}>
          <AlignCenter size={16} />
        </ToolbarButton>
        <ToolbarButton label="오른쪽 정렬" active={editor.isActive({ textAlign: 'right' })} onClick={() => editor.chain().focus().setTextAlign('right').run()}>
          <AlignRight size={16} />
        </ToolbarButton>
        <ToolbarButton label="링크" active={editor.isActive('link')} onClick={setLink}>
          <LinkIcon size={16} />
        </ToolbarButton>
        <ToolbarButton label="링크 해제" onClick={() => editor.chain().focus().unsetLink().run()}>
          <Unlink size={16} />
        </ToolbarButton>
        <ToolbarButton label={isUploadingImage ? '이미지 업로드 중' : '이미지 삽입'} disabled={isUploadingImage} onClick={openImagePicker}>
          <ImagePlus size={16} />
        </ToolbarButton>
        <ToolbarButton label="실행 취소" onClick={() => editor.chain().focus().undo().run()}>
          <Undo2 size={16} />
        </ToolbarButton>
        <ToolbarButton label="다시 실행" onClick={() => editor.chain().focus().redo().run()}>
          <Redo2 size={16} />
        </ToolbarButton>
      </div>
      <EditorContent editor={editor} />
    </div>
  );
}

function looksLikeUrl(text) {
  return /^(https?:\/\/|www\.|[a-z\d-]+(\.[a-z\d-]+)+)/i.test(text);
}

export default RichTextEditor;
