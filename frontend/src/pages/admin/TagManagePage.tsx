/** Screen K-6: Tag management */
import { useEffect, useState, useCallback } from 'react';
import { fetchTags, createTag, updateTag, deleteTag } from '@/api/tags';
import { getApiErrorCode } from '@/api/loadError';
import type { TagItem, TagType } from '@/types';
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

  /* Type filter tab */
  const [activeType, setActiveType] = useState<string>('ALL');

  /* Delete modal */
  const [deleteTarget, setDeleteTarget] = useState<TagItem | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

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
    setIsCreateMode(true);
    setEditTag(null);
    setFormName('');
    setFormType('GENRE');
    setNameError(null);
    setFormError(null);
  };

  /* Open edit modal */
  const openEdit = (tag: TagItem) => {
    setIsCreateMode(false);
    setEditTag(tag);
    setFormName(tag.name);
    setFormType(tag.type);
    setNameError(null);
    setFormError(null);
  };

  /* Close form modal */
  const closeFormModal = () => {
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
    setNameError(null);
    setFormError(null);
    setFormLoading(true);
    try {
      if (isCreateMode) {
        await createTag({ name: canonicalName, type: formType });
      } else if (editTag) {
        await updateTag(editTag.id, { name: canonicalName, type: formType });
      }
      closeFormModal();
      loadTags();
    } catch (error) {
      const errorCode = getApiErrorCode(error);
      if (errorCode === 'TAG_NAME_DUPLICATED') {
        setNameError(TAG_NAME_DUPLICATE_MESSAGE);
      } else if (errorCode === 'TAG_NAME_INVALID') {
        setNameError(TAG_NAME_SERVER_INVALID_MESSAGE);
      } else {
        setFormError(getSafeApiMessage(error, TAG_SAVE_FAILURE_MESSAGE));
      }
    } finally {
      setFormLoading(false);
    }
  };

  const openDelete = (tag: TagItem) => {
    setDeleteTarget(tag);
    setDeleteError(null);
  };

  const closeDeleteModal = () => {
    setDeleteTarget(null);
    setDeleteError(null);
  };

  /* Confirm delete */
  const confirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await deleteTag(deleteTarget.id);
      closeDeleteModal();
      loadTags();
    } catch (error) {
      setDeleteError(getSafeApiMessage(error, TAG_DELETE_FAILURE_MESSAGE));
    } finally {
      setDeleteLoading(false);
    }
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
          <Button variant="ghost" size="sm" onClick={closeFormModal}>
            Cancel
          </Button>
          <Button size="sm" loading={formLoading} onClick={handleFormSubmit}>
            {isCreateMode ? 'Create' : 'Save'}
          </Button>
        </div>
      </Modal>

      {/* Delete confirm modal */}
      <Modal open={deleteTarget !== null} onClose={closeDeleteModal} title="Delete Tag">
        <div className={styles.deleteText}>
          Are you sure you want to delete tag{' '}
          <strong>
            {deleteTarget
              ? formatTagNameForDisplay(deleteTarget.name, deleteTarget.type)
              : undefined}
          </strong>
          ?
        </div>
        {deleteError ? (
          <div className={styles.modalError} role="alert">
            {deleteError}
          </div>
        ) : null}
        <div className={styles.modalActions}>
          <Button variant="ghost" size="sm" onClick={closeDeleteModal}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" loading={deleteLoading} onClick={confirmDelete}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
