// @vitest-environment node

import { describe, expect, it, vi } from 'vitest';
import {
  buildAllowedHosts,
  createViteConfig,
  INTERNAL_CLIENT_IP_HEADER,
  rewriteProxyIdentityHeaders,
} from './vite.config';

Object.defineProperty(globalThis, 'localStorage', {
  configurable: true,
  value: { clear: vi.fn() },
});
Object.defineProperty(globalThis, 'sessionStorage', {
  configurable: true,
  value: { clear: vi.fn() },
});

describe('vite acceptance ingress configuration', () => {
  it('allows only local hosts when no public base URL is supplied', () => {
    const config = createViteConfig({});

    expect(config.server?.allowedHosts).toEqual(['localhost', '127.0.0.1']);
    expect(config.server?.allowedHosts).not.toBe(true);
  });

  it('adds only the exact host derived from APP_PUBLIC_BASE_URL', () => {
    expect(buildAllowedHosts('https://Demo.TryCloudflare.com')).toEqual([
      'localhost',
      '127.0.0.1',
      'demo.trycloudflare.com',
    ]);
  });

  it('proxies both /api and /uploads through the same loopback boundary', () => {
    const config = createViteConfig({
      APP_PUBLIC_BASE_URL: 'https://demo.trycloudflare.com',
    });
    const apiProxy = config.server?.proxy?.['/api'];
    const uploadsProxy = config.server?.proxy?.['/uploads'];

    expect(apiProxy).toMatchObject({
      target: 'http://127.0.0.1:8080',
      changeOrigin: true,
      xfwd: false,
    });
    expect(uploadsProxy).toMatchObject({
      target: 'http://127.0.0.1:8080',
      changeOrigin: true,
      xfwd: false,
    });
  });

  it.each([
    'http://demo.trycloudflare.com',
    'https://demo.trycloudflare.com/',
    'https://demo.trycloudflare.com/path',
    'https://user@demo.trycloudflare.com',
  ])('rejects an invalid public base URL: %s', (publicBaseUrl) => {
    expect(() => buildAllowedHosts(publicBaseUrl)).toThrow(
      'APP_PUBLIC_BASE_URL must be an absolute HTTPS root origin.',
    );
  });

  it('removes spoofable forwarding headers and writes one Cloudflare client IP', () => {
    const removeHeader = vi.fn();
    const setHeader = vi.fn();

    rewriteProxyIdentityHeaders(
      { removeHeader, setHeader },
      {
        headers: {
          forwarded: 'for=203.0.113.9',
          'x-forwarded-for': '203.0.113.10',
          'x-atstudio-client-ip': '203.0.113.11',
          'cf-connecting-ip': '198.51.100.24',
        },
        socket: { remoteAddress: '127.0.0.1' },
      },
      true,
    );

    expect(removeHeader).toHaveBeenCalledWith('forwarded');
    expect(removeHeader).toHaveBeenCalledWith('x-forwarded-for');
    expect(removeHeader).toHaveBeenCalledWith('x-atstudio-client-ip');
    expect(removeHeader).toHaveBeenCalledWith('cf-connecting-ip');
    expect(setHeader).toHaveBeenCalledTimes(1);
    expect(setHeader).toHaveBeenCalledWith(INTERNAL_CLIENT_IP_HEADER, '198.51.100.24');
  });

  it('ignores Cloudflare headers in local mode and uses the socket peer', () => {
    const setHeader = vi.fn();

    rewriteProxyIdentityHeaders(
      { removeHeader: vi.fn(), setHeader },
      {
        headers: { 'cf-connecting-ip': '198.51.100.24' },
        socket: { remoteAddress: '127.0.0.1' },
      },
      false,
    );

    expect(setHeader).toHaveBeenCalledWith(INTERNAL_CLIENT_IP_HEADER, '127.0.0.1');
  });

  it('does not forward an invalid or list-valued client identity', () => {
    const setHeader = vi.fn();

    rewriteProxyIdentityHeaders(
      { removeHeader: vi.fn(), setHeader },
      {
        headers: { 'cf-connecting-ip': '198.51.100.24, 198.51.100.25' },
        socket: { remoteAddress: '127.0.0.1' },
      },
      true,
    );

    expect(setHeader).not.toHaveBeenCalled();
  });
});
