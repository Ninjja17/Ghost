import { useEffect, useState } from 'react'
import {
  ArrowUpRight,
  Bell,
  CaretDown,
  ChartLineUp,
  Check,
  CaretRight,
  CircleNotch,
  Code,
  Compass,
  GitBranch,
  GithubLogo,
  Lightning,
  LockKey,
  MagnifyingGlass,
  Plus,
  ShieldCheck,
  Sparkle,
  Target,
  TrendUp,
  UsersThree,
  X,
} from '@phosphor-icons/react'
import { fetchGitHubSnapshot, type GitHubSnapshot } from './github'
import { runMentorCouncil, type MentorAgent, type MentorCouncil } from './mentors'
import { getCurrentProfile, getInterpretation, getReport, registerProfile, type InterpretationResponse, type ReportResponse } from './api'

type View = 'overview' | 'evidence' | 'trajectory' | 'mentors'

type UserProfile = {
  id: string
  email: string
  name: string
  experience: string
  targetRole: string
  goal: string
}

type Evidence = {
  label: string
  source: string
  detail: string
  confidence: number
  color: string
}

type DnaScore = {
  label: string
  value: number
  note: string
  color: string
}

const evidence: Evidence[] = [
  { label: 'Java + Spring Boot', source: '3 repositories', detail: 'REST APIs, service layers, dependency injection', confidence: 96, color: '#e3f46b' },
  { label: 'React + TypeScript', source: '2 repositories', detail: 'Component systems, typed client state, Vite', confidence: 91, color: '#8dc7ff' },
  { label: 'Automated testing', source: '1 repository', detail: 'JUnit present, low coverage signal', confidence: 67, color: '#ff8a65' },
  { label: 'Deployment practice', source: '0 repositories', detail: 'No release or container evidence found', confidence: 82, color: '#c0a7ff' },
]

const missions = [
  { week: '01', title: 'Containerize your API', meta: 'Docker fundamentals · 3 sessions', done: true },
  { week: '02', title: 'Add Redis-backed caching', meta: 'Architecture practice · 4 sessions', done: false },
  { week: '03', title: 'Ship authentication', meta: 'Spring Security · 4 sessions', done: false },
  { week: '04', title: 'Deploy and document it', meta: 'Production readiness · 3 sessions', done: false },
]

function buildDnaScores(snapshot: GitHubSnapshot): DnaScore[] {
  const repositorySignal = Math.min(42, snapshot.repositories.length * 4)
  const activitySignal = Math.min(30, snapshot.activeRepositories * 7)
  const languageSignal = Math.min(42, snapshot.languages.length * 8)
  const reachSignal = Math.min(20, Math.round(snapshot.stars / 5))
  return [
    { label: 'Builder', value: Math.min(97, 48 + repositorySignal), note: 'Public project volume', color: '#e3f46b' },
    { label: 'Finisher', value: Math.min(78, 25 + activitySignal + reachSignal), note: 'Release evidence is limited', color: '#ff8a65' },
    { label: 'Learner', value: Math.min(94, 42 + languageSignal), note: 'Language exploration signal', color: '#8dc7ff' },
    { label: 'Architect', value: Math.min(72, 35 + Math.round(activitySignal * .6)), note: 'Needs system-level evidence', color: '#c0a7ff' },
    { label: 'Collaborator', value: 28, note: 'Needs pull request data', color: '#f7be5b' },
  ]
}

function buildPersistedDnaScores(scores: NonNullable<ReportResponse['scores']>): DnaScore[] {
  return [
    { label: 'Builder', value: scores.builderScore, note: 'Persisted evidence score', color: '#e3f46b' },
    { label: 'Finisher', value: scores.finisherScore, note: 'Persisted evidence score', color: '#ff8a65' },
    { label: 'Learner', value: scores.learnerScore, note: 'Persisted evidence score', color: '#8dc7ff' },
    { label: 'Architect', value: scores.architectScore, note: 'Persisted evidence score', color: '#c0a7ff' },
    { label: 'Collaborator', value: scores.collaboratorScore, note: 'Needs pull request data', color: '#f7be5b' },
  ]
}

function App() {
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [restoringSession, setRestoringSession] = useState(true)
  const [activeView, setActiveView] = useState<View>('overview')
  const [showEvidence, setShowEvidence] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [missionOpen, setMissionOpen] = useState(false)
  const [sourceOpen, setSourceOpen] = useState(false)
  const [sourceLoading, setSourceLoading] = useState(false)
  const [sourceError, setSourceError] = useState('')
  const [githubSnapshot, setGithubSnapshot] = useState<GitHubSnapshot | null>(null)
  const [reportScores, setReportScores] = useState<ReportResponse['scores']>(null)
  const [notice, setNotice] = useState('')
  const [mentorCouncil, setMentorCouncil] = useState<MentorCouncil>(() => runMentorCouncil())

  useEffect(() => {
    getCurrentProfile()
      .then((apiProfile) => {
        setProfile(toUserProfile(apiProfile))
        return getReport()
      })
      .then((report) => {
        setReportScores(report.scores)
        if (report.source) {
          setGithubSnapshot(toGitHubSnapshot(report))
          if (report.scores) return getInterpretation().then((interpretation) => setMentorCouncil(toMentorCouncil(interpretation)))
        }
      })
      .catch(() => setProfile(null))
      .finally(() => setRestoringSession(false))
  }, [])

  const refreshReport = () => {
    if (!githubSnapshot) {
      setSourceOpen(true)
      return
    }
    setIsRefreshing(true)
    window.setTimeout(() => setIsRefreshing(false), 900)
  }

  const showNotice = (message: string) => {
    setNotice(message)
    window.setTimeout(() => setNotice(''), 2600)
  }

  const connectGitHub = async (username: string) => {
    setSourceLoading(true)
    setSourceError('')
    try {
      const snapshot = await fetchGitHubSnapshot(username)
      setGithubSnapshot(snapshot)
      const report = await getReport()
      setReportScores(report.scores)
      const interpretation = await getInterpretation()
      setMentorCouncil(toMentorCouncil(interpretation))
      setSourceOpen(false)
    } catch (error) {
      setSourceError(error instanceof Error ? error.message : 'GitHub could not be connected.')
    } finally {
      setSourceLoading(false)
    }
  }

  const profileName = githubSnapshot?.profile.name ?? profile?.name ?? 'Shibani Rao'
  const profileInitials = githubSnapshot?.profile.name?.split(' ').map((part) => part[0]).join('').slice(0, 2) ?? profile?.name.split(' ').map((part) => part[0]).join('').slice(0, 2) ?? 'SR'

  if (restoringSession) {
    return <div className="session-loading"><div className="brand-mark"><span /></div><span>Restoring your workspace</span></div>
  }

  if (!profile) {
    return <Onboarding onComplete={async (nextProfile, password) => {
      const auth = await registerProfile({
        email: nextProfile.email,
        password,
        name: nextProfile.name,
        targetRole: nextProfile.targetRole,
        experienceLevel: nextProfile.experience,
        growthGoal: nextProfile.goal,
      })
      setProfile(toUserProfile(auth.profile))
    }} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-lockup">
          <div className="brand-mark"><span /></div>
          <span>shadow<span className="brand-accent">/</span>engineer</span>
        </div>

        <div className="profile-chip">
          <div className="avatar">S</div>
          <div>
            <strong>{profileName}</strong>
            <span>{profile.targetRole} path</span>
          </div>
          <CaretDown size={14} weight="bold" />
        </div>

        <nav className="primary-nav" aria-label="Primary navigation">
          <button className={activeView === 'overview' ? 'nav-item active' : 'nav-item'} onClick={() => setActiveView('overview')}>
            <Compass size={18} weight={activeView === 'overview' ? 'fill' : 'regular'} /> Overview
          </button>
          <button className={activeView === 'evidence' ? 'nav-item active' : 'nav-item'} onClick={() => setActiveView('evidence')}>
            <GitBranch size={18} weight={activeView === 'evidence' ? 'fill' : 'regular'} /> Evidence ledger <span className="nav-count">{githubSnapshot?.profile.publicRepos ?? 12}</span>
          </button>
          <button className={activeView === 'trajectory' ? 'nav-item active' : 'nav-item'} onClick={() => setActiveView('trajectory')}>
            <ChartLineUp size={18} weight={activeView === 'trajectory' ? 'fill' : 'regular'} /> Trajectory
          </button>
        </nav>

        <div className="sidebar-divider" />
        <p className="side-label">Workspace</p>
        <button className="nav-item" onClick={() => setMissionOpen(true)}><Target size={18} /> 30-day mission</button>
        <button className={activeView === 'mentors' ? 'nav-item active' : 'nav-item'} onClick={() => setActiveView('mentors')}><UsersThree size={18} weight={activeView === 'mentors' ? 'fill' : 'regular'} /> Mentor council <span className="status-dot" /></button>

        <div className="sidebar-bottom">
          <div className="privacy-note"><LockKey size={15} /> Private workspace</div>
          <button className="help-button" onClick={() => setSourceOpen(true)}><Plus size={16} /> Connect a source</button>
          <div className="sidebar-footer"><span>SE / 01</span><span>Updated today</span></div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb"><span>Workspace</span><CaretRight size={14} /><strong>{activeView === 'overview' ? 'Shadow report' : activeView === 'evidence' ? 'Evidence ledger' : activeView === 'trajectory' ? 'Career trajectory' : 'Mentor council'}</strong></div>
          <div className="top-actions">
            <button className="icon-button" aria-label="Search evidence" onClick={() => setActiveView('evidence')}><MagnifyingGlass size={18} /></button>
            <button className="icon-button notification" aria-label="Notifications" onClick={() => showNotice(githubSnapshot ? 'Your evidence is current.' : 'Connect GitHub to start collecting evidence.')}><Bell size={18} /><span /></button>
            <div className="top-avatar">{profileInitials}</div>
          </div>
        </header>

        <div className="page-wrap">
          <section className="report-head">
            <div>
              <div className="eyebrow"><span className="live-pulse" /> REPORT / 01 · OCTOBER 2026</div>
              <h1>{!githubSnapshot ? <>Your report starts with <em>your work.</em></> : activeView === 'overview' ? <>Your work is <em>pointing somewhere.</em></> : activeView === 'evidence' ? <>The evidence behind <em>your signal.</em></> : activeView === 'trajectory' ? <>A future you can <em>build toward.</em></> : <>Four lenses on your <em>next move.</em></>}</h1>
              <p className="head-copy">{!githubSnapshot ? 'Connect a source before Shadow Engineer makes an assessment.' : 'A living read on the engineer you are becoming, grounded in what you actually make.'}</p>
            </div>
            <button className="refresh-button" onClick={refreshReport} disabled={isRefreshing}>
              {isRefreshing ? <CircleNotch className="spin" size={16} /> : <Sparkle size={16} />}
              {isRefreshing ? 'Reading signal' : 'Refresh report'}
            </button>
          </section>

          <div className="freshness-bar">
            <span>{githubSnapshot ? <><ShieldCheck size={15} weight="fill" /> Evidence is current</> : <><LockKey size={15} /> No source connected</>}</span>
            <span className="freshness-detail">{githubSnapshot ? `${githubSnapshot.profile.publicRepos} public repositories · ${githubSnapshot.repositories.length} analyzed · ${githubSnapshot.stars} stars` : 'Connect GitHub to generate your first evidence layer'}</span>
            <button onClick={() => githubSnapshot ? setShowEvidence(!showEvidence) : setSourceOpen(true)}>{githubSnapshot ? (showEvidence ? 'Hide details' : 'View details') : 'Connect GitHub'} <ArrowUpRight size={14} /></button>
          </div>
          {showEvidence && <div className="evidence-popover"><strong>Signal coverage</strong><span>GitHub public activity <b>96%</b></span><span>Resume skills <b>74%</b></span><span>Project outcomes <b className="warn">41%</b></span><button onClick={() => setShowEvidence(false)} aria-label="Close"><X size={14} /></button></div>}

          {!githubSnapshot && <SourceRequiredView onConnect={() => setSourceOpen(true)} />}
          {githubSnapshot && activeView === 'overview' && <Overview onOpenMission={() => setMissionOpen(true)} onOpenEvidence={() => setActiveView('evidence')} githubSnapshot={githubSnapshot} reportScores={reportScores} />}
          {githubSnapshot && activeView === 'evidence' && <EvidenceView githubSnapshot={githubSnapshot} />}
          {githubSnapshot && activeView === 'trajectory' && <TrajectoryView />}
          {githubSnapshot && activeView === 'mentors' && <MentorView council={mentorCouncil} onOpenMission={() => setMissionOpen(true)} />}

          <footer className="page-footer"><span>Shadow Engineer</span><span>Insights are estimates, not identity.</span><span>Built from your evidence <ShieldCheck size={13} /></span></footer>
        </div>
      </main>

      {missionOpen && <MissionModal onClose={() => setMissionOpen(false)} />}
      {sourceOpen && <SourceModal onClose={() => setSourceOpen(false)} onConnect={connectGitHub} loading={sourceLoading} error={sourceError} />}
      {notice && <div className="toast"><ShieldCheck size={15} /> {notice}</div>}
    </div>
  )
}

function toUserProfile(profile: { id: string; email: string | null; name: string; targetRole: string | null; experienceLevel: string | null; growthGoal: string | null }): UserProfile {
  return {
    id: profile.id,
    email: profile.email ?? '',
    name: profile.name,
    targetRole: profile.targetRole ?? 'Engineer',
    experience: profile.experienceLevel ?? '1-2 years',
    goal: profile.growthGoal ?? '',
  }
}

function toGitHubSnapshot(report: ReportResponse): GitHubSnapshot {
  const languages = report.evidence
    .filter((record) => record.evidenceType === 'language_signal')
    .map((record) => {
      const [name, countText] = record.value.split(':')
      return { name, count: Number(countText), percentage: 0 }
    })
  const stars = Number(report.evidence.find((record) => record.evidenceType === 'public_reach')?.value ?? 0)
  const activeRepositories = Number(report.evidence.find((record) => record.evidenceType === 'active_project_count')?.value ?? 0)
  return {
    profile: {
      login: report.source?.githubUsername ?? '',
      name: report.profile.name,
      avatarUrl: '',
      publicRepos: report.repositories.length,
      followers: 0,
      htmlUrl: `https://github.com/${report.source?.githubUsername ?? ''}`,
    },
    repositories: report.repositories.map((repository) => ({
      ...repository,
      updatedAt: repository.updatedAt ?? report.source?.lastSyncedAt ?? new Date().toISOString(),
    })),
    languages,
    stars,
    activeRepositories,
    observedAt: report.source?.lastSyncedAt ?? new Date().toISOString(),
  }
}

function toMentorCouncil(response: InterpretationResponse): MentorCouncil {
  const colors = ['#8dc7ff', '#c0a7ff', '#e3f46b', '#ff8a65']
  const names = ['Engineer mentor', 'Architect mentor', 'Recruiter mentor', 'Product mentor']
  const agents = response.mentors.map((mentor, index) => ({
    type: mentor.mentorType as MentorAgent['type'],
    name: names[index] ?? mentor.mentorType,
    role: `${mentor.mentorType} mentor`,
    lens: mentor.lens,
    color: colors[index] ?? '#8dc7ff',
    score: mentor.confidence,
    finding: mentor.finding,
    evidence: mentor.evidenceIds.map((id) => `Evidence record ${id.slice(0, 8)}`),
    recommendation: mentor.recommendation,
    question: `${mentor.question} ${mentor.uncertainty}`,
  }))
  return {
    agents,
    synthesis: response.recommendations.map((recommendation) => recommendation.rationale).join(' '),
    nextMove: response.recommendations[0]?.action ?? 'Collect more evidence before choosing a next move.',
  }
}

function Onboarding({ onComplete }: { onComplete: (profile: Omit<UserProfile, 'id' | 'email'> & { email: string }, password: string) => Promise<void> }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [experience, setExperience] = useState('1-2 years')
  const [targetRole, setTargetRole] = useState('Backend Engineer')
  const [goal, setGoal] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async () => {
    if (!name.trim() || !email.trim() || !targetRole.trim() || password.length < 8) {
      setError('Add your name, email, target role, and a password of at least 8 characters.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      await onComplete({ email: email.trim(), name: name.trim(), experience, targetRole: targetRole.trim(), goal: goal.trim() }, password)
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Your account could not be created.')
    } finally {
      setSubmitting(false)
    }
  }

  return <main className="onboarding-shell"><div className="onboarding-aside"><div className="brand-lockup"><div className="brand-mark"><span /></div><span>shadow<span className="brand-accent">/</span>engineer</span></div><div className="onboarding-manifesto"><span className="section-index">A PERSONAL READ ON YOUR WORK</span><h1>Know what you are <em>becoming.</em></h1><p>Start with a little context. We will use it to read your work against the direction you actually want.</p></div><div className="onboarding-aside-footer"><span>01 / PROFILE</span><span>Private by default</span></div></div><section className="onboarding-form-wrap"><div className="onboarding-form"><div className="form-progress"><span className="active" /><span /><span /></div><div className="eyebrow"><span className="live-pulse" /> FIRST, A LITTLE CONTEXT</div><h2>What are you<br /><em>building toward?</em></h2><p className="form-intro">This is not a test. It gives the report a direction to measure against.</p><label htmlFor="profile-name">Your name</label><input id="profile-name" value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. Shibani Rao" autoFocus /><label htmlFor="profile-email">Email</label><input id="profile-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" /><label htmlFor="profile-password">Password</label><input id="profile-password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" /><label>Experience level</label><div className="choice-row">{['Student', '0-1 years', '1-2 years', '3+ years'].map((option) => <button type="button" className={experience === option ? 'choice active' : 'choice'} key={option} onClick={() => setExperience(option)}>{option}</button>)}</div><label htmlFor="target-role">Target role</label><input id="target-role" value={targetRole} onChange={(event) => setTargetRole(event.target.value)} placeholder="e.g. Backend Engineer" /><label htmlFor="profile-goal">What do you want to get better at? <span>Optional</span></label><textarea id="profile-goal" value={goal} onChange={(event) => setGoal(event.target.value)} placeholder="e.g. Ship production-ready systems" rows={3} />{error && <p className="form-error">{error}</p>}<button className="dark-button onboarding-submit" onClick={submit} disabled={submitting}>{submitting ? <><CircleNotch className="spin" size={16} /> Creating workspace</> : <>Create my report <ArrowUpRight size={16} /></>}</button><p className="form-footnote"><LockKey size={13} /> Your profile is stored in Shadow Engineer.</p></div></section></main>
}

function Overview({ onOpenMission, onOpenEvidence, githubSnapshot, reportScores }: { onOpenMission: () => void; onOpenEvidence: () => void; githubSnapshot: GitHubSnapshot; reportScores: ReportResponse['scores'] }) {
  const currentDnaScores = reportScores ? buildPersistedDnaScores(reportScores) : buildDnaScores(githubSnapshot)
  return <>
    <section className="identity-grid">
      <div className="identity-panel panel-dark">
        <div className="panel-kicker">CURRENT IDENTITY <span>01</span></div>
        <div className="identity-title"><div className="orbit orbit-one" /><div className="orbit orbit-two" /><div className="identity-initial">F</div><div><span>Frontend-heavy</span><strong>Full Stack<br />Developer</strong></div></div>
        <div className="identity-bottom"><span>Confidence / 82%</span><span>{githubSnapshot ? `Based on ${githubSnapshot.repositories.length} public repositories` : 'Based on 12 repositories'}</span></div>
      </div>
      <div className="insight-panel panel-light">
        <div className="panel-kicker">THE READ <span>02</span></div>
        <p className="insight-quote">You learn quickly and start boldly. Your next unlock is learning to <mark>finish in public.</mark></p>
        <div className="insight-source"><Lightning size={15} weight="fill" /> {githubSnapshot ? `Pattern detected across ${githubSnapshot.activeRepositories} active projects` : 'Pattern detected across 8 projects'} <ArrowUpRight size={14} /></div>
      </div>
    </section>

    <section className="section-block">
      <div className="section-heading"><div><span className="section-index">02 / DEVELOPER DNA</span><h2>How you work</h2></div><span className="section-note">Measured from behavior, not self-description</span></div>
      <div className="dna-grid">{currentDnaScores.map((score) => <div className="dna-item" key={score.label}><div className="dna-top"><span>{score.label}</span><strong>{score.value}</strong></div><div className="score-track"><span style={{ width: `${score.value}%`, background: score.color }} /></div><p>{score.note}</p></div>)}</div>
    </section>

    <section className="lower-grid">
      <div className="career-panel panel-dark">
        <div className="panel-kicker">CAREER TWIN <span>03</span></div>
        <div className="career-main"><div className="career-score">78<small>%</small></div><div><h3>Backend Engineer</h3><p>Strongest match right now</p></div></div>
        <div className="career-bars"><div><span>Backend Engineer</span><i><b style={{ width: '78%' }} /></i><strong>78</strong></div><div><span>Product Engineer</span><i><b style={{ width: '58%' }} /></i><strong>58</strong></div><div><span>AI Engineer</span><i><b style={{ width: '25%' }} /></i><strong>25</strong></div></div>
        <button className="text-button" onClick={onOpenEvidence}>See how we got here <ArrowUpRight size={15} /></button>
      </div>
      <div className="mission-panel panel-light">
        <div className="panel-kicker">NEXT BEST MOVE <span>04</span></div>
        <div className="mission-title"><div className="mission-icon"><TrendUp size={21} /></div><div><span>30-DAY MISSION</span><h3>Finish one thing<br />in public.</h3></div></div>
        <p>Turn your strongest Spring foundation into a deployed, documented project.</p>
        <button className="dark-button" onClick={onOpenMission}>Open mission <CaretRight size={16} /></button>
      </div>
    </section>
  </>
}

function EvidenceView({ githubSnapshot }: { githubSnapshot: GitHubSnapshot | null }) {
  const [filter, setFilter] = useState('All signals')
  const currentEvidence = githubSnapshot ? buildGitHubEvidence(githubSnapshot) : evidence
  return <section className="view-section evidence-view">
    <div className="section-heading"><div><span className="section-index">01 / EVIDENCE LEDGER</span><h2>What the signal says</h2></div><div className="filter-wrap"><button className="filter-button">{filter}<CaretDown size={14} /></button><div className="filter-menu"><button onClick={() => setFilter('All signals')}>All signals</button><button onClick={() => setFilter('Skills')}>Skills</button><button onClick={() => setFilter('Gaps')}>Gaps</button></div></div></div>
    <div className="evidence-list">{currentEvidence.filter((item) => filter === 'All signals' || (filter === 'Gaps' ? item.confidence < 80 : item.confidence >= 80)).map((item) => <div className="evidence-row" key={item.label}><div className="evidence-dot" style={{ background: item.color }} /><div className="evidence-copy"><strong>{item.label}</strong><span>{item.detail}</span></div><span className="evidence-source">{item.source}</span><div className="confidence"><span>Confidence</span><strong>{item.confidence}%</strong></div><ArrowUpRight size={17} /></div>)}</div>
    <div className="evidence-callout"><Code size={20} /><div><strong>Insights stay tied to a source.</strong><p>Every recommendation can be traced back to a repository, commit pattern, or resume signal.</p></div><button className="text-button">How it works <ArrowUpRight size={15} /></button></div>
  </section>
}

function buildGitHubEvidence(snapshot: GitHubSnapshot): Evidence[] {
  const languageColors = ['#e3f46b', '#8dc7ff', '#c0a7ff', '#f7be5b', '#ff8a65']
  const languageEvidence = snapshot.languages.slice(0, 3).map((language, index) => ({
    label: language.name,
    source: `${language.count} repositories`,
    detail: `${language.percentage}% of detected repository languages`,
    confidence: Math.min(98, 64 + language.count * 8),
    color: languageColors[index],
  }))
  return [
    ...languageEvidence,
    { label: 'Repository momentum', source: `${snapshot.activeRepositories} active in 6 months`, detail: 'Recent updates indicate an active building loop', confidence: snapshot.activeRepositories ? 88 : 54, color: '#8dc7ff' },
    { label: 'Open source reach', source: `${snapshot.stars} total stars`, detail: 'Public interest signal, not a quality guarantee', confidence: snapshot.stars ? 72 : 42, color: '#ff8a65' },
  ]
}

function SourceRequiredView({ onConnect }: { onConnect: () => void }) {
  return <section className="source-required panel-light"><div className="source-required-mark"><GithubLogo size={28} weight="fill" /></div><span className="section-index">THE REPORT IS WAITING</span><h2>Connect the work<br /><em>before we read it.</em></h2><p>Shadow Engineer does not guess from an empty workspace. Connect GitHub and we will build your first evidence-backed report from what you actually make.</p><button className="dark-button" onClick={onConnect}>Connect GitHub <ArrowUpRight size={16} /></button><div className="source-required-note"><ShieldCheck size={15} /> Public repository metadata only in this prototype</div></section>
}

function MentorView({ council, onOpenMission }: { council: MentorCouncil; onOpenMission: () => void }) {
  const [selectedMentor, setSelectedMentor] = useState<MentorAgent>(council.agents[0])

  return <section className="view-section mentor-view">
    <div className="section-heading"><div><span className="section-index">01 / MENTOR COUNCIL</span><h2>Different lenses. One direction.</h2></div><span className="section-note">Interpretation grounded in your evidence</span></div>
    <div className="mentor-grid">{council.agents.map((mentor) => <button className={selectedMentor.type === mentor.type ? 'mentor-card selected' : 'mentor-card'} key={mentor.type} onClick={() => setSelectedMentor(mentor)}><div className="mentor-card-top"><MentorIcon type={mentor.type} /><span className="mentor-score">{mentor.score}<small>/100</small></span></div><strong>{mentor.name}</strong><span>{mentor.role}</span><i style={{ background: mentor.color }} /></button>)}</div>
    <div className="mentor-detail panel-light"><div className="mentor-detail-head"><div><span className="section-index">{selectedMentor.role.toUpperCase()}</span><h3>{selectedMentor.lens}</h3></div><div className="mentor-detail-score" style={{ color: selectedMentor.color }}>{selectedMentor.score}<small>/100</small></div></div><p className="mentor-finding">{selectedMentor.finding}</p><div className="mentor-evidence"><span className="section-index">EVIDENCE USED</span>{selectedMentor.evidence.map((item) => <div key={item}><span className="evidence-dot" style={{ background: selectedMentor.color }} />{item}<ShieldCheck size={14} /></div>)}</div><div className="mentor-recommendation"><Target size={19} /><div><span className="section-index">RECOMMENDATION</span><strong>{selectedMentor.recommendation}</strong></div></div><p className="mentor-question"><Sparkle size={15} /> {selectedMentor.question}</p></div>
    <div className="council-synthesis panel-dark"><div><span className="panel-kicker">COUNCIL SYNTHESIS <span>05</span></span><p>{council.synthesis}</p></div><div className="synthesis-next"><span>NEXT MOVE</span><strong>{council.nextMove}</strong><button className="text-button" onClick={onOpenMission}>Add to mission <ArrowUpRight size={15} /></button></div></div>
  </section>
}

function MentorIcon({ type }: { type: MentorAgent['type'] }) {
  if (type === 'engineer') return <Code size={21} />
  if (type === 'architect') return <ChartLineUp size={21} />
  if (type === 'recruiter') return <UsersThree size={21} />
  return <Target size={21} />
}

function TrajectoryView() {
  return <section className="view-section trajectory-view"><div className="section-heading"><div><span className="section-index">01 / FUTURE SIMULATOR</span><h2>Paths from here</h2></div><span className="section-note">Scenarios, not predictions</span></div><div className="trajectory-card"><div className="trajectory-axis"><span>NOW</span><span>6 MONTHS</span><span>12 MONTHS</span></div><div className="trajectory-line main-line"><span className="line-label">Backend Engineer</span><i /><b>79%</b></div><div className="trajectory-line second-line"><span className="line-label">Product Engineer</span><i /><b>65%</b></div><div className="trajectory-line third-line"><span className="line-label">AI Engineer</span><i /><b>30%</b></div><div className="trajectory-now"><span>YOU ARE HERE</span><div /></div></div><div className="trajectory-foot"><div><span>Strongest lever</span><strong>Deployment fluency</strong></div><div><span>Evidence gap</span><strong>Production ownership</strong></div><div><span>Next proof point</span><strong>One public release</strong></div></div></section>
}

function MissionModal({ onClose }: { onClose: () => void }) {
  return <div className="modal-backdrop" onClick={onClose}><div className="mission-modal" onClick={(event) => event.stopPropagation()}><div className="modal-head"><div><span className="section-index">30-DAY MISSION</span><h2>Finish one thing<br /><em>in public.</em></h2></div><button className="icon-button" onClick={onClose} aria-label="Close mission"><X size={19} /></button></div><p className="modal-intro">A focused month to turn your Spring foundation into proof of production readiness.</p><div className="mission-list">{missions.map((mission) => <div className={mission.done ? 'mission-row done' : 'mission-row'} key={mission.week}><span className="week-num">{mission.week}</span><div><strong>{mission.title}</strong><span>{mission.meta}</span></div><div className="mission-check">{mission.done ? <Check size={14} weight="bold" /> : <span />}</div></div>)}</div><button className="dark-button full-button" onClick={onClose}>Start with week one <ArrowUpRight size={16} /></button></div></div>
}

function SourceModal({ onClose, onConnect, loading, error }: { onClose: () => void; onConnect: (username: string) => void; loading: boolean; error: string }) {
  const [username, setUsername] = useState('')

  return <div className="modal-backdrop" onClick={onClose}><div className="source-modal" onClick={(event) => event.stopPropagation()}><div className="modal-head"><div><span className="section-index">CONNECT A SOURCE</span><h2>Start with the<br /><em>work itself.</em></h2></div><button className="icon-button" onClick={onClose} aria-label="Close source dialog"><X size={19} /></button></div><p className="modal-intro">Connect a public GitHub profile and Shadow Engineer will turn repository activity into your first evidence layer.</p><div className="source-option selected"><GithubLogo size={25} weight="fill" /><div><strong>GitHub</strong><span>Public repositories, languages, stars and activity</span></div><ShieldCheck size={17} weight="fill" /></div><label className="input-label" htmlFor="github-username">GitHub username</label><div className="input-row"><span>@</span><input id="github-username" value={username} onChange={(event) => setUsername(event.target.value)} placeholder="your-handle" autoFocus onKeyDown={(event) => { if (event.key === 'Enter') onConnect(username) }} /></div>{error && <p className="source-error">{error}</p>}<p className="source-privacy"><LockKey size={14} /> Only public repository metadata is read in this prototype.</p><button className="dark-button full-button" onClick={() => onConnect(username)} disabled={loading}>{loading ? <><CircleNotch className="spin" size={16} /> Reading GitHub</> : <>Analyze my GitHub <ArrowUpRight size={16} /></>}</button></div></div>
}

export default App
