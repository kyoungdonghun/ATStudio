import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import client from '@/api/client';
import Header from '@/layouts/Header';
import TrackDetailPage from '@/pages/public/TrackDetailPage';
import LikeListPage from '@/pages/subscriber/LikeListPage';
import PlaylistListPage from '@/pages/subscriber/PlaylistListPage';
import PlaylistDetailPage from '@/pages/subscriber/PlaylistDetailPage';
import { useAuthStore } from '@/store/authStore';
import { useLikeStore } from '@/store/likeStore';
import { loadPlayHistory, usePlayerStore } from '@/store/playerStore';
import type { User } from '@/types';

vi.mock('@/config/pilotMode', () => ({ isPilotMode: true }));

const adapter = client.defaults.adapter;
afterEach(() => {
  client.defaults.adapter = adapter;
  useAuthStore.getState().clearSession();
  vi.restoreAllMocks();
});

function login(id: number) {
  act(() => useAuthStore.getState().login('tester' + id, { id, role: 'USER' } as User));
}

it('likes a public song, creates a first playlist, adds and plays it across logins without showing the other user’s library', async () => {
  const track = {
    id: 7,
    trackId: 7,
    title: '첫 공개곡',
    artistName: '시험',
    duration: 90,
    bpm: 90,
    tonality: 'C',
    description: '',
    playCount: 0,
    tags: [],
    createdAt: '2026-11-23',
  };
  const publicPlayback = {
    ...track,
    id: 8,
    title: '브라우저 공개 청취',
    thumbnail: null,
    waveformData: null,
  };
  const playlist = { id: 5, title: 'A의 보관함', createdAt: '2026-11-23', trackCount: 0 };
  let liked = false;
  let created = false;
  let added = false;
  client.defaults.adapter = async (config) => {
    const path = config.url;
    const method = config.method;
    const authorization = config.headers.get('Authorization');
    const isA = authorization === 'Bearer tester1';
    let body: object;
    if (path === '/tracks/7' && method === 'get') {
      expect(authorization).toBeUndefined();
      body = { data: track };
    } else if (path === '/tracks/batch' && method === 'post') {
      expect(authorization).toBeUndefined();
      expect(JSON.parse(config.data)).toEqual({ ids: [8] });
      body = { dataList: [publicPlayback] };
    } else {
      expect(['Bearer tester1', 'Bearer tester2']).toContain(authorization);
      if (path === '/likes' && method === 'get') body = { dataList: isA && liked ? [track] : [] };
      else if (path === '/likes/7' && method === 'post' && isA) {
        liked = true;
        body = {};
      } else if (path === '/playlists' && method === 'get') {
        body = { dataList: isA && created ? [playlist] : [] };
      } else if (path === '/playlists/capacity' && method === 'get') {
        body = { data: { maxPlaylists: 3 } };
      } else if (path === '/playlists' && method === 'post' && isA) {
        expect((config.data as FormData).get('title')).toBe(playlist.title);
        created = true;
        body = { data: playlist };
      } else if (path === '/playlists/5/tracks' && method === 'post' && isA) {
        expect(JSON.parse(config.data)).toEqual({ trackId: 7 });
        added = true;
        body = {};
      } else if (path === '/playlists/5' && method === 'get' && isA) {
        body = { data: { ...playlist, tracks: added ? [track] : [] } };
      } else throw new Error('Unexpected ' + method + ' ' + path);
    }
    return { config, status: 200, statusText: 'OK', headers: {}, data: body };
  };

  usePlayerStore.getState().play(publicPlayback);
  await waitFor(() => expect(loadPlayHistory().map(({ track }) => track.id)).toEqual([8]));
  login(1);
  render(
    <MemoryRouter initialEntries={['/tracks/7']}>
      <Header />
      <Routes>
        <Route path="/tracks/:trackId" element={<TrackDetailPage />} />
        <Route path="/likes" element={<LikeListPage />} />
        <Route path="/playlists" element={<PlaylistListPage />} />
        <Route path="/playlists/:playlistId" element={<PlaylistDetailPage />} />
      </Routes>
    </MemoryRouter>,
  );
  fireEvent.click(await screen.findByRole('button', { name: /좋아요/ }));
  await waitFor(() => expect(liked).toBe(true));
  fireEvent.click(screen.getByRole('button', { name: /재생목록에 추가/ }));
  fireEvent.click(await screen.findByRole('link', { name: '새 재생목록 만들기' }));
  fireEvent.change(await screen.findByPlaceholderText('재생목록 이름'), {
    target: { value: playlist.title },
  });
  expect(screen.queryByText('썸네일 (선택)')).toBeNull();
  fireEvent.click(screen.getByRole('button', { name: '만들기' }));
  expect(await screen.findByRole('button', { name: 'A의 보관함 재생목록 보기' })).toBeVisible();

  fireEvent.click(screen.getByRole('link', { name: '좋아요' }));
  expect(await screen.findByRole('link', { name: track.title })).toBeVisible();
  expect(screen.queryByRole('button', { name: '앨범' })).toBeNull();
  fireEvent.click(screen.getByTitle('재생목록에 추가'));
  const dialog = within(await screen.findByRole('dialog'));
  fireEvent.click(await dialog.findByRole('button', { name: /A의 보관함/ }));
  fireEvent.click(dialog.getByRole('button', { name: '추가' }));
  await waitFor(() => expect(added).toBe(true));
  expect(await dialog.findByText('추가되었습니다!')).toBeVisible();

  login(2);
  expect(await screen.findByText('좋아요한 음원이 없습니다.')).toBeVisible();
  expect(screen.queryByRole('link', { name: track.title })).toBeNull();
  expect(useLikeStore.getState().likedIds.size).toBe(0);
  fireEvent.click(screen.getByRole('link', { name: '내 재생목록' }));
  expect(await screen.findByRole('button', { name: '새 재생목록' })).toBeVisible();
  expect(screen.queryByRole('button', { name: /A의 보관함/ })).toBeNull();
  login(1);
  fireEvent.click(await screen.findByRole('button', { name: 'A의 보관함 재생목록 보기' }));
  fireEvent.click(await screen.findByRole('button', { name: '전체 재생' }));
  await waitFor(() => expect(loadPlayHistory().map(({ track }) => track.id)).toEqual([7]));
  expect(usePlayerStore.getState().queue.map((t) => t.id)).toEqual([7]);
  expect(JSON.parse(localStorage.getItem('playerState')!).queueTrackIds).toEqual([8]);
  expect(JSON.parse(localStorage.getItem('playHistory')!)).toHaveLength(1);
  expect(localStorage.getItem('playHistory')).not.toContain(track.title);
  fireEvent.click(screen.getByRole('link', { name: '내 재생목록' }));
  login(2);
  await waitFor(() => expect(usePlayerStore.getState().queue.map((t) => t.id)).toEqual([8]));
  expect(loadPlayHistory().map(({ track }) => track.id)).toEqual([8]);
  expect(usePlayerStore.getState().trackListContext).toEqual([]);
}, 20000);

it('ignores the previous user’s delayed unlike failure after the browser switches accounts', async () => {
  let fail!: (error: Error) => void;
  client.defaults.adapter = async (config) => {
    if (config.method === 'delete')
      return new Promise((_, reject) => {
        fail = reject;
      });
    const ownerA = config.headers.get('Authorization') === 'Bearer tester1';
    return {
      config,
      status: 200,
      statusText: 'OK',
      headers: {},
      data: { dataList: ownerA ? [{ trackId: 7 }] : [{ trackId: 8 }] },
    };
  };
  login(1);
  await useLikeStore.getState().load();
  const removal = useLikeStore.getState().toggle(7);
  await waitFor(() => expect(fail).toBeDefined());
  login(2);
  await useLikeStore.getState().load();
  fail(new Error('late failure'));
  await removal;
  expect([...useLikeStore.getState().likedIds]).toEqual([8]);
});
