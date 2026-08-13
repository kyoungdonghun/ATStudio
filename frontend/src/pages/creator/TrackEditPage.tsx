import { useState, useEffect, type FormEvent, type ChangeEvent } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toUploadUrl } from '@/api/client';
import { fetchTrackDetailForAdmin, updateTrack } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import type { TagItem } from '@/types';
import {
  TITLE_TRACK_MAX,
  BPM_MIN,
  BPM_MAX,
  DESCRIPTION_MAX,
  AUDIO_MAX_SIZE_MB,
  isFileSizeOk,
  getAudioAccept,
  AUDIO_FORMAT_LABEL,
  hasValidAudioExtension,
} from '@/utils/validation';
import { parsePositiveDecimalRouteID } from '@/utils/routeId';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import TrackThumbnailField from './TrackThumbnailField';
import { emptyTrackThumbnailSelection, type TrackThumbnailSelection } from './trackThumbnail';
import styles from './TrackEditPage.module.css';

const TONALITIES = [
  'C',
  'C#',
  'D',
  'D#',
  'E',
  'F',
  'F#',
  'G',
  'G#',
  'A',
  'A#',
  'B',
  'Cm',
  'C#m',
  'Dm',
  'D#m',
  'Em',
  'Fm',
  'F#m',
  'Gm',
  'G#m',
  'Am',
  'A#m',
  'Bm',
];

/** Screen 7: Track edit */
export default function TrackEditPage() {
  const { trackId } = useParams<{ trackId: string }>();
  const navigate = useNavigate();
  const canonicalTrackID = parsePositiveDecimalRouteID(trackId);

  /* ── Form state ── */
  const [title, setTitle] = useState('');
  const [bpm, setBpm] = useState('');
  const [tonality, setTonality] = useState('');
  const [description, setDescription] = useState('');
  const [isActive, setIsActive] = useState(false);
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [thumbnail, setThumbnail] = useState<TrackThumbnailSelection>(() =>
    emptyTrackThumbnailSelection(),
  );
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);

  /* ── Existing file paths ── */
  const [currentAudioFile, setCurrentAudioFile] = useState<string | null>(null);
  const [currentThumbnail, setCurrentThumbnail] = useState<string | null>(null);

  /* ── Tag data ── */
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [instrumentTags, setInstrumentTags] = useState<TagItem[]>([]);
  const [usageTags, setUsageTags] = useState<TagItem[]>([]);

  /* ── UI state ── */
  const [pageLoading, setPageLoading] = useState(canonicalTrackID !== null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /* ── Load existing track + tags ── */
  useEffect(() => {
    let cancelled = false;

    async function loadData() {
      if (canonicalTrackID === null) {
        setPageLoading(false);
        return;
      }

      setPageLoading(true);
      setError(null);

      try {
        const [track, genres, moods, instruments, usages] = await Promise.all([
          fetchTrackDetailForAdmin(canonicalTrackID),
          fetchTags('GENRE'),
          fetchTags('MOOD'),
          fetchTags('INSTRUMENT'),
          fetchTags('USAGE'),
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
        setUsageTags(usages);
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : '데이터를 불러올 수 없습니다.');
        }
      } finally {
        if (!cancelled) setPageLoading(false);
      }
    }

    loadData();
    return () => {
      cancelled = true;
    };
  }, [canonicalTrackID]);

  /* ── Tag toggle ── */
  function toggleTag(tagId: number) {
    setSelectedTagIds((prev) =>
      prev.includes(tagId) ? prev.filter((id) => id !== tagId) : [...prev, tagId],
    );
  }

  /* ── File handlers ── */
  function handleAudioChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] ?? null;
    if (file) {
      if (!hasValidAudioExtension(file.name)) {
        setError(`지원하지 않는 파일 형식입니다. ${AUDIO_FORMAT_LABEL}만 업로드할 수 있습니다.`);
        e.target.value = '';
        return;
      }
      if (!isFileSizeOk(file, AUDIO_MAX_SIZE_MB)) {
        setError(`오디오 파일은 ${AUDIO_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다.`);
        e.target.value = '';
        return;
      }
    }
    setError(null);
    setAudioFile(file);
  }

  /* ── Submit ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (canonicalTrackID === null) return;
    if (thumbnail.status === 'pending') {
      setError('썸네일 이미지 크기를 확인하고 있습니다. 잠시 후 다시 시도해주세요.');
      return;
    }
    if (thumbnail.status === 'invalid') {
      setError(thumbnail.error ?? '썸네일 이미지를 다시 선택해주세요.');
      return;
    }

    const normalizedTitle = title.trim();
    const parsedBpm = Number(bpm);
    if (!normalizedTitle) {
      setError('제목을 입력해 주세요.');
      return;
    }
    if (
      !/^\d+$/.test(bpm) ||
      !Number.isInteger(parsedBpm) ||
      parsedBpm < BPM_MIN ||
      parsedBpm > BPM_MAX
    ) {
      setError(`BPM은 ${BPM_MIN}부터 ${BPM_MAX} 사이의 정수여야 합니다.`);
      return;
    }
    if (!tonality.trim()) {
      setError('조성을 선택해 주세요.');
      return;
    }

    const formData = new FormData();
    formData.append('title', normalizedTitle);
    formData.append('bpm', String(parsedBpm));
    formData.append('tonality', tonality.trim());
    formData.append('description', description.trim());
    formData.append('isActive', String(isActive));
    formData.append('replaceTags', 'true');

    if (audioFile) {
      formData.append('audioFile', audioFile);
    }
    if (thumbnail.file) {
      formData.append('thumbnail', thumbnail.file);
    }
    selectedTagIds.forEach((tagId) => {
      formData.append('tagIds', String(tagId));
    });

    setSubmitting(true);
    try {
      await updateTrack(canonicalTrackID, formData);
      navigate('/admin/track-manage');
    } catch (err) {
      const msg = err instanceof Error ? err.message : '음원 수정에 실패했습니다.';
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

  if (canonicalTrackID === null) {
    return (
      <div className={styles.page}>
        <div className={styles.error} role="alert">
          올바른 음원 주소가 아닙니다.
        </div>
        <div className={styles.actions}>
          <Button type="button" onClick={() => navigate('/admin/track-manage')}>
            음원 관리로 이동
          </Button>
        </div>
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
              <label className={`${styles.fileLabel} ${audioFile ? styles.fileLabelSelected : ''}`}>
                <input
                  type="file"
                  accept={getAudioAccept()}
                  className={styles.fileHidden}
                  onChange={handleAudioChange}
                />
                {audioFile ? audioFile.name : '새 파일 선택'}
              </label>
              {currentAudioFile && !audioFile && (
                <span className={styles.currentFile}>
                  {'현재: '}
                  {currentAudioFile.split('/').pop()}
                </span>
              )}
            </div>
          </div>

          <div className={styles.fileBox}>
            <TrackThumbnailField
              value={thumbnail}
              onChange={setThumbnail}
              existingImageUrl={toUploadUrl(currentThumbnail)}
              existingFileName={currentThumbnail?.split('/').pop() ?? null}
              disabled={submitting}
            />
          </div>
        </div>

        {/* Title */}
        <div className={styles.field}>
          <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
          <input
            className={styles.input}
            type="text"
            maxLength={TITLE_TRACK_MAX}
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
              min={BPM_MIN}
              max={BPM_MAX}
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
            maxLength={DESCRIPTION_MAX}
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
            <span className={styles.toggleLabel}>{isActive ? '활성' : '비활성'}</span>
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

          {usageTags.length > 0 && (
            <div className={styles.tagGroup}>
              <span className={styles.tagGroupLabel}>{'용도'}</span>
              {usageTags.map((tag) => (
                <Tag
                  key={tag.id}
                  label={`#${tag.name}`}
                  active={selectedTagIds.includes(tag.id)}
                  onClick={() => toggleTag(tag.id)}
                />
              ))}
            </div>
          )}
        </div>

        {/* Actions */}
        <div className={styles.actions}>
          <Button variant="ghost" type="button" onClick={() => navigate(-1)} disabled={submitting}>
            {'취소'}
          </Button>
          <Button
            type="submit"
            loading={submitting}
            disabled={thumbnail.status === 'pending' || thumbnail.status === 'invalid'}
          >
            {'저장'}
          </Button>
        </div>
      </form>
    </div>
  );
}
