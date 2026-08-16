import type { Album } from '@/types';
import { toUploadUrl } from '@/api/client';
import CatalogImage from '@/components/catalog/CatalogImage';
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

  function activateCard() {
    onClick?.(album);
  }

  return (
    <div className={classes}>
      {onClick && (
        <button
          type="button"
          className={styles.cardLink}
          onClick={activateCard}
          aria-label={`${album.title} 앨범 보기`}
        />
      )}
      <div className={styles.thumb}>
        {album.thumbnailUrl ? (
          <CatalogImage
            src={toUploadUrl(album.thumbnailUrl)!}
            alt={album.title}
            fallbackLabel={`${album.title} 앨범 커버를 불러올 수 없습니다.`}
          />
        ) : (
          '\u266A'
        )}
        {onToggleLike && (
          <button
            type="button"
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
        {genre ?? ''}
        {genre && album.trackCount ? ' \u00B7 ' : ''}
        {album.trackCount ? `${album.trackCount}곡` : ''}
        {album.likeCount > 0 && (
          <span className={styles.likeCount}>{` \u2665 ${album.likeCount}`}</span>
        )}
      </div>
    </div>
  );
}
