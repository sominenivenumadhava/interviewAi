import React, { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';

type MagneticVariant = 'primary' | 'secondary' | 'outline';

interface SharedProps {
  children: React.ReactNode;
  variant?: MagneticVariant;
  className?: string;
}

type MagneticButtonProps = SharedProps &
  (
    | ({ to: string; onClick?: never } & Omit<React.AnchorHTMLAttributes<HTMLAnchorElement>, 'href' | 'children' | 'className'>)
    | ({ to?: undefined; onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void } & React.ButtonHTMLAttributes<HTMLButtonElement>)
  );

const variantClasses: Record<MagneticVariant, string> = {
  primary:
    'bg-gradient-to-r from-brand-500 via-teal-500 to-cyan-500 text-white border border-white/20 shadow-glow-teal hover:shadow-glow-lg',
  secondary:
    'bg-ink-900/5 text-ink-800 border border-ink-200 backdrop-blur-md hover:bg-ink-900/10 dark:bg-white/10 dark:text-white dark:border-white/15 dark:hover:bg-white/20',
  outline:
    'bg-transparent text-brand-700 border border-brand-500/40 hover:border-brand-400 hover:bg-brand-500/10 dark:text-white',
};

/**
 * Magnetic CTA — renders as <Link> when `to` is set (avoids invalid <a><button>),
 * otherwise as <button>.
 */
export function MagneticButton({
  children,
  variant = 'primary',
  className = '',
  to,
  onClick,
  ...props
}: MagneticButtonProps) {
  const ref = useRef<HTMLElement>(null);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [glowPos, setGlowPos] = useState({ x: 50, y: 50 });

  const handleMouseMove = (e: React.MouseEvent<HTMLElement>) => {
    if (!ref.current) return;
    const rect = ref.current.getBoundingClientRect();
    const x = e.clientX - (rect.left + rect.width / 2);
    const y = e.clientY - (rect.top + rect.height / 2);
    setPosition({ x: x * 0.28, y: y * 0.28 });
    setGlowPos({
      x: ((e.clientX - rect.left) / rect.width) * 100,
      y: ((e.clientY - rect.top) / rect.height) * 100,
    });
  };

  const handleMouseLeave = () => setPosition({ x: 0, y: 0 });

  const classes = cn(
    'relative inline-flex items-center justify-center font-bold overflow-hidden rounded-2xl px-8 py-4 text-base tracking-wide transition-all duration-300 group cursor-pointer no-underline',
    variantClasses[variant],
    className
  );

  const inner = (
    <>
      <div
        className="absolute inset-0 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-300"
        style={{
          background: `radial-gradient(140px circle at ${glowPos.x}% ${glowPos.y}%, rgba(255, 255, 255, 0.35), transparent 70%)`,
        }}
      />
      <div className="absolute inset-0 rounded-2xl border border-white/30 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none" />
      <span className="relative z-10 flex items-center gap-2">{children}</span>
    </>
  );

  const motionProps = {
    onMouseMove: handleMouseMove,
    onMouseLeave: handleMouseLeave,
    animate: { x: position.x, y: position.y },
    transition: { type: 'spring' as const, stiffness: 220, damping: 16, mass: 0.5 },
    whileTap: { scale: 0.95 },
    className: classes,
  };

  if (to) {
    const MotionLink = motion.create(Link);
    return (
      <MotionLink
        ref={ref as React.Ref<HTMLAnchorElement>}
        to={to}
        {...motionProps}
        {...(props as React.AnchorHTMLAttributes<HTMLAnchorElement>)}
      >
        {inner}
      </MotionLink>
    );
  }

  return (
    <motion.button
      ref={ref as React.Ref<HTMLButtonElement>}
      onClick={onClick as (e: React.MouseEvent<HTMLButtonElement>) => void}
      {...motionProps}
      {...(props as React.ButtonHTMLAttributes<HTMLButtonElement>)}
    >
      {inner}
    </motion.button>
  );
}
