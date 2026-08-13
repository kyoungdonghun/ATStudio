import { useEffect, useRef, useState, type ChangeEvent, type SyntheticEvent } from 'react';
import {
  ALBUM_THUMBNAIL_MAX_DIMENSION,
  ALBUM_THUMBNAIL_MAX_PIXELS,
  IMAGE_MAX_SIZE_MB,
  isFileSizeOk,
} from '@/utils/validation';
import { emptyAlbumThumbnailSelection, type AlbumThumbnailSelection } from './albumThumbnail';
import styles from './AlbumThumbnailField.module.css';

const ALBUM_THUMBNAIL_MIME_TYPES = new Set(['image/jpeg', 'image/png']);
const ALBUM_THUMBNAIL_ACCEPT = [...ALBUM_THUMBNAIL_MIME_TYPES].join(',');

interface AlbumThumbnailFieldProps {
  value: AlbumThumbnailSelection;
  onChange: (selection: AlbumThumbnailSelection) => void;
  existingImageUrl?: string | null;
  disabled?: boolean;
}

interface PreviewState {
  file: File;
  url: string;
  generation: number;
  selectionId: number;
}

function getFormatError(file: File): string | null {
  const actualMime = file.type.trim().toLowerCase();

  if (
    actualMime &&
    actualMime !== 'application/octet-stream' &&
    !ALBUM_THUMBNAIL_MIME_TYPES.has(actualMime)
  ) {
    return '앨범 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.';
  }
  return null;
}

export default function AlbumThumbnailField({
  value,
  onChange,
  existingImageUrl = null,
  disabled = false,
}: AlbumThumbnailFieldProps) {
  const [preview, setPreview] = useState<PreviewState | null>(null);
  const generationRef = useRef(0);
  const selectionIdRef = useRef(0);
  const onChangeRef = useRef(onChange);
  const valueRef = useRef(value);
  onChangeRef.current = onChange;
  valueRef.current = value;

  useEffect(() => {
    const generation = ++generationRef.current;
    const file = value.file;
    if (!file) {
      setPreview(null);
      return;
    }

    const url = URL.createObjectURL(file);
    setPreview({ file, url, generation, selectionId: value.selectionId });
    if (valueRef.current.status !== 'pending' || valueRef.current.error !== null) {
      onChangeRef.current({
        file,
        status: 'pending',
        error: null,
        selectionId: value.selectionId,
      });
    }

    return () => {
      generationRef.current = generation + 1;
      URL.revokeObjectURL(url);
    };
  }, [value.file, value.selectionId]);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    event.target.value = '';
    const selectionId = ++selectionIdRef.current;

    if (!file) {
      onChangeRef.current(emptyAlbumThumbnailSelection());
      return;
    }

    const formatError = getFormatError(file);
    if (formatError) {
      onChangeRef.current({ file: null, status: 'invalid', error: formatError, selectionId });
      return;
    }
    if (!isFileSizeOk(file, IMAGE_MAX_SIZE_MB)) {
      onChangeRef.current({
        file: null,
        status: 'invalid',
        error: `앨범 썸네일은 ${IMAGE_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다.`,
        selectionId,
      });
      return;
    }

    onChangeRef.current({ file, status: 'pending', error: null, selectionId });
  }

  function handleImageLoad(ownedPreview: PreviewState, event: SyntheticEvent<HTMLImageElement>) {
    if (generationRef.current !== ownedPreview.generation) return;

    const { naturalWidth, naturalHeight } = event.currentTarget;
    const pixelCount = naturalWidth * naturalHeight;
    if (naturalWidth < 1 || naturalHeight < 1) {
      onChangeRef.current({
        file: null,
        status: 'invalid',
        error: '이미지 파일을 읽을 수 없습니다. JPEG 또는 PNG 파일인지 확인해주세요.',
        selectionId: ownedPreview.selectionId,
      });
      return;
    }
    if (
      naturalWidth > ALBUM_THUMBNAIL_MAX_DIMENSION ||
      naturalHeight > ALBUM_THUMBNAIL_MAX_DIMENSION ||
      pixelCount > ALBUM_THUMBNAIL_MAX_PIXELS
    ) {
      onChangeRef.current({
        file: null,
        status: 'invalid',
        error: `앨범 썸네일은 가로와 세로가 각각 ${ALBUM_THUMBNAIL_MAX_DIMENSION}px 이하여야 합니다.`,
        selectionId: ownedPreview.selectionId,
      });
      return;
    }

    onChangeRef.current({
      file: ownedPreview.file,
      status: 'valid',
      error: null,
      selectionId: ownedPreview.selectionId,
    });
  }

  function handleImageError(ownedPreview: PreviewState) {
    if (generationRef.current !== ownedPreview.generation) return;
    onChangeRef.current({
      file: null,
      status: 'invalid',
      error: '이미지 파일을 읽을 수 없습니다. JPEG 또는 PNG 파일인지 확인해주세요.',
      selectionId: ownedPreview.selectionId,
    });
  }

  const showExistingImage = preview === null && value.file === null && existingImageUrl;

  return (
    <div className={styles.field}>
      <span className={styles.label}>썸네일</span>
      <span className={styles.guidance}>
        JPEG 또는 PNG, {IMAGE_MAX_SIZE_MB}MB 이하, 최대 {ALBUM_THUMBNAIL_MAX_DIMENSION}x
        {ALBUM_THUMBNAIL_MAX_DIMENSION}px
      </span>

      {(preview || showExistingImage) && (
        <div className={styles.preview} data-testid="album-thumbnail-preview">
          {preview ? (
            <img
              key={preview.generation}
              src={preview.url}
              alt="선택한 앨범 썸네일 미리보기"
              className={styles.previewImage}
              onLoad={(event) => handleImageLoad(preview, event)}
              onError={() => handleImageError(preview)}
            />
          ) : (
            <img
              src={existingImageUrl ?? undefined}
              alt="현재 앨범 썸네일"
              className={styles.previewImage}
            />
          )}
        </div>
      )}

      <div className={styles.controls}>
        <label className={`${styles.fileLabel} ${value.file ? styles.fileLabelSelected : ''}`}>
          <input
            type="file"
            accept={ALBUM_THUMBNAIL_ACCEPT}
            className={styles.fileHidden}
            aria-label="앨범 썸네일 이미지"
            disabled={disabled}
            onChange={handleFileChange}
          />
          {value.file ? value.file.name : '이미지 선택'}
        </label>
        {value.status !== 'empty' && (
          <button
            type="button"
            className={styles.clearButton}
            disabled={disabled}
            onClick={() => onChangeRef.current(emptyAlbumThumbnailSelection())}
          >
            선택 지우기
          </button>
        )}
      </div>

      {value.status === 'pending' && (
        <span className={styles.pending} role="status">
          이미지 크기를 확인하는 중입니다.
        </span>
      )}
      {value.error && (
        <span className={styles.error} role="alert">
          {value.error}
        </span>
      )}
    </div>
  );
}
