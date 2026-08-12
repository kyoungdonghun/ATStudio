import { useState, useEffect, useRef, type FormEvent, type ChangeEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  fetchAlbumDetail,
  updateAlbum,
  addTrackToAlbum,
  removeTrackFromAlbum,
  reorderAlbumTracks,
  type AlbumTrack,
} from '@/api/albums';
import { fetchTracks } from '@/api/tracks';
import { toUploadUrl } from '@/api/client';
import { useToastStore } from '@/store/toastStore';
import type { TrackListItem } from '@/types';
import {
  TITLE_ALBUM_MAX,
  DESCRIPTION_MAX,
  IMAGE_MAX_SIZE_MB,
  isFileSizeOk,
  validateImageDimensions,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import styles from './AlbumEditPage.module.css';

/** Screen L-5: Album edit */
export default function AlbumEditPage() {
  const { albumId: id } = useParams<{ albumId: string }>();
  const navigate = useNavigate();
  const toast = useToastStore((s) => s.show);

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [thumbPreview, setThumbPreview] = useState<string | null>(null);

  /* ── Existing data ── */
  const [currentThumbUrl, setCurrentThumbUrl] = useState<string | null>(null);

  /* ── Track management state ── */
  const [tracks, setTracks] = useState<AlbumTrack[]>([]);
  const [trackBusy, setTrackBusy] = useState(false);

  /* ── Track search state ── */
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<TrackListItem[]>([]);
  const [searchOpen, setSearchOpen] = useState(false);
  const [searching, setSearching] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  /* ── UI state ── */
  const [pageLoading, setPageLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Load existing album ── */
  useEffect(() => {
    let cancelled = false;

    async function loadAlbum() {
      if (!id) return;

      try {
        const album = await fetchAlbumDetail(Number(id));
        if (cancelled) return;

        setTitle(album.title);
        setDescription(album.description ?? '');
        setCurrentThumbUrl(toUploadUrl(album.thumbnailUrl));
        setTracks([...album.tracks].sort((a, b) => a.order - b.order));
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : '앨범 정보를 불러올 수 없습니다.');
        }
      } finally {
        if (!cancelled) setPageLoading(false);
      }
    }

    loadAlbum();
    return () => {
      cancelled = true;
    };
  }, [id]);

  /* ── Close search dropdown on outside click ── */
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
        setSearchOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  /* ── Refetch album tracks ── */
  async function refetchTracks() {
    if (!id) return;
    try {
      const album = await fetchAlbumDetail(Number(id));
      setTracks([...album.tracks].sort((a, b) => a.order - b.order));
    } catch {
      /* silent — list will be stale but non-critical */
    }
  }

  /* ── Track search handler ── */
  async function handleTrackSearch() {
    const kw = searchKeyword.trim();
    if (!kw) return;

    setSearching(true);
    try {
      const res = await fetchTracks({ keyword: kw, size: 10 });
      setSearchResults(res.dataList);
      setSearchOpen(true);
    } catch {
      toast('error', '트랙 검색에 실패했습니다.');
    } finally {
      setSearching(false);
    }
  }

  /* ── Add track ── */
  async function handleAddTrack(trackId: number) {
    if (!id) return;
    if (tracks.some((t) => t.trackId === trackId)) {
      toast('error', '이미 앨범에 포함된 트랙입니다.');
      return;
    }
    setTrackBusy(true);
    try {
      await addTrackToAlbum(Number(id), trackId);
      await refetchTracks();
      toast('success', '트랙이 추가되었습니다.');
      setSearchOpen(false);
      setSearchKeyword('');
      setSearchResults([]);
    } catch (err) {
      const msg = err instanceof Error ? err.message : '트랙 추가에 실패했습니다.';
      toast('error', msg);
    } finally {
      setTrackBusy(false);
    }
  }

  /* ── Remove track ── */
  async function handleRemoveTrack(trackId: number) {
    if (!id) return;
    setTrackBusy(true);
    try {
      await removeTrackFromAlbum(Number(id), trackId);
      await refetchTracks();
      toast('success', '트랙이 제거되었습니다.');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '트랙 제거에 실패했습니다.';
      toast('error', msg);
    } finally {
      setTrackBusy(false);
    }
  }

  /* ── Reorder tracks (move up / down) ── */
  async function handleMoveTrack(index: number, direction: 'up' | 'down') {
    if (!id) return;
    const swapIdx = direction === 'up' ? index - 1 : index + 1;
    if (swapIdx < 0 || swapIdx >= tracks.length) return;

    const reordered = [...tracks];
    [reordered[index], reordered[swapIdx]] = [reordered[swapIdx], reordered[index]];

    const trackOrders = reordered.map((t, i) => ({ trackId: t.trackId, order: i }));

    /* Optimistic update */
    setTracks(reordered);
    setTrackBusy(true);
    try {
      await reorderAlbumTracks(Number(id), trackOrders);
      await refetchTracks();
    } catch (err) {
      const msg = err instanceof Error ? err.message : '순서 변경에 실패했습니다.';
      toast('error', msg);
      await refetchTracks();
    } finally {
      setTrackBusy(false);
    }
  }

  /* ── Thumbnail handler ── */
  async function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    if (!file) {
      setThumbnail(null);
      if (thumbPreview) URL.revokeObjectURL(thumbPreview);
      setThumbPreview(null);
      return;
    }

    if (!isFileSizeOk(file, IMAGE_MAX_SIZE_MB)) {
      setError(`썸네일 이미지는 ${IMAGE_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다.`);
      e.target.value = '';
      return;
    }

    const dimError = await validateImageDimensions(file);
    if (dimError) {
      setError(dimError);
      e.target.value = '';
      return;
    }

    setError(null);
    setThumbnail(file);

    if (thumbPreview) {
      URL.revokeObjectURL(thumbPreview);
    }
    setThumbPreview(URL.createObjectURL(file));
  }

  /* ── Displayed thumbnail: new preview > current ── */
  const displayThumb = thumbPreview ?? currentThumbUrl;

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!id) return;

    if (!title.trim()) {
      setError('앨범 제목을 입력해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    formData.append('description', description.trim());
    if (thumbnail) {
      formData.append('thumbnailFile', thumbnail);
    }

    setSubmitting(true);
    try {
      await updateAlbum(Number(id), formData);
      navigate('/admin/albums');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '앨범 수정에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  if (pageLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'Loading...'}</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'앨범 수정'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* Title */}
        <div className={styles.field}>
          <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
          <input
            className={styles.input}
            type="text"
            maxLength={TITLE_ALBUM_MAX}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        {/* Description */}
        <div className={styles.field}>
          <label className={styles.label}>{'설명'}</label>
          <textarea
            className={styles.textarea}
            maxLength={DESCRIPTION_MAX}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        {/* Thumbnail */}
        <div className={styles.field}>
          <span className={styles.label}>{'썸네일'}</span>
          <div className={styles.thumbArea}>
            {displayThumb && (
              <img src={displayThumb} alt="앨범 썸네일" className={styles.thumbPreview} />
            )}
            <label className={`${styles.fileLabel} ${thumbnail ? styles.fileLabelSelected : ''}`}>
              <input
                type="file"
                accept="image/*"
                className={styles.fileHidden}
                onChange={handleThumbnailChange}
              />
              {thumbnail ? thumbnail.name : '새 이미지 선택'}
            </label>
            {currentThumbUrl && !thumbnail && (
              <span className={styles.currentFile}>{'현재 썸네일이 설정되어 있습니다'}</span>
            )}
          </div>
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={submitting}>
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting}>
            {'저장'}
          </Button>
        </div>
      </form>

      {/* ── Track management (separate from form) ── */}
      <section className={styles.trackSection}>
        <h2 className={styles.trackSectionTitle}>{'앨범 트랙'}</h2>

        {/* Search */}
        <div className={styles.trackSearch} ref={searchRef}>
          <div className={styles.trackSearchRow}>
            <input
              className={styles.input}
              type="text"
              placeholder="트랙 검색 (제목 또는 아티스트)"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleTrackSearch();
              }}
            />
            <Button
              type="button"
              variant="ghost"
              onClick={handleTrackSearch}
              loading={searching}
              disabled={trackBusy}
            >
              {'검색'}
            </Button>
          </div>

          {searchOpen && searchResults.length > 0 && (
            <ul className={styles.searchDropdown}>
              {searchResults.map((t) => (
                <li key={t.id} className={styles.searchItem}>
                  <span className={styles.searchItemInfo}>
                    <span className={styles.searchItemTitle}>{t.title}</span>
                    <span className={styles.searchItemArtist}>{t.artistName}</span>
                  </span>
                  <button
                    type="button"
                    className={styles.addBtn}
                    disabled={trackBusy || tracks.some((at) => at.trackId === t.id)}
                    onClick={() => handleAddTrack(t.id)}
                  >
                    {tracks.some((at) => at.trackId === t.id) ? '추가됨' : '+ 추가'}
                  </button>
                </li>
              ))}
            </ul>
          )}

          {searchOpen && searchResults.length === 0 && !searching && (
            <div className={styles.searchEmpty}>{'검색 결과가 없습니다.'}</div>
          )}
        </div>

        {/* Track list */}
        {tracks.length === 0 ? (
          <p className={styles.trackEmpty}>{'앨범에 트랙이 없습니다.'}</p>
        ) : (
          <ul className={styles.trackList}>
            {tracks.map((t, idx) => (
              <li key={t.trackId} className={styles.trackRow}>
                <span className={styles.trackOrder}>{idx + 1}</span>
                <span className={styles.trackInfo}>
                  <span className={styles.trackTitle}>{t.title}</span>
                  <span className={styles.trackArtist}>{t.artistName}</span>
                </span>
                <div className={styles.trackActions}>
                  <button
                    type="button"
                    className={styles.moveBtn}
                    disabled={idx === 0 || trackBusy}
                    onClick={() => handleMoveTrack(idx, 'up')}
                    title="위로"
                  >
                    {'\u25B2'}
                  </button>
                  <button
                    type="button"
                    className={styles.moveBtn}
                    disabled={idx === tracks.length - 1 || trackBusy}
                    onClick={() => handleMoveTrack(idx, 'down')}
                    title="아래로"
                  >
                    {'\u25BC'}
                  </button>
                  <button
                    type="button"
                    className={styles.removeBtn}
                    disabled={trackBusy}
                    onClick={() => handleRemoveTrack(t.trackId)}
                    title="제거"
                  >
                    {'\u2715'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
