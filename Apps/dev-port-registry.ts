import fs from 'node:fs';
import path from 'node:path';
import type { Plugin, ViteDevServer } from 'vite';

const REGISTRY_PATH = path.resolve(__dirname, '.dev-ports.json');

function readRegistry(): Record<string, number> {
  try {
    return JSON.parse(fs.readFileSync(REGISTRY_PATH, 'utf-8'));
  } catch {
    return {};
  }
}

function writeRegistry(registry: Record<string, number>) {
  fs.writeFileSync(REGISTRY_PATH, JSON.stringify(registry, null, 2));
}

/**
 * Vite picks its own port dynamically whenever the preferred one is taken (5173 -> 5174 -> ...),
 * so no app may hardcode a counterpart's port for cross-portal links (staff-console linking to
 * guardian-portal and back). This plugin records this app's actual bound port into a small shared
 * JSON file once its dev server starts listening, and serves the whole registry same-origin at
 * `/__dev-ports.json` so the OTHER app's client code can look up this app's real port at runtime
 * instead of guessing. Dev-only — production origins come from VITE_*_URL env vars instead.
 */
export function devPortRegistryPlugin(appName: string): Plugin {
  return {
    name: 'dev-port-registry',
    configureServer(server: ViteDevServer) {
      server.httpServer?.once('listening', () => {
        const address = server.httpServer?.address();
        const port = typeof address === 'object' && address ? address.port : undefined;
        if (port) {
          const registry = readRegistry();
          registry[appName] = port;
          writeRegistry(registry);
        }
      });

      server.middlewares.use('/__dev-ports.json', (_req, res) => {
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify(readRegistry()));
      });
    },
  };
}
