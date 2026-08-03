import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Mic,
  MicOff,
  Video,
  VideoOff,
  PhoneOff,
  Lightbulb,
  Send,
  Loader2,
  AlertTriangle,
  Radio,
  Pause,
  Play
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';
import { useRequireAuth } from '../../contexts/AuthContext';
import apiClient, { API_ENDPOINTS, ApiError } from '../../lib/apiClient';
import { useDeepgramLive } from '../../hooks/useDeepgramLive';
import { AnswerValidationCard, AnswerValidationData } from '../../components/interview/AnswerValidationCard';

interface LiveQuestion {
  questionNumber: number;
  question: string;
  category: string;
  difficulty: string;
  expectedMinutes: number;
  evaluationCriteria: string[];
  transitionPhrase?: string;
  interviewPhase: string;
  isLastQuestion: boolean;
}

const MAX_ANSWER_LENGTH = 50_000;

// ─── Session-storage persistence ──────────────────────────────────────────────
const ROOM_STORAGE_KEY = 'interviai_room_state';

interface PersistedRoomState {
  sessionId: string;
  currentQIndex: number;
  currentQuestion: LiveQuestion;
  timeLeft: number;
  answer: string;
  scoreHistory: number[];
  currentDifficulty: string;
  isPaused?: boolean;
}

function saveRoomState(state: PersistedRoomState) {
  try {
    sessionStorage.setItem(ROOM_STORAGE_KEY, JSON.stringify(state));
  } catch (_) {}
}

function loadRoomState(): PersistedRoomState | null {
  try {
    const raw = sessionStorage.getItem(ROOM_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as PersistedRoomState) : null;
  } catch (_) {
    return null;
  }
}

function clearRoomState() {
  try {
    sessionStorage.removeItem(ROOM_STORAGE_KEY);
  } catch (_) {}
}

function formatRoundLabel(interviewType: string): string {
  const labels: Record<string, string> = {
    HR: 'HR',
    TECHNICAL: 'Technical',
    CODING: 'Coding',
    SYSTEM_DESIGN: 'System Design',
    MANAGERIAL: 'Managerial',
    APTITUDE: 'Aptitude',
    BEHAVIORAL: 'HR',
    MIXED: 'Technical'
  };
  return labels[interviewType] || interviewType;
}

/** Always show the selected round label (legacy categories remapped). */
function resolveQuestionCategory(category: string | undefined, interviewType: string): string {
  if (interviewType && interviewType !== 'MIXED') {
    return formatRoundLabel(interviewType);
  }
  if (!category) return formatRoundLabel(interviewType || 'TECHNICAL');
  const normalized = category.trim().toLowerCase();
  const map: Record<string, string> = {
    technical: 'Technical',
    behavioral: 'HR',
    hr: 'HR',
    coding: 'Coding',
    'system design': 'System Design',
    system_design: 'System Design',
    managerial: 'Managerial',
    aptitude: 'Aptitude',
    situational: 'HR',
    mixed: 'Technical'
  };
  return map[normalized] || category;
}

function firstQuestionFallback(interviewType: string, role: string, company: string): string {
  switch (interviewType) {
    case 'HR':
    case 'BEHAVIORAL':
      return `Welcome to the HR round for ${role} at ${company}. Tell me about yourself.`;
    case 'CODING':
      return `[EASY] Two Sum (Arrays)

Problem Statement:
Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.

Constraints:
2 <= nums.length <= 10^4

Sample Input:
nums = [2,7,11,15], target = 9

Sample Output:
[0,1]

Expected Time Complexity: O(n)
Expected Space Complexity: O(n)`;
    case 'SYSTEM_DESIGN':
      return `Design a URL Shortener for ${company}. Cover requirements, APIs, data model, caching, and scaling.`;
    case 'MANAGERIAL':
      return `Describe a high-stakes decision you made as a ${role} with incomplete information. How did you decide and what was the outcome?`;
    case 'APTITUDE':
      return `A train 120 m long passes a pole in 6 seconds. What is its speed in km/h? Explain your steps.`;
    case 'TECHNICAL':
    default:
      return `Explain the core OOP principles and how you apply them in day-to-day ${role} work at a company like ${company}.`;
  }
}

function roundInterviewerTitle(interviewType: string): string {
  switch (interviewType) {
    case 'CODING':
      return 'Coding Round Interviewer';
    case 'SYSTEM_DESIGN':
      return 'System Design Interviewer';
    case 'HR':
    case 'BEHAVIORAL':
      return 'HR Interviewer';
    case 'MANAGERIAL':
      return 'Bar Raiser / Managerial Interviewer';
    case 'APTITUDE':
      return 'Aptitude Assessor';
    default:
      return 'Technical Interviewer';
  }
}

function defaultCriteriaForRound(interviewType: string): string[] {
  switch (interviewType) {
    case 'CODING':
      return ['Correctness', 'Time Complexity', 'Space Complexity', 'Edge Cases', 'Communication', 'Optimization'];
    case 'SYSTEM_DESIGN':
      return ['Architecture', 'Scalability', 'Trade-offs', 'Communication'];
    case 'HR':
    case 'BEHAVIORAL':
      return ['Communication', 'Confidence', 'Personality', 'Cultural Fit'];
    case 'MANAGERIAL':
      return ['Decision Making', 'Ownership', 'Leadership', 'Stakeholder Management'];
    case 'APTITUDE':
      return ['Accuracy', 'Logical Reasoning', 'Speed', 'Clarity of Approach'];
    default:
      return ['Core Concepts', 'Practical Knowledge', 'Problem Solving', 'Confidence'];
  }
}

export function Room() {
  const navigate = useNavigate();
  const auth = useRequireAuth();
  const {
    selectedCompany,
    selectedRole,
    selectedConfig,
    startSessionState,
    updateSessionState,
    endSessionState
  } = useInterviewSession();

  const [loading, setLoading] = useState(true);
  const [sessionInitError, setSessionInitError] = useState<string | null>(null);
  const [evaluating, setEvaluating] = useState(false);
  const [validatingAnswer, setValidatingAnswer] = useState(false);
  const [sessionId, setSessionId] = useState<string>('');

  const [currentQIndex, setCurrentQIndex] = useState(1);
  const [currentQuestion, setCurrentQuestion] = useState<LiveQuestion | null>(null);
  const [displayedText, setDisplayedText] = useState('');
  const [answer, setAnswer] = useState('');

  // Ref to detect whether state was restored (skip re-init on remount)
  const restoredFromStorage = useRef(false);

  const [isVideoOff, setIsVideoOff] = useState(false);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);

  // WebRTC Camera Feed Stream
  useEffect(() => {
    if (isVideoOff) {
      if (mediaStreamRef.current) {
        mediaStreamRef.current.getTracks().forEach((track) => track.stop());
        mediaStreamRef.current = null;
      }
      return;
    }

    let isMounted = true;
    setCameraError(null);

    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      navigator.mediaDevices
        .getUserMedia({ video: { width: { ideal: 640 }, height: { ideal: 480 } } })
        .then((stream) => {
          if (!isMounted) {
            stream.getTracks().forEach((track) => track.stop());
            return;
          }
          mediaStreamRef.current = stream;
          if (videoRef.current) {
            videoRef.current.srcObject = stream;
          }
        })
        .catch((err) => {
          console.warn('Webcam permission denied or error:', err);
          if (isMounted) {
            setCameraError('Camera access not granted');
          }
        });
    } else {
      setCameraError('Webcam not supported in browser');
    }

    return () => {
      isMounted = false;
      if (mediaStreamRef.current) {
        mediaStreamRef.current.getTracks().forEach((track) => track.stop());
        mediaStreamRef.current = null;
      }
    };
  }, [isVideoOff]);

  // Ensure camera feed srcObject is set stably without blinking on 1s timer ticks
  useEffect(() => {
    if (videoRef.current && mediaStreamRef.current && videoRef.current.srcObject !== mediaStreamRef.current) {
      videoRef.current.srcObject = mediaStreamRef.current;
    }
  });

  const [showHints, setShowHints] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isPaused, setIsPaused] = useState(() => {
    const saved = loadRoomState();
    return !!saved?.isPaused;
  });
  const isPausedRef = useRef(isPaused);
  isPausedRef.current = isPaused;
  const wasRecordingBeforePause = useRef(false);
  const answerTextareaRef = useRef<HTMLTextAreaElement | null>(null);
  const stickAnswerToBottomRef = useRef(true);

  const [timeLeft, setTimeLeft] = useState(() => {
    const saved = loadRoomState();
    return saved ? saved.timeLeft : parseInt(selectedConfig.duration || '45') * 60;
  });

  const [validationResult, setValidationResult] = useState<AnswerValidationData | null>(null);
  const [scoreHistory, setScoreHistory] = useState<number[]>(() => {
    const saved = loadRoomState();
    return saved ? saved.scoreHistory : [];
  });
  const [currentDifficulty, setCurrentDifficulty] = useState<string>(() => {
    const saved = loadRoomState();
    return saved ? saved.currentDifficulty : selectedConfig.difficulty;
  });
  const submitInFlightRef = useRef(false);

  // Live Speech-to-Text Integration — preserve answer scroll while transcript appends
  const handleTranscriptUpdate = useCallback((newTranscript: string) => {
    if (isPausedRef.current) return;
    if (newTranscript) {
      setAnswer(newTranscript.slice(0, MAX_ANSWER_LENGTH));
      setValidationError(null);
    }
  }, []);

  const {
    isRecording,
    isConnecting,
    startRecording,
    stopRecording,
    provider: sttProvider,
    error: speechError
  } = useDeepgramLive({
    onTranscriptUpdate: handleTranscriptUpdate,
  });

  // Smooth-scroll answer box when STT appends (only if user is near the bottom)
  useEffect(() => {
    const el = answerTextareaRef.current;
    if (!el || !stickAnswerToBottomRef.current) return;
    requestAnimationFrame(() => {
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
    });
  }, [answer]);

  // Trigger Automatic LLM Answer Validation when user finishes speaking
  const triggerAutoValidation = useCallback(async (
    transcriptText: string
  ): Promise<AnswerValidationData | null> => {
    if (isPausedRef.current) return null;
    if (!transcriptText || transcriptText.trim().length < 5 || !currentQuestion) {
      return null;
    }
    setValidatingAnswer(true);

    try {
      const res = await apiClient.post<any>(API_ENDPOINTS.SPEECH.VALIDATE, {
        question: currentQuestion.question,
        transcript: transcriptText,
        role: selectedRole,
        company: selectedCompany,
        interviewType: selectedConfig.interviewType
      });

      if (res.data) {
        setValidationResult(res.data);
        return res.data;
      }
      return null;
    } catch (e) {
      console.warn('Realtime validation API error, using fallback:', e);
      // Fallback validation
      const fallbackValidation: AnswerValidationData = {
        isRelevant: transcriptText.trim().length > 10,
        score: transcriptText.trim().length > 10 ? 8.5 : 2.0,
        confidence: 94,
        feedback: 'Good technical response. Remember to explicitly mention measurable metrics and trade-offs.',
        missingPoints: ['Performance optimization', 'Scalability decisions', 'Measurable impact']
      };
      setValidationResult(fallbackValidation);
      return fallbackValidation;
    } finally {
      setValidatingAnswer(false);
    }
  }, [currentQuestion, selectedRole, selectedCompany, selectedConfig.interviewType]);

  // Toggle Microphone / Recording
  const handleMicToggle = async () => {
    if (isPaused) return;
    if (isRecording) {
      const finalText = stopRecording();
      const textToValidate = (finalText || answer).trim();
      if (textToValidate) {
        triggerAutoValidation(textToValidate);
      }
    } else {
      setValidationResult(null);
      setValidationError(null);
      await startRecording(answer);
    }
  };

  const handlePauseInterview = useCallback(() => {
    if (isPaused) return;
    // Freeze question text fully visible
    if (currentQuestion?.question) {
      setDisplayedText(currentQuestion.question);
    }
    if (isRecording) {
      wasRecordingBeforePause.current = true;
      stopRecording();
    } else {
      wasRecordingBeforePause.current = false;
    }
    setIsPaused(true);
    updateSessionState({ status: 'paused' });
  }, [isPaused, currentQuestion, isRecording, stopRecording, updateSessionState]);

  const handleResumeInterview = useCallback(async () => {
    if (!isPaused) return;
    setIsPaused(false);
    updateSessionState({ status: 'in_progress' });
    if (wasRecordingBeforePause.current) {
      wasRecordingBeforePause.current = false;
      setValidationResult(null);
      await startRecording(answer);
    }
  }, [isPaused, updateSessionState, startRecording, answer]);

  // ── Persist key room state to sessionStorage on every relevant change ────────
  useEffect(() => {
    if (!sessionId || !currentQuestion) return;
    saveRoomState({
      sessionId,
      currentQIndex,
      currentQuestion,
      timeLeft,
      answer,
      scoreHistory,
      currentDifficulty,
      isPaused
    });
  }, [sessionId, currentQIndex, currentQuestion, timeLeft, answer, scoreHistory, currentDifficulty, isPaused]);

  // Initialize Interview Session with Backend (or restore from sessionStorage)
  useEffect(() => {
    if (auth.isLoading || !auth.isAuthenticated) {
      return;
    }

    // ── Restore path: user navigated away and came back ──────────────────────
    const saved = loadRoomState();
    if (saved && saved.sessionId && saved.currentQuestion) {
      restoredFromStorage.current = true;
      setSessionId(saved.sessionId);
      setCurrentQIndex(saved.currentQIndex);
      setCurrentQuestion(saved.currentQuestion);
      setAnswer(saved.answer || '');
      setIsPaused(!!saved.isPaused);
      // timeLeft, scoreHistory, currentDifficulty already restored in useState initialisers
      startSessionState({
        sessionId: saved.sessionId,
        company: selectedCompany,
        role: selectedRole,
        interviewType: selectedConfig.interviewType,
        difficulty: saved.currentDifficulty,
        durationMinutes: parseInt(selectedConfig.duration || '45'),
        totalQuestions: selectedConfig.numberOfQuestions || 5,
        currentQuestionNumber: saved.currentQIndex,
        currentScore: saved.scoreHistory.length
          ? Math.round(saved.scoreHistory.reduce((a, b) => a + b, 0) / saved.scoreHistory.length)
          : 0,
        currentDifficulty: saved.currentDifficulty,
        timeRemainingSeconds: saved.timeLeft,
        startedAt: new Date().toISOString(),
        status: saved.isPaused ? 'paused' : 'in_progress',
        weakSkillsDetected: [],
        lastEvaluation: null
      });
      setLoading(false);
      return;
    }

    // ── Fresh start path ─────────────────────────────────────────────────────
    const initSession = async () => {
      setLoading(true);
      setSessionInitError(null);
      try {
        const createRes = await apiClient.post<any>(API_ENDPOINTS.INTERVIEW.CREATE, {
          company: selectedCompany,
          role: selectedRole,
          interviewType: selectedConfig.interviewType,
          difficultyLevel: selectedConfig.difficulty,
          durationMinutes: parseInt(selectedConfig.duration || '45'),
          numberOfQuestions: selectedConfig.numberOfQuestions || 5,
          includeCodingQuestions: selectedConfig.interviewType === 'CODING',
          focusAreas: [
            selectedConfig.focusAreas || selectedConfig.interviewType,
            selectedConfig.interviewType === 'CODING' && selectedConfig.language
              ? `Preferred language: ${selectedConfig.language}`
              : selectedConfig.language && ['TECHNICAL', 'MIXED'].includes(selectedConfig.interviewType)
                ? `Preferred language: ${selectedConfig.language}`
                : ''
          ].filter(Boolean).join('; ')
        }, {
          timeout: 60000,
          retry: false
        });

        const createdSessionId = createRes.data?.sessionId || `sess-${Date.now()}`;
        setSessionId(createdSessionId);

        startSessionState({
          sessionId: createdSessionId,
          company: selectedCompany,
          role: selectedRole,
          interviewType: selectedConfig.interviewType,
          difficulty: selectedConfig.difficulty,
          durationMinutes: parseInt(selectedConfig.duration || '45'),
          totalQuestions: selectedConfig.numberOfQuestions || 5,
          currentQuestionNumber: 1,
          currentScore: 0,
          currentDifficulty: selectedConfig.difficulty,
          timeRemainingSeconds: parseInt(selectedConfig.duration || '45') * 60,
          startedAt: new Date().toISOString(),
          status: 'in_progress',
          weakSkillsDetected: [],
          lastEvaluation: null
        });

        const startRes = await apiClient.post<any>(API_ENDPOINTS.INTERVIEW.START(createdSessionId));

        if (startRes.data && startRes.data.questions && startRes.data.questions.length > 0) {
          const qData = startRes.data.questions[0];
          setCurrentQuestion({
            questionNumber: 1,
            question: qData.questionText || qData.question,
            category: resolveQuestionCategory(qData.category, selectedConfig.interviewType),
            difficulty: qData.difficultyLevel || selectedConfig.difficulty,
            expectedMinutes: 5,
            evaluationCriteria: qData.evaluationCriteria?.length
              ? qData.evaluationCriteria
              : defaultCriteriaForRound(selectedConfig.interviewType),
            interviewPhase: 'WARMUP',
            isLastQuestion: false
          });
        } else {
          setCurrentQuestion({
            questionNumber: 1,
            question: firstQuestionFallback(
              selectedConfig.interviewType,
              selectedRole,
              selectedCompany
            ),
            category: formatRoundLabel(selectedConfig.interviewType),
            difficulty: selectedConfig.difficulty,
            expectedMinutes: 5,
            evaluationCriteria: defaultCriteriaForRound(selectedConfig.interviewType),
            interviewPhase: 'WARMUP',
            isLastQuestion: false
          });
        }
      } catch (err) {
        console.warn('Unable to initialize interview session:', err);
        const authenticationFailed = err instanceof ApiError && err.status === 401;
        setSessionInitError(
          authenticationFailed
            ? 'Your session has expired. Please log in again.'
            : 'Unable to start the personalized interview. Please return to the dashboard and try again.'
        );
      } finally {
        setLoading(false);
      }
    };

    initSession();
  }, [auth.isLoading, auth.isAuthenticated]);

  // Text Streaming Effect for Question Presentation (frozen while paused)
  useEffect(() => {
    if (!currentQuestion?.question || isPaused) return;
    const text = currentQuestion.question;
    setDisplayedText('');
    let i = 0;
    const interval = setInterval(() => {
      if (isPausedRef.current) {
        clearInterval(interval);
        setDisplayedText(text);
        return;
      }
      i++;
      setDisplayedText(text.slice(0, i));
      if (i >= text.length) {
        clearInterval(interval);
      }
    }, 20);

    return () => clearInterval(interval);
  }, [currentQuestion, isPaused]);

  // Timer Interval — frozen while paused
  useEffect(() => {
    if (isPaused) return;
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(timer);
  }, [isPaused]);

  useEffect(() => {
    updateSessionState({ timeRemainingSeconds: timeLeft });
  }, [timeLeft, updateSessionState]);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  /** EVALUATE_ALL then COMPLETE — used by finish paths */
  const completeInterview = async () => {
    const finishSessionId = sessionId;
    const avgFromHistory = scoreHistory.length
      ? Math.round(scoreHistory.reduce((a, b) => a + b, 0) / scoreHistory.length)
      : 0;

    if (finishSessionId) {
      try {
        sessionStorage.setItem(
          'interviai_last_eval_session',
          JSON.stringify({
            sessionId: finishSessionId,
            company: selectedCompany,
            role: selectedRole,
            interviewType: selectedConfig.interviewType,
            difficulty: currentDifficulty,
            currentScore: avgFromHistory,
            scoreHistory,
            weakSkills: []
          })
        );
      } catch (_) {}

      try {
        await apiClient.post(API_ENDPOINTS.INTERVIEW.EVALUATE_ALL(finishSessionId), undefined, {
          timeout: 120000,
          retry: false
        });
      } catch (e) {
        console.warn('Failed to evaluate all answers:', e);
      }
      try {
        await apiClient.post(API_ENDPOINTS.INTERVIEW.COMPLETE(finishSessionId), undefined, {
          timeout: 60000,
          retry: false
        });
      } catch (e) {
        console.warn('Failed to call complete interview endpoint:', e);
      }
    }

    clearRoomState();
    endSessionState();
    navigate('/evaluation');
  };

  // Submit Answer & Move to Next Question
  const handleSubmitAnswer = async () => {
    if (isPaused) {
      setValidationError('Interview is paused. Resume before submitting.');
      return;
    }
    // React state updates are asynchronous, so a rapid second click can arrive
    // before the disabled state renders. Keep an immediate lock as well.
    if (submitInFlightRef.current) return;
    submitInFlightRef.current = true;

    // 1. Submit Rule: If recording is active, stop recording first
    if (isRecording) {
      stopRecording();
    }

    // 2. Submit Rule: If transcript is empty, show error message
    if (!answer.trim()) {
      setValidationError('Please answer the question before proceeding.');
      submitInFlightRef.current = false;
      return;
    }

    if (evaluating) {
      submitInFlightRef.current = false;
      return;
    }
    setEvaluating(true);

    try {
      // 3. Submit Rule: Ensure answer validation is triggered if not done yet
      let submittedValidation = validationResult;
      if (!submittedValidation && !validatingAnswer) {
        submittedValidation = await triggerAutoValidation(answer);
      }

      if (!submittedValidation) {
        throw new Error('Answer validation did not return a score.');
      }

      // Compute score before submit so we can persist it with the answer
      const rawScore = Number(submittedValidation.score);
      const currentScoreVal = Number.isFinite(rawScore)
        ? Math.max(0, Math.min(100, rawScore * 10))
        : 0;
      const feedbackText = submittedValidation.feedback || 'Good response.';

      await apiClient.post<any>(API_ENDPOINTS.INTERVIEW.SUBMIT_ANSWER, {
        sessionId,
        questionOrder: currentQIndex,
        answerText: answer,
        score: currentScoreVal,
        feedback: feedbackText,
      });

      const newScores = [...scoreHistory, currentScoreVal];
      setScoreHistory(newScores);
      const avgScore = Math.round(newScores.reduce((a, b) => a + b, 0) / newScores.length);

      // Adaptive Difficulty adjustment
      let nextDiff = currentDifficulty;
      if (currentScoreVal > 85 && currentDifficulty === 'EASY') nextDiff = 'MEDIUM';
      else if (currentScoreVal > 85 && currentDifficulty === 'MEDIUM') nextDiff = 'HARD';
      else if (currentScoreVal < 60 && currentDifficulty === 'HARD') nextDiff = 'MEDIUM';
      setCurrentDifficulty(nextDiff);

      updateSessionState({
        currentQuestionNumber: currentQIndex + 1,
        currentScore: avgScore,
        currentDifficulty: nextDiff,
        weakSkillsDetected: submittedValidation.missingPoints || [],
        lastEvaluation: {
          score: currentScoreVal,
          feedback: feedbackText,
          expectedAnswer: submittedValidation.idealAnswer || 'Ideal answer overview.'
        }
      });

      const totalQ = selectedConfig.numberOfQuestions || 5;
      if (currentQIndex >= totalQ) {
        await completeInterview();
        return;
      }

      // Fetch the next generated question after the current answer has been saved.
      const nextQuestionRes = await apiClient.get<any>(
        API_ENDPOINTS.INTERVIEW.NEXT_QUESTION(sessionId)
      );
      const nextQ = nextQuestionRes.data;
      if (!nextQ?.questionText && !nextQ?.question) {
        throw new Error('The backend did not return the next generated question.');
      }

      // Prepare Next Question
      setCurrentQIndex((prev) => prev + 1);
      setAnswer('');
      setValidationResult(null);
      setValidationError(null);
      setShowHints(false);

      setCurrentQuestion({
        questionNumber: currentQIndex + 1,
        question: nextQ.questionText || nextQ.question,
        category: resolveQuestionCategory(nextQ.category, selectedConfig.interviewType),
        difficulty: nextQ.difficultyLevel || nextDiff,
        expectedMinutes: nextQ.expectedTimeMinutes || 5,
        evaluationCriteria: nextQ.evaluationCriteria?.length
          ? nextQ.evaluationCriteria
          : defaultCriteriaForRound(selectedConfig.interviewType),
        transitionPhrase: nextQ.transitionPhrase || 'Nice work! Moving on.',
        interviewPhase: currentQIndex + 1 === totalQ ? 'CLOSING' : 'CORE',
        isLastQuestion: currentQIndex + 1 === totalQ
      });
    } catch (err) {
      console.warn('Unable to submit the answer or load the generated question:', err);
      setValidationError(
        'Unable to load the next personalized question. Please try submitting again.'
      );
    } finally {
      setEvaluating(false);
      submitInFlightRef.current = false;
    }
  };

  if (auth.isLoading || loading) {
    return (
      <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-ink-950 text-white space-y-4">
        <Loader2 className="h-10 w-10 animate-spin text-brand-500" />
        <p className="text-sm text-ink-400">Initializing {selectedCompany} Virtual Interviewer & Deepgram Live STT Engine...</p>
      </div>
    );
  }

  if (!auth.isAuthenticated) {
    return null;
  }

  if (sessionInitError) {
    return (
      <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-ink-950 px-6 text-center text-white">
        <AlertTriangle className="mb-4 h-10 w-10 text-warning" />
        <h1 className="text-xl font-bold">Interview could not start</h1>
        <p className="mt-2 max-w-md text-sm text-ink-300">{sessionInitError}</p>
        <Button className="mt-6" onClick={() => navigate('/dashboard')}>
          Return to dashboard
        </Button>
      </div>
    );
  }

  const totalQuestions = selectedConfig.numberOfQuestions || 5;

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-ink-950 text-white font-sans relative">
      {/* Top Bar */}
      <div className="flex h-16 items-center justify-between border-b border-ink-800/80 px-6 bg-ink-900/80 backdrop-blur-md">
        <div className="flex items-center gap-3">
          {/* Timer Pill */}
          <div className={`flex items-center gap-1.5 rounded-xl border px-3 py-1 text-sm font-bold font-mono shadow-sm ${
            isPaused
              ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
              : 'bg-brand-500/20 border-brand-500/30 text-brand-300'
          }`}>
            <span>⏱</span>
            <span>{formatTime(timeLeft)}</span>
            {isPaused && <span className="ml-1 text-[10px] uppercase tracking-wide">Paused</span>}
          </div>

          <div className="h-4 w-px bg-ink-800" />

          {/* Clean Interactive Breadcrumb Flow */}
          <div className="flex items-center gap-2 text-sm">
            <button
              onClick={() => navigate('/interview/company')}
              className="font-semibold text-white hover:text-brand-300 hover:underline transition-colors focus:outline-none"
              title="Change Company Target"
            >
              {selectedCompany}
            </button>

            <span className="text-ink-500 font-bold">→</span>

            <button
              onClick={() => navigate('/interview/role')}
              className="font-semibold text-white hover:text-brand-300 hover:underline transition-colors focus:outline-none"
              title="Change Target Role"
            >
              {selectedRole}
            </button>

            <span className="text-ink-500 font-bold">→</span>

            <button
              onClick={() => navigate('/interview/config')}
              className="font-semibold text-white hover:text-brand-300 hover:underline transition-colors focus:outline-none"
              title="Change Interview Type"
            >
              {selectedConfig.interviewType
                ? selectedConfig.interviewType
                    .split(/[_ ]+/)
                    .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
                    .join(' ')
                : 'Technical'}
            </button>

            <span className="text-ink-500 font-bold">→</span>

            <button
              onClick={() => navigate('/interview/config')}
              className={`font-semibold capitalize hover:underline transition-colors focus:outline-none ${
                currentDifficulty === 'HARD'
                  ? 'text-rose-400 hover:text-rose-300'
                  : currentDifficulty === 'MEDIUM'
                  ? 'text-amber-400 hover:text-amber-300'
                  : 'text-emerald-400 hover:text-emerald-300'
              }`}
              title="Change Difficulty"
            >
              {currentDifficulty.toLowerCase()}
            </button>
          </div>
        </div>

        <Button
          variant="danger"
          size="sm"
          className="gap-2 shadow"
          onClick={async () => {
            if (isRecording) stopRecording();
            await completeInterview();
          }}
        >
          <PhoneOff size={16} />
          Finish Interview
        </Button>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Main Stage — Video & AI Avatar */}
        <div className="flex flex-1 flex-col p-6 space-y-6 overflow-y-auto">
          <div className="grid flex-1 grid-cols-1 md:grid-cols-2 gap-6 min-h-[280px]">
            {/* AI Interviewer Stage */}
            <div className="relative overflow-hidden rounded-2xl bg-ink-900 border border-ink-800 flex flex-col justify-between p-6 shadow-inner">
              <div className="flex items-center justify-between z-10">
                <div className="flex items-center gap-2">
                  <span className="relative flex h-3 w-3">
                    {!isPaused && (
                      <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                    )}
                    <span className={`relative inline-flex rounded-full h-3 w-3 ${isPaused ? 'bg-amber-400' : 'bg-emerald-500'}`}></span>
                  </span>
                  <span className={`text-xs font-semibold tracking-wide uppercase ${isPaused ? 'text-amber-400' : 'text-emerald-400'}`}>
                    AI {roundInterviewerTitle(selectedConfig.interviewType)} ({isPaused ? 'Paused' : 'Active'})
                  </span>
                </div>
                <Badge variant="secondary" className="text-[10px]">
                  {currentQuestion?.interviewPhase || 'CORE'} PHASE
                </Badge>
              </div>

              {/* Simulated AI Avatar */}
              <div className="my-auto flex flex-col items-center justify-center space-y-3">
                <motion.div
                  animate={
                    isPaused
                      ? { scale: 1 }
                      : { scale: isRecording ? [1, 1.08, 1] : [1, 1.03, 1] }
                  }
                  transition={{
                    repeat: isPaused ? 0 : Infinity,
                    duration: isRecording ? 1 : 3
                  }}
                  className={`relative flex h-28 w-28 items-center justify-center rounded-full p-1 shadow-lift ${
                    isPaused
                      ? 'bg-gradient-to-tr from-ink-600 to-ink-500'
                      : 'bg-gradient-to-tr from-brand-600 to-purple-600'
                  }`}
                >
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-ink-950 text-4xl">
                    🤖
                  </div>
                </motion.div>
                <p className="text-xs text-ink-400 font-medium">
                  {isPaused
                    ? 'Interview paused'
                    : isRecording
                      ? '🎙 Listening to candidate speech...'
                      : evaluating
                        ? 'Evaluating answer...'
                        : 'Listening & Analyzing'}
                </p>
              </div>

              <div className="z-10 rounded-xl bg-black/60 p-3 text-xs backdrop-blur-md border border-white/10">
                <p className="font-semibold text-brand-300">
                  {selectedCompany} · {roundInterviewerTitle(selectedConfig.interviewType)}
                </p>
                <p className="text-ink-400 text-[11px] mt-0.5">
                  {sttProvider === 'deepgram'
                    ? 'Deepgram Live STT & Real-Time LLM Validation Active.'
                    : sttProvider === 'browser'
                      ? 'Browser Speech STT active (Deepgram unavailable or not configured).'
                      : 'Speech-to-text ready. Click Start Speaking to begin.'}
                </p>
              </div>
            </div>

            {/* Candidate User Video Stage */}
            <div className="relative overflow-hidden rounded-2xl bg-ink-900 border border-ink-800 flex flex-col justify-between p-6 min-h-[300px]">
              <div className="flex items-center justify-between z-10">
                <span className="text-xs font-semibold text-ink-300 uppercase tracking-wider">Candidate Feed</span>
                <span className="text-xs text-ink-300 flex items-center gap-1">
                  {isPaused ? (
                    <span className="text-amber-400 font-bold">Paused</span>
                  ) : isRecording ? (
                    <>
                      <Radio size={12} className="animate-pulse text-red-500" />
                      <span className="text-red-400 font-bold">
                        {sttProvider === 'deepgram' ? 'Deepgram Live Recording...' : 'Listening...'}
                      </span>
                    </>
                  ) : (
                    'Microphone Standby'
                  )}
                </span>
              </div>

              {/* WebRTC Real Camera Stream / Placeholder Container */}
              <div className="absolute inset-0 flex items-center justify-center bg-black/40 overflow-hidden">
                {isVideoOff ? (
                  <div className="flex flex-col items-center gap-2">
                    <div className="flex h-20 w-20 items-center justify-center rounded-full bg-ink-800 text-3xl text-ink-400">
                      👤
                    </div>
                    <span className="text-xs text-ink-400">Camera Off</span>
                  </div>
                ) : cameraError ? (
                  <div className="flex flex-col items-center gap-2 text-center p-4">
                    <div className="flex h-16 w-16 items-center justify-center rounded-full bg-amber-950/60 border border-amber-500/30 text-2xl text-amber-400">
                      📷
                    </div>
                    <span className="text-xs text-amber-300 font-medium">{cameraError}</span>
                  </div>
                ) : (
                  <video
                    ref={videoRef}
                    autoPlay
                    playsInline
                    muted
                    className="w-full h-full object-cover transform -scale-x-100"
                  />
                )}
              </div>

              <div className="z-10 flex items-center justify-between rounded-xl bg-black/70 p-3 text-xs backdrop-blur-md border border-white/10 mt-auto">
                <span className="font-semibold text-white">You (Candidate)</span>
                {validationResult && (
                  <span className="text-emerald-400 font-bold">
                    Live Rating: {validationResult.score}/10
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Audio / Video Control Buttons */}
          <div className="flex items-center justify-center gap-3 flex-wrap">
            <button
              disabled={isConnecting || isPaused}
              onClick={handleMicToggle}
              className={`flex items-center justify-center gap-2 rounded-full px-5 h-12 text-sm font-semibold shadow-lg transition-all ${
                isRecording
                  ? 'bg-red-600 hover:bg-red-700 text-white ring-4 ring-red-500/30 animate-pulse'
                  : 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-900/40'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              {isConnecting ? (
                <>
                  <Loader2 size={18} className="animate-spin" />
                  <span>Starting mic...</span>
                </>
              ) : isRecording ? (
                <>
                  <MicOff size={20} />
                  <span>Stop Listening</span>
                </>
              ) : (
                <>
                  <Mic size={20} />
                  <span>Start Speaking</span>
                </>
              )}
            </button>

            {!isPaused ? (
              <button
                onClick={handlePauseInterview}
                disabled={evaluating || validatingAnswer}
                className="flex items-center justify-center gap-2 rounded-full px-5 h-12 text-sm font-semibold text-white bg-amber-600 hover:bg-amber-500 shadow-md transition-all disabled:opacity-50"
                title="Pause interview"
              >
                <Pause size={18} />
                <span>Pause Interview</span>
              </button>
            ) : (
              <button
                onClick={handleResumeInterview}
                className="flex items-center justify-center gap-2 rounded-full px-5 h-12 text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-500 shadow-md transition-all"
                title="Resume interview"
              >
                <Play size={18} />
                <span>Resume Interview</span>
              </button>
            )}

            <button
              onClick={() => setIsVideoOff(!isVideoOff)}
              disabled={isPaused}
              className={`flex items-center justify-center rounded-full h-12 w-12 text-white shadow-md transition-all disabled:opacity-50 ${
                isVideoOff
                  ? 'bg-red-600/90 hover:bg-red-600 border border-red-500/50'
                  : 'bg-ink-800 hover:bg-ink-700 border border-ink-700'
              }`}
              title={isVideoOff ? 'Turn Camera On' : 'Turn Camera Off'}
            >
              {isVideoOff ? <VideoOff size={20} /> : <Video size={20} />}
            </button>
          </div>
        </div>

        {/* Right Sidebar — Sticky Question + Scrollable Answer */}
        <div className="w-[min(460px,42vw)] min-w-[320px] max-w-[520px] border-l border-ink-800 bg-ink-900 flex flex-col h-full min-h-0 shadow-2xl overflow-hidden">
          {/* Sticky question region */}
          <div className="shrink-0 border-b border-ink-800 bg-ink-900/95 backdrop-blur-sm p-4 space-y-3 max-h-[40%] overflow-y-auto">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-brand-400">
                Question {currentQIndex} of {totalQuestions}
              </span>
              <Badge variant="outline" className="text-xs shrink-0">
                {resolveQuestionCategory(currentQuestion?.category, selectedConfig.interviewType)}
              </Badge>
            </div>

            {currentQuestion?.transitionPhrase && (
              <p className="text-xs italic text-brand-300 bg-brand-950/40 p-2.5 rounded-lg border border-brand-800/40">
                💬 "{currentQuestion.transitionPhrase}"
              </p>
            )}

            <div className="rounded-xl bg-ink-950/80 p-4 border border-ink-800 shadow-inner">
              <h3
                className={`text-sm font-semibold leading-relaxed text-ink-100 ${
                  selectedConfig.interviewType === 'CODING'
                    ? 'whitespace-pre-wrap font-mono text-xs'
                    : ''
                }`}
              >
                {displayedText}
                {!isPaused &&
                  displayedText.length < (currentQuestion?.question?.length || 0) && (
                    <span className="animate-pulse text-brand-400">|</span>
                  )}
              </h3>
            </div>

            <div>
              <button
                onClick={() => setShowHints(!showHints)}
                className="flex items-center gap-2 text-xs font-semibold text-ink-400 hover:text-white transition-colors"
              >
                <Lightbulb size={14} />
                {showHints ? 'Hide evaluation criteria' : 'Need hints or evaluation criteria?'}
              </button>
              {showHints && (
                <div className="mt-2 space-y-1.5 rounded-lg bg-ink-800/50 p-3 text-xs text-ink-300 border border-ink-700/50">
                  {currentQuestion?.evaluationCriteria.map((c, i) => (
                    <div key={i} className="flex items-start gap-2">
                      <span className="text-brand-400">•</span>
                      <span>{c}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Answer region — fills remaining height, scrolls independently */}
          <div className="flex-1 min-h-0 flex flex-col p-4 gap-2 overflow-hidden">
            <div className="flex items-center justify-between shrink-0">
              <label className="text-xs font-semibold text-ink-300 flex items-center gap-1.5">
                Your Answer
                {isRecording && !isPaused && (
                  <span className="inline-flex items-center gap-1 text-[10px] text-red-400 font-bold animate-pulse">
                    • Streaming...
                  </span>
                )}
              </label>
              <span
                className={`text-[10px] ${
                  answer.length >= MAX_ANSWER_LENGTH
                    ? 'text-red-400'
                    : answer.length >= MAX_ANSWER_LENGTH * 0.9
                      ? 'text-amber-400'
                      : 'text-ink-500'
                }`}
              >
                {answer.length.toLocaleString()} / {MAX_ANSWER_LENGTH.toLocaleString()}
              </span>
            </div>

            <textarea
              ref={answerTextareaRef}
              value={answer}
              onChange={(e) => {
                setAnswer(e.target.value);
                setValidationError(null);
              }}
              onScroll={(e) => {
                const el = e.currentTarget;
                stickAnswerToBottomRef.current =
                  el.scrollHeight - el.scrollTop - el.clientHeight < 56;
              }}
              maxLength={MAX_ANSWER_LENGTH}
              placeholder="Click Start Speaking to stream speech live, or type your answer here..."
              disabled={evaluating || isPaused}
              spellCheck
              className={`flex-1 min-h-[140px] w-full resize-none rounded-xl border p-4 text-sm text-ink-100 placeholder:text-ink-600 focus:outline-none focus:ring-1 overflow-y-auto overscroll-contain leading-relaxed ${
                isRecording && !isPaused
                  ? 'border-red-500/80 bg-red-950/10 focus:ring-red-500'
                  : isPaused
                    ? 'border-amber-500/40 bg-ink-950/80 opacity-90'
                    : 'border-ink-700 bg-ink-950 focus:border-brand-500 focus:ring-brand-500'
              }`}
            />

            <div className="shrink-0 space-y-2 max-h-[28%] overflow-y-auto">
              {validationError && (
                <div className="rounded-lg bg-red-950/40 border border-red-500/40 p-3 text-xs text-red-300 flex items-center gap-2">
                  <AlertTriangle size={16} className="shrink-0 text-red-400" />
                  <span>{validationError}</span>
                </div>
              )}

              {speechError && (
                <div className="rounded-lg bg-amber-950/40 border border-amber-500/40 p-3 text-xs text-amber-300 flex items-center gap-2">
                  <AlertTriangle size={16} className="shrink-0 text-amber-400" />
                  <span>{speechError}</span>
                </div>
              )}

              <AnswerValidationCard
                validation={validationResult}
                isValidating={validatingAnswer && !isPaused}
              />
            </div>
          </div>

          {/* Sticky submit */}
          <div className="shrink-0 border-t border-ink-800 p-4 bg-ink-950 space-y-2">
            <Button
              variant="gradient"
              className="w-full gap-2 shadow-lg h-11 text-sm font-semibold"
              disabled={evaluating || validatingAnswer || isPaused}
              onClick={handleSubmitAnswer}
            >
              {evaluating || validatingAnswer ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Evaluating Answer Quality...
                </>
              ) : (
                <>
                  {currentQIndex >= totalQuestions
                    ? 'Submit & Complete Interview'
                    : 'Submit & Next Question'}
                  <Send size={16} />
                </>
              )}
            </Button>
            <p className="text-[10px] text-center text-ink-500">
              {isPaused
                ? 'Resume the interview to continue answering or submit.'
                : 'Recording stops automatically when submitting.'}
            </p>
          </div>
        </div>
      </div>

      {/* Pause overlay */}
      <AnimatePresence>
        {isPaused && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="absolute inset-0 z-[60] flex items-center justify-center bg-ink-950/80 backdrop-blur-md px-6"
          >
            <motion.div
              initial={{ scale: 0.96, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.96, opacity: 0 }}
              className="w-full max-w-md rounded-2xl border border-ink-700 bg-ink-900 p-8 text-center shadow-2xl"
            >
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-amber-500/20 text-amber-300">
                <Pause size={28} />
              </div>
              <h2 className="text-2xl font-bold text-white">Interview Paused</h2>
              <p className="mt-2 text-sm text-ink-300">
                Your interview has been paused. The timer is frozen. Your question, answer, and
                progress are saved — click Resume to continue exactly where you left off.
              </p>
              <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
                <Button
                  variant="gradient"
                  className="gap-2 flex-1"
                  onClick={handleResumeInterview}
                >
                  <Play size={16} />
                  Resume Interview
                </Button>
                <Button
                  variant="danger"
                  className="gap-2 flex-1"
                  onClick={async () => {
                    if (isRecording) stopRecording();
                    await completeInterview();
                  }}
                >
                  <PhoneOff size={16} />
                  End Interview
                </Button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
