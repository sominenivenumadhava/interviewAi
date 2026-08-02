import React, { createContext, useContext, useState, ReactNode } from 'react';

export interface ActiveInterviewState {
  sessionId: string;
  company: string;
  role: string;
  interviewType: string;
  difficulty: string;
  durationMinutes: number;
  totalQuestions: number;
  currentQuestionNumber: number;
  currentScore: number;
  currentDifficulty: string;
  timeRemainingSeconds: number;
  startedAt: string;
  status: 'idle' | 'in_progress' | 'paused' | 'completed';
  weakSkillsDetected: string[];
  lastEvaluation: {
    score: number;
    feedback: string;
    expectedAnswer: string;
  } | null;
}

interface InterviewSessionContextType {
  activeSession: ActiveInterviewState | null;
  selectedCompany: string;
  setSelectedCompany: (company: string) => void;
  selectedRole: string;
  setSelectedRole: (role: string) => void;
  selectedConfig: {
    interviewType: string;
    difficulty: string;
    duration: string;
    language: string;
    focusAreas: string;
    numberOfQuestions: number;
  };
  setSelectedConfig: React.Dispatch<React.SetStateAction<{
    interviewType: string;
    difficulty: string;
    duration: string;
    language: string;
    focusAreas: string;
    numberOfQuestions: number;
  }>>;
  startSessionState: (session: ActiveInterviewState) => void;
  updateSessionState: (partial: Partial<ActiveInterviewState>) => void;
  endSessionState: () => void;
}

const InterviewSessionContext = createContext<InterviewSessionContextType | undefined>(undefined);

export const InterviewSessionProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [selectedCompany, setSelectedCompany] = useState<string>('Google');
  const [selectedRole, setSelectedRole] = useState<string>('Software Engineer');
  const [selectedConfig, setSelectedConfig] = useState({
    interviewType: 'TECHNICAL',
    difficulty: 'MEDIUM',
    duration: '45',
    language: 'TypeScript',
    focusAreas: 'System Design & React',
    numberOfQuestions: 5
  });

  const [activeSession, setActiveSession] = useState<ActiveInterviewState | null>(null);

  const startSessionState = (session: ActiveInterviewState) => {
    setActiveSession(session);
  };

  const updateSessionState = (partial: Partial<ActiveInterviewState>) => {
    setActiveSession((prev) => (prev ? { ...prev, ...partial } : null));
  };

  const endSessionState = () => {
    setActiveSession(null);
  };

  return (
    <InterviewSessionContext.Provider
      value={{
        activeSession,
        selectedCompany,
        setSelectedCompany,
        selectedRole,
        setSelectedRole,
        selectedConfig,
        setSelectedConfig,
        startSessionState,
        updateSessionState,
        endSessionState
      }}
    >
      {children}
    </InterviewSessionContext.Provider>
  );
};

export const useInterviewSession = () => {
  const context = useContext(InterviewSessionContext);
  if (!context) {
    throw new Error('useInterviewSession must be used within an InterviewSessionProvider');
  }
  return context;
};
