let tokens: { accessToken: string; refreshToken: string } | null = null;

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
  headers.set('Content-Type', 'application/json');
  if (t) {
    headers.set('Authorization', `Bearer ${t.accessToken}`);
  }

  let response = await fetch(`/api/v1${url}`, { ...options, headers });

  if (response.status === 401 && t) {
    // Attempt refresh
    try {
      const refreshRes = await fetch(`/api/v1/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: t.refreshToken })
      });
      if (refreshRes.ok) {
        const newTokens = await refreshRes.json();
        setTokens(newTokens);
        headers.set('Authorization', `Bearer ${newTokens.accessToken}`);
        response = await fetch(`/api/v1${url}`, { ...options, headers });
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
    const errorData = await response.json().catch(() => null);
    throw errorData || { status: response.status, title: 'Network Error' };
  }

  // Some endpoints return empty body on 204
  if (response.status === 204) {
    return null;
  }

  return response.json();
}
