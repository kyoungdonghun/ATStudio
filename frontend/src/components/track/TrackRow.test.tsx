import { fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import TrackRow from '@/components/track/TrackRow';
import { isListeningMode } from '@/config/appMode';
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
  it('groups genre, instrument and mood chips in one desktop cell and preserves usage', () => {
    const tagged = {
      ...track,
      tags: [
        { id: 1, name: 'Jazz', type: 'GENRE' as const },
        { id: 2, name: 'Piano', type: 'INSTRUMENT' as const },
        { id: 3, name: 'Calm', type: 'MOOD' as const },
        { id: 4, name: 'Shorts', type: 'USAGE' as const },
        { id: 5, name: 'Strings', type: 'INSTRUMENT' as const },
      ],
    };
    const { container } = render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow index={1} track={tagged} />
          </tbody>
        </table>
      </MemoryRouter>,
    );
    const cells = screen.getAllByRole('cell');
    expect(cells).toHaveLength(7);
    expect(Array.from(cells[2].children, (chip) => chip.textContent)).toEqual([
      'Jazz',
      'Piano',
      'Strings',
      'Calm',
    ]);
    expect(within(cells[1]).getByText('#Shorts')).toBeInTheDocument();
    const compact = container.querySelector('dl')!;
    expect([...compact.querySelectorAll('dt')].map((node) => node.textContent)).toEqual([
      '장르',
      '악기',
      '분위기',
    ]);
    expect(compact.querySelectorAll('dd')[1]).toHaveTextContent('PianoStrings');
    for (const title of ['Like', 'Add to playlist', 'Download']) {
      if (isListeningMode) expect(screen.queryByTitle(title)).not.toBeInTheDocument();
      else expect(screen.getByTitle(title)).toBeInTheDocument();
    }
  });

  it('keeps the tag cell empty and subsequent columns aligned when tags are empty', () => {
    render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow index={1} track={track} />
          </tbody>
        </table>
      </MemoryRouter>,
    );
    const cells = screen.getAllByRole('cell');
    expect(cells).toHaveLength(7);
    expect(cells[2]).toBeEmptyDOMElement();
    expect(cells[3]).toHaveTextContent('120');
    expect(cells[4]).toHaveTextContent('C');
  });

  it('keeps an instrument-only long name in the desktop cell and compact labeled group', () => {
    const name = 'VeryLongInstrumentName'.repeat(2);
    const { container } = render(
      <MemoryRouter>
        <table>
          <tbody>
            <TrackRow index={1} track={{ ...track, tags: [{ id: 9, name, type: 'INSTRUMENT' }] }} />
          </tbody>
        </table>
      </MemoryRouter>,
    );
    expect(screen.getAllByRole('cell')[2]).toHaveTextContent(name);
    expect(container.querySelector('dl dt')).toHaveTextContent('악기');
    expect(container.querySelector('dl dd')).toHaveTextContent(name);
  });

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
