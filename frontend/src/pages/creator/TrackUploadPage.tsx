import { useState, useEffect, useRef, useCallback, type FormEvent, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTrack } from '@/api/tracks';
import { fetchTags } from '@/api/tags';
import type { TagItem } from '@/types';
import {
  TITLE_TRACK_MAX,
  BPM_MIN,
  BPM_MAX,
  DESCRIPTION_MAX,
  AUDIO_MAX_SIZE_MB,
  IMAGE_MAX_SIZE_MB,
  TRACK_UPLOAD_MAX_COUNT,
  isFileSizeOk,
  AUDIO_ACCEPT,
  hasValidAudioExtension,
} from '@/utils/validation';
import Button from '@/components/ui/Button';
import Tag from '@/components/ui/Tag';
import styles from './TrackUploadPage.module.css';

const TONALITIES = [
  'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B',
  'Cm', 'C#m', 'Dm', 'D#m', 'Em', 'Fm', 'F#m', 'Gm', 'G#m', 'Am', 'A#m', 'Bm',
];

interface TrackEntry {
  id: string; // client-side key
  audioFile: File;
  thumbnail: File | null;
  title: string;
  bpm: string;
  tonality: string;
  description: string;
  tagIds: number[];
}

type UploadStatus = 'idle' | 'uploading' | 'done' | 'error';

interface TrackUploadState {
  status: UploadStatus;
  error?: string;
}

/** Screen 6: Track upload (single + multi) */
export default function TrackUploadPage() {
  const navigate = useNavigate();

  /* ── Track entries ── */
  const [tracks, setTracks] = useState<TrackEntry[]>([]);
  const [uploadStates, setUploadStates] = useState<Record<string, TrackUploadState>>({});

  /* ── Tag data ── */
  const [genreTags, setGenreTags] = useState<TagItem[]>([]);
  const [moodTags, setMoodTags] = useState<TagItem[]>([]);
  const [instrumentTags, setInstrumentTags] = useState<TagItem[]>([]);
  const [usageTags, setUsageTags] = useState<TagItem[]>([]);

  /* ── UI state ── */
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [expandedIdx, setExpandedIdx] = useState<number | null>(null);

  const audioInputRef = useRef<HTMLInputElement>(null);

  /* ── Load tags ── */
  useEffect(() => {
    let cancelled = false;
    async function loadTags() {
      try {
        const [genres, moods, instruments, usages] = await Promise.all([
          fetchTags('GENRE'),
          fetchTags('MOOD'),
          fetchTags('INSTRUMENT'),
          fetchTags('USAGE'),
        ]);
        if (!cancelled) {
          setGenreTags(genres);
          setMoodTags(moods);
          setInstrumentTags(instruments);
          setUsageTags(usages);
        }
      } catch {
        /* tags are supplementary */
      }
    }
    loadTags();
    return () => { cancelled = true; };
  }, []);

  /* ── Add audio files ── */
  const handleAudioSelect = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files;
    if (!selected) return;

    const newFiles = Array.from(selected);

    // File extension check (iOS Safari workaround — accept 속성으로 걸러지지 않는 케이스 대응)
    const invalidType = newFiles.filter((f) => !hasValidAudioExtension(f.name));
    if (invalidType.length > 0) {
      setError(`지원하지 않는 파일 형식입니다: ${invalidType.map((f) => f.name).join(', ')} (MP3, WAV, M4A, AAC, FLAC, OGG만 업로드 가능)`);
      if (audioInputRef.current) audioInputRef.current.value = '';
      return;
    }

    // File size check
    const oversized = newFiles.filter((f) => !isFileSizeOk(f, AUDIO_MAX_SIZE_MB));
    if (oversized.length > 0) {
      setError(`오디오 파일은 ${AUDIO_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다. (초과: ${oversized.map((f) => f.name).join(', ')})`);
      return;
    }

    const remaining = TRACK_UPLOAD_MAX_COUNT - tracks.length;
    if (remaining <= 0) {
      setError(`최대 ${TRACK_UPLOAD_MAX_COUNT}곡까지 업로드할 수 있습니다.`);
      return;
    }

    const filesToAdd = newFiles.slice(0, remaining);
    if (newFiles.length > remaining) {
      setError(`최대 ${TRACK_UPLOAD_MAX_COUNT}곡 제한으로 ${newFiles.length - remaining}개 파일이 제외되었습니다.`);
    }

    const newEntries: TrackEntry[] = filesToAdd.map((file) => ({
      id: crypto.randomUUID(),
      audioFile: file,
      thumbnail: null,
      title: file.name.replace(/\.[^.]+$/, ''),
      bpm: '',
      tonality: '',
      description: '',
      tagIds: [],
    }));

    setTracks((prev) => [...prev, ...newEntries]);
    // Expand first new track if nothing expanded
    if (expandedIdx === null && newEntries.length > 0) {
      setExpandedIdx(tracks.length);
    }

    if (audioInputRef.current) audioInputRef.current.value = '';
  }, [tracks.length, expandedIdx]);

  /* ── Update track field ── */
  function updateTrack(idx: number, updates: Partial<TrackEntry>) {
    setTracks((prev) => prev.map((t, i) => i === idx ? { ...t, ...updates } : t));
  }

  /* ── Toggle tag ── */
  function toggleTag(idx: number, tagId: number) {
    setTracks((prev) => prev.map((t, i) => {
      if (i !== idx) return t;
      const tagIds = t.tagIds.includes(tagId)
        ? t.tagIds.filter((id) => id !== tagId)
        : [...t.tagIds, tagId];
      return { ...t, tagIds };
    }));
  }

  /* ── Remove track ── */
  function removeTrack(idx: number) {
    setTracks((prev) => prev.filter((_, i) => i !== idx));
    setUploadStates((prev) => {
      const next = { ...prev };
      delete next[tracks[idx]?.id];
      return next;
    });
    if (expandedIdx === idx) {
      setExpandedIdx(null);
    } else if (expandedIdx !== null && expandedIdx > idx) {
      setExpandedIdx(expandedIdx - 1);
    }
  }

  /* ── Validate ── */
  function validateTrack(track: TrackEntry): string | null {
    if (!track.title.trim()) return '제목을 입력해주세요.';
    if (!track.bpm || Number(track.bpm) <= 0) return 'BPM을 올바르게 입력해주세요.';
    if (!track.tonality) return '조성을 선택해주세요.';
    return null;
  }

  /* ── Submit all ── */
  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (tracks.length === 0) {
      setError('오디오 파일을 선택해주세요.');
      return;
    }

    // Validate all
    for (let i = 0; i < tracks.length; i++) {
      const err = validateTrack(tracks[i]);
      if (err) {
        setError(`트랙 ${i + 1} (${tracks[i].title}): ${err}`);
        setExpandedIdx(i);
        return;
      }
    }

    setSubmitting(true);

    // Upload sequentially
    let hasError = false;
    for (const track of tracks) {
      const state = uploadStates[track.id];
      if (state?.status === 'done') continue; // Skip already uploaded

      setUploadStates((prev) => ({
        ...prev,
        [track.id]: { status: 'uploading' },
      }));

      const formData = new FormData();
      formData.append('title', track.title.trim());
      formData.append('bpm', track.bpm);
      formData.append('tonality', track.tonality);
      if (track.description.trim()) {
        formData.append('description', track.description.trim());
      }
      formData.append('audioFile', track.audioFile);
      if (track.thumbnail) {
        formData.append('thumbnail', track.thumbnail);
      }
      track.tagIds.forEach((tagId) => {
        formData.append('tagIds', String(tagId));
      });

      try {
        await createTrack(formData);
        setUploadStates((prev) => ({
          ...prev,
          [track.id]: { status: 'done' },
        }));
      } catch (err) {
        const msg = err instanceof Error ? err.message : '업로드 실패';
        setUploadStates((prev) => ({
          ...prev,
          [track.id]: { status: 'error', error: msg },
        }));
        hasError = true;
        // Continue to next track instead of stopping entirely
      }
    }

    setSubmitting(false);

    if (!hasError) {
      navigate('/admin/track-manage');
    } else {
      setError('일부 트랙 업로드에 실패했습니다. 실패한 항목을 확인하고 다시 시도해주세요.');
    }
  }

  const doneCount = Object.values(uploadStates).filter((s) => s.status === 'done').length;

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>{'음원 업로드'}</h1>

      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <div className={styles.error}>{error}</div>}

        {/* File select */}
        <div className={styles.field}>
          <span className={`${styles.label} ${styles.required}`}>
            {'오디오 파일 선택'}
          </span>
          <label className={styles.fileLabel}>
            <input
              ref={audioInputRef}
              type="file"
              {...(AUDIO_ACCEPT && { accept: AUDIO_ACCEPT })}
              multiple
              className={styles.fileHidden}
              onChange={handleAudioSelect}
            />
            {tracks.length === 0
              ? '파일 선택 (최대 20곡)'
              : `${tracks.length}곡 선택됨 — 추가 선택`}
          </label>
        </div>

        {/* Track list */}
        {tracks.length > 0 && (
          <div className={styles.trackList}>
            {submitting && (
              <div className={styles.progressBar}>
                {`업로드 중... ${doneCount} / ${tracks.length}`}
              </div>
            )}

            {tracks.map((track, idx) => {
              const state = uploadStates[track.id];
              const isExpanded = expandedIdx === idx;
              const statusIcon =
                state?.status === 'done' ? '\u2705' :
                state?.status === 'error' ? '\u274C' :
                state?.status === 'uploading' ? '\u23F3' : '';

              return (
                <div
                  key={track.id}
                  className={`${styles.trackCard} ${
                    state?.status === 'done' ? styles.trackDone : ''
                  } ${state?.status === 'error' ? styles.trackError : ''}`}
                >
                  {/* Header row */}
                  <div
                    className={styles.trackHeader}
                    onClick={() => setExpandedIdx(isExpanded ? null : idx)}
                  >
                    <span className={styles.trackIdx}>{idx + 1}</span>
                    <span className={styles.trackFileName}>
                      {statusIcon} {track.title || track.audioFile.name}
                    </span>
                    <span className={styles.trackChevron}>
                      {isExpanded ? '\u25B2' : '\u25BC'}
                    </span>
                    {state?.status !== 'done' && (
                      <button
                        type="button"
                        className={styles.trackRemove}
                        onClick={(e) => { e.stopPropagation(); removeTrack(idx); }}
                      >
                        {'×'}
                      </button>
                    )}
                  </div>

                  {state?.status === 'error' && (
                    <div className={styles.trackErrorMsg}>{state.error}</div>
                  )}

                  {/* Expanded detail */}
                  {isExpanded && state?.status !== 'done' && (
                    <div className={styles.trackBody}>
                      {/* Title */}
                      <div className={styles.field}>
                        <label className={`${styles.label} ${styles.required}`}>{'제목'}</label>
                        <input
                          className={styles.input}
                          type="text"
                          maxLength={TITLE_TRACK_MAX}
                          value={track.title}
                          onChange={(e) => updateTrack(idx, { title: e.target.value })}
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
                            min={BPM_MIN}
                            max={BPM_MAX}
                            value={track.bpm}
                            onChange={(e) => updateTrack(idx, { bpm: e.target.value })}
                            placeholder="BPM을 입력해주세요"
                          />
                        </div>
                        <div className={styles.field}>
                          <label className={`${styles.label} ${styles.required}`}>{'조성'}</label>
                          <select
                            className={styles.select}
                            value={track.tonality}
                            onChange={(e) => updateTrack(idx, { tonality: e.target.value })}
                          >
                            <option value="">{'선택'}</option>
                            {TONALITIES.map((t) => (
                              <option key={t} value={t}>{t}</option>
                            ))}
                          </select>
                        </div>
                      </div>

                      {/* Thumbnail */}
                      <div className={styles.field}>
                        <span className={styles.label}>{'썸네일'}</span>
                        <label
                          className={`${styles.fileLabel} ${track.thumbnail ? styles.fileLabelSelected : ''}`}
                        >
                          <input
                            type="file"
                            accept="image/*"
                            className={styles.fileHidden}
                            onChange={(e) => {
                              const file = e.target.files?.[0] ?? null;
                              if (file && !isFileSizeOk(file, IMAGE_MAX_SIZE_MB)) {
                                setError(`썸네일 이미지는 ${IMAGE_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다.`);
                                return;
                              }
                              updateTrack(idx, { thumbnail: file });
                            }}
                          />
                          {track.thumbnail ? track.thumbnail.name : '이미지 선택'}
                        </label>
                      </div>

                      {/* Description */}
                      <div className={styles.field}>
                        <label className={styles.label}>{'설명'}</label>
                        <textarea
                          className={styles.textarea}
                          maxLength={DESCRIPTION_MAX}
                          value={track.description}
                          onChange={(e) => updateTrack(idx, { description: e.target.value })}
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
                                active={track.tagIds.includes(tag.id)}
                                onClick={() => toggleTag(idx, tag.id)}
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
                                active={track.tagIds.includes(tag.id)}
                                onClick={() => toggleTag(idx, tag.id)}
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
                                active={track.tagIds.includes(tag.id)}
                                onClick={() => toggleTag(idx, tag.id)}
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
                                active={track.tagIds.includes(tag.id)}
                                onClick={() => toggleTag(idx, tag.id)}
                              />
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

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
          <Button type="submit" loading={submitting} disabled={tracks.length === 0}>
            {tracks.length <= 1
              ? '업로드'
              : `${tracks.length}곡 업로드`}
          </Button>
        </div>
      </form>
    </div>
  );
}
