import type { Album } from '@/types';
import styles from './AlbumCard.module.css';

interface AlbumCardProps {
  album: Album;
  genre?: string;
  onPlay?: (album: Album) => void;
  onClick?: (album: Album) => void;
  className?: string;
}

export default function AlbumCard({
  album,
  genre,
  onPlay,
  onClick,
  className,
}: AlbumCardProps) {
  const classes = [styles.card, className ?? ''].filter(Boolean).join(' ');

  const handlePlay = (e: React.MouseEvent) => {
    e.stopPropagation();
    onPlay?.(album);
  };

  return (
    <div className={classes} onClick={() => onClick?.(album)}>
      <div className={styles.thumb}>
        {album.thumbnailUrl ? (
          <img src={album.thumbnailUrl} alt={album.title} />
        ) : (
          '\u266A'
        )}
        <div className={styles.playOverlay}>
          <button
            className={styles.playBtn}
            onClick={handlePlay}
            aria-label="Play album"
          >
            &#9654;
          </button>
        </div>
      </div>
      <div className={styles.name}>{album.title}</div>
      <div className={styles.meta}>
        {genre ?? ''}{genre && album.trackCount ? ' \u00B7 ' : ''}{album.trackCount ? `${album.trackCount}곡` : ''}
      </div>
    </div>
  );
}
