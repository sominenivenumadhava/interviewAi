import React, { useEffect, useRef } from 'react';
import { motion, useMotionValue, useSpring } from 'framer-motion';
import { useReducedMotion } from '../../hooks/useReducedMotion';

interface AnimatedBackgroundProps {
  variant?: 'auth' | 'dashboard' | 'landing' | 'interview';
  children?: React.ReactNode;
  className?: string;
}

/**
 * AnimatedBackground — Full-page cinematic background.
 * Aurora gradient mesh, floating glow blobs, cursor spotlight, noise texture.
 * Fully theme-aware: adapts between light and dark modes.
 */
export function AnimatedBackground({
  variant = 'auth',
  children,
  className = '',
}: AnimatedBackgroundProps) {
  const reduced = useReducedMotion();
  const containerRef = useRef<HTMLDivElement>(null);

  // Reactive cursor tracking via motion values
  const mouseX = useMotionValue(0.5);
  const mouseY = useMotionValue(0.5);
  const spotX  = useSpring(mouseX, { stiffness: 55, damping: 22 });
  const spotY  = useSpring(mouseY, { stiffness: 55, damping: 22 });

  useEffect(() => {
    if (reduced) return;
    const el = containerRef.current;
    if (!el) return;
    const onMove = (e: MouseEvent) => {
      const rect = el.getBoundingClientRect();
      mouseX.set((e.clientX - rect.left) / rect.width);
      mouseY.set((e.clientY - rect.top)  / rect.height);
    };
    el.addEventListener('mousemove', onMove);
    return () => el.removeEventListener('mousemove', onMove);
  }, [reduced, mouseX, mouseY]);

  const blobConfigs = {
    auth: [
      { dark: 'rgba(20,184,166,0.18)',  light: 'rgba(20,184,166,0.10)',  size: 600, top: '5%',  left: '10%', delay: 0  },
      { dark: 'rgba(6,182,212,0.14)',  light: 'rgba(6,182,212,0.08)',  size: 500, top: '50%', left: '70%', delay: 4  },
      { dark: 'rgba(236,72,153,0.10)',  light: 'rgba(236,72,153,0.06)',  size: 400, top: '75%', left: '20%', delay: 8  },
    ],
    dashboard: [
      { dark: 'rgba(20,184,166,0.11)',  light: 'rgba(20,184,166,0.06)',  size: 700, top: '-5%', left: '-5%', delay: 0  },
      { dark: 'rgba(6,182,212,0.09)',   light: 'rgba(6,182,212,0.05)',   size: 500, top: '60%', left: '80%', delay: 5  },
      { dark: 'rgba(6,182,212,0.07)',  light: 'rgba(6,182,212,0.04)',  size: 450, top: '40%', left: '40%', delay: 9  },
    ],
    landing: [
      { dark: 'rgba(20,184,166,0.22)',  light: 'rgba(20,184,166,0.12)',  size: 800, top: '-10%', left: '-10%', delay: 0  },
      { dark: 'rgba(6,182,212,0.18)',  light: 'rgba(6,182,212,0.10)',  size: 650, top: '30%',  left: '60%',  delay: 3  },
      { dark: 'rgba(6,182,212,0.12)',   light: 'rgba(6,182,212,0.07)',   size: 500, top: '80%',  left: '10%',  delay: 7  },
      { dark: 'rgba(236,72,153,0.10)',  light: 'rgba(236,72,153,0.06)',  size: 400, top: '60%',  left: '85%',  delay: 11 },
    ],
    interview: [
      { dark: 'rgba(6,182,212,0.14)',   light: 'rgba(6,182,212,0.07)',   size: 600, top: '5%',  left: '60%', delay: 0 },
      { dark: 'rgba(20,184,166,0.11)',  light: 'rgba(20,184,166,0.06)',  size: 500, top: '70%', left: '5%',  delay: 5 },
    ],
  };

  const blobs = blobConfigs[variant];

  return (
    <div
      ref={containerRef}
      className={`relative min-h-screen overflow-hidden ${className} bg-slate-50 dark:bg-obsidian-950`}
    >
      {/* ── Light-mode wash ── */}
      <div className="absolute inset-0 pointer-events-none dark:hidden bg-gradient-to-br from-slate-50 via-teal-50/40 to-cyan-50/20" />

      {/* ── Dark-mode base ── */}
      <div className="absolute inset-0 pointer-events-none hidden dark:block bg-obsidian-950" />

      {/* ── Noise texture ── */}
      <div
        className="absolute inset-0 pointer-events-none z-0 opacity-[0.018] dark:opacity-[0.025]"
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='1'/%3E%3C/svg%3E")`,
          backgroundSize: '200px 200px',
        }}
      />

      {/* ── Light-mode glow blobs ── */}
      {!reduced && blobs.map((blob, i) => (
        <motion.div
          key={`light-${i}`}
          className="absolute rounded-full pointer-events-none dark:hidden"
          style={{
            width: blob.size,
            height: blob.size,
            top: blob.top,
            left: blob.left,
            background: `radial-gradient(circle, ${blob.light} 0%, transparent 70%)`,
            filter: 'blur(72px)',
            transform: 'translate(-50%, -50%)',
          }}
          animate={{ scale: [1, 1.12, 0.96, 1.08, 1], x: [0, 25, -18, 12, 0], y: [0, -18, 28, -8, 0] }}
          transition={{ duration: 18 + i * 4, delay: blob.delay, repeat: Infinity, ease: 'easeInOut' }}
        />
      ))}

      {/* ── Dark-mode glow blobs ── */}
      {!reduced && blobs.map((blob, i) => (
        <motion.div
          key={`dark-${i}`}
          className="absolute rounded-full pointer-events-none hidden dark:block"
          style={{
            width: blob.size,
            height: blob.size,
            top: blob.top,
            left: blob.left,
            background: `radial-gradient(circle, ${blob.dark} 0%, transparent 70%)`,
            filter: 'blur(60px)',
            transform: 'translate(-50%, -50%)',
          }}
          animate={{ scale: [1, 1.12, 0.96, 1.08, 1], x: [0, 25, -18, 12, 0], y: [0, -18, 28, -8, 0] }}
          transition={{ duration: 18 + i * 4, delay: blob.delay, repeat: Infinity, ease: 'easeInOut' }}
        />
      ))}

      {/* ── Cursor spotlight — FIXED: uses style prop with motion values ── */}
      {!reduced && (
        <motion.div
          className="absolute pointer-events-none z-0 rounded-full"
          style={{
            width: 650,
            height: 650,
            background: 'radial-gradient(circle, rgba(20,184,166,0.055) 0%, transparent 65%)',
            // ✅ Correct reactive approach: pass motion values directly to style
            left: spotX,
            top:  spotY,
            x: '-50%',
            y: '-50%',
          }}
        />
      )}

      {/* ── Subtle grid lines ── */}
      <div
        className="absolute inset-0 pointer-events-none z-0 opacity-[0.010] dark:opacity-[0.022]"
        style={{
          backgroundImage:
            'linear-gradient(rgba(100,100,200,0.5) 1px, transparent 1px), linear-gradient(90deg, rgba(100,100,200,0.5) 1px, transparent 1px)',
          backgroundSize: '80px 80px',
        }}
      />

      {/* ── Content ── */}
      <div className="relative z-10">{children}</div>
    </div>
  );
}
