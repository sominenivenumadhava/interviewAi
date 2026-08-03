import { useState, useRef, useCallback, useEffect } from 'react';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

export interface UseDeepgramLiveOptions {
  onTranscriptUpdate?: (transcript: string, isFinal: boolean) => void;
  onError?: (error: string) => void;
}

type SpeechTokenPayload = {
  key?: string | null;
  url?: string | null;
  available?: boolean;
  provider?: string;
  authScheme?: string | null;
  message?: string | null;
  expiresAt?: number | null;
};

function unwrapSpeechToken(tokenRes: any): SpeechTokenPayload {
  // apiClient returns the JSON body. Backend wraps as ApiResponse { data: {...} }.
  const body = tokenRes?.data?.available !== undefined || tokenRes?.data?.key || tokenRes?.data?.provider
    ? tokenRes.data
    : tokenRes?.data?.data ?? tokenRes?.data ?? tokenRes;
  return (body ?? {}) as SpeechTokenPayload;
}

export function useDeepgramLive(options: UseDeepgramLiveOptions = {}) {
  const [isRecording, setIsRecording] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [provider, setProvider] = useState<'deepgram' | 'browser' | null>(null);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const webSocketRef = useRef<WebSocket | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const finalTranscriptsRef = useRef<string[]>([]);
  const currentInterimRef = useRef<string>('');
  const speechRecognitionRef = useRef<any>(null);
  const wantListeningRef = useRef(false);
  const isRecordingRef = useRef(false);
  const handedOffToBrowserRef = useRef(false);
  const optionsRef = useRef(options);
  optionsRef.current = options;

  const setRecordingState = useCallback((recording: boolean) => {
    isRecordingRef.current = recording;
    setIsRecording(recording);
  }, []);

  const buildFullText = () =>
    [...finalTranscriptsRef.current, currentInterimRef.current].filter(Boolean).join(' ').trim();

  const stopRecording = useCallback((): string => {
    wantListeningRef.current = false;
    handedOffToBrowserRef.current = false;

    try {
      if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
        mediaRecorderRef.current.stop();
      }
    } catch (e) {
      console.warn('Error stopping media recorder:', e);
    }
    mediaRecorderRef.current = null;

    if (speechRecognitionRef.current) {
      try {
        speechRecognitionRef.current.onresult = null;
        speechRecognitionRef.current.onerror = null;
        speechRecognitionRef.current.onend = null;
        speechRecognitionRef.current.abort();
      } catch (e) {
        console.warn('Error stopping speech recognition:', e);
      }
      speechRecognitionRef.current = null;
    }

    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (webSocketRef.current) {
      try {
        if (webSocketRef.current.readyState === WebSocket.OPEN) {
          webSocketRef.current.send(JSON.stringify({ type: 'CloseStream' }));
        }
        webSocketRef.current.close();
      } catch (e) {
        console.warn('Error closing websocket:', e);
      }
      webSocketRef.current = null;
    }

    setRecordingState(false);
    setIsConnecting(false);

    const fullText = buildFullText();
    optionsRef.current.onTranscriptUpdate?.(fullText, true);
    return fullText;
  }, [setRecordingState]);

  const startBrowserWebSpeech = useCallback((seedTranscript = '') => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      const msg = 'Live Speech Recognition is not supported on this browser. Try Chrome.';
      setError(msg);
      setIsConnecting(false);
      setRecordingState(false);
      wantListeningRef.current = false;
      optionsRef.current.onError?.(msg);
      return;
    }

    try {
      if (seedTranscript.trim()) {
        finalTranscriptsRef.current = [seedTranscript.trim()];
        currentInterimRef.current = '';
      }

      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'en-US';
      speechRecognitionRef.current = recognition;
      wantListeningRef.current = true;
      setProvider('browser');

      recognition.onstart = () => {
        setIsConnecting(false);
        setRecordingState(true);
      };

      recognition.onresult = (event: any) => {
        if (!speechRecognitionRef.current || !wantListeningRef.current) return;

        let interim = '';
        let finalChunk = '';

        for (let i = event.resultIndex; i < event.results.length; ++i) {
          if (event.results[i].isFinal) {
            finalChunk += event.results[i][0].transcript + ' ';
          } else {
            interim += event.results[i][0].transcript;
          }
        }

        if (finalChunk) finalTranscriptsRef.current.push(finalChunk.trim());
        currentInterimRef.current = interim.trim();

        const combined = buildFullText();
        setTranscript(combined);
        optionsRef.current.onTranscriptUpdate?.(combined, false);
      };

      recognition.onerror = (e: any) => {
        const code = e?.error;
        if (code === 'aborted' || code === 'no-speech') return;

        if (code === 'not-allowed') {
          wantListeningRef.current = false;
          const msg = 'Microphone permission denied. Please allow microphone access.';
          setError(msg);
          setRecordingState(false);
          setIsConnecting(false);
          speechRecognitionRef.current = null;
          optionsRef.current.onError?.(msg);
          return;
        }

        console.warn('WebSpeech error:', e);
      };

      recognition.onend = () => {
        if (!wantListeningRef.current) {
          speechRecognitionRef.current = null;
          return;
        }
        try {
          recognition.start();
        } catch {
          speechRecognitionRef.current = null;
          wantListeningRef.current = false;
          setRecordingState(false);
          setIsConnecting(false);
          optionsRef.current.onTranscriptUpdate?.(buildFullText(), true);
        }
      };

      recognition.start();
    } catch (e) {
      console.warn('WebSpeech start error:', e);
      wantListeningRef.current = false;
      setIsConnecting(false);
      setRecordingState(false);
      const msg = 'Failed to start speech recognition.';
      setError(msg);
      optionsRef.current.onError?.(msg);
    }
  }, [setRecordingState]);

  const startDeepgramSocket = useCallback((
    stream: MediaStream,
    tokenKey: string,
    wsUrl: string,
    authScheme: string,
    seedTranscript: string
  ) => {
    handedOffToBrowserRef.current = false;
    setProvider('deepgram');

    let socket: WebSocket;
    try {
      // Browser WebSockets cannot set Authorization headers — Deepgram accepts
      // the scheme via Sec-WebSocket-Protocol: ["bearer"|"token", <credential>]
      socket = new WebSocket(wsUrl, [authScheme, tokenKey]);
    } catch {
      // Fallback: put credential in query string (works for some Deepgram setups)
      const sep = wsUrl.includes('?') ? '&' : '?';
      const param = authScheme === 'bearer' ? `authorization=Bearer%20${encodeURIComponent(tokenKey)}` : `token=${encodeURIComponent(tokenKey)}`;
      socket = new WebSocket(`${wsUrl}${sep}${param}`);
    }

    webSocketRef.current = socket;

    const connectTimeout = window.setTimeout(() => {
      if (!isRecordingRef.current && wantListeningRef.current && !handedOffToBrowserRef.current) {
        console.warn('Deepgram connect timeout — falling back to browser STT');
        handedOffToBrowserRef.current = true;
        try { socket.close(); } catch { /* ignore */ }
        webSocketRef.current = null;
        stream.getTracks().forEach((t) => t.stop());
        streamRef.current = null;
        startBrowserWebSpeech(seedTranscript);
      }
    }, 8000);

    socket.onopen = () => {
      window.clearTimeout(connectTimeout);
      setIsConnecting(false);
      setRecordingState(true);

      const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : MediaRecorder.isTypeSupported('audio/webm')
          ? 'audio/webm'
          : 'audio/mp4';

      const mediaRecorder = new MediaRecorder(stream, { mimeType });
      mediaRecorderRef.current = mediaRecorder;

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0 && socket.readyState === WebSocket.OPEN) {
          socket.send(event.data);
        }
      };

      mediaRecorder.start(250);
    };

    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.channel?.alternatives?.[0]) {
          const alt = data.channel.alternatives[0];
          const text = alt.transcript || '';
          const isFinal = data.is_final;

          if (text) {
            if (isFinal) {
              finalTranscriptsRef.current.push(text);
              currentInterimRef.current = '';
            } else {
              currentInterimRef.current = text;
            }

            const currentFull = buildFullText();
            setTranscript(currentFull);
            optionsRef.current.onTranscriptUpdate?.(currentFull, !!isFinal);
          }
        }
      } catch (e) {
        console.warn('Deepgram WS parse message warning:', e);
      }
    };

    socket.onerror = () => {
      window.clearTimeout(connectTimeout);
      if (handedOffToBrowserRef.current || !wantListeningRef.current) return;
      console.warn('Deepgram WS error — falling back to browser STT');
      handedOffToBrowserRef.current = true;
      try { mediaRecorderRef.current?.stop(); } catch { /* ignore */ }
      mediaRecorderRef.current = null;
      try { socket.close(); } catch { /* ignore */ }
      webSocketRef.current = null;
      stream.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
      startBrowserWebSpeech(seedTranscript);
    };

    socket.onclose = () => {
      window.clearTimeout(connectTimeout);
      if (handedOffToBrowserRef.current || speechRecognitionRef.current) return;
      if (isRecordingRef.current && wantListeningRef.current) {
        setRecordingState(false);
        setIsConnecting(false);
      }
    };
  }, [setRecordingState, startBrowserWebSpeech]);

  const startRecording = useCallback(async (seedTranscript = '') => {
    if (isRecordingRef.current || isConnecting) return;
    setError(null);
    setIsConnecting(true);
    finalTranscriptsRef.current = seedTranscript.trim() ? [seedTranscript.trim()] : [];
    currentInterimRef.current = '';
    wantListeningRef.current = true;
    handedOffToBrowserRef.current = false;

    try {
      let speechAvailable = false;
      let tokenKey = '';
      let wsUrl =
        'wss://api.deepgram.com/v1/listen?model=nova-2&smart_format=true&interim_results=true&punctuate=true&encoding=opus&container=webm';
      let authScheme = 'bearer';
      let tokenMessage = '';

      try {
        const tokenRes = await apiClient.get<any>(API_ENDPOINTS.SPEECH.TOKEN);
        const tokenData = unwrapSpeechToken(tokenRes);
        tokenMessage = tokenData.message || '';

        if (tokenData.available && tokenData.key) {
          speechAvailable = true;
          tokenKey = tokenData.key;
          if (tokenData.url) wsUrl = tokenData.url;
          if (tokenData.authScheme) authScheme = tokenData.authScheme;
        } else {
          speechAvailable = false;
        }
      } catch (e) {
        console.warn('Backend speech token fetch warning, using browser STT:', e);
        speechAvailable = false;
      }

      if (!speechAvailable || !tokenKey) {
        if (tokenMessage) {
          console.info('[STT]', tokenMessage);
        }
        startBrowserWebSpeech(seedTranscript);
        return;
      }

      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
        },
      });
      streamRef.current = stream;
      startDeepgramSocket(stream, tokenKey, wsUrl, authScheme, seedTranscript);
    } catch (err: any) {
      wantListeningRef.current = false;
      const errMsg =
        err.name === 'NotAllowedError'
          ? 'Microphone permission denied. Please allow microphone access in your browser.'
          : err.message || 'Failed to start microphone recording.';
      setError(errMsg);
      setIsConnecting(false);
      setRecordingState(false);
      optionsRef.current.onError?.(errMsg);
    }
  }, [isConnecting, setRecordingState, startBrowserWebSpeech, startDeepgramSocket]);

  useEffect(() => {
    return () => {
      wantListeningRef.current = false;
      try {
        if (speechRecognitionRef.current) {
          speechRecognitionRef.current.onresult = null;
          speechRecognitionRef.current.onerror = null;
          speechRecognitionRef.current.onend = null;
          speechRecognitionRef.current.abort();
        }
      } catch { /* ignore */ }
      speechRecognitionRef.current = null;
      try {
        if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
          mediaRecorderRef.current.stop();
        }
      } catch { /* ignore */ }
      streamRef.current?.getTracks().forEach((t) => t.stop());
      try { webSocketRef.current?.close(); } catch { /* ignore */ }
    };
  }, []);

  return {
    isRecording,
    isConnecting,
    transcript,
    error,
    provider,
    startRecording,
    stopRecording,
  };
}
