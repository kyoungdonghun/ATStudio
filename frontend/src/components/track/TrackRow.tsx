import { Link } from 'react-router-dom';
import type { TrackListItem } from '@/types';
import { toUploadUrl } from '@/api/client';
import Badge from '@/components/ui/Badge';
import CatalogImage from '@/components/catalog/CatalogImage';
import styles from './TrackRow.module.css';

interface TrackRowProps {
  index: number;
  track: TrackListItem;
  playing?: boolean;
  liked?: boolean;
  badge?: 'new' | 'hot' | null;
  showAuthActions?: boolean;
  onPlay?: (track: TrackListItem) => void;
  onLike?: (track: TrackListItem) => void;
  onAddToPlaylist?: (track: TrackListItem) => void;
  onDownload?: (track: TrackListItem) => void;
  downloadPending?: boolean;
  onGuestAction?: () => void;
}

export default function TrackRow({
  index,
  track,
  playing = false,
  liked = false,
  badge = null,
  showAuthActions = true,
  onPlay,
  onLike,
  onAddToPlaylist,
  onDownload,
  downloadPending = false,
  onGuestAction,
}: TrackRowProps) {
  const rowClass = [styles.row, playing ? styles.playing : ''].filter(Boolean).join(' ');

  const genreTags = track.tags.filter((t) => t.type === 'GENRE');
  const moodTags = track.tags.filter((t) => t.type === 'MOOD');
  const usageTags = track.tags.filter((t) => t.type === 'USAGE');
  const usageText = usageTags.map((t) => `#${t.name}`).join(' ');

  return (
    <tr className={rowClass}>
      {/* Number / Play */}
      <td className={styles.cellNum}>
        <span className={styles.num}>{index}</span>
        <button
          type="button"
          className={styles.playBtn}
          onClick={() => onPlay?.(track)}
          aria-label={playing ? 'Pause' : 'Play'}
        >
          {playing ? '\u23F8' : '\u25B6'}
        </button>
      </td>

      {/* Info */}
      <td className={styles.cellInfo}>
        <div className={styles.info}>
          <div className={styles.thumb}>
            {track.thumbnail ? (
              <CatalogImage
                src={toUploadUrl(track.thumbnail)!}
                alt={track.title}
                fallbackLabel={`${track.title} 음원 커버를 불러올 수 없습니다.`}
              />
            ) : (
              '\u266A'
            )}
          </div>
          <div className={styles.infoText}>
            <div className={styles.title}>
              <Link to={`/tracks/${track.id}`} className={styles.titleLink}>
                {track.title}
              </Link>
              {badge === 'new' && (
                <>
                  {' '}
                  <Badge variant="new">NEW</Badge>
                </>
              )}
              {badge === 'hot' && (
                <>
                  {' '}
                  <Badge variant="hot">HOT</Badge>
                </>
              )}
            </div>
            {usageText && <div className={styles.usageLine}>{usageText}</div>}
          </div>
        </div>
      </td>

      {/* Tags */}
      <td className={styles.cellTag}>
        {genreTags.map((t) => (
          <span key={t.id} className={styles.tagChip}>
            {t.name}
          </span>
        ))}
        {moodTags.map((t) => (
          <span key={t.id} className={styles.tagChip}>
            {t.name}
          </span>
        ))}
      </td>

      {/* BPM */}
      <td className={styles.cellBpm}>{track.bpm ?? '-'}</td>

      {/* Key (tonality) */}
      <td className={styles.cellKey}>{track.tonality ?? '-'}</td>

      {/* Duration */}
      <td className={styles.cellDur}>
        {track.duration
          ? `${Math.floor(track.duration / 60)}:${String(track.duration % 60).padStart(2, '0')}`
          : '-'}
      </td>

      {/* Actions */}
      <td className={styles.cellActs}>
        <div className={styles.actions}>
          {showAuthActions ? (
            <>
              <button
                className={`${styles.actBtn} ${liked ? styles.liked : ''}`}
                onClick={() => onLike?.(track)}
                title="Like"
              >
                {liked ? '\u2665' : '\u2661'}
              </button>
              <button
                className={styles.actBtn}
                onClick={() => onAddToPlaylist?.(track)}
                title="Add to playlist"
              >
                +
              </button>
              <button
                className={styles.actBtn}
                onClick={() => onDownload?.(track)}
                disabled={downloadPending}
                aria-busy={downloadPending}
                title="Download"
              >
                &#8595;
              </button>
            </>
          ) : (
            <>
              <button className={styles.actBtn} onClick={() => onGuestAction?.()} title="Like">
                {'\u2661'}
              </button>
              <button
                className={styles.actBtn}
                onClick={() => onGuestAction?.()}
                title="Add to playlist"
              >
                +
              </button>
              <button className={styles.actBtn} onClick={() => onGuestAction?.()} title="Download">
                &#8595;
              </button>
            </>
          )}
        </div>
      </td>
    </tr>
  );
}
