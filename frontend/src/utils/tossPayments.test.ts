import { beforeEach, describe, expect, it, vi } from 'vitest';

const TOSS_SDK_URL = 'https://js.tosspayments.com/v2/standard';

describe('loadTossPaymentsSdk', () => {
  beforeEach(() => {
    vi.resetModules();
    delete window.TossPayments;
    document.querySelectorAll(`script[src="${TOSS_SDK_URL}"]`).forEach((script) => script.remove());
  });

  it('removes a failed loader and succeeds on the next attempt', async () => {
    const { loadTossPaymentsSdk } = await import('@/utils/tossPayments');

    const firstAttempt = loadTossPaymentsSdk();
    const failedScript = document.querySelector<HTMLScriptElement>(`script[src="${TOSS_SDK_URL}"]`);
    expect(failedScript).not.toBeNull();
    failedScript?.dispatchEvent(new Event('error'));

    await expect(firstAttempt).rejects.toThrow('Toss SDK load failed');
    expect(document.querySelector(`script[src="${TOSS_SDK_URL}"]`)).toBeNull();

    const factory = vi.fn();
    const secondAttempt = loadTossPaymentsSdk();
    const retryScript = document.querySelector<HTMLScriptElement>(`script[src="${TOSS_SDK_URL}"]`);
    expect(retryScript).not.toBeNull();
    window.TossPayments = factory;
    retryScript?.dispatchEvent(new Event('load'));

    await expect(secondAttempt).resolves.toBe(factory);
  });

  it('returns the already loaded factory without adding a script', async () => {
    const factory = vi.fn();
    window.TossPayments = factory;
    const { loadTossPaymentsSdk } = await import('@/utils/tossPayments');
    await expect(loadTossPaymentsSdk()).resolves.toBe(factory);
    expect(document.querySelector(`script[src="${TOSS_SDK_URL}"]`)).toBeNull();
  });

  it('shares one pending script load across concurrent callers', async () => {
    const { loadTossPaymentsSdk } = await import('@/utils/tossPayments');
    const first = loadTossPaymentsSdk();
    const second = loadTossPaymentsSdk();
    expect(first).toBe(second);
    expect(document.querySelectorAll(`script[src="${TOSS_SDK_URL}"]`)).toHaveLength(1);
    const factory = vi.fn();
    window.TossPayments = factory;
    document
      .querySelector<HTMLScriptElement>(`script[src="${TOSS_SDK_URL}"]`)
      ?.dispatchEvent(new Event('load'));
    await expect(Promise.all([first, second])).resolves.toEqual([factory, factory]);
  });

  it('listens to an existing script and rejects if it loads without a factory', async () => {
    const script = document.createElement('script');
    script.src = TOSS_SDK_URL;
    document.head.appendChild(script);
    const { loadTossPaymentsSdk } = await import('@/utils/tossPayments');
    const pending = loadTossPaymentsSdk();
    script.dispatchEvent(new Event('load'));
    await expect(pending).rejects.toThrow();
    expect(document.querySelector(`script[src="${TOSS_SDK_URL}"]`)).toBeNull();
  });
});
