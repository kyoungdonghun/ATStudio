import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({ toUploadUrl: (value: string | null) => value }));

import TagFilterModal from '@/components/filter/TagFilterModal';
import TrackRow from '@/components/track/TrackRow';
import type { TagItem, TrackListItem } from '@/types';

const genreTags: TagItem[] = [
  { id: 1, name: 'Rock', type: 'GENRE' },
  { id: 2, name: 'Jazz', type: 'GENRE' },
];
const moodTags: TagItem[] = [{ id: 3, name: 'Bright', type: 'MOOD' }];
const usageTags: TagItem[] = [{ id: 4, name: 'Shorts', type: 'USAGE' }];

describe('catalog interaction components', () => {
  it('edits, clears, filters, and applies tag selections', () => {
    vi.useFakeTimers();
    const onApply = vi.fn();
    const onClose = vi.fn();
    render(
      <TagFilterModal
        open
        onClose={onClose}
        genreTags={genreTags}
        moodTags={moodTags}
        usageTags={usageTags}
        activeGenres={['Rock']}
        activeMoods={[]}
        activeUsages={[]}
        activeBpmLabel="80-99"
        bpmPresets={[{ label: '80-99' }, { label: '100-119' }]}
        onApply={onApply}
      />,
    );
    vi.advanceTimersByTime(100);

    fireEvent.click(screen.getByRole('button', { name: 'Jazz' }));
    fireEvent.click(screen.getByRole('button', { name: 'Bright' }));
    fireEvent.click(screen.getByRole('button', { name: '#Shorts' }));
    fireEvent.click(screen.getByRole('button', { name: '100-119' }));
    const footerButtons = screen.getAllByRole('button');
    fireEvent.click(footerButtons[footerButtons.length - 1]!);
    expect(onApply).toHaveBeenCalledWith(['Rock', 'Jazz'], ['Bright'], ['Shorts'], '100-119');
    expect(onClose).toHaveBeenCalledOnce();
    vi.useRealTimers();
  });

  it('searches tags, clears the query, and resets every active selection', () => {
    const onApply = vi.fn();
    render(
      <TagFilterModal
        open
        onClose={vi.fn()}
        genreTags={genreTags}
        moodTags={moodTags}
        usageTags={usageTags}
        activeGenres={['Rock']}
        activeMoods={['Bright']}
        activeUsages={['Shorts']}
        activeBpmLabel="80-99"
        bpmPresets={[{ label: '80-99' }]}
        onApply={onApply}
      />,
    );

    const search = screen.getByRole('textbox');
    fireEvent.change(search, { target: { value: 'missing' } });
    expect(screen.queryByRole('button', { name: 'Rock' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '✕' }));
    expect(screen.getByRole('button', { name: 'Rock' })).toBeInTheDocument();
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[buttons.length - 2]!);
    fireEvent.click(buttons[buttons.length - 1]!);
    expect(onApply).toHaveBeenCalledWith([], [], [], '');
  });

  it('renders track metadata and dispatches authenticated actions', () => {
    const track: TrackListItem = {
      id: 11,
      title: 'Spring Drive',
      artistName: 'AT.M',
      duration: 125,
      bpm: 110,
      tonality: 'C#',
      thumbnail: '/cover.png',
      playCount: 3,
      likeCount: 4,
      downloadCount: 5,
      tags: [...genreTags.slice(0, 1), ...moodTags, ...usageTags],
      createdAt: '2026-07-17',
    };
    const onPlay = vi.fn();
    const onLike = vi.fn();
    const onAdd = vi.fn();
    const onDownload = vi.fn();
    render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow
              index={1}
              track={track}
              playing
              liked
              badge="hot"
              onPlay={onPlay}
              onLike={onLike}
              onAddToPlaylist={onAdd}
              onDownload={onDownload}
            />
          </tbody>
        </table>
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: 'Spring Drive' })).toHaveAttribute(
      'href',
      '/tracks/11',
    );
    expect(screen.getByText('#Shorts')).toBeInTheDocument();
    expect(screen.getByText('2:05')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Pause' }));
    fireEvent.click(screen.getByTitle('Like'));
    fireEvent.click(screen.getByTitle('Add to playlist'));
    fireEvent.click(screen.getByTitle('Download'));
    expect(onPlay).toHaveBeenCalledWith(track);
    expect(onLike).toHaveBeenCalledWith(track);
    expect(onAdd).toHaveBeenCalledWith(track);
    expect(onDownload).toHaveBeenCalledWith(track);
  });

  it('routes every protected action through the guest handler', () => {
    const onGuestAction = vi.fn();
    const track: TrackListItem = {
      id: 12,
      title: 'Untitled',
      artistName: '',
      duration: 0,
      bpm: 0,
      tonality: '',
      thumbnail: null,
      playCount: 0,
      likeCount: 0,
      downloadCount: 0,
      tags: [],
      createdAt: '2026-07-17',
    };
    render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow
              index={2}
              track={track}
              showAuthActions={false}
              onGuestAction={onGuestAction}
            />
          </tbody>
        </table>
      </MemoryRouter>,
    );
    expect(screen.getByText('-')).toBeInTheDocument();
    fireEvent.click(screen.getByTitle('Like'));
    fireEvent.click(screen.getByTitle('Add to playlist'));
    fireEvent.click(screen.getByTitle('Download'));
    expect(onGuestAction).toHaveBeenCalledTimes(3);
  });
});
