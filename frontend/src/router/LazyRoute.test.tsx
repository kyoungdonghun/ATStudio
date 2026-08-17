import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { RouterProvider, createMemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { createLazyPage } from '@/router/LazyRoute';

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
      '잠시 후 홈에서 다시 접근해 주세요.',
    );
    expect(screen.queryByRole('button', { name: '다시 시도' })).not.toBeInTheDocument();
    expect(`${router.state.location.pathname}${router.state.location.search}`).toBe(
      '/lazy?from=deep-link',
    );
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
