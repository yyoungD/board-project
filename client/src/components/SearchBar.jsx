import React from 'react';
import { Search } from 'lucide-react';

function SearchBar({ initialKeyword = '', placeholder = '검색어를 입력하세요', onSearch }) {
  const [keyword, setKeyword] = React.useState(initialKeyword);

  React.useEffect(() => {
    setKeyword(initialKeyword);
  }, [initialKeyword]);

  function handleSubmit(event) {
    event.preventDefault();
    onSearch(keyword.trim());
  }

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <input
        className="search-input"
        type="search"
        value={keyword}
        onChange={(event) => setKeyword(event.target.value)}
        placeholder={placeholder}
        aria-label={placeholder}
      />
      <button className="search-button" type="submit" aria-label="검색">
        <Search size={18} aria-hidden="true" />
      </button>
    </form>
  );
}

export default SearchBar;
