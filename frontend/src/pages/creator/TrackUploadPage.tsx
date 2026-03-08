import { useState, useEffect, useRef, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTrack } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import type { TagItem } from '@/types';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import styles from './TrackUploadPage.module.css';

const TONALITIES = [
  'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B',
  'Cm', 'C#m', 'Dm', 'D#m', 'Em', 'Fm', 'F#m', 'Gm', 'G#m', 'Am', 'A#m', 'Bm',
];

/** Screen 6: Track upload (single) */
export default function TrackUploadPage() {
  const navigate = useNavigate();

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [bpm, setBpm] = useState('');
  const [tonality, setTonality] = useState('');
  const [description, setDescription] = useState('');
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  /* ── Tag data ── */
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [instrumentTags, setInstrumentTags] = useState<TagItem[]>([]);

  /* ── UI state ── */
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const audioInputRef = useRef<HTMLInputElement>(null);
  const thumbInputRef = useRef<HTMLInputElement>(null);

  /* ── Load tags ── */
  useEffect(() => {
    let cancelled = false;

    async function loadTags() {
      try {
        const [genres, moods, instruments] = await Promise.all([
          fetchTags('GENRE'),
          fetchTags('MOOD'),
          fetchTags('INSTRUMENT'),
        ]);
        if (!cancelled) {
          setGenreTags(genres);
          setMoodTags(moods);
          setInstrumentTags(instruments);
        }
      } catch {
        /* tags are supplementary */
      }
    }

    loadTags();
    return () => { cancelled = true; };
  }, []);

  /* ── Tag toggle ── */
  function toggleTag(tagId: number) {
    setSelectedTagIds((prev) =>
      prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId],
    );
  }

  /* ── File handlers ── */
  function handleAudioChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setAudioFile(file);
  }

  function handleThumbnailChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    setThumbnail(file);
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (!title.trim()) {
      setError('제목을 입력해주세요.');
      return;
    }
    if (!bpm || Number(bpm) <= 0) {
      setError('BPM을 올바르게 입력해주세요.');
      return;
    }
    if (!tonality) {
      setError('조성을 선택해주세요.');
      return;
    }
    if (!audioFile) {
      setError('오디오 파일을 선택해주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', title.trim());
    formData.append('bpm', bpm);
    formData.append('tonality', tonality);
    if (description.trim()) {
      formData.append('description', description.trim());
    }
    formData.append('audioFile', audioFile);
    if (thumbnail) {
      formData.append('thumbnail', thumbnail);
    }
    selectedTagIds.forEach((tagId) => {
      formData.append('tagIds', String(tagId));
    });

    setSubmitting(true);
    try {
      await createTrack(formData);
      navigate('/admin/track-manage');
    } catch (err) {
      const msg =
        err instanceof Error ? err.message : '음원 업로드에 실패했습니다.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'음원 업로드'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* File uploads */}
        <div className={styles.fileRow}>
          <div className={styles.fileBox}>
            <div className={styles.field}>
              <span className={`${styles.label} ${styles.required}`}>
                {'오디오 파일'}
              </span>
              <label
                className={`${styles.fileLabel} ${audioFile ? styles.fileLabelSelected : ''}`}
              >
                <input
                  ref={audioInputRef}
                  type="file"
                  accept="audio/*"
                  className={styles.fileHidden}
                  onChange={handleAudioChange}
                />
                {audioFile ? audioFile.name : '파일 선택'}
              </label>
            </div>
          </div>

          <div className={styles.fileBox}>
            <div className={styles.field}>
              <span className={styles.label}>{'썸네일'}</span>
              <label
                className={`${styles.fileLabel} ${thumbnail ? styles.fileLabelSelected : ''}`}
              >
                <input
                  ref={thumbInputRef}
                  type="file"
                  accept="image/*"
                  className={styles.fileHidden}
                  onChange={handleThumbnailChange}
                />
                {thumbnail ? thumbnail.name : '이미지 선택'}
              </label>
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
            placeholder="음원 제목"
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
              placeholder="120"
            />
          </div>
          <div className={styles.field}>
            <label className={`${styles.label} ${styles.required}`}>
              {'조성'}
            </label>
            <select
              className={styles.select}
              value={tonality}
              onChange={(e) => setTonality(e.target.value)}
            >
              <option value="">{'선택'}</option>
              {TONALITIES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
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
            placeholder="음원에 대한 설명 (선택사항)"
          />
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
            {'업로드'}
          </Button>
        </div>
      </form>
    </div>
  );
}
