import type { Album } from '@/types';
import { toUploadUrl } from '@/api/client';
import styles from './AlbumCard.module.css';

interface AlbumCardProps {
  album: Album;
  genre?: string;
  onClick?: (album: Album) => void;
  className?: string;
  isLiked?: boolean;
  onToggleLike?: (albumId: number) => void;
}

export default function AlbumCard({
  album,
  genre,
  onClick,
  className,
  isLiked,
  onToggleLike,
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
        {onToggleLike && (
          <button
            className={`${styles.likeBtn} ${isLiked ? styles.likeBtnActive : ''}`}
            onClick={(e) => {
              e.stopPropagation();
              onToggleLike(album.id);
            }}
            aria-label={isLiked ? '좋아요 해제' : '좋아요'}
          >
            {isLiked ? '\u2665' : '\u2661'}
          </button>
        )}
      </div>
      <div className={styles.name}>{album.title}</div>
      <div className={styles.meta}>
        {genre ?? ''}{genre && album.trackCount ? ' \u00B7 ' : ''}{album.trackCount ? `${album.trackCount}곡` : ''}
        {album.likeCount > 0 && (
          <span className={styles.likeCount}>{` \u2665 ${album.likeCount}`}</span>
        )}
      </div>
    </div>
  );
}
