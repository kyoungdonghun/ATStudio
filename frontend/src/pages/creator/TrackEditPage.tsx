import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchTrackDetail, updateTrack } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import type { TagItem } from '@/types';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import styles from './TrackEditPage.module.css';

const TONALITIES = [
  'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B',
  'Cm', 'C#m', 'Dm', 'D#m', 'Em', 'Fm', 'F#m', 'Gm', 'G#m', 'Am', 'A#m', 'Bm',
];

/** Screen 7: Track edit */
export default function TrackEditPage() {
  const { trackId } = useParams<{ trackId: string }>();
  const navigate = useNavigate();

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [bpm, setBpm] = useState('');
  const [tonality, setTonality] = useState('');
  const [description, setDescription] = useState('');
  const [isActive, setIsActive] = useState(false);
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  /* ── Existing file paths ── */
  const [currentAudioFile, setCurrentAudioFile] = useState<string | null>(null);
  const [currentThumbnail, setCurrentThumbnail] = useState<string | null>(null);

  /* ── Tag data ── */
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [instrumentTags, setInstrumentTags] = useState<TagItem[]>([]);

  /* ── UI state ── */
  const [pageLoading, setPageLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Load existing track + tags ── */
  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      if (!trackId) return;

      try {
        const [track, genres, moods, instruments] = await Promise.all([
          fetchTrackDetail(Number(trackId)),
          fetchTags('GENRE'),
          fetchTags('MOOD'),
          fetchTags('INSTRUMENT'),
        ]);

        if (cancelled) return;

        setTitle(track.title);
        setBpm(String(track.bpm));
        setTonality(track.tonality);
        setDescription(track.description ?? '');
        setIsActive(track.isActive);
        setCurrentAudioFile(track.audioFile);
        setCurrentThumbnail(track.thumbnail);
        setSelectedTagIds(track.tags.map((t) => t.id));

        setGenreTags(genres);
        setMoodTags(moods);
        setInstrumentTags(instruments);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : '데이터를 불러올 수 없습니다.');
        }
      } finally {
        if (!cancelled) setPageLoading(false);
      }
    }

    loadData();
    return () => { cancelled = true; };
  }, [trackId]);

  /* ── Tag toggle ── */
  function toggleTag(tagId: number) {
    setSelectedTagIds((prev) =>
      prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId],
    );
  }

  /* ── File handlers ── */
  function handleAudioChange(e: ChangeEvent<HTMLInputElement>) {
    setAudioFile(e.target.files?.[0] ?? null);
  }

  function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    setThumbnail(e.target.files?.[0] ?? null);
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!trackId) return;

    const formData = new FormData();
    if (title.trim()) formData.append('title', title.trim());
    if (bpm) formData.append('bpm', bpm);
    if (tonality) formData.append('tonality', tonality);
    formData.append('description', description.trim());
    formData.append('isActive', String(isActive));

    if (audioFile) {
      formData.append('audioFile', audioFile);
    }
    if (thumbnail) {
      formData.append('thumbnail', thumbnail);
    }
    selectedTagIds.forEach((tagId) => {
      formData.append('tagIds', String(tagId));
    });

    setSubmitting(true);
    try {
      await updateTrack(Number(trackId), formData);
      navigate('/admin/track-manage');
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : '음원 수정에 실패했습니다.';
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
      <h1 className={styles.pageTitle}>{'음원 수정'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* File uploads */}
        <div className={styles.fileRow}>
          <div className={styles.fileBox}>
            <div className={styles.field}>
              <span className={styles.label}>{'오디오 파일'}</span>
              <label
                className={`${styles.fileLabel} ${audioFile ? styles.fileLabelSelected : ''}`}
              >
                <input
                  type="file"
                  accept="audio/*"
                  className={styles.fileHidden}
                  onChange={handleAudioChange}
                />
                {audioFile ? audioFile.name : '새 파일 선택'}
              </label>
              {currentAudioFile && !audioFile && (
                <span className={styles.currentFile}>
                  {'현재: '}{currentAudioFile.split('/').pop()}
                </span>
              )}
            </div>
          </div>

          <div className={styles.fileBox}>
            <div className={styles.field}>
              <span className={styles.label}>{'썸네일'}</span>
              <label
                className={`${styles.fileLabel} ${thumbnail ? styles.fileLabelSelected : ''}`}
              >
                <input
                  type="file"
                  accept="image/*"
                  className={styles.fileHidden}
                  onChange={handleThumbnailChange}
                />
                {thumbnail ? thumbnail.name : '새 이미지 선택'}
              </label>
              {currentThumbnail && !thumbnail && (
                <span className={styles.currentFile}>
                  {'현재: '}{currentThumbnail.split('/').pop()}
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Title */}
        <div className={styles.field}>
          <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
          <input
            className={styles.input}
            type="text"
            maxLength={100}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        {/* BPM + Tonality */}
        <div className={styles.numberRow}>
          <div className={styles.field}>
            <label className={`${styles.label} ${styles.required}`}>BPM</label>
            <input
              className={styles.input}
              type="number"
              min={1}
              max={999}
              value={bpm}
              onChange={(e) => setBpm(e.target.value)}
            />
          </div>
          <div className={styles.field}>
            <label className={`${styles.label} ${styles.required}`}>{'조성'}</label>
            <select
              className={styles.select}
              value={tonality}
              onChange={(e) => setTonality(e.target.value)}
            >
              <option value="">{'선택'}</option>
              {TONALITIES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Description */}
        <div className={styles.field}>
          <label className={styles.label}>{'설명'}</label>
          <textarea
            className={styles.textarea}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>

        {/* Active toggle */}
        <div className={styles.field}>
          <span className={styles.label}>{'활성화 상태'}</span>
          <div className={styles.toggleRow}>
            <button
              type="button"
              className={`${styles.toggle} ${isActive ? styles.toggleActive : ''}`}
              onClick={() => setIsActive((v) => !v)}
              aria-label="Toggle active"
            />
            <span className={styles.toggleLabel}>
              {isActive ? '활성' : '비활성'}
            </span>
          </div>
        </div>

        {/* Tags */}
        <div className={styles.tagSection}>
          <span className={styles.label}>{'태그'}</span>

          {genreTags.length > 0 && (
            <div className={styles.tagGroup}>
              <span className={styles.tagGroupLabel}>{'장르'}</span>
              {genreTags.map((tag) => (
                <Tag
                  key={tag.id}
                  label={tag.name}
                  active={selectedTagIds.includes(tag.id)}
                  onClick={() => toggleTag(tag.id)}
                />
              ))}
            </div>
          )}

          {moodTags.length > 0 && (
            <div className={styles.tagGroup}>
              <span className={styles.tagGroupLabel}>{'분위기'}</span>
              {moodTags.map((tag) => (
                <Tag
                  key={tag.id}
                  label={tag.name}
                  active={selectedTagIds.includes(tag.id)}
                  onClick={() => toggleTag(tag.id)}
                />
              ))}
            </div>
          )}

          {instrumentTags.length > 0 && (
            <div className={styles.tagGroup}>
              <span className={styles.tagGroupLabel}>{'악기'}</span>
              {instrumentTags.map((tag) => (
                <Tag
                  key={tag.id}
                  label={tag.name}
                  active={selectedTagIds.includes(tag.id)}
                  onClick={() => toggleTag(tag.id)}
                />
              ))}
            </div>
          )}
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button
            variant="ghost"
            type="button"
            onClick={() => navigate(-1)}
            disabled={submitting}
          >
            {'취소'}
          </Button>
          <Button type="submit" loading={submitting}>
            {'저장'}
          </Button>
        </div>
      </form>
    </div>
  );
}
