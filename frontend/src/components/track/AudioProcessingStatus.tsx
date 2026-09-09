import { useEffect, useRef, useState } from 'react';
import { fetchAudioProcessing, retryAudioProcessing, type AudioProcessing } from '@/api/tracks';
import {
  audioProcessingError,
  audioProcessingFailureMessage,
  isAudioProcessingPending,
} from '@/utils/audioProcessing';
import styles from './AudioProcessingStatus.module.css';

const POLL_INTERVAL_MS = 3000;
const MAX_POLLS = 60;
const LABELS: Record<AudioProcessing['state'], string> = {
  READY: '재생 준비 완료',
  PENDING: '변환 대기',
  PROCESSING: '변환 중',
  FAILED: '변환 실패',
  CANCELLED: '변환 취소',
};

interface Props {
  value?: AudioProcessing | null;
  disabled?: boolean;
  onChange?: (value: AudioProcessing) => void;
}

export default function AudioProcessingStatus(props: Props) {
  const { value } = props;
  if (!value) return null;
  return (
    <ProcessingStatus
      key={`${value.trackId}:${value.generation}:${value.state}:${value.updatedAt}`}
      {...props}
      value={value}
    />
  );
}

function ProcessingStatus({
  value,
  disabled = false,
  onChange,
}: Props & { value: AudioProcessing }) {
  const [status, setStatus] = useState(value);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [paused, setPaused] = useState(false);
  const currentRef = useRef(value);
  const stoppedRef = useRef(false);
  const uncertainRetryRef = useRef(false);
  const pollsRef = useRef(0);
  const onChangeRef = useRef(onChange);
  const runRef = useRef<(mode: 'refresh' | 'retry') => void>(() => undefined);
  onChangeRef.current = onChange;

  useEffect(() => {
    if (disabled) return;
    let disposed = false;
    let controller: AbortController | null = null;
    let timer: ReturnType<typeof setTimeout> | undefined;
    let retrying = false;

    function schedule() {
      if (
        disposed ||
        stoppedRef.current ||
        document.hidden ||
        !isAudioProcessingPending(currentRef.current)
      ) {
        return;
      }
      if (pollsRef.current >= MAX_POLLS) {
        setPaused(true);
        return;
      }
      timer = setTimeout(() => void run('poll'), POLL_INTERVAL_MS);
    }

    async function run(mode: 'poll' | 'refresh' | 'retry') {
      if (disposed || controller || document.hidden) return;
      if (mode === 'retry' && (!currentRef.current.retryAllowed || stoppedRef.current)) return;
      clearTimeout(timer);
      if (mode === 'poll') pollsRef.current += 1;
      else pollsRef.current = 0;
      const request = new AbortController();
      controller = request;
      retrying = mode === 'retry';
      if (retrying) {
        uncertainRetryRef.current = true;
        stoppedRef.current = true;
      }
      setBusy(true);
      setPaused(false);
      setError(null);
      const previous = currentRef.current;
      try {
        const next =
          mode === 'retry'
            ? await retryAudioProcessing(previous.trackId, previous.generation, request.signal)
            : await fetchAudioProcessing(previous.trackId, request.signal);
        if (disposed || request.signal.aborted || controller !== request) return;
        // A response must still own this row and must never roll its generation back.
        if (!next || next.trackId !== previous.trackId || next.generation < previous.generation) {
          throw new Error('Stale audio processing response');
        }
        currentRef.current = next;
        setStatus(next);
        onChangeRef.current?.(next);
        stoppedRef.current = false;
        uncertainRetryRef.current = false;
      } catch (failure) {
        if (disposed || request.signal.aborted || controller !== request) return;
        stoppedRef.current = true;
        setError(
          audioProcessingError(
            failure,
            mode === 'retry'
              ? '재시도 결과를 확인하지 못했습니다. 최신 상태를 확인해 주세요.'
              : '처리 상태를 불러오지 못했습니다. 최신 상태를 확인해 주세요.',
          ),
        );
      } finally {
        if (!disposed && controller === request) {
          controller = null;
          retrying = false;
          setBusy(false);
          schedule();
        }
      }
    }

    function visibilityChanged() {
      clearTimeout(timer);
      if (document.hidden) {
        if (retrying) {
          stoppedRef.current = true;
          setError('재시도 결과 확인이 중단되었습니다. 최신 상태를 확인해 주세요.');
        }
        controller?.abort();
        controller = null;
        retrying = false;
        setBusy(false);
      } else {
        schedule();
      }
    }

    runRef.current = (mode) => void run(mode);
    setBusy(false);
    if (uncertainRetryRef.current) {
      setError('재시도 결과 확인이 중단되었습니다. 최신 상태를 확인해 주세요.');
    }
    schedule();
    document.addEventListener('visibilitychange', visibilityChanged);
    return () => {
      disposed = true;
      clearTimeout(timer);
      controller?.abort();
      runRef.current = () => undefined;
      document.removeEventListener('visibilitychange', visibilityChanged);
    };
  }, [disabled]);

  return (
    <div className={styles.processing} aria-busy={busy}>
      <div className={styles.row}>
        <span className={styles.status} role="status">
          {status.state === 'READY' && !status.streamReady
            ? '재생 파일 준비 안 됨'
            : LABELS[status.state]}
        </span>
        {status.retryAllowed && !error && (
          <button
            type="button"
            className={styles.action}
            title="음원 변환 재시도"
            aria-label="음원 변환 재시도"
            disabled={busy || disabled}
            onClick={() => runRef.current('retry')}
          >
            <span aria-hidden="true">{'\u21bb'}</span>
          </button>
        )}
        {(error || paused) && (
          <button
            type="button"
            className={styles.action}
            title="음원 처리 상태 새로고침"
            aria-label="음원 처리 상태 새로고침"
            disabled={busy || disabled}
            onClick={() => runRef.current('refresh')}
          >
            <span aria-hidden="true">{'\u21bb'}</span>
          </button>
        )}
      </div>
      {status.state !== 'READY' && (
        <span className={styles.detail}>
          {status.streamReady ? '기존 재생 파일 유지' : '재생 파일 준비 안 됨'}
        </span>
      )}
      {status.state === 'FAILED' && (
        <span className={styles.error}>{audioProcessingFailureMessage(status.errorCode)}</span>
      )}
      {error && (
        <span className={styles.error} role="alert">
          {error}
        </span>
      )}
      {paused && <span className={styles.detail}>자동 상태 확인 일시 중지</span>}
    </div>
  );
}
