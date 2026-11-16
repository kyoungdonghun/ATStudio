import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, expect, it, vi } from 'vitest';

const mocks = vi.hoisted(() => ({
  fetchTags: vi.fn(),
  createTag: vi.fn(),
  updateTag: vi.fn(),
  fetchTagDeletionImpact: vi.fn(),
  deleteTag: vi.fn(),
}));
vi.mock('@/api/tags', () => mocks);
vi.mock('@/config/appMode', () => ({ isListeningMode: true }));
import TagManagePage from './TagManagePage';

beforeEach(() => {
  vi.clearAllMocks();
  mocks.fetchTags.mockResolvedValue([{ id: 7, name: 'QA Tag', type: 'GENRE' }]);
  mocks.createTag.mockResolvedValue({ id: 8, name: 'QA Mood', type: 'MOOD' });
});

it('creates a tag, reloads the dictionary and never offers the excluded delete action', async () => {
  render(<TagManagePage />);
  await screen.findByText('QA Tag');
  expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: '+ New Tag' }));
  const dialog = screen.getByRole('dialog', { name: 'Create Tag' });
  fireEvent.change(within(dialog).getByLabelText('Name'), { target: { value: 'QA Mood' } });
  fireEvent.change(within(dialog).getByLabelText('Type'), { target: { value: 'MOOD' } });
  mocks.fetchTags.mockResolvedValue([{ id: 8, name: 'QA Mood', type: 'MOOD' }]);
  fireEvent.click(within(dialog).getByRole('button', { name: 'Create' }));
  expect(await screen.findByText('QA Mood')).toBeInTheDocument();
  expect(mocks.createTag).toHaveBeenCalledExactlyOnceWith({ name: 'QA Mood', type: 'MOOD' });
  expect(mocks.fetchTags).toHaveBeenCalledTimes(2);
  expect(mocks.deleteTag).not.toHaveBeenCalled();
});

it('preserves edit input and explains the selected-track protection without retrying', async () => {
  mocks.updateTag.mockRejectedValue({
    response: { status: 409, data: { message: 'Unpublish selected track before editing' } },
  });
  render(<TagManagePage />);
  fireEvent.click(await screen.findByRole('button', { name: 'Edit' }));
  const dialog = screen.getByRole('dialog', { name: 'Edit Tag' });
  fireEvent.change(within(dialog).getByLabelText('Name'), { target: { value: 'New QA Tag' } });
  fireEvent.click(within(dialog).getByRole('button', { name: 'Save' }));
  await waitFor(() =>
    expect(within(dialog).getByRole('alert')).toHaveTextContent('공개 음원의 공개를 먼저 해제'),
  );
  expect(within(dialog).getByLabelText('Name')).toHaveValue('New QA Tag');
  expect(mocks.updateTag).toHaveBeenCalledTimes(1);
});
