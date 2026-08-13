import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import CatalogDetailRecovery from './CatalogDetailRecovery';

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{location.pathname}</div>;
}

function renderRecovery(initialEntries = ['/albums/7'], initialIndex = 0) {
  const onRetry = vi.fn();
  render(
    <MemoryRouter initialEntries={initialEntries} initialIndex={initialIndex}>
      <Routes>
        <Route
          path="*"
          element={
            <>
              <CatalogDetailRecovery
                title="앨범 정보를 불러오지 못했습니다"
                message="잠시 후 다시 시도해주세요."
                onRetry={onRetry}
              />
              <LocationProbe />
            </>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
  return onRetry;
}

describe('CatalogDetailRecovery', () => {
  it('offers retry and a direct home action', () => {
    const onRetry = renderRecovery();

    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(onRetry).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('link', { name: '홈으로' })).toHaveAttribute('href', '/');
  });

  it('returns to the previous catalog location when history exists', () => {
    renderRecovery(['/albums?page=3', '/albums/7'], 1);

    fireEvent.click(screen.getByRole('button', { name: '이전 화면' }));

    expect(screen.getByTestId('location')).toHaveTextContent('/albums');
  });

  it('replaces a direct entry with home when no previous history exists', () => {
    renderRecovery();

    fireEvent.click(screen.getByRole('button', { name: '이전 화면' }));

    expect(screen.getByTestId('location')).toHaveTextContent('/');
  });
});
