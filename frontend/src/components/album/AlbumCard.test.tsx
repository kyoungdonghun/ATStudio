import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import AlbumCard from '@/components/album/AlbumCard';
import type { Album } from '@/types';

const album = {
  id: 1,
  title: 'Keyboard Album',
  thumbnailUrl: 'albums/keyboard.jpg',
  trackCount: 2,
  likeCount: 0,
} as Album;

describe('AlbumCard', () => {
  it('uses native buttons and delegates each card action once', () => {
    const onClick = vi.fn();
    const onToggleLike = vi.fn();
    render(<AlbumCard album={album} onClick={onClick} onToggleLike={onToggleLike} />);

    const card = screen.getByRole('button', { name: 'Keyboard Album 앨범 보기' });
    expect(card).toHaveAttribute('type', 'button');
    fireEvent.click(card);
    expect(onClick).toHaveBeenCalledTimes(1);
    expect(onClick).toHaveBeenCalledWith(album);

    const likeButton = screen.getByRole('button', { name: '좋아요' });
    expect(likeButton).toHaveAttribute('type', 'button');
    fireEvent.click(likeButton);
    expect(onToggleLike).toHaveBeenCalledTimes(1);
    expect(onToggleLike).toHaveBeenCalledWith(album.id);
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it('uses the existing safe fallback after a nonempty image fails', () => {
    render(<AlbumCard album={album} onClick={vi.fn()} />);

    fireEvent.error(screen.getByRole('img', { name: album.title }));
    expect(
      screen.getByRole('img', { name: 'Keyboard Album 앨범 커버를 불러올 수 없습니다.' }),
    ).toBeInTheDocument();
  });
});
