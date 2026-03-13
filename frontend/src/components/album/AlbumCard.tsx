import type { Album } from '@/types';
import { toUploadUrl } from '@/api/client';
import styles from './AlbumCard.module.css';

interface AlbumCardProps {
  album: Album;
  genre?: string;
  onClick?: (album: Album) => void;
  className?: string;
}

export default function AlbumCard({
  album,
  genre,
  onClick,
  className,
}: AlbumCardProps) {
  const classes = [styles.card, className ?? ''].filter(Boolean).join(' ');

  return (
    <div className={classes} onClick={() => onClick?.(album)}>
      <div className={styles.thumb}>
        {album.thumbnailUrl ? (
          <img src={toUploadUrl(album.thumbnailUrl)!} alt={album.title} />
        ) : (
          '\u266A'
        )}
      </div>
      <div className={styles.name}>{album.title}</div>
      <div className={styles.meta}>
        {genre ?? ''}{genre && album.trackCount ? ' \u00B7 ' : ''}{album.trackCount ? `${album.trackCount}곡` : ''}
      </div>
    </div>
  );
}
