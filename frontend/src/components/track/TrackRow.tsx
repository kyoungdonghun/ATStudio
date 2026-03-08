import type { TrackListItem } from '@/types';
import Badge from '@/components/ui/Badge';
import styles from './TrackRow.module.css';

interface TrackRowProps {
  index: number;
  track: TrackListItem;
  playing?: boolean;
  liked?: boolean;
  badge?: 'new' | 'hot' | null;
  onPlay?: (track: TrackListItem) => void;
  onLike?: (track: TrackListItem) => void;
  onAddToPlaylist?: (track: TrackListItem) => void;
  onDownload?: (track: TrackListItem) => void;
  onBuy?: (track: TrackListItem) => void;
}

export default function TrackRow({
  index,
  track,
  playing = false,
  liked = false,
  badge = null,
  onPlay,
  onLike,
  onAddToPlaylist,
  onDownload,
  onBuy,
}: TrackRowProps) {
  const rowClass = [styles.row, playing ? styles.playing : '']
    .filter(Boolean)
    .join(' ');

  const genreTag = track.tags.find((t) => t.type === 'GENRE');
  const moodTag = track.tags.find((t) => t.type === 'MOOD');

  return (
    <tr className={rowClass}>
      {/* Number / Play */}
      <td className={styles.cellNum}>
        <span className={styles.num}>{index}</span>
        <button
          className={styles.playBtn}
          onClick={() => onPlay?.(track)}
          aria-label="Play"
        >
          &#9654;
        </button>
      </td>

      {/* Info */}
      <td className={styles.cellInfo}>
        <div className={styles.info}>
          <div className={styles.thumb}>
            {track.thumbnail ? (
              <img src={track.thumbnail} alt={track.title} />
            ) : (
              '\u266A'
            )}
          </div>
          <div>
            <div className={styles.title}>
              {track.title}
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
            <div className={styles.artist}>
              {genreTag?.name ?? ''}
            </div>
          </div>
        </div>
      </td>

      {/* Tags */}
      <td className={styles.cellTag}>
        {genreTag && <span className={styles.tagChip}>{genreTag.name}</span>}
        {moodTag && <span className={styles.tagChip}>{moodTag.name}</span>}
      </td>

      {/* BPM */}
      <td className={styles.cellBpm}>{track.bpm ?? '-'}</td>

      {/* Key (tonality) */}
      <td className={styles.cellKey}>{track.tonality ?? '-'}</td>

      {/* Duration — not available in list API, show dash */}
      <td className={styles.cellDur}>-</td>

      {/* Actions */}
      <td className={styles.cellActs}>
        <div className={styles.actions}>
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
            title="Download"
          >
            &#8595;
          </button>
          <button
            className={styles.buyBtn}
            onClick={() => onBuy?.(track)}
          >
            Buy
          </button>
        </div>
      </td>
    </tr>
  );
}
