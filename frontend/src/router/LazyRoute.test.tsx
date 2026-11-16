import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createLazyPage } from '@/router/LazyRoute';
import { reloadCurrentDocument } from '@/router/reloadCurrentDocument';

vi.mock('@/router/reloadCurrentDocument', () => ({ reloadCurrentDocument: vi.fn() }));

type PageModule = { default: () => JSX.Element };

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((nextResolve, nextReject) => {
    resolve = nextResolve;
    reject = nextReject;
  });
  return { promise, resolve, reject };
}

function renderLazyRoute(
  loader: () => Promise<PageModule>,
  initialEntries = ['/lazy?from=deep-link'],
) {
  const LazyPage = createLazyPage(loader);
  const router = createMemoryRouter(
    [
      { path: '/', element: <div>Home Page</div> },
      { path: '/previous', element: <div>Previous Page</div> },
      { path: '/lazy', element: <LazyPage /> },
      { path: '/other', element: <div>Other Page</div> },
    ],
    { initialEntries },
  );
  const view = render(<RouterProvider router={router} />);
  return { router, ...view };
}

describe('lazy route recovery', () => {
  beforeEach(() => vi.mocked(reloadCurrentDocument).mockClear());
  it('keeps a copied deep-link URL while the first loader is pending', () => {
    const pending = deferred<PageModule>();
    const { router } = renderLazyRoute(() => pending.promise);

    expect(screen.getByText('로딩 중...')).toBeInTheDocument();
    expect(`${router.state.location.pathname}${router.state.location.search}`).toBe(
      '/lazy?from=deep-link',
    );
  });

  it('performs one fresh retry and keeps the internal URL when it succeeds', async () => {
    const loader = vi
      .fn<() => Promise<PageModule>>()
      .mockRejectedValueOnce(new Error('chunk https://cdn.example/private.js failed'))
      .mockResolvedValueOnce({ default: () => <div>Recovered Page</div> });
    const { router } = renderLazyRoute(loader);

    expect(await screen.findByRole('alert')).toHaveTextContent('페이지를 불러오지 못했습니다');
    expect(screen.queryByText(/cdn\.example|private\.js|chunk/i)).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));

    expect(await screen.findByText('Recovered Page')).toBeInTheDocument();
    expect(loader).toHaveBeenCalledTimes(2);
    expect(`${router.state.location.pathname}${router.state.location.search}`).toBe(
      '/lazy?from=deep-link',
    );
  });

  it('stops after one failed retry without polling, recursion, or reload', async () => {
    const loader = vi.fn<() => Promise<PageModule>>().mockRejectedValue(new Error('raw import'));
    const { router } = renderLazyRoute(loader);

    fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));
    await waitFor(() => expect(loader).toHaveBeenCalledTimes(2));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '화면이 업데이트되었거나 연결이 일시적으로 끊겼을 수 있습니다.',
    );
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: '최신 화면 불러오기' })).toBeEnabled();
    expect(reloadCurrentDocument).not.toHaveBeenCalled();
    expect(`${router.state.location.pathname}${router.state.location.search}`).toBe(
      '/lazy?from=deep-link',
    );
  });

  it('reloads a stale-version failure only on explicit click without replaying the loader', async () => {
    const loader = vi
      .fn<() => Promise<PageModule>>()
      .mockRejectedValue(
        new TypeError('Failed to fetch dynamically imported module: /assets/old-version.js'),
      );
    const { router } = renderLazyRoute(loader, ['/lazy?from=deep-link#tags']);
    const reloadButton = await screen.findByRole('button', { name: '최신 화면 불러오기' });

    expect(screen.getByRole('alert')).toHaveTextContent('저장하지 않은 입력은 사라질 수 있습니다.');
    expect(reloadCurrentDocument).not.toHaveBeenCalled();
    expect(loader).toHaveBeenCalledTimes(1);
    fireEvent.click(reloadButton);
    fireEvent.click(reloadButton);

    expect(reloadCurrentDocument).toHaveBeenCalledTimes(1);
    expect(loader).toHaveBeenCalledTimes(1);
    expect(reloadButton).toBeDisabled();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeDisabled();
    expect(router.state.location).toMatchObject({
      pathname: '/lazy',
      search: '?from=deep-link',
      hash: '#tags',
    });
  });

  it('keeps explicit reload available after a cached import rejection also defeats retry', async () => {
    const rejectedImport = Promise.reject<PageModule>(new TypeError('offline or obsolete chunk'));
    const loader = vi.fn(() => rejectedImport);
    renderLazyRoute(loader);
    fireEvent.click(await screen.findByRole('button', { name: '다시 시도' }));
    await waitFor(() =>
      expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument(),
    );
    expect(reloadCurrentDocument).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: '최신 화면 불러오기' }));
    expect(reloadCurrentDocument).toHaveBeenCalledTimes(1);
    expect(loader).toHaveBeenCalledTimes(2);
  });

  it('offers safe Home and Back recovery actions', async () => {
    const loader = vi.fn<() => Promise<PageModule>>().mockRejectedValue(new Error('raw import'));
    const home = renderLazyRoute(loader);

    fireEvent.click(await screen.findByRole('link', { name: '홈으로' }));
    expect(await screen.findByText('Home Page')).toBeInTheDocument();
    home.unmount();

    const back = renderLazyRoute(loader, ['/previous', '/lazy?from=history']);
    fireEvent.click(await screen.findByRole('button', { name: '이전 화면' }));
    expect(await screen.findByText('Previous Page')).toBeInTheDocument();
    back.unmount();
  });

  it('does not publish a late rejected import after route departure', async () => {
    const pending = deferred<PageModule>();
    const { router } = renderLazyRoute(() => pending.promise);

    await act(async () => {
      await router.navigate('/other');
    });
    expect(screen.getByText('Other Page')).toBeInTheDocument();

    await act(async () => {
      pending.reject(new Error('late raw import'));
      await pending.promise.catch(() => undefined);
    });

    expect(screen.getByText('Other Page')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });
});
