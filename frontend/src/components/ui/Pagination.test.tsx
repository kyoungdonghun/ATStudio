import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Pagination from '@/components/ui/Pagination';
import type { PageInfo } from '@/types';

const PAGE_INFO: PageInfo = {
  page: 2,
  size: 10,
  total: 50,
  start: 1,
  end: 3,
  prev: true,
  next: true,
};

describe('Pagination accessibility', () => {
  it('names navigation controls and identifies the current page', () => {
    const onPageChange = vi.fn();
    render(<Pagination pageInfo={PAGE_INFO} currentPage={2} onPageChange={onPageChange} />);

    expect(screen.getByRole('navigation', { name: '페이지 탐색' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '2페이지' })).toHaveAttribute('aria-current', 'page');
    expect(screen.getByRole('button', { name: '1페이지' })).not.toHaveAttribute('aria-current');

    fireEvent.click(screen.getByRole('button', { name: '이전 페이지' }));
    fireEvent.click(screen.getByRole('button', { name: '다음 페이지' }));

    expect(onPageChange).toHaveBeenNthCalledWith(1, 1);
    expect(onPageChange).toHaveBeenNthCalledWith(2, 3);
  });

  it('does not render navigation for an empty result', () => {
    const { container } = render(
      <Pagination
        pageInfo={{ ...PAGE_INFO, total: 0, prev: false, next: false }}
        currentPage={1}
        onPageChange={vi.fn()}
      />,
    );

    expect(container).toBeEmptyDOMElement();
  });
});
