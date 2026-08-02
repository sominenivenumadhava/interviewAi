import React, { useEffect, useRef } from 'react';

interface HeroCanvasProps {
  className?: string;
  theme?: 'light' | 'dark';
}

export function HeroCanvas({ className = '', theme = 'dark' }: HeroCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const themeRef = useRef(theme);

  useEffect(() => {
    themeRef.current = theme;
  }, [theme]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    const getDocHeight = () => Math.max(window.innerHeight, document.documentElement.scrollHeight);
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = getDocHeight());

    // Mouse coordinates with spring inertia
    const mouse = {
      x: width / 2,
      y: height / 2,
      targetX: width / 2,
      targetY: height / 2,
    };

    const handleMouseMove = (e: MouseEvent) => {
      mouse.targetX = e.clientX;
      mouse.targetY = e.clientY + window.scrollY;
    };

    const handleResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = getDocHeight();
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('resize', handleResize);
    window.addEventListener('scroll', handleResize);

    // ─── Neural Particles ───────────────────────────────────────────────────
    const particleCount = Math.min(80, Math.floor(width / 18));
    const particles = Array.from({ length: particleCount }).map(() => ({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.4,
      vy: (Math.random() - 0.5) * 0.4,
      radius: Math.random() * 1.8 + 0.8,
      alpha: Math.random() * 0.5 + 0.2,
      pulse: Math.random() * Math.PI * 2,
    }));

    // ─── 3D Core Sphere Points ──────────────────────────────────────────────
    const sphereRadius = Math.min(130, width * 0.15);
    const spherePointCount = 220;
    const spherePoints: { x: number; y: number; z: number; baseR: number }[] = [];
    const phi = Math.PI * (3 - Math.sqrt(5)); // golden angle

    for (let i = 0; i < spherePointCount; i++) {
      const y = 1 - (i / (spherePointCount - 1)) * 2; // -1 to 1
      const radiusAtY = Math.sqrt(1 - y * y);
      const theta = phi * i;

      spherePoints.push({
        x: Math.cos(theta) * radiusAtY * sphereRadius,
        y: y * sphereRadius,
        z: Math.sin(theta) * radiusAtY * sphereRadius,
        baseR: Math.random() * 1.5 + 1.2,
      });
    }

    let rotationX = 0;
    let rotationY = 0;
    let gridOffset = 0;
    let time = 0;

    // ─── Render Loop ────────────────────────────────────────────────────────
    const render = () => {
      time += 0.015;
      const isLight = themeRef.current === 'light';

      // Mouse inertia interpolation
      mouse.x += (mouse.targetX - mouse.x) * 0.05;
      mouse.y += (mouse.targetY - mouse.y) * 0.05;

      const normMouseX = (mouse.x - width / 2) / (width / 2);
      const normMouseY = (mouse.y - height / 2) / (height / 2);

      // Clear canvas with theme background
      if (isLight) {
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, width, height);
        animationFrameId = requestAnimationFrame(render);
        return;
      }

      ctx.fillStyle = '#04060A';
      ctx.fillRect(0, 0, width, height);

      // ── Layer 1: Ambient Dynamic Gradient Mesh (Dark Mode Viewport) ──
      const gradX = width / 2 + normMouseX * 140;
      const gradY = height * 0.4 + normMouseY * 100;
      const bgGlow = ctx.createRadialGradient(
        gradX, gradY, 20,
        width / 2, height / 2, Math.max(width, height) * 0.95
      );
      bgGlow.addColorStop(0, 'rgba(91, 93, 254, 0.25)');
      bgGlow.addColorStop(0.3, 'rgba(62, 69, 255, 0.14)');
      bgGlow.addColorStop(0.6, 'rgba(6, 182, 212, 0.08)');
      bgGlow.addColorStop(1, '#04060A');
      ctx.fillStyle = bgGlow;
      ctx.fillRect(0, 0, width, height);

      // ── Layer 4: Secondary Orbiting Ambient Light Nodes ──
      const glow1X = width * 0.3 + Math.sin(time * 0.4) * (width * 0.2);
      const glow1Y = height * 0.25 + Math.cos(time * 0.3) * (height * 0.15);
      const accentGlow1 = ctx.createRadialGradient(glow1X, glow1Y, 10, glow1X, glow1Y, width * 0.35);
      accentGlow1.addColorStop(0, 'rgba(6, 182, 212, 0.22)');
      accentGlow1.addColorStop(0.6, 'rgba(20, 184, 166, 0.08)');
      accentGlow1.addColorStop(1, 'transparent');
      ctx.fillStyle = accentGlow1;
      ctx.fillRect(0, 0, width, height);

      const glow2X = width * 0.7 + Math.cos(time * 0.35) * (width * 0.2);
      const glow2Y = height * 0.75 + Math.sin(time * 0.45) * (height * 0.15);
      const accentGlow2 = ctx.createRadialGradient(glow2X, glow2Y, 10, glow2X, glow2Y, width * 0.4);
      accentGlow2.addColorStop(0, 'rgba(6, 182, 212, 0.18)');
      accentGlow2.addColorStop(0.5, 'rgba(62, 69, 255, 0.06)');
      accentGlow2.addColorStop(1, 'transparent');
      ctx.fillStyle = accentGlow2;
      ctx.fillRect(0, 0, width, height);

      // ── Layer 5: Perspective Grid Line Horizon ──
      ctx.save();
      ctx.strokeStyle = 'rgba(20, 184, 166, 0.04)';
      ctx.lineWidth = 1;
      gridOffset = (gridOffset + 0.35) % 40;
      const horizonY = height * 0.55;

      for (let x = -width; x < width * 2; x += 55) {
        ctx.beginPath();
        ctx.moveTo(x, height);
        ctx.lineTo(width / 2 + (x - width / 2) * 0.12, horizonY);
        ctx.stroke();
      }
      for (let y = horizonY; y < height; y += 22) {
        const lineY = y + (gridOffset % 22);
        if (lineY <= height) {
          ctx.beginPath();
          ctx.moveTo(0, lineY);
          ctx.lineTo(width, lineY);
          ctx.stroke();
        }
      }
      ctx.restore();

      // ── Layer 2 & 3: Neural Particles & Links ──
      particles.forEach((p, i) => {
        p.x += p.vx + normMouseX * 0.2;
        p.y += p.vy + normMouseY * 0.2;
        p.pulse += 0.02;

        if (p.x < 0) p.x = width;
        if (p.x > width) p.x = 0;
        if (p.y < 0) p.y = height;
        if (p.y > height) p.y = 0;

        const currentRadius = p.radius + Math.sin(p.pulse) * 0.5;

        ctx.beginPath();
        ctx.arc(p.x, p.y, Math.max(0.5, currentRadius), 0, Math.PI * 2);
        ctx.fillStyle = `rgba(165, 180, 252, ${p.alpha * 0.8})`;
        ctx.shadowBlur = 8;
        ctx.shadowColor = 'rgba(20, 184, 166, 0.6)';
        ctx.fill();
        ctx.shadowBlur = 0;

        // Neural connections
        for (let j = i + 1; j < particles.length; j++) {
          const p2 = particles[j];
          const dx = p.x - p2.x;
          const dy = p.y - p2.y;
          const dist = Math.sqrt(dx * dx + dy * dy);

          if (dist < 130) {
            const lineAlpha = (1 - dist / 130) * 0.18;
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            ctx.lineTo(p2.x, p2.y);
            ctx.strokeStyle = `rgba(20, 184, 166, ${lineAlpha})`;
            ctx.lineWidth = 0.8;
            ctx.stroke();
          }
        }
      });

      // ── Centerpiece 3D Interactive AI Core Sphere ──
      const coreCenterX = width / 2 + normMouseX * 40;
      const coreCenterY = height * 0.28 + normMouseY * 25;

      rotationY += 0.008 + normMouseX * 0.005;
      rotationX += 0.004 + normMouseY * 0.005;

      // Outer Energy Glow Ring
      ctx.save();
      ctx.translate(coreCenterX, coreCenterY);

      // Orbital Ring 1
      ctx.save();
      ctx.rotate(time * 0.4);
      ctx.beginPath();
      ctx.ellipse(0, 0, sphereRadius * 1.5, sphereRadius * 0.5, time * 0.2, 0, Math.PI * 2);
      ctx.strokeStyle = 'rgba(6, 182, 212, 0.25)';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([12, 8]);
      ctx.stroke();
      ctx.restore();

      // Orbital Ring 2
      ctx.save();
      ctx.rotate(-time * 0.3);
      ctx.beginPath();
      ctx.ellipse(0, 0, sphereRadius * 1.3, sphereRadius * 0.6, -time * 0.4, 0, Math.PI * 2);
      ctx.strokeStyle = 'rgba(6, 182, 212, 0.3)';
      ctx.lineWidth = 1.2;
      ctx.stroke();
      ctx.restore();

      // Core Inner Radial Glow
      const coreGlow = ctx.createRadialGradient(0, 0, 5, 0, 0, sphereRadius * 1.1);
      coreGlow.addColorStop(0, 'rgba(129, 140, 248, 0.8)');
      coreGlow.addColorStop(0.3, 'rgba(20, 184, 166, 0.4)');
      coreGlow.addColorStop(0.7, 'rgba(62, 69, 255, 0.12)');
      coreGlow.addColorStop(1, 'transparent');
      ctx.fillStyle = coreGlow;
      ctx.beginPath();
      ctx.arc(0, 0, sphereRadius * 1.2, 0, Math.PI * 2);
      ctx.fill();

      // Render 3D Projected Point Sphere
      const cosX = Math.cos(rotationX);
      const sinX = Math.sin(rotationX);
      const cosY = Math.cos(rotationY);
      const sinY = Math.sin(rotationY);

      const projectedPoints = spherePoints.map((pt) => {
        // Rotate Y
        let x1 = pt.x * cosY - pt.z * sinY;
        let z1 = pt.z * cosY + pt.x * sinY;
        // Rotate X
        let y2 = pt.y * cosX - z1 * sinX;
        let z2 = z1 * cosX + pt.y * sinX;

        // Perspective scale factor
        const fov = 350;
        const scale = fov / (fov + z2);
        return {
          px: x1 * scale,
          py: y2 * scale,
          z: z2,
          scale,
          baseR: pt.baseR,
        };
      });

      // Sort points by Z depth
      projectedPoints.sort((a, b) => b.z - a.z);

      projectedPoints.forEach((pt) => {
        const depthAlpha = Math.max(0.15, (pt.z + sphereRadius) / (sphereRadius * 2));
        const pointRadius = Math.max(0.8, pt.baseR * pt.scale);

        ctx.beginPath();
        ctx.arc(pt.px, pt.py, pointRadius, 0, Math.PI * 2);
        ctx.fillStyle = pt.z > 0
          ? `rgba(224, 231, 255, ${depthAlpha * 0.95})`
          : `rgba(20, 184, 166, ${depthAlpha * 0.6})`;

        if (pt.z > sphereRadius * 0.3) {
          ctx.shadowBlur = 10;
          ctx.shadowColor = 'rgba(6, 182, 212, 0.8)';
        } else {
          ctx.shadowBlur = 0;
        }

        ctx.fill();
        ctx.shadowBlur = 0;
      });

      ctx.restore();

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('scroll', handleResize);
      cancelAnimationFrame(animationFrameId);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      className={`absolute inset-0 w-full h-full pointer-events-none z-0 ${className}`}
    />
  );
}
