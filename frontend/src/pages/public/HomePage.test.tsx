import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import HomePage from '@/pages/public/HomePage';
import type { PagedResponse, TagItem, TrackListItem } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchAlbums: vi.fn(),
  fetchTracks: vi.fn(),
  fetchTags: vi.fn(),
  fetchAvailableTags: vi.fn(),
}));

vi.mock('@/api/albums', () => ({
  fetchAlbums: (...args: unknown[]) => mocks.fetchAlbums(...args),
}));

vi.mock('@/api/tracks', () => ({
  fetchTracks: (...args: unknown[]) => mocks.fetchTracks(...args),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
  fetchAvailableTags: (...args: unknown[]) => mocks.fetchAvailableTags(...args),
}));

vi.mock('@/components/album/AlbumCard', () => ({
  default: () => <div>album</div>,
}));

const emptyPage: PagedResponse<TrackListItem> = {
  dataList: [],
  pageInfo: {
    page: 1,
    size: 20,
    total: 0,
    start: 0,
    end: 0,
    prev: false,
    next: false,
  },
};

function tag(id: number, name: string, type: TagItem['type']): TagItem {
  return { id, name, type };
}

function renderHome() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <HomePage />
    </MemoryRouter>,
  );
}

describe('HomePage tag discovery', () => {
  beforeEach(() => {
    mocks.fetchAlbums.mockReset();
    mocks.fetchTracks.mockReset();
    mocks.fetchTags.mockReset();
    mocks.fetchAvailableTags.mockReset();
    mocks.fetchAlbums.mockResolvedValue(emptyPage);
    mocks.fetchTracks.mockResolvedValue(emptyPage);
    mocks.fetchTags.mockResolvedValue([]);
    mocks.fetchAvailableTags.mockResolvedValue([]);
  });

  it('uses the creator audience copy in the hero and footer', () => {
    renderHome();

    const heroTitle = screen.getByRole('heading', { level: 1 });
    const footer = screen.getByRole('contentinfo');

    expect(heroTitle).toHaveTextContent(/창작자를 위한\s*최고의 음악/);
    expect(
      screen.getByText(/창작자를 위한 고품질 라이선스 음악\.\s*구독 하나로 무제한 사용하세요\./),
    ).toBeInTheDocument();
    expect(footer).toHaveTextContent(/창작자를 위한\s*음악 라이선스 플랫폼/);
    expect(heroTitle).not.toHaveTextContent('쇼츠를 위한');
    expect(screen.queryByText('크리에이터를 위한 고품질 라이선스 음악.')).not.toBeInTheDocument();
    expect(footer).not.toHaveTextContent('쇼츠 크리에이터를 위한');
  });

  it('keeps Usage first, falls back to the first category with results, and supports roving focus', async () => {
    const registered = [
      tag(1, 'Shorts', 'USAGE'),
      tag(2, 'K-Pop', 'GENRE'),
      tag(3, '차분함', 'MOOD'),
      tag(4, 'Piano', 'INSTRUMENT'),
    ];
    mocks.fetchTags.mockResolvedValue(registered);
    mocks.fetchAvailableTags.mockResolvedValue(registered.slice(1));
    renderHome();

    const tabs = await screen.findAllByRole('tab');
    expect(tabs.map((tabElement) => tabElement.textContent)).toEqual([
      '용도',
      '장르',
      '분위기',
      '악기',
    ]);
    expect(tabs[0]).toHaveAttribute('aria-selected', 'false');
    expect(tabs[1]).toHaveAttribute('aria-selected', 'true');
    expect(screen.queryByText(/License/i)).not.toBeInTheDocument();

    tabs[1]?.focus();
    fireEvent.keyDown(tabs[1]!, { key: 'End' });
    expect(tabs[3]).toHaveFocus();
    expect(tabs[3]).toHaveAttribute('aria-selected', 'true');
    fireEvent.keyDown(tabs[3]!, { key: 'Home' });
    expect(tabs[0]).toHaveFocus();
    fireEvent.keyDown(tabs[0]!, { key: 'ArrowLeft' });
    expect(tabs[3]).toHaveFocus();

    fireEvent.click(tabs[0]!);
    expect(screen.getByText('활성 음원에 연결된 용도 태그가 아직 없습니다.')).toBeInTheDocument();
  });

  it('distinguishes no registered tags', async () => {
    renderHome();

    expect(await screen.findByText('등록된 태그가 아직 없습니다.')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: '용도' })).toBeInTheDocument();
  });

  it('distinguishes registered tags without an active-track result', async () => {
    mocks.fetchTags.mockResolvedValue([tag(1, 'Shorts', 'USAGE')]);
    renderHome();

    expect(
      await screen.findByText('활성 음원에 연결된 용도 태그가 아직 없습니다.'),
    ).toBeInTheDocument();
  });

  it('distinguishes API failure and retries the tag module only', async () => {
    mocks.fetchAvailableTags.mockRejectedValueOnce(new Error('unavailable'));
    renderHome();

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '태그 탐색 정보를 불러오지 못했습니다.',
    );
    mocks.fetchTags.mockResolvedValueOnce([tag(1, 'Piano', 'INSTRUMENT')]);
    mocks.fetchAvailableTags.mockResolvedValueOnce([tag(1, 'Piano', 'INSTRUMENT')]);
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByRole('button', { name: 'Piano' })).toBeInTheDocument();
    expect(mocks.fetchAlbums).toHaveBeenCalledTimes(2);
    expect(mocks.fetchAvailableTags).toHaveBeenCalledTimes(2);
  });

  it('encodes Korean, spaces, commas, and hashes while preserving repeated AND values', async () => {
    const usages = [
      tag(1, '한글 태그', 'USAGE'),
      tag(2, 'space value', 'USAGE'),
      tag(3, 'comma,value', 'USAGE'),
      tag(4, 'hash#value', 'USAGE'),
    ];
    mocks.fetchTags.mockResolvedValue(usages);
    mocks.fetchAvailableTags.mockResolvedValue(usages);
    renderHome();

    for (const name of ['#한글 태그', '#space value', '#comma,value', '#hash#value']) {
      fireEvent.click(await screen.findByRole('button', { name }));
    }

    const link = screen.getByRole('link', { name: '선택한 태그로 탐색' });
    const href = link.getAttribute('href')!;
    const values = new URLSearchParams(href.split('?')[1]).getAll('usage');
    expect(values).toHaveLength(4);
    expect(values).toEqual(expect.arrayContaining(usages.map((usage) => usage.name)));
    expect(href).toContain('%ED%95%9C%EA%B8%80+%ED%83%9C%EA%B7%B8');
    expect(href).toContain('space+value');
    expect(href).toContain('comma%2Cvalue');
    expect(href).toContain('hash%23value');
  });

  it('bounds initial tags and exposes a clear more interaction', async () => {
    const instruments = Array.from({ length: 10 }, (_, index) =>
      tag(index + 1, `Instrument ${String(index + 1).padStart(2, '0')}`, 'INSTRUMENT'),
    );
    mocks.fetchTags.mockResolvedValue(instruments);
    mocks.fetchAvailableTags.mockResolvedValue(instruments);
    renderHome();

    expect(await screen.findByRole('button', { name: '더보기 (2)' })).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Instrument 10' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '더보기 (2)' }));
    expect(screen.getByRole('button', { name: 'Instrument 10' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '접기' }));
    await waitFor(() =>
      expect(screen.queryByRole('button', { name: 'Instrument 10' })).not.toBeInTheDocument(),
    );
  });
});
