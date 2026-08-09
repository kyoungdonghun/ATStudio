import { describe, expect, it } from 'vitest';
import type { AlbumTrack } from '@/api/albums';
import type { PlaylistTrack } from '@/api/playlists';
import type { LikeItem } from '@/types';
import { toPlayableTrack } from '@/utils/playableTrack';

describe('toPlayableTrack', () => {
  it('maps the common persisted fields without invented defaults', () => {
    expect(
      toPlayableTrack({
        trackId: 7,
        title: 'Track',
        artistName: 'Artist',
        duration: 187,
        thumbnailUrl: 'tracks/thumbnails/7.jpg',
        waveformData: '[0.1,0.9]',
      }),
    ).toEqual({
      id: 7,
      title: 'Track',
      artistName: 'Artist',
      duration: 187,
      thumbnail: 'tracks/thumbnails/7.jpg',
      waveformData: '[0.1,0.9]',
    });
  });

  it('preserves optional playback metadata only when the source supplies it', () => {
    const tags = [{ id: 1, name: 'Shorts', type: 'USAGE' as const }];

    expect(
      toPlayableTrack({
        id: 8,
        title: 'Tagged Track',
        artistName: 'Artist',
        duration: 205,
        thumbnail: null,
        waveformData: null,
        bpm: 122,
        tonality: 'Am',
        tags,
      }),
    ).toMatchObject({ bpm: 122, tonality: 'Am', tags });
  });

  it('normalizes omitted nullable fields from album, playlist, and like wire objects', () => {
    const albumTrack: AlbumTrack = {
      trackId: 9,
      title: 'Album Track',
      artistName: 'Album Artist',
      duration: 91,
      order: 1,
    };
    const playlistTrack: PlaylistTrack = {
      trackOrder: 1,
      trackId: 10,
      title: 'Playlist Track',
      artistName: 'Playlist Artist',
      duration: 102,
      bpm: 118,
      tonality: 'Dm',
    };
    const likeItem: LikeItem = {
      trackId: 11,
      title: 'Liked Track',
      artistName: 'Liked Artist',
      duration: 113,
      bpm: 124,
      tonality: 'F',
      createdAt: '2026-08-09T00:00:00Z',
    };

    expect([albumTrack, playlistTrack, likeItem].map((source) => toPlayableTrack(source))).toEqual([
      {
        id: 9,
        title: 'Album Track',
        artistName: 'Album Artist',
        duration: 91,
        thumbnail: null,
        waveformData: null,
      },
      {
        id: 10,
        title: 'Playlist Track',
        artistName: 'Playlist Artist',
        duration: 102,
        thumbnail: null,
        waveformData: null,
        bpm: 118,
        tonality: 'Dm',
      },
      {
        id: 11,
        title: 'Liked Track',
        artistName: 'Liked Artist',
        duration: 113,
        thumbnail: null,
        waveformData: null,
        bpm: 124,
        tonality: 'F',
      },
    ]);
  });

  it('keeps positive integer identity validation when nullable keys are omitted', () => {
    const source = {
      trackId: 0,
      title: 'Incomplete',
      artistName: 'Artist',
      duration: 100,
    };

    expect(() => toPlayableTrack(source)).toThrow(TypeError);
  });
});
