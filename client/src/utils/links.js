export function normalizeLinkUrl(url) {
  const trimmedUrl = String(url || '').trim();

  if (!trimmedUrl) {
    return trimmedUrl;
  }

  if (/^[a-z][a-z\d+.-]*:/i.test(trimmedUrl) || trimmedUrl.startsWith('/') || trimmedUrl.startsWith('#')) {
    return trimmedUrl;
  }

  return `https://${trimmedUrl}`;
}

export function normalizeHtmlLinks(html) {
  if (!html || typeof DOMParser === 'undefined') {
    return html || '';
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(html, 'text/html');

  doc.querySelectorAll('a[href]').forEach((link) => {
    link.setAttribute('href', normalizeLinkUrl(link.getAttribute('href')));
    link.setAttribute('target', '_blank');
    link.setAttribute('rel', 'noopener noreferrer');
  });

  return doc.body.innerHTML;
}
