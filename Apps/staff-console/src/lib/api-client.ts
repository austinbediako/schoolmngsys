let tokens: { accessToken: string; refreshToken: string } | null = null;

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const setTokens = (t: { accessToken: string; refreshToken: string } | null) => {
  tokens = t;
  if (t) {
    localStorage.setItem('auth_tokens', JSON.stringify(t));
  } else {
    localStorage.removeItem('auth_tokens');
  }
};

export const getTokens = () => {
  if (!tokens) {
    try {
      const stored = localStorage.getItem('auth_tokens');
      if (stored) tokens = JSON.parse(stored);
    } catch (e) {
      // ignore
    }
  }
  return tokens;
};

export async function apiClient(url: string, options: RequestInit = {}) {
  const t = getTokens();
  const headers = new Headers(options.headers);

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (t) {
    headers.set('Authorization', `Bearer ${t.accessToken}`);
  }

  const endpointUrl = url.startsWith('http') ? url : `${API_BASE}${url}`;
  let response = await fetch(endpointUrl, { ...options, headers });

  if (response.status === 401 && t?.refreshToken) {
    // Attempt refresh
    try {
      const refreshRes = await fetch(`${API_BASE}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: t.refreshToken })
      });
      if (refreshRes.ok) {
        const newTokens = await refreshRes.json();
        setTokens(newTokens);
        headers.set('Authorization', `Bearer ${newTokens.accessToken}`);
        response = await fetch(endpointUrl, { ...options, headers });
      } else {
        setTokens(null);
        window.location.href = '/login';
        throw new Error('Session expired');
      }
    } catch (e) {
      setTokens(null);
      window.location.href = '/login';
      throw new Error('Session expired');
    }
  }

  if (!response.ok) {
    const contentType = response.headers.get('content-type') || '';
    const errorData = contentType.includes('application/json')
      ? await response.json().catch(() => null)
      : null;
    throw errorData || { status: response.status, title: 'Network Error' };
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }

  return response.text();
}
