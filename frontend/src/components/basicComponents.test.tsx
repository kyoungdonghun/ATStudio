import { act, fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({ toUploadUrl: (value: string | null) => value }));

import AlbumCard from '@/components/album/AlbumCard';
import Badge from '@/components/ui/Badge';
import FilterChip from '@/components/ui/FilterChip';
import Tag from '@/components/ui/Tag';
import { useThemeStore } from '@/store/themeStore';
import { useToastStore } from '@/store/toastStore';

describe('small reusable UI and client state', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useToastStore.setState({ toasts: [] });
    document.documentElement.removeAttribute('data-theme');
    localStorage.clear();
  });

  it('renders album metadata and keeps like clicks separate from card navigation', () => {
    const onClick = vi.fn();
    const onToggleLike = vi.fn();
    render(
      <AlbumCard
        album={{
          id: 4,
          title: 'Focus Mix',
          description: null,
          thumbnailUrl: '/cover.png',
          trackCount: 12,
          likeCount: 3,
          createdAt: '2026-07-17',
        }}
        genre="Lo-fi"
        isLiked
        onClick={onClick}
        onToggleLike={onToggleLike}
      />,
    );

    expect(screen.getByAltText('Focus Mix')).toHaveAttribute('src', '/cover.png');
    expect(screen.getByText(/Lo-fi/)).toHaveTextContent('12');
    fireEvent.click(screen.getByRole('button'));
    expect(onToggleLike).toHaveBeenCalledWith(4);
    expect(onClick).not.toHaveBeenCalled();
    fireEvent.click(screen.getByText('Focus Mix'));
    expect(onClick).toHaveBeenCalledOnce();
  });

  it('renders fallback album art and interactive chips', () => {
    const onFilter = vi.fn();
    const onTag = vi.fn();
    const { container } = render(
      <>
        <AlbumCard
          album={{
            id: 5,
            title: 'No Cover',
            description: null,
            thumbnailUrl: null,
            trackCount: 0,
            likeCount: 0,
            createdAt: '2026-07-17',
          }}
        />
        <Badge variant="hot">HOT</Badge>
        <FilterChip label="Popular" active onClick={onFilter} />
        <Tag label="Shorts" active onClick={onTag} />
      </>,
    );

    expect(container).toHaveTextContent('♪');
    fireEvent.click(screen.getByRole('button', { name: 'Popular' }));
    fireEvent.click(screen.getByRole('button', { name: 'Shorts' }));
    expect(onFilter).toHaveBeenCalledOnce();
    expect(onTag).toHaveBeenCalledOnce();
    expect(screen.getByText('HOT')).toBeInTheDocument();
  });

  it('toggles the document theme and persists the choice', () => {
    useThemeStore.setState({ theme: 'dark' });
    useThemeStore.getState().toggle();
    expect(useThemeStore.getState().theme).toBe('light');
    expect(document.documentElement).toHaveAttribute('data-theme', 'light');
    expect(localStorage.getItem('theme')).toBe('light');
    useThemeStore.getState().toggle();
    expect(document.documentElement).not.toHaveAttribute('data-theme');
  });

  it('adds, dismisses, and automatically expires toasts', () => {
    vi.useFakeTimers();
    useToastStore.getState().show('success', 'Saved');
    const firstId = useToastStore.getState().toasts[0]?.id;
    expect(useToastStore.getState().toasts).toHaveLength(1);
    useToastStore.getState().dismiss(firstId!);
    expect(useToastStore.getState().toasts).toHaveLength(0);
    useToastStore.getState().show('info', 'Queued');
    act(() => vi.advanceTimersByTime(3000));
    expect(useToastStore.getState().toasts).toHaveLength(0);
    vi.useRealTimers();
  });
});
