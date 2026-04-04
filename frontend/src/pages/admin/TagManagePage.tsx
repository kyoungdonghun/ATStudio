/** Screen K-6: Tag management */
import { useEffect, useState, useCallback } from 'react';
import { fetchTags, createTag, updateTag, deleteTag } from '@/api/tags';
import type { TagItem, TagType } from '@/types';
import { TAG_NAME_MAX } from '@/utils/validation';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import styles from './TagManagePage.module.css';

const TAG_TYPES: TagType[] = ['GENRE', 'MOOD', 'INSTRUMENT'];

export default function TagManagePage() {
  const [tags, setTags] = useState<TagItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* Create/Edit modal */
  const [editTag, setEditTag] = useState<TagItem | null>(null);
  const [isCreateMode, setIsCreateMode] = useState(false);
  const [formName, setFormName] = useState('');
  const [formType, setFormType] = useState<TagType>('GENRE');
  const [formLoading, setFormLoading] = useState(false);

  /* Delete modal */
  const [deleteTarget, setDeleteTarget] = useState<TagItem | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const loadTags = useCallback(() => {
    setLoading(true);
    setError(null);
    fetchTags()
      .then(setTags)
      .catch(() => setError('Failed to load tags'))
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
  };

  /* Open edit modal */
  const openEdit = (tag: TagItem) => {
    setIsCreateMode(false);
    setEditTag(tag);
    setFormName(tag.name);
    setFormType(tag.type);
  };

  /* Close form modal */
  const closeFormModal = () => {
    setEditTag(null);
    setIsCreateMode(false);
  };

  /* Submit create/edit */
  const handleFormSubmit = async () => {
    if (!formName.trim()) return;
    setFormLoading(true);
    try {
      if (isCreateMode) {
        await createTag({ name: formName.trim(), type: formType });
      } else if (editTag) {
        await updateTag(editTag.id, { name: formName.trim(), type: formType });
      }
      closeFormModal();
      loadTags();
    } catch {
      setError('Failed to save tag');
    } finally {
      setFormLoading(false);
    }
  };

  /* Confirm delete */
  const confirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleteLoading(true);
    try {
      await deleteTag(deleteTarget.id);
      setDeleteTarget(null);
      loadTags();
    } catch {
      setError('Failed to delete tag');
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

  if (error) {
    return (
      <div className={styles.page}>
        <div className={styles.error}>{error}</div>
      </div>
    );
  }

  const formModalOpen = isCreateMode || editTag !== null;

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Tag Management</h1>
        <Button size="sm" onClick={openCreate}>
          + New Tag
        </Button>
      </div>

      {/* Table */}
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
          {tags.length === 0 && (
            <tr>
              <td colSpan={4} className={styles.empty}>
                No tags found.
              </td>
            </tr>
          )}
          {tags.map((tag) => (
            <tr key={tag.id} className={styles.row}>
              <td>{tag.id}</td>
              <td>{tag.name}</td>
              <td>
                <span className={styles.typeBadge}>{tag.type}</span>
              </td>
              <td>
                <div className={styles.actionBtns}>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openEdit(tag)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => setDeleteTarget(tag)}
                  >
                    Delete
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Create / Edit Modal */}
      <Modal
        open={formModalOpen}
        onClose={closeFormModal}
        title={isCreateMode ? 'Create Tag' : 'Edit Tag'}
      >
        <div className={styles.modalBody}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>Name</label>
            <input
              className={styles.formInput}
              placeholder="Tag name"
              maxLength={TAG_NAME_MAX}
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
            />
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>Type</label>
            <select
              className={styles.formSelect}
              value={formType}
              onChange={(e) => setFormType(e.target.value as TagType)}
            >
              {TAG_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
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
      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete Tag"
      >
        <div className={styles.deleteText}>
          Are you sure you want to delete tag{' '}
          <strong>{deleteTarget?.name}</strong>?
        </div>
        <div className={styles.modalActions}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setDeleteTarget(null)}
          >
            Cancel
          </Button>
          <Button
            variant="danger"
            size="sm"
            loading={deleteLoading}
            onClick={confirmDelete}
          >
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
