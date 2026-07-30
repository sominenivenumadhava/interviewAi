import React, { useRef, useState } from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';

interface MagneticButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'outline';
  className?: string;
  onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void;
}

export function MagneticButton({
  children,
  variant = 'primary',
  className = '',
  onClick,
  ...props
}: MagneticButtonProps) {
  const buttonRef = useRef<HTMLButtonElement>(null);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [glowPos, setGlowPos] = useState({ x: 50, y: 50 });

  const handleMouseMove = (e: React.MouseEvent<HTMLButtonElement>) => {
    if (!buttonRef.current) return;
    const rect = buttonRef.current.getBoundingClientRect();
    const x = e.clientX - (rect.left + rect.width / 2);
    const y = e.clientY - (rect.top + rect.height / 2);

    // Magnetic pull distance cap
    setPosition({ x: x * 0.28, y: y * 0.28 });

    // Relative glow percentage
    const px = ((e.clientX - rect.left) / rect.width) * 100;
    const py = ((e.clientY - rect.top) / rect.height) * 100;
    setGlowPos({ x: px, y: py });
  };

  const handleMouseLeave = () => {
    setPosition({ x: 0, y: 0 });
  };

  const variants = {
    primary:
      'bg-gradient-to-r from-brand-500 via-indigo-600 to-violet-600 text-white border border-white/20 shadow-glow-indigo hover:shadow-glow-lg',
    secondary:
      'bg-white/10 text-white border border-white/15 backdrop-blur-md hover:bg-white/20',
    outline:
      'bg-transparent text-white border border-brand-500/40 hover:border-brand-400 hover:bg-brand-500/10',
  };

  return (
    <motion.button
      ref={buttonRef}
      onMouseMove={handleMouseMove}
      onMouseLeave={handleMouseLeave}
      animate={{ x: position.x, y: position.y }}
      transition={{ type: 'spring', stiffness: 220, damping: 16, mass: 0.5 }}
      whileTap={{ scale: 0.95 }}
      onClick={onClick}
      className={cn(
        'relative inline-flex items-center justify-center font-bold overflow-hidden rounded-2xl px-8 py-4 text-base tracking-wide transition-all duration-300 group cursor-pointer',
        variants[variant],
        className
      )}
      {...(props as any)}
    >
      {/* Light Sweep Glow Hover Effect */}
      <div
        className="absolute inset-0 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-300"
        style={{
          background: `radial-gradient(140px circle at ${glowPos.x}% ${glowPos.y}%, rgba(255, 255, 255, 0.35), transparent 70%)`,
        }}
      />

      {/* Shimmer Border */}
      <div className="absolute inset-0 rounded-2xl border border-white/30 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none" />

      {/* Button Content */}
      <span className="relative z-10 flex items-center gap-2">{children}</span>
    </motion.button>
  );
}
