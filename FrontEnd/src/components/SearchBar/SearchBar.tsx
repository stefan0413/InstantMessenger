import type { ChangeEvent } from "react";
import "./SearchBar.css";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  resultCount: number | null;
  isSearching: boolean;
}

export function SearchBar({ value, onChange, resultCount, isSearching }: SearchBarProps) {
  const hasQuery = value.trim().length > 0;

  function handleChange(event: ChangeEvent<HTMLInputElement>): void {
    onChange(event.target.value);
  }

  function handleClear(): void {
    onChange("");
  }

  return (
    <div className="search-bar">
      <label className="search-bar__field">
        <span className="search-bar__icon" aria-hidden="true">
          🔍
        </span>
        <input
          type="search"
          value={value}
          onChange={handleChange}
          placeholder="Search messages in this channel"
          aria-label="Search messages"
        />
        {hasQuery && (
          <button
            type="button"
            className="search-bar__clear"
            onClick={handleClear}
            aria-label="Clear search"
          >
            ✕
          </button>
        )}
      </label>

      {hasQuery && (
        <div className="search-bar__status" role="status" aria-live="polite">
          {isSearching ? (
            <span>Searching...</span>
          ) : resultCount === 0 ? (
            <span className="search-bar__status--empty">No matches found</span>
          ) : (
            <span>
              {resultCount} {resultCount === 1 ? "result" : "results"} for{" "}
              <strong>"{value}"</strong>
            </span>
          )}
        </div>
      )}
    </div>
  );
}
