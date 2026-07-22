import { useState, useRef, useCallback } from 'react';
import apiClient, { API_ENDPOINTS } from '../lib/apiClient';

export interface UseDeepgramLiveOptions {
  onTranscriptUpdate?: (transcript: string, isFinal: boolean) => void;
  onError?: (error: string) => void;
}

export function useDeepgramLive(options: UseDeepgramLiveOptions = {}) {
  const [isRecording, setIsRecording] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [error, setError] = useState<string | null>(null);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const webSocketRef = useRef<WebSocket | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const finalTranscriptsRef = useRef<string[]>([]);
  const currentInterimRef = useRef<string>('');

  // Stop recording and close WebSocket
  const stopRecording = useCallback(() => {
    try {
      if (mediaRecorderRef.current && mediaRecorderRef.current.state !== 'inactive') {
        mediaRecorderRef.current.stop();
      }
    } catch (e) {
      console.warn('Error stopping media recorder:', e);
    }

    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }

    if (webSocketRef.current) {
      try {
        if (webSocketRef.current.readyState === WebSocket.OPEN) {
          // Send Deepgram close stream message
          webSocketRef.current.send(JSON.stringify({ type: 'CloseStream' }));
        }
        webSocketRef.current.close();
      } catch (e) {
        console.warn('Error closing websocket:', e);
      }
      webSocketRef.current = null;
    }

    setIsRecording(false);
    setIsConnecting(false);

    // Finalize full text transcript
    const fullText = [...finalTranscriptsRef.current, currentInterimRef.current].filter(Boolean).join(' ').trim();
    if (options.onTranscriptUpdate) {
      options.onTranscriptUpdate(fullText, true);
    }
  }, [options]);

  // Start Real-Time Speech-to-Text
  const startRecording = useCallback(async () => {
    if (isRecording || isConnecting) return;
    setError(null);
    setIsConnecting(true);
    finalTranscriptsRef.current = [];
    currentInterimRef.current = '';

    try {
      // 1. Request Microphone Permissions
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          autoGainControl: true,
          sampleRate: 16000
        }
      });
      streamRef.current = stream;

      // 2. Fetch Deepgram Token Config from Backend
      let tokenKey = 'mock-key';
      let wsUrl = 'wss://api.deepgram.com/v1/listen?model=nova-2&smart_format=true&interim_results=true&punctuate=true';

      try {
        const tokenRes = await apiClient.get<any>(API_ENDPOINTS.SPEECH.TOKEN);
        if (tokenRes.data && tokenRes.data.key) {
          tokenKey = tokenRes.data.key;
          if (tokenRes.data.url) wsUrl = tokenRes.data.url;
        }
      } catch (e) {
        console.warn('Backend speech token fetch warning, connecting using browser stream:', e);
      }

      // 3. Connect to WebSocket
      let socket: WebSocket;
      try {
        // Deepgram WebSocket expects Sec-WebSocket-Protocol or query parameter for token
        socket = new WebSocket(wsUrl, ['token', tokenKey]);
      } catch (wsErr) {
        // Fallback for standard WebSocket URL format
        socket = new WebSocket(wsUrl);
      }

      webSocketRef.current = socket;

      socket.onopen = () => {
        setIsConnecting(false);
        setIsRecording(true);

        // 4. Start MediaRecorder to stream audio chunks
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

        // Stream audio chunks every 250ms
        mediaRecorder.start(250);
      };

      socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (data.channel && data.channel.alternatives && data.channel.alternatives[0]) {
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

              const currentFull = [...finalTranscriptsRef.current, currentInterimRef.current]
                .filter(Boolean)
                .join(' ')
                .trim();

              setTranscript(currentFull);
              if (options.onTranscriptUpdate) {
                options.onTranscriptUpdate(currentFull, isFinal);
              }
            }
          }
        } catch (e) {
          console.warn('Deepgram WS parse message warning:', e);
        }
      };

      socket.onerror = (errEvent) => {
        console.warn('Deepgram WS error / WebSpeech fallback active:', errEvent);
        // Seamless WebSpeech Fallback if WebSocket drops or key is invalid
        startBrowserWebSpeech(stream);
      };

      socket.onclose = () => {
        if (isRecording) {
          setIsRecording(false);
        }
      };

    } catch (err: any) {
      const errMsg = err.name === 'NotAllowedError'
        ? 'Microphone permission denied. Please allow microphone access in your browser.'
        : err.message || 'Failed to start microphone recording.';
      setError(errMsg);
      setIsConnecting(false);
      setIsRecording(false);
      if (options.onError) options.onError(errMsg);
    }
  }, [isRecording, isConnecting, options, stopRecording]);

  // Fallback to Native Web Speech API for seamless offline / no-key operation
  const startBrowserWebSpeech = (_stream: MediaStream) => {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setError('Live Speech Recognition is not supported on this browser.');
      setIsConnecting(false);
      setIsRecording(false);
      return;
    }

    try {
      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'en-US';

      recognition.onstart = () => {
        setIsConnecting(false);
        setIsRecording(true);
      };

      recognition.onresult = (event: any) => {
        let interim = '';
        let final = '';

        for (let i = event.resultIndex; i < event.results.length; ++i) {
          if (event.results[i].isFinal) {
            final += event.results[i][0].transcript + ' ';
          } else {
            interim += event.results[i][0].transcript;
          }
        }

        if (final) finalTranscriptsRef.current.push(final.trim());
        currentInterimRef.current = interim.trim();

        const combined = [...finalTranscriptsRef.current, currentInterimRef.current].filter(Boolean).join(' ').trim();
        setTranscript(combined);
        if (options.onTranscriptUpdate) {
          options.onTranscriptUpdate(combined, false);
        }
      };

      recognition.onerror = (e: any) => {
        console.warn('WebSpeech error:', e);
      };

      recognition.start();
    } catch (e) {
      console.warn('WebSpeech start error:', e);
    }
  };

  return {
    isRecording,
    isConnecting,
    transcript,
    error,
    startRecording,
    stopRecording
  };
}
