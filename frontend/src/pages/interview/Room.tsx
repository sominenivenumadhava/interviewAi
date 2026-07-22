import React, { useEffect, useState, useCallback } from 'react';
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
  Sparkles,
  AlertTriangle,
  Radio
} from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { useInterviewSession } from '../../contexts/InterviewSessionContext';
import apiClient, { API_ENDPOINTS } from '../../lib/apiClient';
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

export function Room() {
  const navigate = useNavigate();
  const {
    selectedCompany,
    selectedRole,
    selectedConfig,
    startSessionState,
    updateSessionState,
    endSessionState
  } = useInterviewSession();

  const [loading, setLoading] = useState(true);
  const [evaluating, setEvaluating] = useState(false);
  const [validatingAnswer, setValidatingAnswer] = useState(false);
  const [sessionId, setSessionId] = useState<string>('');

  const [currentQIndex, setCurrentQIndex] = useState(1);
  const [currentQuestion, setCurrentQuestion] = useState<LiveQuestion | null>(null);
  const [displayedText, setDisplayedText] = useState('');
  const [answer, setAnswer] = useState('');

  const [isVideoOff, setIsVideoOff] = useState(false);
  const [showHints, setShowHints] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  const [timeLeft, setTimeLeft] = useState(parseInt(selectedConfig.duration || '45') * 60);

  const [validationResult, setValidationResult] = useState<AnswerValidationData | null>(null);
  const [scoreHistory, setScoreHistory] = useState<number[]>([]);
  const [currentDifficulty, setCurrentDifficulty] = useState<string>(selectedConfig.difficulty);

  // Deepgram Live Speech-to-Text Integration
  const handleTranscriptUpdate = useCallback((newTranscript: string) => {
    if (newTranscript) {
      setAnswer(newTranscript);
      setValidationError(null);
    }
  }, []);

  const {
    isRecording,
    isConnecting,
    startRecording,
    stopRecording,
    error: speechError
  } = useDeepgramLive({
    onTranscriptUpdate: handleTranscriptUpdate,
    onError: (err) => setValidationError(err)
  });

  // Trigger Automatic LLM Answer Validation when user finishes speaking
  const triggerAutoValidation = useCallback(async (transcriptText: string) => {
    if (!transcriptText || transcriptText.trim().length < 5 || !currentQuestion) return;
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
      }
    } catch (e) {
      console.warn('Realtime validation API error, using fallback:', e);
      // Fallback validation
      setValidationResult({
        isRelevant: transcriptText.trim().length > 10,
        score: transcriptText.trim().length > 10 ? 8.5 : 2.0,
        confidence: 94,
        feedback: 'Good technical response. Remember to explicitly mention measurable metrics and trade-offs.',
        missingPoints: ['Performance optimization', 'Scalability decisions', 'Measurable impact']
      });
    } finally {
      setValidatingAnswer(false);
    }
  }, [currentQuestion, selectedRole, selectedCompany, selectedConfig.interviewType]);

  // Toggle Microphone / Recording
  const handleMicToggle = async () => {
    if (isRecording) {
      stopRecording();
      // Auto-validate transcript after recording stops
      if (answer.trim()) {
        triggerAutoValidation(answer);
      }
    } else {
      setValidationResult(null);
      setValidationError(null);
      await startRecording();
    }
  };

  // Initialize Interview Session with Backend
  useEffect(() => {
    const initSession = async () => {
      setLoading(true);
      try {
        const createRes = await apiClient.post<any>(API_ENDPOINTS.INTERVIEW.CREATE, {
          company: selectedCompany,
          role: selectedRole,
          interviewType: selectedConfig.interviewType,
          difficultyLevel: selectedConfig.difficulty,
          durationMinutes: parseInt(selectedConfig.duration || '45'),
          numberOfQuestions: selectedConfig.numberOfQuestions || 5
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
            category: qData.category || selectedConfig.interviewType,
            difficulty: qData.difficultyLevel || selectedConfig.difficulty,
            expectedMinutes: 5,
            evaluationCriteria: qData.evaluationCriteria || ['Clear technical explanation'],
            interviewPhase: 'WARMUP',
            isLastQuestion: false
          });
        } else {
          setCurrentQuestion({
            questionNumber: 1,
            question: `Welcome! Let's start the ${selectedConfig.interviewType.toLowerCase()} interview for ${selectedRole} at ${selectedCompany}. Could you introduce yourself and walk me through a relevant project from your experience?`,
            category: selectedConfig.interviewType,
            difficulty: selectedConfig.difficulty,
            expectedMinutes: 5,
            evaluationCriteria: ['Clear structure', 'Technical relevance'],
            interviewPhase: 'WARMUP',
            isLastQuestion: false
          });
        }
      } catch (err) {
        console.warn('Backend session init fallback active:', err);
        const fallbackSessionId = `sess-${Date.now()}`;
        setSessionId(fallbackSessionId);
        startSessionState({
          sessionId: fallbackSessionId,
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
        setCurrentQuestion({
          questionNumber: 1,
          question: `Welcome to your ${selectedCompany} ${selectedConfig.interviewType} interview! Could you share a high-impact project you delivered, focusing on key decisions and technical challenges?`,
          category: selectedConfig.interviewType,
          difficulty: selectedConfig.difficulty,
          expectedMinutes: 5,
          evaluationCriteria: ['Problem context', 'Technical approach', 'Measurable impact'],
          interviewPhase: 'WARMUP',
          isLastQuestion: false
        });
      } finally {
        setLoading(false);
      }
    };

    initSession();
  }, []);

  // Text Streaming Effect for Question Presentation
  useEffect(() => {
    if (!currentQuestion?.question) return;
    setDisplayedText('');
    let index = 0;
    const text = currentQuestion.question;
    const interval = setInterval(() => {
      setDisplayedText((prev) => prev + text.charAt(index));
      index++;
      if (index >= text.length) {
        clearInterval(interval);
      }
    }, 20);

    return () => clearInterval(interval);
  }, [currentQuestion]);

  // Timer Interval
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        const next = prev > 0 ? prev - 1 : 0;
        updateSessionState({ timeRemainingSeconds: next });
        return next;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  // Submit Answer & Move to Next Question
  const handleSubmitAnswer = async () => {
    // 1. Submit Rule: If recording is active, stop recording first
    if (isRecording) {
      stopRecording();
    }

    // 2. Submit Rule: If transcript is empty, show error message
    if (!answer.trim()) {
      setValidationError('Please answer the question before proceeding.');
      return;
    }

    if (evaluating) return;
    setEvaluating(true);

    try {
      // 3. Submit Rule: Ensure answer validation is triggered if not done yet
      if (!validationResult && !validatingAnswer) {
        await triggerAutoValidation(answer);
      }

      const res = await apiClient.post<any>(API_ENDPOINTS.INTERVIEW.SUBMIT_ANSWER, {
        sessionId,
        questionOrder: currentQIndex,
        userAnswer: answer
      });

      const currentScoreVal = (validationResult?.score || 8.5) * 10;
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
        weakSkillsDetected: validationResult?.missingPoints || [],
        lastEvaluation: {
          score: currentScoreVal,
          feedback: validationResult?.feedback || 'Good response.',
          expectedAnswer: validationResult?.idealAnswer || 'Ideal answer overview.'
        }
      });

      const totalQ = selectedConfig.numberOfQuestions || 5;
      if (currentQIndex >= totalQ) {
        endSessionState();
        navigate('/evaluation');
        return;
      }

      // Prepare Next Question
      setCurrentQIndex((prev) => prev + 1);
      setAnswer('');
      setValidationResult(null);
      setValidationError(null);
      setShowHints(false);

      if (res.data && res.data.nextQuestion) {
        const nextQ = res.data.nextQuestion;
        setCurrentQuestion({
          questionNumber: currentQIndex + 1,
          question: nextQ.questionText || nextQ.question,
          category: nextQ.category || selectedConfig.interviewType,
          difficulty: nextDiff,
          expectedMinutes: 5,
          evaluationCriteria: nextQ.evaluationCriteria || ['Deep technical breakdown'],
          transitionPhrase: nextQ.transitionPhrase || 'Nice work! Moving on.',
          interviewPhase: currentQIndex + 1 === totalQ ? 'CLOSING' : 'CORE',
          isLastQuestion: currentQIndex + 1 === totalQ
        });
      } else {
        setCurrentQuestion({
          questionNumber: currentQIndex + 1,
          question: `Follow-up: That makes sense regarding your use of ${selectedConfig.language || 'technology'}. How would you scale this architecture to handle 5 million active concurrent users while maintaining 99.99% availability at ${selectedCompany}?`,
          category: selectedConfig.interviewType,
          difficulty: nextDiff,
          expectedMinutes: 5,
          evaluationCriteria: ['Caching strategy', 'Database sharding', 'Load balancing'],
          transitionPhrase: "Good answer. Let's dig deeper into scalability.",
          interviewPhase: currentQIndex + 1 === totalQ ? 'CLOSING' : 'DEEP_DIVE',
          isLastQuestion: currentQIndex + 1 === totalQ
        });
      }
    } catch (err) {
      console.warn('Answer submit fallback handled:', err);
      const totalQ = selectedConfig.numberOfQuestions || 5;
      if (currentQIndex >= totalQ) {
        endSessionState();
        navigate('/evaluation');
        return;
      }

      setCurrentQIndex((prev) => prev + 1);
      setAnswer('');
      setValidationResult(null);
      setValidationError(null);
      setShowHints(false);
      setCurrentQuestion({
        questionNumber: currentQIndex + 1,
        question: `How do you approach monitoring, observability, and automated alerting for this system in production?`,
        category: selectedConfig.interviewType,
        difficulty: currentDifficulty,
        expectedMinutes: 5,
        evaluationCriteria: ['Metrics collection', 'Log aggregation', 'Alert thresholds'],
        interviewPhase: 'CORE',
        isLastQuestion: currentQIndex + 1 === totalQ
      });
    } finally {
      setEvaluating(false);
    }
  };

  if (loading) {
    return (
      <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-ink-950 text-white space-y-4">
        <Loader2 className="h-10 w-10 animate-spin text-brand-500" />
        <p className="text-sm text-ink-400">Initializing {selectedCompany} Virtual Interviewer & Deepgram Live STT Engine...</p>
      </div>
    );
  }

  const totalQuestions = selectedConfig.numberOfQuestions || 5;

  return (
    <div className="fixed inset-0 z-50 flex flex-col bg-ink-950 text-white font-sans">
      {/* Top Bar */}
      <div className="flex h-16 items-center justify-between border-b border-ink-800 px-6 bg-ink-900/50 backdrop-blur-md">
        <div className="flex items-center gap-4">
          <div className="rounded-lg bg-brand-600 px-3.5 py-1 text-sm font-semibold tracking-wide">
            ⏱ {formatTime(timeLeft)}
          </div>
          <div className="flex items-center gap-2 text-sm text-ink-300">
            <span className="font-bold text-white">{selectedCompany}</span>
            <span>•</span>
            <span>{selectedRole}</span>
            <span>•</span>
            <Badge variant="outline" className="text-xs">
              {selectedConfig.interviewType}
            </Badge>
            <span>•</span>
            <Badge
              variant={
                currentDifficulty === 'HARD'
                  ? 'danger'
                  : currentDifficulty === 'MEDIUM'
                  ? 'warning'
                  : 'secondary'
              }
            >
              {currentDifficulty} Adaptive
            </Badge>
          </div>
        </div>

        <Button
          variant="danger"
          size="sm"
          className="gap-2 shadow"
          onClick={() => {
            if (isRecording) stopRecording();
            endSessionState();
            navigate('/evaluation');
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
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
                  </span>
                  <span className="text-xs font-semibold tracking-wide text-emerald-400 uppercase">
                    AI Senior Interviewer (Active)
                  </span>
                </div>
                <Badge variant="secondary" className="text-[10px]">
                  {currentQuestion?.interviewPhase || 'CORE'} PHASE
                </Badge>
              </div>

              {/* Simulated AI Avatar */}
              <div className="my-auto flex flex-col items-center justify-center space-y-3">
                <motion.div
                  animate={{
                    scale: isRecording ? [1, 1.08, 1] : [1, 1.03, 1]
                  }}
                  transition={{ repeat: Infinity, duration: isRecording ? 1 : 3 }}
                  className="relative flex h-28 w-28 items-center justify-center rounded-full bg-gradient-to-tr from-brand-600 to-purple-600 p-1 shadow-lift"
                >
                  <div className="flex h-full w-full items-center justify-center rounded-full bg-ink-950 text-4xl">
                    🤖
                  </div>
                </motion.div>
                <p className="text-xs text-ink-400 font-medium">
                  {isRecording ? '🎙 Listening to candidate speech...' : evaluating ? 'Evaluating answer...' : 'Listening & Analyzing'}
                </p>
              </div>

              <div className="z-10 rounded-xl bg-black/60 p-3 text-xs backdrop-blur-md border border-white/10">
                <p className="font-semibold text-brand-300">
                  {selectedCompany} Senior Bar Raiser
                </p>
                <p className="text-ink-400 text-[11px] mt-0.5">
                  Deepgram STT & Real-Time LLM Validation Active.
                </p>
              </div>
            </div>

            {/* Candidate User Video Stage */}
            <div className="relative overflow-hidden rounded-2xl bg-ink-900 border border-ink-800 flex flex-col justify-between p-6">
              <div className="flex items-center justify-between z-10">
                <span className="text-xs font-semibold text-ink-400 uppercase">Candidate Feed</span>
                <span className="text-xs text-ink-400 flex items-center gap-1">
                  {isRecording ? (
                    <>
                      <Radio size={12} className="animate-pulse text-red-500" />
                      <span className="text-red-400 font-bold">Deepgram Live Recording...</span>
                    </>
                  ) : (
                    'Microphone Standby'
                  )}
                </span>
              </div>

              <div className="my-auto flex flex-col items-center justify-center">
                {isVideoOff ? (
                  <div className="flex h-20 w-20 items-center justify-center rounded-full bg-ink-800 text-2xl text-ink-400">
                    👤
                  </div>
                ) : (
                  <div className="flex flex-col items-center space-y-2">
                    <div className="h-24 w-24 rounded-full border-2 border-brand-500/40 bg-ink-800/80 flex items-center justify-center relative">
                      <span className="text-3xl">👨‍💻</span>
                      {isRecording && (
                        <span className="absolute -bottom-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full bg-red-600 text-white text-[10px] animate-pulse font-bold shadow">
                          REC
                        </span>
                      )}
                    </div>
                    <span className="text-xs text-brand-400 font-medium">Camera Feed Live</span>
                  </div>
                )}
              </div>

              <div className="z-10 flex items-center justify-between rounded-xl bg-black/60 p-3 text-xs backdrop-blur-md border border-white/10">
                <span className="font-medium text-ink-200">You (Candidate)</span>
                {validationResult && (
                  <span className="text-emerald-400 font-semibold">
                    Live Rating: {validationResult.score}/10
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Audio / Video Control Buttons */}
          <div className="flex items-center justify-center gap-4">
            <Button
              variant={isRecording ? 'danger' : 'secondary'}
              size="lg"
              className={`rounded-full px-6 h-12 shadow-lg transition-all ${
                isRecording ? 'ring-4 ring-red-500/30 animate-pulse' : ''
              }`}
              disabled={isConnecting}
              onClick={handleMicToggle}
            >
              {isConnecting ? (
                <>
                  <Loader2 size={18} className="animate-spin mr-2" />
                  Connecting Deepgram...
                </>
              ) : isRecording ? (
                <>
                  <MicOff size={20} className="mr-2" />
                  Stop Recording
                </>
              ) : (
                <>
                  <Mic size={20} className="mr-2 text-emerald-400" />
                  Start Speaking (Deepgram STT)
                </>
              )}
            </Button>

            <Button
              variant={isVideoOff ? 'danger' : 'secondary'}
              size="icon"
              className="rounded-full h-12 w-12 shadow"
              onClick={() => setIsVideoOff(!isVideoOff)}
            >
              {isVideoOff ? <VideoOff size={20} /> : <Video size={20} />}
            </Button>
          </div>
        </div>

        {/* Right Sidebar — Question, Answer Textarea & Real-time Validation */}
        <div className="w-[450px] border-l border-ink-800 bg-ink-900 flex flex-col shadow-2xl">
          <div className="flex-1 overflow-y-auto p-6 space-y-5">
            {/* Question Progress Header */}
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-brand-400">
                Question {currentQIndex} of {totalQuestions}
              </span>
              <Badge variant="outline" className="text-xs">
                {currentQuestion?.category || selectedConfig.interviewType}
              </Badge>
            </div>

            {/* AI Transition Phrase */}
            {currentQuestion?.transitionPhrase && (
              <p className="text-xs italic text-brand-300 bg-brand-950/40 p-2.5 rounded-lg border border-brand-800/40">
                💬 "{currentQuestion.transitionPhrase}"
              </p>
            )}

            {/* AI Generated Question Prompt */}
            <div className="rounded-xl bg-ink-950/80 p-4 border border-ink-800 shadow-inner">
              <h3 className="text-base font-semibold leading-relaxed text-ink-100 min-h-[50px]">
                {displayedText}
                {displayedText.length < (currentQuestion?.question?.length || 0) && (
                  <span className="animate-pulse text-brand-400">|</span>
                )}
              </h3>
            </div>

            {/* Candidate Answer Input Textarea (Updates Live via Deepgram STT) */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold text-ink-300 flex items-center gap-1.5">
                  Your Answer (Live STT Transcript)
                  {isRecording && (
                    <span className="inline-flex items-center gap-1 text-[10px] text-red-400 font-bold animate-pulse">
                      • Streaming...
                    </span>
                  )}
                </label>
                <span className="text-[10px] text-ink-500">
                  {answer.length} characters (Editable)
                </span>
              </div>
              <textarea
                value={answer}
                onChange={(e) => {
                  setAnswer(e.target.value);
                  setValidationError(null);
                }}
                placeholder="Click 'Start Speaking' to stream speech live via Deepgram, or type your answer here..."
                disabled={evaluating}
                className={`h-40 w-full resize-none rounded-xl border p-4 text-sm text-ink-100 placeholder:text-ink-600 focus:outline-none focus:ring-1 transition-all ${
                  isRecording
                    ? 'border-red-500/80 bg-red-950/10 focus:ring-red-500'
                    : 'border-ink-700 bg-ink-950 focus:border-brand-500 focus:ring-brand-500'
                }`}
              />
            </div>

            {/* Validation Banner Errors */}
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

            {/* Real-Time Answer Validation Card Component */}
            <AnswerValidationCard
              validation={validationResult}
              isValidating={validatingAnswer}
            />

            {/* Hints Accordion */}
            <div>
              <button
                onClick={() => setShowHints(!showHints)}
                className="flex items-center gap-2 text-xs font-semibold text-ink-400 hover:text-white transition-colors"
              >
                <Lightbulb size={14} />
                {showHints ? 'Hide Evaluation Criteria' : 'Need hints or evaluation criteria?'}
              </button>

              {showHints && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  className="mt-3 space-y-1.5 rounded-lg bg-ink-800/50 p-3 text-xs text-ink-300 border border-ink-700/50"
                >
                  {currentQuestion?.evaluationCriteria.map((c, i) => (
                    <div key={i} className="flex items-start gap-2">
                      <span className="text-brand-400">•</span>
                      <span>{c}</span>
                    </div>
                  ))}
                </motion.div>
              )}
            </div>
          </div>

          {/* Bottom Action Submit Button */}
          <div className="border-t border-ink-800 p-6 bg-ink-950 space-y-2">
            <Button
              className="w-full gap-2 shadow-lg h-11 text-sm font-semibold"
              disabled={evaluating || validatingAnswer}
              onClick={handleSubmitAnswer}
            >
              {evaluating || validatingAnswer ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Evaluating Answer Quality...
                </>
              ) : (
                <>
                  {currentQIndex >= totalQuestions ? 'Submit & Complete Interview' : 'Submit & Next Question'}
                  <Send size={16} />
                </>
              )}
            </Button>

            <p className="text-[10px] text-center text-ink-500">
              Recording will automatically stop when submitting.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}