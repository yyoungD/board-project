function Pagination({ page, totalPages, onPageChange }) {
  const displayTotalPages = Math.max(totalPages, 1);
  const currentPage = Math.min(page, displayTotalPages);

  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(displayTotalPages, startPage + 4);
  const pages = Array.from(
    { length: endPage - startPage + 1 },
    (_, index) => startPage + index
  );

  return (
    <nav className="pagination" aria-label="페이지네이션">
      <button
        type="button"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage <= 1}
      >
        이전
      </button>
      {pages.map((pageNumber) => (
        <button
          className={pageNumber === currentPage ? 'active' : ''}
          key={pageNumber}
          type="button"
          onClick={() => onPageChange(pageNumber)}
          disabled={pageNumber === currentPage}
        >
          {pageNumber}
        </button>
      ))}
      <button
        type="button"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= displayTotalPages}
      >
        다음
      </button>
    </nav>
  );
}

export default Pagination;
