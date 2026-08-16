import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import TrackRow from '@/components/track/TrackRow';
import type { TrackListItem } from '@/types';

const track: TrackListItem = {
  id: 1,
  title: 'Visible Track Command',
  artistName: 'Keyboard Artist',
  duration: 30,
  bpm: 120,
  tonality: 'C',
  thumbnail: 'tracks/visible.jpg',
  playCount: 0,
  likeCount: 0,
  downloadCount: 0,
  waveformData: null,
  tags: [],
  createdAt: '2026-08-14T00:00:00Z',
};

describe('TrackRow', () => {
  it('keeps the play command visible and delegates one click through its native button', () => {
    const onPlay = vi.fn();
    render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow index={1} track={track} onPlay={onPlay} />
          </tbody>
        </table>
      </MemoryRouter>,
    );

    const playButton = screen.getByRole('button', { name: 'Play' });
    expect(playButton).toBeVisible();
    expect(playButton).toHaveAttribute('type', 'button');
    fireEvent.click(playButton);
    expect(onPlay).toHaveBeenCalledTimes(1);
    expect(onPlay).toHaveBeenCalledWith(track);
  });

  it('uses the existing safe fallback after a nonempty image fails', () => {
    render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow index={1} track={track} />
          </tbody>
        </table>
      </MemoryRouter>,
    );

    fireEvent.error(screen.getByRole('img', { name: track.title }));
    expect(
      screen.getByRole('img', { name: 'Visible Track Command 음원 커버를 불러올 수 없습니다.' }),
    ).toBeInTheDocument();
  });
});
