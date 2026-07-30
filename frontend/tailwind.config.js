export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}'
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'ui-monospace', 'monospace'],
      },
      colors: {
        // ─── Core Palette ─────────────────────────────────────────────────
        obsidian: {
          950: '#07090e',
          900: '#0b0f19',
          850: '#111827',
          800: '#161e31',
          700: '#232d44ff',
        },
        // ─── Brand = Indigo (unified — was split between blue + indigo) ───
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
          950: '#1e1b4b',
        },
        // ─── Neon accents ─────────────────────────────────────────────────
        neon: {
          cyan: '#06b6d4',
          violet: '#8b5cf6',
          pink: '#ec4899',
          indigo: '#6366f1',
          emerald: '#10b981',
        },
        // ─── Ink (neutral grays) ──────────────────────────────────────────
        ink: {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
          950: '#020617',
        },
        // ─── Semantic ─────────────────────────────────────────────────────
        success: '#10b981',  // emerald
        warning: '#f59e0b',  // amber
        danger: '#f43f5e',  // rose (was red #ef4444)
        info: '#06b6d4',  // cyan
        // ─── Chart palette (unified) ──────────────────────────────────────
        chart: {
          indigo: '#6366f1',
          cyan: '#06b6d4',
          emerald: '#10b981',
          amber: '#f59e0b',
          rose: '#f43f5e',
          violet: '#8b5cf6',
          sky: '#38bdf8',
        },
      },
      // ─── Border radius ───────────────────────────────────────────────────
      borderRadius: {
        xl: '12px',
        '2xl': '16px',
        '3xl': '24px',
        '4xl': '32px',
      },
      // ─── Shadows ─────────────────────────────────────────────────────────
      boxShadow: {
        // Light
        soft: '0 1px 3px rgba(15,23,42,0.06), 0 4px 16px rgba(15,23,42,0.05)',
        lift: '0 8px 30px rgba(15,23,42,0.08)',
        // Dark
        card: '0 0 0 1px rgba(255,255,255,0.05), 0 4px 24px rgba(0,0,0,0.4)',
        'card-hover': '0 0 0 1px rgba(99,102,241,0.3), 0 8px 32px rgba(0,0,0,0.5)',
        // Glass
        glass: '0 8px 32px 0 rgba(0, 0, 0, 0.4)',
        // Glows
        'glow-sm': '0 0 12px -3px rgba(99,102,241,0.3)',
        'glow-md': '0 0 24px -5px rgba(99,102,241,0.4)',
        'glow-lg': '0 0 48px -8px rgba(99,102,241,0.5)',
        'glow-indigo': '0 0 25px -5px rgba(99, 102, 241, 0.4)',
        'glow-violet': '0 0 25px -5px rgba(139, 92, 246, 0.4)',
        'glow-cyan': '0 0 25px -5px rgba(6, 182, 212, 0.4)',
        'glow-emerald': '0 0 25px -5px rgba(16, 185, 129, 0.4)',
        'glow-rose': '0 0 25px -5px rgba(244, 63, 94, 0.4)',
        'glow-amber': '0 0 25px -5px rgba(245, 158, 11, 0.4)',
      },
      // ─── Keyframes ───────────────────────────────────────────────────────
      keyframes: {
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        shimmer: {
          '100%': { transform: 'translateX(100%)' },
        },
        pulseGlow: {
          '0%, 100%': { opacity: '0.6', transform: 'scale(1)' },
          '50%': { opacity: '0.9', transform: 'scale(1.05)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-8px)' },
        },
        'border-pulse': {
          '0%, 100%': { borderColor: 'rgba(99,102,241,0.3)' },
          '50%': { borderColor: 'rgba(99,102,241,0.8)' },
        },
        'progress-fill': {
          '0%': { width: '0%' },
          '100%': { width: 'var(--progress-width)' },
        },
        'slide-in-right': {
          '0%': { transform: 'translateX(100%)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        'slide-out-right': {
          '0%': { transform: 'translateX(0)', opacity: '1' },
          '100%': { transform: 'translateX(100%)', opacity: '0' },
        },
        ping: {
          '75%, 100%': { transform: 'scale(2)', opacity: '0' },
        },
      },
      // ─── Animations ──────────────────────────────────────────────────────
      animation: {
        'fade-up': 'fade-up 0.4s ease-out both',
        'fade-in': 'fade-in 0.3s ease-out both',
        shimmer: 'shimmer 1.6s infinite',
        'pulse-glow': 'pulseGlow 6s ease-in-out infinite',
        float: 'float 6s ease-in-out infinite',
        'border-pulse': 'border-pulse 2s ease-in-out infinite',
        'slide-in-right': 'slide-in-right 0.3s ease-out both',
        'slide-out-right': 'slide-out-right 0.3s ease-in both',
      },
      // ─── Backdrop blur ───────────────────────────────────────────────────
      backdropBlur: {
        xs: '2px',
      },
      // ─── Transition timing ───────────────────────────────────────────────
      transitionTimingFunction: {
        'spring': 'cubic-bezier(0.34, 1.56, 0.64, 1)',
        'smooth': 'cubic-bezier(0.4, 0, 0.2, 1)',
        'premium': 'cubic-bezier(0.25, 0.1, 0.25, 1)',
      },
    },
  },
  plugins: [],
}
