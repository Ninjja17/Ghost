export type ApiProfile = {
  id: string
  name: string
  email: string | null
  targetRole: string | null
  experienceLevel: string | null
  growthGoal: string | null
  createdAt: string
  updatedAt: string
}

export type AuthResponse = {
  expiresAt: string
  profile: ApiProfile
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'

async function request<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers, credentials: 'include' })
  const body = await response.json().catch(() => null) as { message?: string } | null
  if (!response.ok) throw new Error(body?.message ?? 'The API request could not be completed.')
  return body as T
}

export function registerProfile(input: {
  email: string
  password: string
  name: string
  targetRole: string
  experienceLevel: string
  growthGoal: string
}) {
  return request<AuthResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(input),
  })
}

export function getCurrentProfile() {
  return request<ApiProfile>('/auth/me')
}
