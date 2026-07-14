import type { ClientRequest, IncomingHttpHeaders } from 'node:http';
import { isIP } from 'node:net';
import { defineConfig } from 'vitest/config';
import { loadEnv, type ProxyOptions, type UserConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export const INTERNAL_CLIENT_IP_HEADER = 'X-ATStudio-Client-IP';

const LOCAL_ALLOWED_HOSTS = ['localhost', '127.0.0.1'] as const;
const CLOUDFLARE_CLIENT_IP_HEADER = 'cf-connecting-ip';

interface ProxySourceRequest {
  headers: IncomingHttpHeaders;
  socket: {
    remoteAddress?: string;
  };
}

type ProxyHeaderTarget = Pick<ClientRequest, 'removeHeader' | 'setHeader'>;

export function parseAcceptancePublicHost(publicBaseUrl: string | undefined): string | undefined {
  if (!publicBaseUrl) {
    return undefined;
  }
  if (publicBaseUrl !== publicBaseUrl.trim()) {
    throw new Error('APP_PUBLIC_BASE_URL must be an absolute HTTPS root origin.');
  }

  let url: URL;
  try {
    url = new URL(publicBaseUrl);
  } catch {
    throw new Error('APP_PUBLIC_BASE_URL must be an absolute HTTPS root origin.');
  }

  if (
    url.protocol !== 'https:' ||
    !url.hostname ||
    url.username ||
    url.password ||
    url.pathname !== '/' ||
    url.search ||
    url.hash ||
    publicBaseUrl.endsWith('/')
  ) {
    throw new Error('APP_PUBLIC_BASE_URL must be an absolute HTTPS root origin.');
  }

  return url.hostname.toLowerCase();
}

export function buildAllowedHosts(publicBaseUrl: string | undefined): string[] {
  const publicHost = parseAcceptancePublicHost(publicBaseUrl);
  return [...new Set(publicHost ? [...LOCAL_ALLOWED_HOSTS, publicHost] : LOCAL_ALLOWED_HOSTS)];
}

export function validateSingleIpLiteral(value: unknown): string | undefined {
  if (
    typeof value !== 'string' ||
    value.length === 0 ||
    value.length > 64 ||
    value !== value.trim() ||
    /[\s,%\[\]]/.test(value) ||
    isIP(value) === 0
  ) {
    return undefined;
  }
  return value;
}

export function resolveProxyClientIp(
  request: ProxySourceRequest,
  acceptanceMode: boolean,
): string | undefined {
  const candidate = acceptanceMode
    ? request.headers[CLOUDFLARE_CLIENT_IP_HEADER]
    : request.socket.remoteAddress;
  return validateSingleIpLiteral(candidate);
}

export function rewriteProxyIdentityHeaders(
  proxyRequest: ProxyHeaderTarget,
  request: ProxySourceRequest,
  acceptanceMode: boolean,
): void {
  for (const headerName of Object.keys(request.headers)) {
    const normalizedName = headerName.toLowerCase();
    if (
      normalizedName === 'forwarded' ||
      normalizedName.startsWith('x-forwarded-') ||
      normalizedName === INTERNAL_CLIENT_IP_HEADER.toLowerCase()
    ) {
      proxyRequest.removeHeader(headerName);
    }
  }
  proxyRequest.removeHeader(CLOUDFLARE_CLIENT_IP_HEADER);
  proxyRequest.removeHeader(INTERNAL_CLIENT_IP_HEADER);

  const clientIp = resolveProxyClientIp(request, acceptanceMode);
  if (clientIp) {
    proxyRequest.setHeader(INTERNAL_CLIENT_IP_HEADER, clientIp);
  }
}

function createProxyOptions(acceptanceMode: boolean): ProxyOptions {
  return {
    target: 'http://127.0.0.1:8080',
    changeOrigin: true,
    xfwd: false,
    configure(proxy) {
      proxy.on('proxyReq', (proxyRequest, request) => {
        rewriteProxyIdentityHeaders(proxyRequest, request, acceptanceMode);
      });
    },
  };
}

export function createViteConfig(environment: Record<string, string | undefined>): UserConfig {
  const publicBaseUrl = environment.APP_PUBLIC_BASE_URL;
  const acceptanceMode = parseAcceptancePublicHost(publicBaseUrl) !== undefined;
  const proxyOptions = createProxyOptions(acceptanceMode);

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      host: '127.0.0.1',
      port: 5173,
      strictPort: true,
      allowedHosts: buildAllowedHosts(publicBaseUrl),
      proxy: {
        '/api': proxyOptions,
        '/uploads': createProxyOptions(acceptanceMode),
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: './src/test/setup.ts',
      css: true,
    },
  };
}

export default defineConfig(({ mode }) => {
  const environment = {
    ...loadEnv(mode, process.cwd(), ''),
    ...process.env,
  };
  return createViteConfig(environment);
});
