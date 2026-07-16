/** Screen H-1: YouTube whitelist channel management */
import { useEffect, useState, useCallback, useRef, type FormEvent } from 'react';
import {
  deleteChannel,
  fetchWhitelistChannels,
  registerChannel,
  requestWhitelistRegistration,
  setPrimaryWhitelistChannel,
  updateChannel,
  type WhitelistChannelRequest,
} from '@/api/whitelistChannels';
import {
  fetchMySubscription,
  isNoActiveSubscriptionError,
  type MySubscription,
} from '@/api/userSubscriptions';
import { classifyLoadError, getLoadErrorMessage } from '@/api/loadError';
import Button from '@/components/ui/Button';
import { formatDate, formatDateTime } from '@/utils/format';
import { getSafeYoutubeUrl } from '@/utils/safeYoutubeUrl';
import type { WhitelistChannel, WhitelistChannelStatus } from '@/types';
import {
  CHANNEL_NAME_MAX,
  CHANNEL_URL_PATTERN,
  YOUTUBE_CHANNEL_ID_MAX,
  YOUTUBE_HANDLE_MAX,
} from '@/utils/validation';
import styles from './WhitelistChannelPage.module.css';
import { isWhitelistChannelEditable } from './whitelistChannelPolicy';

const STATUS_LABELS: Record<WhitelistChannelStatus, string> = {
  DRAFT: '저장됨',
  PENDING: '등록 요청',
  EXPORTED: '외부 처리 중',
  REGISTERED: '등록 완료',
  REVISION_REQUESTED: '수정 요청',
  REJECTED: '반려',
  CANCELLED: '해제 완료',
  REMOVAL_REQUESTED: '해제 요청',
};

const LIMIT_COUNT_STATUSES = new Set<WhitelistChannelStatus>([
  'PENDING',
  'EXPORTED',
  'REGISTERED',
  'REVISION_REQUESTED',
  'REMOVAL_REQUESTED',
]);

const REQUESTABLE_STATUSES = new Set<WhitelistChannelStatus>([
  'DRAFT',
  'REJECTED',
  'REVISION_REQUESTED',
]);

interface FormState {
  channelName: string;
  youtubeHandle: string;
  channelUrl: string;
  youtubeChannelId: string;
}

const EMPTY_FORM: FormState = {
  channelName: '',
  youtubeHandle: '',
  channelUrl: '',
  youtubeChannelId: '',
};

export default function WhitelistChannelPage() {
  const [channels, setChannels] = useState<WhitelistChannel[]>([]);
  const [mySub, setMySub] = useState<MySubscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [editId, setEditId] = useState<number | null>(null);
  const [busyKey, setBusyKey] = useState<string | null>(null);
  const requestId = useRef(0);
  const loadBlocked = useRef(false);
  const loadQueued = useRef(false);

  const load = useCallback(async () => {
    loadQueued.current = true;
    if (loadBlocked.current) return;

    loadBlocked.current = true;
    let finalRequestId: number | null = null;
    try {
      while (loadQueued.current) {
        loadQueued.current = false;
        const currentRequestId = ++requestId.current;
        finalRequestId = currentRequestId;
        try {
          setLoading(true);
          const result = await fetchWhitelistChannels();
          if (currentRequestId !== requestId.current) return;
          setChannels(result.dataList ?? []);
          try {
            const subscription = await fetchMySubscription();
            if (currentRequestId !== requestId.current) return;
            setMySub(subscription);
          } catch (subscriptionError) {
            if (currentRequestId !== requestId.current) return;
            if (isNoActiveSubscriptionError(subscriptionError)) {
              setMySub(null);
            } else {
              throw subscriptionError;
            }
          }
          setLoadError(null);
        } catch (error: unknown) {
          if (currentRequestId !== requestId.current || classifyLoadError(error) === 'cancelled') {
            return;
          }
          setLoadError(getLoadErrorMessage(error, '채널 목록'));
        }
      }
    } finally {
      loadBlocked.current = false;
      if (finalRequestId === requestId.current) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    void load();
    return () => {
      requestId.current += 1;
      loadBlocked.current = false;
      loadQueued.current = false;
    };
  }, [load]);

  const usedSlots = channels.filter((channel) => LIMIT_COUNT_STATUSES.has(channel.status)).length;
  const maxSlots = mySub?.subscription.maxWhitelistChannels ?? 0;
  const hasSubscription = mySub !== null;

  function updateForm<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function validate(): boolean {
    if (!form.channelName.trim()) {
      setError('채널명을 입력해주세요.');
      return false;
    }
    if (!form.channelUrl.trim()) {
      setError('채널 링크를 입력해주세요.');
      return false;
    }
    if (!CHANNEL_URL_PATTERN.test(form.channelUrl.trim())) {
      setError('올바른 채널 링크를 입력해주세요. 예: https://www.youtube.com/@your_channel');
      return false;
    }
    return true;
  }

  function resetForm() {
    setForm(EMPTY_FORM);
    setEditId(null);
  }

  function buildRequest(): WhitelistChannelRequest {
    return {
      channelName: form.channelName.trim(),
      channelUrl: form.channelUrl.trim(),
      youtubeHandle: form.youtubeHandle.trim() || null,
      youtubeChannelId: form.youtubeChannelId.trim() || null,
    };
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!validate()) return;

    try {
      setBusyKey('save');
      if (editId) {
        await updateChannel(editId, buildRequest());
        setMessage('채널 정보가 저장되었습니다.');
      } else {
        await registerChannel(buildRequest());
        setMessage('채널이 저장되었습니다. 필요하면 화이트리스트 등록 요청을 진행해주세요.');
      }
      resetForm();
      await load();
    } catch (err) {
      setError(errorMessage(err, '채널 저장에 실패했습니다.'));
    } finally {
      setBusyKey(null);
    }
  }

  function startEdit(channel: WhitelistChannel) {
    setEditId(channel.id);
    setForm({
      channelName: channel.channelName,
      channelUrl: channel.channelUrl,
      youtubeHandle: channel.youtubeHandle ?? '',
      youtubeChannelId: channel.youtubeChannelId ?? '',
    });
    setMessage(null);
    setError(null);
  }

  async function handleRequest(channel: WhitelistChannel) {
    if (!hasSubscription) {
      setError('화이트리스트 등록 요청은 구독 후 이용할 수 있습니다.');
      return;
    }
    try {
      setBusyKey(`request-${channel.id}`);
      setError(null);
      setMessage(null);
      await requestWhitelistRegistration(channel.id);
      setMessage('화이트리스트 등록 요청이 접수되었습니다.');
      await load();
    } catch (err) {
      setError(errorMessage(err, '등록 요청에 실패했습니다.'));
    } finally {
      setBusyKey(null);
    }
  }

  async function handlePrimary(channel: WhitelistChannel) {
    try {
      setBusyKey(`primary-${channel.id}`);
      setError(null);
      setMessage(null);
      await setPrimaryWhitelistChannel(channel.id);
      setMessage('대표 채널이 변경되었습니다.');
      await load();
    } catch (err) {
      setError(errorMessage(err, '대표 채널 설정에 실패했습니다.'));
    } finally {
      setBusyKey(null);
    }
  }

  async function handleDelete(channel: WhitelistChannel) {
    const removalFlow = channel.status === 'EXPORTED' || channel.status === 'REGISTERED';
    const ok = window.confirm(
      removalFlow
        ? '이미 외부 처리 중이거나 등록 완료된 채널입니다. 삭제 대신 등록 해제를 요청할까요?'
        : '이 채널을 삭제할까요?',
    );
    if (!ok) return;

    try {
      setBusyKey(`delete-${channel.id}`);
      setError(null);
      setMessage(null);
      await deleteChannel(channel.id);
      setMessage(removalFlow ? '등록 해제 요청이 접수되었습니다.' : '채널이 삭제되었습니다.');
      await load();
    } catch (err) {
      setError(errorMessage(err, '채널 삭제에 실패했습니다.'));
    } finally {
      setBusyKey(null);
    }
  }

  if (loading) {
    if (loadError) {
      return (
        <div className={styles.page}>
          <div className={styles.loadError} role="alert">
            <p>{loadError}</p>
            <button type="button" onClick={() => void load()} disabled={loading}>
              다시 시도
            </button>
          </div>
        </div>
      );
    }
    return (
      <div className={styles.page}>
        <div className={styles.loading}>{'채널 정보를 불러오는 중...'}</div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className={styles.page}>
        <div className={styles.loadError} role="alert">
          <p>{loadError}</p>
          <button type="button" onClick={() => void load()} disabled={loading}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>{'화이트리스트 채널'}</h1>
          <p className={styles.subtitle}>
            {
              'YouTube에서 음원을 사용할 채널 정보를 저장하고, 구독 플랜 한도 안에서 등록 요청을 진행합니다.'
            }
          </p>
        </div>
        <div className={styles.limitBox}>
          <span>{'등록 슬롯'}</span>
          <strong>{hasSubscription ? `${usedSlots}/${maxSlots}` : '구독 필요'}</strong>
        </div>
      </div>

      <form className={styles.formPanel} onSubmit={handleSubmit}>
        <div className={styles.formHeader}>
          <strong>{editId ? '채널 수정' : '채널 추가'}</strong>
          {editId && (
            <button className={styles.textButton} type="button" onClick={resetForm}>
              {'새 채널 입력'}
            </button>
          )}
        </div>
        <div className={styles.formGrid}>
          <Field
            id="whitelist-channel-name"
            label="채널명"
            value={form.channelName}
            placeholder="예: AT.M Shorts"
            maxLength={CHANNEL_NAME_MAX}
            onChange={(value) => updateForm('channelName', value)}
          />
          <Field
            id="whitelist-youtube-handle"
            label="유튜브 아이디"
            value={form.youtubeHandle}
            placeholder="예: @your_channel"
            maxLength={YOUTUBE_HANDLE_MAX}
            onChange={(value) => updateForm('youtubeHandle', value)}
          />
          <Field
            id="whitelist-channel-url"
            label="채널 링크"
            value={form.channelUrl}
            placeholder="예: https://www.youtube.com/@your_channel"
            onChange={(value) => updateForm('channelUrl', value)}
          />
          <Field
            id="whitelist-channel-id"
            label="채널 ID"
            value={form.youtubeChannelId}
            placeholder="예: UCxxxxxxxxxxxxxxxxxxxxxx"
            maxLength={YOUTUBE_CHANNEL_ID_MAX}
            onChange={(value) => updateForm('youtubeChannelId', value)}
          />
        </div>
        <div className={styles.formActions}>
          <Button type="submit" loading={busyKey === 'save'}>
            {editId ? '수정 저장' : '채널 저장'}
          </Button>
        </div>
      </form>

      {message && <div className={styles.success}>{message}</div>}
      {error && <div className={styles.error}>{error}</div>}

      {channels.length === 0 ? (
        <div className={styles.empty}>
          <p>{'저장된 채널이 없습니다.'}</p>
          <p>{'위 폼에서 YouTube 채널 정보를 추가해주세요.'}</p>
        </div>
      ) : (
        <div className={styles.channelList}>
          {channels.map((channel) => {
            const safeChannelUrl = getSafeYoutubeUrl(channel.channelUrl);
            return (
              <article key={channel.id} className={styles.channelCard}>
                <div className={styles.channelMain}>
                  <div className={styles.channelTitleRow}>
                    <strong>{channel.channelName}</strong>
                    {channel.primary && <span className={styles.primaryBadge}>{'대표'}</span>}
                    <span className={`${styles.statusBadge} ${styles[`status${channel.status}`]}`}>
                      {STATUS_LABELS[channel.status]}
                    </span>
                  </div>
                  <div className={styles.channelMeta}>
                    <span>{channel.youtubeHandle || '-'}</span>
                    {safeChannelUrl ? (
                      <a href={safeChannelUrl} target="_blank" rel="noopener noreferrer">
                        {channel.channelUrl}
                      </a>
                    ) : (
                      <span>{channel.channelUrl}</span>
                    )}
                    <span>{channel.youtubeChannelId || '채널 ID 미입력'}</span>
                  </div>
                  {channel.adminNote && <div className={styles.adminNote}>{channel.adminNote}</div>}
                  <div className={styles.dateLine}>
                    <span>{`저장일 ${formatDate(channel.createdAt)}`}</span>
                    {channel.requestedAt && (
                      <span>{`요청 ${formatDateTime(channel.requestedAt)}`}</span>
                    )}
                    {channel.exportedAt && (
                      <span>{`외부 전달 ${formatDateTime(channel.exportedAt)}`}</span>
                    )}
                  </div>
                </div>
                <div className={styles.cardActions}>
                  {!channel.primary && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => void handlePrimary(channel)}
                      loading={busyKey === `primary-${channel.id}`}
                    >
                      {'대표 설정'}
                    </Button>
                  )}
                  {REQUESTABLE_STATUSES.has(channel.status) && (
                    <Button
                      size="sm"
                      onClick={() => void handleRequest(channel)}
                      loading={busyKey === `request-${channel.id}`}
                      disabled={
                        hasSubscription &&
                        usedSlots >= maxSlots &&
                        !LIMIT_COUNT_STATUSES.has(channel.status)
                      }
                    >
                      {channel.status === 'REVISION_REQUESTED' ? '수정 후 재요청' : '등록 요청'}
                    </Button>
                  )}
                  {isWhitelistChannelEditable(channel.status) && (
                    <Button variant="ghost" size="sm" onClick={() => startEdit(channel)}>
                      {'수정'}
                    </Button>
                  )}
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => void handleDelete(channel)}
                    loading={busyKey === `delete-${channel.id}`}
                  >
                    {channel.status === 'EXPORTED' || channel.status === 'REGISTERED'
                      ? '해제 요청'
                      : '삭제'}
                  </Button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

function Field({
  id,
  label,
  value,
  placeholder,
  maxLength,
  onChange,
}: {
  id: string;
  label: string;
  value: string;
  placeholder: string;
  maxLength?: number;
  onChange: (value: string) => void;
}) {
  return (
    <div className={styles.fieldGroup}>
      <label className={styles.fieldLabel} htmlFor={id}>
        {label}
      </label>
      <input
        id={id}
        className={styles.input}
        type="text"
        value={value}
        placeholder={placeholder}
        maxLength={maxLength}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}

function errorMessage(err: unknown, fallback: string): string {
  return (
    (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
    (err instanceof Error ? err.message : fallback)
  );
}
