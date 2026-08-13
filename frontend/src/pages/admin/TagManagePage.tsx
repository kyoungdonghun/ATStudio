/** Screen K-6: Tag management */
import { useEffect, useState, useCallback, useRef } from 'react';
import { fetchTags, createTag, updateTag, fetchTagDeletionImpact, deleteTag } from '@/api/tags';
import { getApiErrorCode } from '@/api/loadError';
import type { TagDeletionImpact, TagItem, TagType } from '@/types';
import {
  formatTagNameForDisplay,
  getTagNameValidationError,
  isDuplicateTagName,
  normalizeTagName,
  TAG_NAME_DUPLICATE_MESSAGE,
  TAG_NAME_RAW_MAX,
} from '@/utils/tagName';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './TagManagePage.module.css';

const TAG_TYPES: TagType[] = ['GENRE', 'MOOD', 'INSTRUMENT', 'USAGE'];
const TAG_NAME_SERVER_INVALID_MESSAGE = '태그 이름 형식을 확인해주세요.';
const TAG_SAVE_FAILURE_MESSAGE = '태그를 저장하지 못했습니다. 잠시 후 다시 시도해주세요.';
const TAG_DELETE_FAILURE_MESSAGE = '태그를 삭제하지 못했습니다. 잠시 후 다시 시도해주세요.';
const TAG_IMPACT_FAILURE_MESSAGE =
  '삭제 영향을 확인하지 못했습니다. 삭제하지 않고 다시 시도하거나 닫아 주세요.';

function getSafeApiMessage(error: unknown, fallback: string): string {
  const message = (error as { response?: { data?: { message?: unknown } } })?.response?.data
    ?.message;
  return typeof message === 'string' && message.trim() ? message : fallback;
}

export default function TagManagePage() {
  const [tags, setTags] = useState<TagItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  /* Create/Edit modal */
  const [editTag, setEditTag] = useState<TagItem | null>(null);
  const [isCreateMode, setIsCreateMode] = useState(false);
  const [formName, setFormName] = useState('');
  const [formType, setFormType] = useState<TagType>('GENRE');
  const [formLoading, setFormLoading] = useState(false);
  const [nameError, setNameError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const formGenerationRef = useRef(0);
  const formPendingRef = useRef(false);

  /* Type filter tab */
  const [activeType, setActiveType] = useState<string>('ALL');

  /* Delete modal */
  const [deleteTarget, setDeleteTarget] = useState<TagItem | null>(null);
  const [deletionImpact, setDeletionImpact] = useState<TagDeletionImpact | null>(null);
  const [impactLoading, setImpactLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const impactRequestGenerationRef = useRef(0);
  const deletePendingRef = useRef(false);

  const loadTags = useCallback(() => {
    setLoading(true);
    setLoadError(null);
    fetchTags()
      .then(setTags)
      .catch(() => setLoadError('Failed to load tags'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadTags();
  }, [loadTags]);

  /* Open create modal */
  const openCreate = () => {
    formGenerationRef.current += 1;
    setIsCreateMode(true);
    setEditTag(null);
    setFormName('');
    setFormType('GENRE');
    setNameError(null);
    setFormError(null);
  };

  /* Open edit modal */
  const openEdit = (tag: TagItem) => {
    formGenerationRef.current += 1;
    setIsCreateMode(false);
    setEditTag(tag);
    setFormName(tag.name);
    setFormType(tag.type);
    setNameError(null);
    setFormError(null);
  };

  /* Close form modal */
  const closeFormModal = () => {
    if (formPendingRef.current) return;
    formGenerationRef.current += 1;
    setEditTag(null);
    setIsCreateMode(false);
    setNameError(null);
    setFormError(null);
  };

  const handleNameChange = (nextName: string) => {
    setFormName(nextName);
    setFormError(null);

    const validationError = getTagNameValidationError(nextName);
    if (validationError) {
      setNameError(validationError);
      return;
    }
    setNameError(
      isDuplicateTagName(tags, nextName, editTag?.id) ? TAG_NAME_DUPLICATE_MESSAGE : null,
    );
  };

  /* Submit create/edit */
  const handleFormSubmit = async () => {
    if (formPendingRef.current) return;
    const validationError = getTagNameValidationError(formName);
    if (validationError) {
      setNameError(validationError);
      return;
    }
    if (isDuplicateTagName(tags, formName, editTag?.id)) {
      setNameError(TAG_NAME_DUPLICATE_MESSAGE);
      return;
    }

    const canonicalName = normalizeTagName(formName);
    const generation = formGenerationRef.current;
    const operation = isCreateMode
      ? { mode: 'create' as const, name: canonicalName, type: formType }
      : editTag
        ? { mode: 'edit' as const, id: editTag.id, name: canonicalName, type: formType }
        : null;
    if (!operation) return;
    setNameError(null);
    setFormError(null);
    formPendingRef.current = true;
    setFormLoading(true);
    try {
      if (operation.mode === 'create') {
        await createTag({ name: operation.name, type: operation.type });
      } else {
        await updateTag(operation.id, { name: operation.name, type: operation.type });
      }
      if (formGenerationRef.current === generation) {
        formGenerationRef.current += 1;
        setEditTag(null);
        setIsCreateMode(false);
        setNameError(null);
        setFormError(null);
      }
      loadTags();
    } catch (error) {
      if (formGenerationRef.current !== generation) return;
      const errorCode = getApiErrorCode(error);
      if (errorCode === 'TAG_NAME_DUPLICATED') {
        setNameError(TAG_NAME_DUPLICATE_MESSAGE);
      } else if (errorCode === 'TAG_NAME_INVALID') {
        setNameError(TAG_NAME_SERVER_INVALID_MESSAGE);
      } else {
        setFormError(getSafeApiMessage(error, TAG_SAVE_FAILURE_MESSAGE));
      }
    } finally {
      formPendingRef.current = false;
      setFormLoading(false);
    }
  };

  const loadDeletionImpact = async (tag: TagItem, generation: number) => {
    setImpactLoading(true);
    setDeletionImpact(null);
    setDeleteError(null);
    try {
      const impact = await fetchTagDeletionImpact(tag.id);
      if (impactRequestGenerationRef.current !== generation) return;
      if (
        impact.id !== tag.id ||
        !Number.isSafeInteger(impact.trackAssociationCount) ||
        impact.trackAssociationCount < 0
      ) {
        throw new Error('Invalid Tag deletion impact response');
      }
      setDeletionImpact(impact);
    } catch {
      if (impactRequestGenerationRef.current === generation) {
        setDeleteError(TAG_IMPACT_FAILURE_MESSAGE);
      }
    } finally {
      if (impactRequestGenerationRef.current === generation) {
        setImpactLoading(false);
      }
    }
  };

  const openDelete = (tag: TagItem) => {
    if (deletePendingRef.current) return;
    const generation = ++impactRequestGenerationRef.current;
    setDeleteTarget(tag);
    setDeletionImpact(null);
    setDeleteError(null);
    void loadDeletionImpact(tag, generation);
  };

  const closeDeleteModal = () => {
    if (deletePendingRef.current) return;
    impactRequestGenerationRef.current += 1;
    setDeleteTarget(null);
    setDeletionImpact(null);
    setImpactLoading(false);
    setDeleteError(null);
  };

  /* Confirm delete */
  const confirmDelete = async () => {
    if (!deleteTarget || !deletionImpact || deletePendingRef.current) return;
    const targetID = deleteTarget.id;
    const generation = impactRequestGenerationRef.current;
    deletePendingRef.current = true;
    setDeleteLoading(true);
    try {
      await deleteTag(targetID);
      if (impactRequestGenerationRef.current === generation) {
        impactRequestGenerationRef.current += 1;
        setDeleteTarget(null);
        setDeletionImpact(null);
        setDeleteError(null);
      }
      loadTags();
    } catch (error) {
      if (impactRequestGenerationRef.current === generation) {
        setDeleteError(getSafeApiMessage(error, TAG_DELETE_FAILURE_MESSAGE));
      }
    } finally {
      deletePendingRef.current = false;
      setDeleteLoading(false);
    }
  };

  const retryDeletionImpact = () => {
    if (!deleteTarget || impactLoading || deleteLoading) return;
    const generation = ++impactRequestGenerationRef.current;
    void loadDeletionImpact(deleteTarget, generation);
  };

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.loading}>Loading...</div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{loadError}</div>
      </div>
    );
  }

  const formModalOpen = isCreateMode || editTag !== null;
  const filteredTags = activeType === 'ALL' ? tags : tags.filter((t) => t.type === activeType);

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Tag Management</h1>
        <Button size="sm" onClick={openCreate}>
          + New Tag
        </Button>
      </div>

      {/* Type filter tabs */}
      <div className={styles.typeTabs}>
        {['ALL', ...TAG_TYPES].map((t) => (
          <button
            key={t}
            type="button"
            className={`${styles.typeTab} ${activeType === t ? styles.typeTabActive : ''}`}
            onClick={() => setActiveType(t)}
            aria-pressed={activeType === t}
          >
            {t === 'ALL' ? '전체' : t}
            <span className={styles.typeTabCount}>
              {t === 'ALL' ? tags.length : tags.filter((tag) => tag.type === t).length}
            </span>
          </button>
        ))}
      </div>

      {/* Table */}
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Type</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredTags.length === 0 && (
              <tr>
                <td colSpan={4} className={styles.empty}>
                  No tags found.
                </td>
              </tr>
            )}
            {filteredTags.map((tag) => (
              <tr key={tag.id} className={styles.row}>
                <td>{tag.id}</td>
                <td>{formatTagNameForDisplay(tag.name, tag.type)}</td>
                <td>
                  <span className={styles.typeBadge}>{tag.type}</span>
                </td>
                <td>
                  <div className={styles.actionBtns}>
                    <Button variant="outline" size="sm" onClick={() => openEdit(tag)}>
                      Edit
                    </Button>
                    <Button variant="danger" size="sm" onClick={() => openDelete(tag)}>
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create / Edit Modal */}
      <Modal
        open={formModalOpen}
        onClose={closeFormModal}
        title={isCreateMode ? 'Create Tag' : 'Edit Tag'}
        busy={formLoading}
      >
        <div className={styles.modalBody}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="tag-name">
              Name
            </label>
            <input
              id="tag-name"
              className={`${styles.formInput} ${nameError ? styles.formInputInvalid : ''}`}
              placeholder="Tag name"
              maxLength={TAG_NAME_RAW_MAX}
              value={formName}
              disabled={formLoading}
              onChange={(event) => handleNameChange(event.target.value)}
              aria-invalid={nameError ? 'true' : undefined}
              aria-describedby={nameError ? 'tag-name-error' : undefined}
            />
            {nameError ? (
              <div id="tag-name-error" className={styles.fieldError} role="alert">
                {nameError}
              </div>
            ) : null}
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel} htmlFor="tag-type">
              Type
            </label>
            <select
              id="tag-type"
              className={styles.formSelect}
              value={formType}
              disabled={formLoading}
              onChange={(event) => {
                setFormType(event.target.value as TagType);
                setFormError(null);
              }}
            >
              {TAG_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
          {formError ? (
            <div className={styles.modalError} role="alert">
              {formError}
            </div>
          ) : null}
        </div>
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={closeFormModal} disabled={formLoading}>
            Cancel
          </Button>
          <Button size="sm" loading={formLoading} onClick={handleFormSubmit}>
            {isCreateMode ? 'Create' : 'Save'}
          </Button>
        </div>
      </Modal>

      {/* Delete confirm modal */}
      <Modal
        open={deleteTarget !== null}
        onClose={closeDeleteModal}
        title="Delete Tag"
        busy={deleteLoading}
      >
        <div className={styles.deleteText}>
          {impactLoading ? (
            'Track 연결 영향을 확인하고 있습니다.'
          ) : deletionImpact ? (
            <>
              <strong>{formatTagNameForDisplay(deletionImpact.name, deletionImpact.type)}</strong>
              {deletionImpact.trackAssociationCount === 0 ? (
                <> Tag는 연결된 Track이 없어 제거할 Track 연결이 없습니다. Tag만 삭제됩니다.</>
              ) : (
                <>
                  {' '}
                  Tag는 현재 {deletionImpact.trackAssociationCount.toLocaleString()}개의 Track에
                  연결되어 있습니다. Tag를 삭제하기 전에 해당 Track 연결을 모두 제거합니다.
                </>
              )}
            </>
          ) : (
            '삭제 영향을 확인해야 Tag를 삭제할 수 있습니다.'
          )}
        </div>
        {deleteError ? (
          <div className={styles.modalError} role="alert">
            {deleteError}
          </div>
        ) : null}
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={closeDeleteModal} disabled={deleteLoading}>
            Cancel
          </Button>
          {!impactLoading && !deletionImpact ? (
            <Button size="sm" onClick={retryDeletionImpact}>
              Retry impact check
            </Button>
          ) : null}
          {deletionImpact ? (
            <Button variant="danger" size="sm" loading={deleteLoading} onClick={confirmDelete}>
              Delete
            </Button>
          ) : null}
        </div>
      </Modal>
    </div>
  );
}
