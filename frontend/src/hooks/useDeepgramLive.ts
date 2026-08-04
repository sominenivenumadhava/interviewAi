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

const DEEPGRAM_PCM_URL =
  'wss://api.deepgram.com/v1/listen?model=nova-2&encoding=linear16&sample_rate=16000&channels=1&punctuate=true&interim_results=true&smart_format=true&endpointing=300&utterance_end_ms=1000';

function unwrapSpeechToken(tokenRes: any): SpeechTokenPayload {
  // apiClient returns JSON body. Backend wraps as ApiResponse { data: {...} }.
  if (tokenRes?.data && typeof tokenRes.data === 'object') {
    const inner = tokenRes.data;
    if (
      inner.available !== undefined ||
      inner.key ||
      inner.provider ||
      inner.authScheme
    ) {
      return inner as SpeechTokenPayload;
    }
    if (inner.data && typeof inner.data === 'object') {
      return inner.data as SpeechTokenPayload;
    }
  }
  return (tokenRes ?? {}) as SpeechTokenPayload;
}

function downsampleBuffer(
  buffer: Float32Array,
  inputSampleRate: number,
  outputSampleRate: number
): Float32Array {
  if (outputSampleRate === inputSampleRate) {
    return buffer;
  }
  const sampleRateRatio = inputSampleRate / outputSampleRate;
  const newLength = Math.round(buffer.length / sampleRateRatio);
  const result = new Float32Array(newLength);
  let offsetResult = 0;
  let offsetBuffer = 0;
  while (offsetResult < result.length) {
    const nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
    let accum = 0;
    let count = 0;
    for (let i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++) {
      accum += buffer[i];
      count++;
    }
    result[offsetResult] = count > 0 ? accum / count : 0;
    offsetResult++;
    offsetBuffer = nextOffsetBuffer;
  }
  return result;
}

function floatTo16BitPCM(float32Array: Float32Array): ArrayBuffer {
  const buffer = new ArrayBuffer(float32Array.length * 2);
  const view = new DataView(buffer);
  for (let i = 0; i < float32Array.length; i++) {
    let sample = Math.max(-1, Math.min(1, float32Array[i]));
    view.setInt16(i * 2, sample < 0 ? sample * 0x8000 : sample * 0x7fff, true);
  }
  return buffer;
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
  const audioContextRef = useRef<AudioContext | null>(null);
  const processorRef = useRef<ScriptProcessorNode | null>(null);
  const sourceRef = useRef<MediaStreamAudioSourceNode | null>(null);
  const keepAliveRef = useRef<number | null>(null);
  const finalTranscriptsRef = useRef<string[]>([]);
  const currentInterimRef = useRef<string>('');
  const speechRecognitionRef = useRef<any>(null);
  const wantListeningRef = useRef(false);
  const isRecordingRef = useRef(false);
  const handedOffToBrowserRef = useRef(false);
  /** Fatal WebSpeech errors that must not auto-restart (avoids network-error spam). */
  const browserFatalErrorRef = useRef(false);
  const optionsRef = useRef(options);
  optionsRef.current = options;

  const setRecordingState = useCallback((recording: boolean) => {
    isRecordingRef.current = recording;
    setIsRecording(recording);
  }, []);

  const buildFullText = () =>
    [...finalTranscriptsRef.current, currentInterimRef.current].filter(Boolean).join(' ').trim();

  const clearKeepAlive = () => {
    if (keepAliveRef.current != null) {
      window.clearInterval(keepAliveRef.current);
      keepAliveRef.current = null;
    }
  };

  const stopAudioPipeline = () => {
    try {
      processorRef.current?.disconnect();
    } catch { /* ignore */ }
    processorRef.current = null;
    try {
      sourceRef.current?.disconnect();
    } catch { /* ignore */ }
    sourceRef.current = null;
    if (audioContextRef.current) {
      try {
        void audioContextRef.current.close();
      } catch { /* ignore */ }
      audioContextRef.current = null;
    }
  };

  const stopRecording = useCallback((): string => {
    wantListeningRef.current = false;
    handedOffToBrowserRef.current = false;
    browserFatalErrorRef.current = false;
    clearKeepAlive();

    try {
      if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
        mediaRecorderRef.current.stop();
      }
    } catch (e) {
      console.warn('Error stopping media recorder:', e);
    }
    mediaRecorderRef.current = null;

    stopAudioPipeline();

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

  const failBrowserSpeech = useCallback((msg: string) => {
    browserFatalErrorRef.current = true;
    wantListeningRef.current = false;
    setError(msg);
    setRecordingState(false);
    setIsConnecting(false);
    if (speechRecognitionRef.current) {
      try {
        speechRecognitionRef.current.onresult = null;
        speechRecognitionRef.current.onerror = null;
        speechRecognitionRef.current.onend = null;
        speechRecognitionRef.current.abort();
      } catch { /* ignore */ }
      speechRecognitionRef.current = null;
    }
    optionsRef.current.onError?.(msg);
  }, [setRecordingState]);

  const startBrowserWebSpeech = useCallback((seedTranscript = '') => {
    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      failBrowserSpeech('Live Speech Recognition is not supported on this browser. Try Chrome.');
      return;
    }

    try {
      browserFatalErrorRef.current = false;
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
          failBrowserSpeech('Microphone permission denied. Please allow microphone access.');
          return;
        }

        // Chrome Web Speech talks to Google servers — network/service errors retry forever via onend
        // unless we stop listening. Prefer Deepgram (DEEPGRAM_API_KEY) for reliable STT.
        if (code === 'network' || code === 'service-not-allowed') {
          console.warn('WebSpeech fatal error:', code, e);
          failBrowserSpeech(
            'Browser speech failed (network). Set DEEPGRAM_API_KEY in .env and restart the backend for reliable live transcription.'
          );
          return;
        }

        console.warn('WebSpeech error:', e);
      };

      recognition.onend = () => {
        if (!wantListeningRef.current || browserFatalErrorRef.current) {
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
      failBrowserSpeech('Failed to start speech recognition.');
    }
  }, [failBrowserSpeech, setRecordingState]);

  const startDeepgramPcmStream = useCallback((
    stream: MediaStream,
    tokenKey: string,
    authScheme: string,
    seedTranscript: string
  ) => {
    handedOffToBrowserRef.current = false;
    setProvider('deepgram');

    const fallbackToBrowser = (reason: string) => {
      if (handedOffToBrowserRef.current || !wantListeningRef.current) return;
      console.warn('Deepgram fallback:', reason);
      handedOffToBrowserRef.current = true;
      clearKeepAlive();
      stopAudioPipeline();
      try { webSocketRef.current?.close(); } catch { /* ignore */ }
      webSocketRef.current = null;
      stream.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
      startBrowserWebSpeech(seedTranscript);
    };

    let socket: WebSocket;
    try {
      socket = new WebSocket(DEEPGRAM_PCM_URL, [authScheme || 'token', tokenKey]);
    } catch (e) {
      fallbackToBrowser(`WebSocket construct failed: ${e}`);
      return;
    }

    webSocketRef.current = socket;
    socket.binaryType = 'arraybuffer';

    const connectTimeout = window.setTimeout(() => {
      if (!isRecordingRef.current && wantListeningRef.current && !handedOffToBrowserRef.current) {
        fallbackToBrowser('connect timeout');
      }
    }, 10000);

    socket.onopen = async () => {
      window.clearTimeout(connectTimeout);
      try {
        const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
        const audioContext: AudioContext = new AudioCtx();
        audioContextRef.current = audioContext;
        if (audioContext.state === 'suspended') {
          await audioContext.resume();
        }

        const source = audioContext.createMediaStreamSource(stream);
        sourceRef.current = source;

        // ScriptProcessor is deprecated but widely supported; buffer 4096 ≈ 85–250ms depending on rate.
        const processor = audioContext.createScriptProcessor(4096, 1, 1);
        processorRef.current = processor;

        processor.onaudioprocess = (event) => {
          if (!wantListeningRef.current || socket.readyState !== WebSocket.OPEN) return;
          const input = event.inputBuffer.getChannelData(0);
          const downsampled = downsampleBuffer(input, audioContext.sampleRate, 16000);
          if (downsampled.length === 0) return;
          const pcm = floatTo16BitPCM(downsampled);
          try {
            socket.send(pcm);
          } catch (err) {
            console.warn('Deepgram PCM send failed:', err);
          }
        };

        source.connect(processor);
        // Keep processor active without routing mic audio to speakers
        const sink = audioContext.createMediaStreamDestination();
        processor.connect(sink);

        keepAliveRef.current = window.setInterval(() => {
          if (socket.readyState === WebSocket.OPEN) {
            try {
              socket.send(JSON.stringify({ type: 'KeepAlive' }));
            } catch { /* ignore */ }
          }
        }, 8000);

        setIsConnecting(false);
        setRecordingState(true);
      } catch (err: any) {
        fallbackToBrowser(err?.message || 'audio pipeline failed');
      }
    };

    socket.onmessage = (event) => {
      try {
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : null;
        if (!data) return;

        if (data.type === 'Error' || data.error) {
          console.warn('Deepgram error message:', data);
          return;
        }

        const alt = data.channel?.alternatives?.[0];
        if (!alt) return;

        const text = (alt.transcript || '').trim();
        const isFinal = !!data.is_final;

        if (!text && !isFinal) return;

        if (isFinal) {
          if (text) finalTranscriptsRef.current.push(text);
          currentInterimRef.current = '';
        } else if (text) {
          currentInterimRef.current = text;
        }

        const currentFull = buildFullText();
        setTranscript(currentFull);
        optionsRef.current.onTranscriptUpdate?.(currentFull, isFinal);
      } catch (e) {
        console.warn('Deepgram WS parse warning:', e);
      }
    };

    socket.onerror = () => {
      window.clearTimeout(connectTimeout);
      fallbackToBrowser('websocket error');
    };

    socket.onclose = (ev) => {
      window.clearTimeout(connectTimeout);
      clearKeepAlive();
      if (handedOffToBrowserRef.current || speechRecognitionRef.current) return;
      if (wantListeningRef.current && !isRecordingRef.current) {
        // Never opened successfully
        fallbackToBrowser(`websocket closed before start (${ev.code})`);
        return;
      }
      if (isRecordingRef.current && wantListeningRef.current) {
        // Unexpected close mid-session — try browser fallback
        fallbackToBrowser(`websocket closed (${ev.code})`);
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
    browserFatalErrorRef.current = false;

    try {
      let speechAvailable = false;
      let tokenKey = '';
      let authScheme = 'token';
      let tokenMessage = '';

      try {
        const tokenRes = await apiClient.get<any>(API_ENDPOINTS.SPEECH.TOKEN, { retry: false });
        const tokenData = unwrapSpeechToken(tokenRes);
        tokenMessage = tokenData.message || '';
        console.info('[STT] token response', {
          available: tokenData.available,
          provider: tokenData.provider,
          authScheme: tokenData.authScheme,
          hasKey: !!tokenData.key,
          message: tokenData.message,
        });

        if (tokenData.available && tokenData.key) {
          speechAvailable = true;
          tokenKey = tokenData.key;
          if (tokenData.authScheme) authScheme = tokenData.authScheme;
        }
      } catch (e) {
        console.warn('Backend speech token fetch warning, using browser STT:', e);
        speechAvailable = false;
      }

      if (!speechAvailable || !tokenKey) {
        if (tokenMessage) console.info('[STT]', tokenMessage);
        startBrowserWebSpeech(seedTranscript);
        return;
      }

      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          channelCount: 1,
        },
      });
      streamRef.current = stream;
      startDeepgramPcmStream(stream, tokenKey, authScheme, seedTranscript);
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
  }, [isConnecting, setRecordingState, startBrowserWebSpeech, startDeepgramPcmStream]);

  useEffect(() => {
    return () => {
      wantListeningRef.current = false;
      clearKeepAlive();
      stopAudioPipeline();
      try {
        if (speechRecognitionRef.current) {
          speechRecognitionRef.current.onresult = null;
          speechRecognitionRef.current.onerror = null;
          speechRecognitionRef.current.onend = null;
          speechRecognitionRef.current.abort();
        }
      } catch { /* ignore */ }
      speechRecognitionRef.current = null;
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
