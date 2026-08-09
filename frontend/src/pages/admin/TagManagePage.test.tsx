import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TagManagePage from '@/pages/admin/TagManagePage';
import type { TagItem, TagType } from '@/types';

const mocks = vi.hoisted(() => ({
  fetchTags: vi.fn(),
  createTag: vi.fn(),
  updateTag: vi.fn(),
  deleteTag: vi.fn(),
}));

vi.mock('@/api/tags', () => ({
  fetchTags: (...args: unknown[]) => mocks.fetchTags(...args),
  createTag: (...args: unknown[]) => mocks.createTag(...args),
  updateTag: (...args: unknown[]) => mocks.updateTag(...args),
  deleteTag: (...args: unknown[]) => mocks.deleteTag(...args),
}));

const TAGS: TagItem[] = [
  { id: 1, name: 'Hip Hop', type: 'GENRE' },
  { id: 2, name: 'Focus', type: 'MOOD' },
  { id: 3, name: 'Shorts', type: 'USAGE' },
  { id: 4, name: 'Guitar', type: 'INSTRUMENT' },
];

const DUPLICATE_MESSAGE = '이미 존재하는 태그 이름입니다.';

function filterButton(type: 'ALL' | TagType) {
  return screen.getByRole('button', { name: new RegExp(`^${type === 'ALL' ? '전체' : type}`) });
}

async function renderLoadedPage() {
  render(<TagManagePage />);
  await screen.findByRole('heading', { name: 'Tag Management' });
}

function openCreateDialog() {
  fireEvent.click(screen.getByRole('button', { name: '+ New Tag' }));
  return screen.getByRole('dialog', { name: 'Create Tag' });
}

function assertPreservedCreateState(
  dialog: HTMLElement,
  name: string,
  type: TagType,
  activeFilter: 'ALL' | TagType,
  visibleTag: string,
) {
  expect(within(dialog).getByLabelText('Name')).toHaveValue(name);
  expect(within(dialog).getByLabelText('Type')).toHaveValue(type);
  expect(filterButton(activeFilter)).toHaveAttribute('aria-pressed', 'true');
  expect(screen.getByText(visibleTag)).toBeInTheDocument();
}

describe('TagManagePage', () => {
  beforeEach(() => {
    mocks.fetchTags.mockReset().mockResolvedValue(TAGS);
    mocks.createTag.mockReset().mockResolvedValue(TAGS[0]);
    mocks.updateTag.mockReset().mockResolvedValue(TAGS[0]);
    mocks.deleteTag.mockReset().mockResolvedValue(undefined);
  });

  it('blocks a canonical duplicate without a request and recomputes the field error', async () => {
    await renderLoadedPage();
    fireEvent.click(filterButton('GENRE'));
    const dialog = openCreateDialog();
    const nameInput = within(dialog).getByLabelText('Name');
    fireEvent.change(within(dialog).getByLabelText('Type'), { target: { value: 'MOOD' } });
    fireEvent.change(nameInput, { target: { value: '  Hip  Hop  ' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));

    expect(mocks.createTag).not.toHaveBeenCalled();
    expect(within(dialog).getByRole('alert')).toHaveTextContent(DUPLICATE_MESSAGE);
    assertPreservedCreateState(dialog, '  Hip  Hop  ', 'MOOD', 'GENRE', 'Hip Hop');

    fireEvent.change(nameInput, { target: { value: 'Fresh Tag' } });
    expect(within(dialog).queryByText(DUPLICATE_MESSAGE)).not.toBeInTheDocument();
    fireEvent.change(nameInput, { target: { value: 'Focus' } });
    expect(within(dialog).getByRole('alert')).toHaveTextContent(DUPLICATE_MESSAGE);
  });

  it('allows an unchanged self edit and submits a USAGE name without the display hash', async () => {
    await renderLoadedPage();
    fireEvent.click(filterButton('USAGE'));
    const usageCell = screen.getByText('#Shorts');
    const usageRow = usageCell.closest('tr');
    expect(usageRow).not.toBeNull();
    fireEvent.click(within(usageRow as HTMLTableRowElement).getByRole('button', { name: 'Edit' }));
    const dialog = screen.getByRole('dialog', { name: 'Edit Tag' });
    const nameInput = within(dialog).getByLabelText('Name');
    expect(nameInput).toHaveValue('Shorts');

    fireEvent.change(nameInput, { target: { value: '  Shorts  ' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(mocks.updateTag).toHaveBeenCalledWith(3, { name: 'Shorts', type: 'USAGE' }),
    );
    await waitFor(() => expect(screen.queryByRole('dialog', { name: 'Edit Tag' })).toBeNull());
    expect(mocks.createTag).not.toHaveBeenCalled();
  });

  it('maps a server duplicate to the name field and preserves modal, list, filter, name, and type', async () => {
    mocks.createTag.mockRejectedValue({
      response: { status: 409, data: { errorCode: 'TAG_NAME_DUPLICATED' } },
    });
    await renderLoadedPage();
    fireEvent.click(filterButton('MOOD'));
    const dialog = openCreateDialog();
    const nameInput = within(dialog).getByLabelText('Name');
    fireEvent.change(nameInput, { target: { value: 'New Focus' } });
    fireEvent.change(within(dialog).getByLabelText('Type'), { target: { value: 'USAGE' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(DUPLICATE_MESSAGE);
    assertPreservedCreateState(dialog, 'New Focus', 'USAGE', 'MOOD', 'Focus');

    fireEvent.change(nameInput, { target: { value: 'New Focus 2' } });
    expect(within(dialog).queryByText(DUPLICATE_MESSAGE)).not.toBeInTheDocument();
  });

  it('maps a server invalid response to the name field without replacing page state', async () => {
    mocks.createTag.mockRejectedValue({
      response: { status: 400, data: { errorCode: 'TAG_NAME_INVALID' } },
    });
    await renderLoadedPage();
    fireEvent.click(filterButton('GENRE'));
    const dialog = openCreateDialog();
    fireEvent.change(within(dialog).getByLabelText('Name'), { target: { value: 'Server Valid' } });
    fireEvent.change(within(dialog).getByLabelText('Type'), { target: { value: 'MOOD' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '태그 이름 형식을 확인해주세요.',
    );
    assertPreservedCreateState(dialog, 'Server Valid', 'MOOD', 'GENRE', 'Hip Hop');
  });

  it('keeps client-invalid feedback field-local with accessible error semantics', async () => {
    await renderLoadedPage();
    const dialog = openCreateDialog();
    const nameInput = within(dialog).getByLabelText('Name');
    fireEvent.change(within(dialog).getByLabelText('Type'), {
      target: { value: 'INSTRUMENT' },
    });
    fireEvent.change(nameInput, { target: { value: 'Bad#Name' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));

    const fieldError = within(dialog).getByRole('alert');
    expect(mocks.createTag).not.toHaveBeenCalled();
    expect(nameInput).toHaveAttribute('aria-invalid', 'true');
    expect(nameInput).toHaveAttribute('aria-describedby', fieldError.id);
    assertPreservedCreateState(dialog, 'Bad#Name', 'INSTRUMENT', 'ALL', '#Shorts');

    fireEvent.change(nameInput, { target: { value: 'Valid Name' } });
    expect(within(dialog).queryByRole('alert')).not.toBeInTheDocument();
    expect(nameInput).not.toHaveAttribute('aria-invalid');
    expect(nameInput).not.toHaveAttribute('aria-describedby');
  });

  it('keeps a generic save failure modal-local and preserves all working state', async () => {
    mocks.createTag.mockRejectedValue(new Error('save failed'));
    await renderLoadedPage();
    fireEvent.click(filterButton('INSTRUMENT'));
    const dialog = openCreateDialog();
    fireEvent.change(within(dialog).getByLabelText('Name'), { target: { value: 'Synth Lead' } });
    fireEvent.change(within(dialog).getByLabelText('Type'), { target: { value: 'MOOD' } });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '태그를 저장하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
    assertPreservedCreateState(dialog, 'Synth Lead', 'MOOD', 'INSTRUMENT', 'Guitar');
    expect(screen.getByRole('heading', { name: 'Tag Management' })).toBeInTheDocument();
  });

  it('keeps delete failure inside the delete modal and displays the USAGE hash', async () => {
    mocks.deleteTag.mockRejectedValue({
      response: { status: 409, data: { message: '연결된 트랙 때문에 삭제할 수 없습니다.' } },
    });
    await renderLoadedPage();
    fireEvent.click(filterButton('USAGE'));
    const usageRow = screen.getByText('#Shorts').closest('tr');
    expect(usageRow).not.toBeNull();
    fireEvent.click(
      within(usageRow as HTMLTableRowElement).getByRole('button', { name: 'Delete' }),
    );
    const dialog = screen.getByRole('dialog', { name: 'Delete Tag' });
    fireEvent.click(within(dialog).getByRole('button', { name: 'Delete' }));

    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '연결된 트랙 때문에 삭제할 수 없습니다.',
    );
    expect(within(dialog).getByText('#Shorts')).toBeInTheDocument();
    expect(filterButton('USAGE')).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('heading', { name: 'Tag Management' })).toBeInTheDocument();
  });
});
