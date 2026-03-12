/** Screen H-1: YouTube whitelist channel management */
import { useState, useEffect, useCallback, type FormEvent } from 'react';
import {
  fetchWhitelistChannels,
  registerChannel,
  updateChannel,
  deleteChannel,
  type WhitelistChannelRequest,
} from '@/api/whitelistChannels';
import type { WhitelistChannel } from '@/types';
import styles from './WhitelistChannelPage.module.css';

export default function WhitelistChannelPage() {
  const [channels, setChannels] = useState<WhitelistChannel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* add form */
  const [newUrl, setNewUrl] = useState('');
  const [newName, setNewName] = useState('');
  const [adding, setAdding] = useState(false);

  /* inline edit */
  const [editId, setEditId] = useState<number | null>(null);
  const [editUrl, setEditUrl] = useState('');
  const [editName, setEditName] = useState('');

  const load = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await fetchWhitelistChannels();
      setChannels(result.dataList ?? []);
    } catch {
      setError('채널 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleAdd(e: FormEvent) {
    e.preventDefault();
    const url = newUrl.trim();
    const name = newName.trim();
    if (!url || !name) return;

    try {
      setAdding(true);
      setError(null);
      const req: WhitelistChannelRequest = { channelUrl: url, channelName: name };
      const created = await registerChannel(req);
      setChannels((prev) => [...prev, created]);
      setNewUrl('');
      setNewName('');
    } catch {
      setError('채널 등록에 실패했습니다.');
    } finally {
      setAdding(false);
    }
  }

  function startEdit(ch: WhitelistChannel) {
    setEditId(ch.id);
    setEditUrl(ch.channelUrl);
    setEditName(ch.channelName);
  }

  function cancelEdit() {
    setEditId(null);
  }

  async function handleSaveEdit(id: number) {
    const url = editUrl.trim();
    const name = editName.trim();
    if (!url || !name) return;

    try {
      setError(null);
      const req: WhitelistChannelRequest = { channelUrl: url, channelName: name };
      const updated = await updateChannel(id, req);
      setChannels((prev) => prev.map((ch) => (ch.id === id ? updated : ch)));
      setEditId(null);
    } catch {
      setError('채널 수정에 실패했습니다.');
    }
  }

  async function handleDelete(id: number) {
    try {
      setError(null);
      await deleteChannel(id);
      setChannels((prev) => prev.filter((ch) => ch.id !== id));
    } catch {
      setError('채널 삭제에 실패했습니다.');
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>
          {'화이트리스트 채널'}
          <span className={styles.count}>{channels.length}개</span>
        </h1>
      </div>

      {/* Add form */}
      <form className={styles.addForm} onSubmit={handleAdd}>
        <div className={styles.fieldGroup}>
          <label className={styles.fieldLabel}>{'채널 URL'}</label>
          <input
            className={styles.input}
            type="url"
            placeholder="https://youtube.com/@channel"
            value={newUrl}
            onChange={(e) => setNewUrl(e.target.value)}
            required
          />
        </div>
        <div className={styles.fieldGroup}>
          <label className={styles.fieldLabel}>{'채널 이름'}</label>
          <input
            className={styles.input}
            type="text"
            placeholder="채널 이름"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            required
          />
        </div>
        <button className={styles.btnAdd} type="submit" disabled={adding}>
          {adding ? '등록 중...' : '채널 등록'}
        </button>
      </form>

      {loading ? (
        <div className={styles.loading}>{'불러오는 중...'}</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : channels.length === 0 ? (
        <div className={styles.empty}>
          <p>{'등록된 채널이 없습니다.'}</p>
          <p>{'위 폼에서 YouTube 채널을 등록해 주세요.'}</p>
        </div>
      ) : (
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.thNum}>#</th>
                <th>{'채널 이름'}</th>
                <th>{'채널 URL'}</th>
                <th className={styles.thDate}>{'등록일'}</th>
                <th className={styles.thActs}>{'관리'}</th>
              </tr>
            </thead>
            <tbody>
              {channels.map((ch, idx) => (
                <tr key={ch.id} className={styles.row}>
                  <td className={styles.cellNum}>{idx + 1}</td>

                  {/* Name cell */}
                  <td className={styles.cellName}>
                    {editId === ch.id ? (
                      <input
                        className={styles.editInput}
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                      />
                    ) : (
                      ch.channelName
                    )}
                  </td>

                  {/* URL cell */}
                  <td className={styles.cellUrl}>
                    {editId === ch.id ? (
                      <input
                        className={styles.editInput}
                        value={editUrl}
                        onChange={(e) => setEditUrl(e.target.value)}
                      />
                    ) : (
                      <a
                        className={styles.channelLink}
                        href={ch.channelUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        {ch.channelUrl}
                      </a>
                    )}
                  </td>

                  {/* Date cell */}
                  <td className={styles.cellDate}>
                    {ch.createdAt?.slice(0, 10) ?? '-'}
                  </td>

                  {/* Actions */}
                  <td className={styles.cellActs}>
                    {editId === ch.id ? (
                      <>
                        <button
                          className={styles.saveBtn}
                          onClick={() => handleSaveEdit(ch.id)}
                          title="저장"
                        >
                          {'저장'}
                        </button>
                        <button
                          className={styles.cancelBtn}
                          onClick={cancelEdit}
                          title="취소"
                        >
                          {'취소'}
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          className={styles.editBtn}
                          onClick={() => startEdit(ch)}
                          title="수정"
                        >
                          {'\u270E'}
                        </button>
                        <button
                          className={styles.deleteBtn}
                          onClick={() => handleDelete(ch.id)}
                          title="삭제"
                        >
                          {'\u2715'}
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
