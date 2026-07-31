/**
 * Resolves the guardian-portal login URL at runtime. In dev, Vite picks its own port dynamically
 * (5173 -> 5174 -> ... whenever the preferred one is taken), so a hardcoded port would drift out
 * of sync — instead we ask our own dev server (same-origin, no CORS involved) for the counterpart
 * app's actual bound port via the dev-port registry (see ../../dev-port-registry.ts). In
 * production there is no such registry; VITE_GUARDIAN_PORTAL_URL is the source of truth there.
 */
export async function resolveGuardianPortalUrl(): Promise<string> {
  const envUrl = import.meta.env.VITE_GUARDIAN_PORTAL_URL as string | undefined;

  if (import.meta.env.PROD) {
    return envUrl || 'http://localhost:5174/login';
  }

  try {
    const res = await fetch('/__dev-ports.json');
    if (res.ok) {
      const registry: Record<string, number> = await res.json();
      const port = registry['guardian-portal'];
      if (port) {
        return `http://localhost:${port}/login`;
      }
    }
  } catch {
    // dev-port registry unavailable — fall through to the configured/default value below.
  }

  return envUrl || 'http://localhost:5174/login';
}
