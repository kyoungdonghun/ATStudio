import {
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
  type ChangeEvent,
  type SyntheticEvent,
} from 'react';
import { IMAGE_MAX_SIZE_MB, isFileSizeOk } from '@/utils/validation';
import { emptyTrackThumbnailSelection, type TrackThumbnailSelection } from './trackThumbnail';
import styles from './TrackThumbnailField.module.css';

const TRACK_THUMBNAIL_ACCEPT = 'image/jpeg,image/png';

interface TrackThumbnailFieldProps {
  value: TrackThumbnailSelection;
  onChange: (selection: TrackThumbnailSelection) => void;
  existingImageUrl?: string | null;
  existingFileName?: string | null;
  disabled?: boolean;
}

interface PreviewState {
  file: File;
  url: string;
  version: number;
}

type ExistingImageStatus = 'none' | 'pending' | 'square' | 'non-square' | 'error';

const MIME_BY_EXTENSION: Record<string, string> = {
  jpg: 'image/jpeg',
  jpeg: 'image/jpeg',
  png: 'image/png',
};

function getFileFormatError(file: File): string | null {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
  const expectedMime = MIME_BY_EXTENSION[extension];
  const actualMime = file.type.toLowerCase();

  if (!expectedMime || (actualMime && actualMime !== expectedMime)) {
    return '트랙 썸네일은 JPEG 또는 PNG 파일만 업로드할 수 있습니다.';
  }
  return null;
}

export default function TrackThumbnailField({
  value,
  onChange,
  existingImageUrl = null,
  existingFileName = null,
  disabled = false,
}: TrackThumbnailFieldProps) {
  const [preview, setPreview] = useState<PreviewState | null>(null);
  const [existingStatus, setExistingStatus] = useState<ExistingImageStatus>('none');
  const selectionVersionRef = useRef(0);
  const onChangeRef = useRef(onChange);
  const valueRef = useRef(value);
  const existingImageUrlRef = useRef(existingImageUrl);
  onChangeRef.current = onChange;
  valueRef.current = value;
  existingImageUrlRef.current = existingImageUrl;

  useEffect(() => {
    const version = ++selectionVersionRef.current;
    const file = value.file;
    if (!file) {
      setPreview(null);
      return;
    }

    const url = URL.createObjectURL(file);
    setPreview({ file, url, version });
    const currentValue = valueRef.current;
    if (currentValue.status !== 'pending' || currentValue.error !== null) {
      onChangeRef.current({ file, status: 'pending', error: null });
    }

    return () => {
      selectionVersionRef.current = version + 1;
      URL.revokeObjectURL(url);
    };
  }, [value.file]);

  useLayoutEffect(() => {
    setExistingStatus(existingImageUrl ? 'pending' : 'none');
  }, [existingImageUrl]);

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    event.target.value = '';

    if (!file) {
      onChangeRef.current(emptyTrackThumbnailSelection());
      return;
    }

    const formatError = getFileFormatError(file);
    if (formatError) {
      onChangeRef.current({ file: null, status: 'invalid', error: formatError });
      return;
    }
    if (!isFileSizeOk(file, IMAGE_MAX_SIZE_MB)) {
      onChangeRef.current({
        file: null,
        status: 'invalid',
        error: `트랙 썸네일은 ${IMAGE_MAX_SIZE_MB}MB 이하만 업로드할 수 있습니다.`,
      });
      return;
    }

    onChangeRef.current({ file, status: 'pending', error: null });
  }

  function handleSelectedImageLoad(
    loadedPreview: PreviewState,
    event: SyntheticEvent<HTMLImageElement>,
  ) {
    if (selectionVersionRef.current !== loadedPreview.version) {
      return;
    }

    const { naturalWidth, naturalHeight } = event.currentTarget;
    if (naturalWidth < 1 || naturalHeight < 1) {
      onChangeRef.current({
        file: loadedPreview.file,
        status: 'invalid',
        error: '이미지 파일을 읽을 수 없습니다. JPEG 또는 PNG 파일인지 확인해주세요.',
      });
      return;
    }
    if (naturalWidth !== naturalHeight) {
      onChangeRef.current({
        file: loadedPreview.file,
        status: 'invalid',
        error: '트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.',
      });
      return;
    }

    onChangeRef.current({ file: loadedPreview.file, status: 'valid', error: null });
  }

  function handleSelectedImageError(loadedPreview: PreviewState) {
    if (selectionVersionRef.current !== loadedPreview.version) {
      return;
    }
    onChangeRef.current({
      file: loadedPreview.file,
      status: 'invalid',
      error: '이미지 파일을 읽을 수 없습니다. JPEG 또는 PNG 파일인지 확인해주세요.',
    });
  }

  function handleExistingImageLoad(event: SyntheticEvent<HTMLImageElement>) {
    if (existingImageUrlRef.current !== existingImageUrl) {
      return;
    }
    const { naturalWidth, naturalHeight } = event.currentTarget;
    if (naturalWidth < 1 || naturalHeight < 1) {
      setExistingStatus('error');
      return;
    }
    setExistingStatus(naturalWidth === naturalHeight ? 'square' : 'non-square');
  }

  function handleExistingImageError() {
    if (existingImageUrlRef.current === existingImageUrl) {
      setExistingStatus('error');
    }
  }

  const showExistingImage = preview === null && value.file === null && existingImageUrl;

  return (
    <div className={styles.field}>
      <span className={styles.label}>썸네일</span>
      <span className={styles.guidance}>JPEG 또는 PNG, 1:1 필수, {IMAGE_MAX_SIZE_MB}MB 이하</span>
      <span className={styles.recommendation}>2048x2048px 권장 (필수 아님)</span>

      {(preview || showExistingImage) && (
        <div
          className={styles.preview}
          data-testid="track-thumbnail-preview"
          data-preview-ratio="1:1"
        >
          {preview ? (
            <img
              key={preview.version}
              src={preview.url}
              alt="선택한 트랙 썸네일 미리보기"
              className={styles.previewImage}
              onLoad={(event) => handleSelectedImageLoad(preview, event)}
              onError={() => handleSelectedImageError(preview)}
            />
          ) : (
            <img
              key={existingImageUrl ?? undefined}
              src={existingImageUrl ?? undefined}
              alt="현재 트랙 썸네일"
              className={styles.previewImage}
              onLoad={handleExistingImageLoad}
              onError={handleExistingImageError}
            />
          )}
        </div>
      )}

      <label className={`${styles.fileLabel} ${value.file ? styles.fileLabelSelected : ''}`}>
        <input
          type="file"
          accept={TRACK_THUMBNAIL_ACCEPT}
          className={styles.fileHidden}
          aria-label="썸네일 이미지"
          disabled={disabled}
          onChange={handleFileChange}
        />
        {value.file ? value.file.name : '새 이미지 선택'}
      </label>

      {existingFileName && value.file === null && (
        <span className={styles.currentFile}>현재: {existingFileName}</span>
      )}
      {value.status === 'pending' && (
        <span className={styles.pending} role="status">
          이미지 크기를 확인하는 중입니다.
        </span>
      )}
      {value.error && (
        <span className={styles.fieldError} role="alert">
          {value.error}
        </span>
      )}
      {existingStatus === 'non-square' && value.file === null && (
        <span className={styles.existingWarning} role="status">
          현재 썸네일이 1:1이 아닙니다. 새 정사각형 이미지로 교체를 권장합니다.
        </span>
      )}
    </div>
  );
}
