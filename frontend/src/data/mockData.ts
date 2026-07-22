export interface Company {
  id: string;
  name: string;
  logo: string;
  industry: string;
  category: 'FAANG' | 'Big Tech' | 'Startups' | 'Indian Product' | 'FinTech' | 'Cloud' | 'AI' | 'Security' | 'Gaming' | 'Service';
  difficulty: 'Easy' | 'Medium' | 'Hard';
  hiringDifficulty: string;
  interviewStyle: string;
  popularRoles: string[];
  techStack: string[];
  description: string;
}

export const mockUser = {
  name: 'Alex Chen',
  email: 'alex.chen@example.com',
  role: 'Software Engineer',
  avatar: 'https://i.pravatar.cc/150?u=alex',
  streak: 12,
  averageScore: 84,
  interviewsCompleted: 24
};

export const mockResume = {
  fileName: 'Alex_Chen_Resume_2024.pdf',
  score: 92,
  extractedSkills: [
    'React',
    'TypeScript',
    'Node.js',
    'Python',
    'AWS',
    'System Design',
    'GraphQL'
  ],
  detectedProjects: [
    { name: 'E-commerce Platform', tech: 'React, Node.js, PostgreSQL' },
    { name: 'Real-time Chat App', tech: 'WebSocket, Redis, React' }
  ],
  detectedExperience: [
    {
      role: 'Frontend Engineer',
      company: 'TechCorp',
      duration: '2021 - Present'
    },
    {
      role: 'Software Developer Intern',
      company: 'StartupInc',
      duration: '2020 - 2021'
    }
  ]
};

export const mockCompanies: Company[] = [
  // FAANG & Big Tech
  {
    id: 'google',
    name: 'Google',
    logo: 'https://logo.clearbit.com/google.com',
    industry: 'Technology & Search',
    category: 'FAANG',
    difficulty: 'Hard',
    hiringDifficulty: 'Very High',
    interviewStyle: 'Data Structures, System Design, Googleyness',
    popularRoles: ['Software Engineer', 'Frontend Engineer', 'Staff Engineer'],
    techStack: ['C++', 'Java', 'Python', 'Go', 'Angular'],
    description: 'Focuses heavily on algorithmic efficiency, clean code, scalable architecture, and leadership principles.'
  },
  {
    id: 'microsoft',
    name: 'Microsoft',
    logo: 'https://logo.clearbit.com/microsoft.com',
    industry: 'Enterprise Software & Cloud',
    category: 'Big Tech',
    difficulty: 'Medium',
    hiringDifficulty: 'High',
    interviewStyle: 'Coding, Object-Oriented Design, System Architecture',
    popularRoles: ['Software Engineer', 'DevOps Engineer', 'Cloud Architect'],
    techStack: ['C#', '.NET', 'Azure', 'TypeScript', 'C++'],
    description: 'Values strong OOP principles, clear communication, system design, and Azure ecosystem knowledge.'
  },
  {
    id: 'amazon',
    name: 'Amazon',
    logo: 'https://logo.clearbit.com/amazon.com',
    industry: 'E-commerce & Cloud Computing',
    category: 'FAANG',
    difficulty: 'Hard',
    hiringDifficulty: 'Very High',
    interviewStyle: 'Leadership Principles (STAR) & Coding / Architecture',
    popularRoles: ['Software Development Engineer', 'System Architect', 'Data Engineer'],
    techStack: ['Java', 'AWS', 'Python', 'React', 'DynamoDB'],
    description: 'Behavioral answers MUST use the STAR method tied directly to Amazon 16 Leadership Principles.'
  },
  {
    id: 'meta',
    name: 'Meta',
    logo: 'https://logo.clearbit.com/meta.com',
    industry: 'Social Media & Virtual Reality',
    category: 'FAANG',
    difficulty: 'Hard',
    hiringDifficulty: 'Very High',
    interviewStyle: 'Fast-paced Coding, Rapid Problem Solving, Product Architecture',
    popularRoles: ['Frontend Engineer', 'Backend Engineer', 'ML Engineer'],
    techStack: ['React', 'Python', 'Hack/PHP', 'PyTorch', 'C++'],
    description: 'High speed coding expectation (2 hard LC questions in 45 mins) and frontend mastery.'
  },
  {
    id: 'apple',
    name: 'Apple',
    logo: 'https://logo.clearbit.com/apple.com',
    industry: 'Consumer Electronics & Software',
    category: 'FAANG',
    difficulty: 'Hard',
    hiringDifficulty: 'Very High',
    interviewStyle: 'Deep Technical Knowledge, Low-level Systems & Craftsmanship',
    popularRoles: ['iOS Engineer', 'Embedded Systems Engineer', 'Software Engineer'],
    techStack: ['Swift', 'Objective-C', 'C++', 'Metal', 'Python'],
    description: 'Team-specific interviews focusing deeply on low-level memory management and pixel perfection.'
  },
  {
    id: 'openai',
    name: 'OpenAI',
    logo: 'https://logo.clearbit.com/openai.com',
    industry: 'Artificial Intelligence',
    category: 'AI',
    difficulty: 'Hard',
    hiringDifficulty: 'Elite',
    interviewStyle: 'LLM Architectures, High Performance Systems, Distributed Training',
    popularRoles: ['AI Research Engineer', 'Infrastructure SDE', 'Full Stack SDE'],
    techStack: ['Python', 'PyTorch', 'Rust', 'Kubernetes', 'TypeScript'],
    description: 'Expect questions on Transformer models, CUDA optimization, vector databases, and AI safety.'
  },
  {
    id: 'stripe',
    name: 'Stripe',
    logo: 'https://logo.clearbit.com/stripe.com',
    industry: 'Financial Technology',
    category: 'FinTech',
    difficulty: 'Hard',
    hiringDifficulty: 'Very High',
    interviewStyle: 'Practical Coding, API Design, Refactoring & Bug Hunt',
    popularRoles: ['Backend Engineer', 'Infrastructure Engineer', 'Security Engineer'],
    techStack: ['Ruby', 'Java', 'Go', 'TypeScript', 'PostgreSQL'],
    description: 'Uses real-world development environment interviews: bug fixing, API extension, and integration tests.'
  },
  {
    id: 'phonepe',
    name: 'PhonePe',
    logo: 'https://logo.clearbit.com/phonepe.com',
    industry: 'FinTech & Digital Payments',
    category: 'Indian Product',
    difficulty: 'Hard',
    hiringDifficulty: 'High',
    interviewStyle: 'High Concurrency, Distributed Transactions & Machine Coding',
    popularRoles: ['Backend SDE II', 'System Architect'],
    techStack: ['Java', 'Spring Boot', 'Kafka', 'HBase', 'Aerospike'],
    description: 'Expect LLD machine coding rounds (build a working app in 90 mins) and extreme transaction concurrency.'
  },
  {
    id: 'tcs',
    name: 'TCS (Digital / Prime)',
    logo: 'https://logo.clearbit.com/tcs.com',
    industry: 'IT Services & Consulting',
    category: 'Service',
    difficulty: 'Easy',
    hiringDifficulty: 'Moderate',
    interviewStyle: 'CS Fundamentals, Aptitude, Core Coding',
    popularRoles: ['Systems Engineer', 'Digital Developer'],
    techStack: ['Java', 'Python', 'SQL', 'HTML/CSS'],
    description: 'Tests foundational data structures, OOP concepts, basic SQL queries, and logical problem solving.'
  }
];

export const mockRoles = [
  'Frontend Engineer',
  'Backend Engineer',
  'Full Stack Developer',
  'Machine Learning Engineer',
  'Data Engineer',
  'DevOps Engineer',
  'Cloud Architect',
  'iOS Developer',
  'Android Developer',
  'System Architect'
];

export const mockHistory = [
  {
    id: '1',
    company: 'Google',
    role: 'Frontend Engineer',
    date: '2024-05-12',
    score: 86,
    status: 'Completed',
    duration: '45m'
  },
  {
    id: '2',
    company: 'Stripe',
    role: 'Full Stack Developer',
    date: '2024-05-08',
    score: 92,
    status: 'Completed',
    duration: '60m'
  }
];

export const mockSkillGap = {
  missingSkills: [
    'System Design Scaling',
    'Advanced Graph Algorithms',
    'Kubernetes Basics'
  ],
  recommendedCourses: [
    {
      title: 'Grokking the System Design Interview',
      platform: 'Educative',
      duration: '20h'
    }
  ],
  recommendedLeetCode: [
    'Course Schedule (Graphs)',
    'LRU Cache (Design)'
  ]
};

export const mockAdminStats = {
  totalUsers: 12450,
  activeInterviews: 342,
  apiRequests: 1250000,
  avgLatency: '240ms',
  recentUsers: [
    {
      id: 1,
      name: 'Sarah Jenkins',
      email: 'sarah@example.com',
      joined: '2 mins ago',
      plan: 'Pro'
    }
  ],
  systemLogs: [
    {
      time: '10:42 AM',
      level: 'INFO',
      message: 'AI Model scaled up to 5 instances'
    }
  ]
};