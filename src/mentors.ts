export type MentorType = 'engineer' | 'architect' | 'recruiter' | 'product'

export type MentorAgent = {
  type: MentorType
  name: string
  role: string
  lens: string
  color: string
  score: number
  finding: string
  evidence: string[]
  recommendation: string
  question: string
}

export type MentorCouncil = {
  agents: MentorAgent[]
  synthesis: string
  nextMove: string
}

const mentorSeeds: MentorAgent[] = [
  {
    type: 'engineer',
    name: 'Maya Chen',
    role: 'Engineer mentor',
    lens: 'Code quality and execution',
    color: '#8dc7ff',
    score: 68,
    finding: 'You move quickly from idea to implementation. The next quality jump is making your work easier for another engineer to trust.',
    evidence: ['React + TypeScript in 2 repositories', 'JUnit signal in 1 repository', 'Documentation coverage is inconsistent'],
    recommendation: 'Add a small test suite and a clear local setup path to the project you are most proud of.',
    question: 'What would another engineer need to run this without asking you for help?',
  },
  {
    type: 'architect',
    name: 'Ishan Patel',
    role: 'Architect mentor',
    lens: 'Systems thinking and scale',
    color: '#c0a7ff',
    score: 59,
    finding: 'Your service instincts are forming. You have the ingredients for backend depth, but not yet enough production evidence to prove the loop.',
    evidence: ['Spring Boot patterns across 3 repositories', 'No deployment or container signal found', 'Redis and distributed systems are current gaps'],
    recommendation: 'Take one API from local development to a deployed system with caching, observability, and a short architecture note.',
    question: 'Where does this system fail first when traffic, data, or team size grows?',
  },
  {
    type: 'recruiter',
    name: 'Ava Williams',
    role: 'Recruiter mentor',
    lens: 'Employability and proof',
    color: '#e3f46b',
    score: 73,
    finding: 'Your technology range is visible. Your public story is less complete than your building ability.',
    evidence: ['12 public repositories', 'Strong Java and React keyword signal', 'Few visible releases or outcome statements'],
    recommendation: 'Turn one repository into a portfolio proof point with a concise README, live link, architecture diagram, and impact statement.',
    question: 'Can a hiring manager understand the problem you solved in thirty seconds?',
  },
  {
    type: 'product',
    name: 'Noah Okafor',
    role: 'Product mentor',
    lens: 'User value and outcomes',
    color: '#ff8a65',
    score: 51,
    finding: 'You demonstrate implementation energy. The strongest next signal is connecting technical choices to a real user outcome.',
    evidence: ['High builder score: 92', 'Finisher score is 34', 'Project purpose is not consistently visible in documentation'],
    recommendation: 'Choose one real user problem, define one measurable outcome, and ship the smallest version that proves it.',
    question: 'Who is better off because this project exists?',
  },
]

export function runMentorCouncil(): MentorCouncil {
  const averageScore = Math.round(mentorSeeds.reduce((sum, mentor) => sum + mentor.score, 0) / mentorSeeds.length)
  return {
    agents: mentorSeeds,
    synthesis: `The council sees strong implementation momentum with a ${averageScore}% readiness signal. The shared recommendation is to finish and publish one production-shaped project, because completion is the evidence gap connecting your technical ability to your next role.`,
    nextMove: 'Deploy one Spring Boot API with tests, Docker, a README, and a real user outcome.',
  }
}
