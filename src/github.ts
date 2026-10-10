export type GitHubRepository = {
  name: string
  htmlUrl: string
  language: string | null
  stars: number
  forks: number
  size: number
  updatedAt: string
  fork: boolean
}

export type GitHubProfile = {
  login: string
  name: string | null
  avatarUrl: string
  publicRepos: number
  followers: number
  htmlUrl: string
}

export type GitHubSnapshot = {
  profile: GitHubProfile
  repositories: GitHubRepository[]
  languages: Array<{ name: string; count: number; percentage: number }>
  stars: number
  activeRepositories: number
  observedAt: string
}

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'

export async function fetchGitHubSnapshot(username: string): Promise<GitHubSnapshot> {
  const normalizedUsername = username.trim().replace(/^@/, '')
  if (!normalizedUsername) throw new Error('Enter a GitHub username to continue.')

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}/sources/github/connect?username=${encodeURIComponent(normalizedUsername)}`, {
      method: 'POST',
      credentials: 'include',
    })
  } catch {
    throw new Error(`Shadow Engineer API is unavailable at ${API_BASE_URL}. Start the backend on port 8080 and try again.`)
  }
  const body = await response.json().catch(() => null) as GitHubSnapshot & { message?: string } | null
  if (!response.ok) throw new Error(body?.message ?? 'GitHub could not provide this profile right now.')
  return body as GitHubSnapshot
}
