import React, { useEffect, useRef } from 'react';

interface AuthCanvasProps {
  className?: string;
}

export function AuthCanvas({ className = '' }: AuthCanvasProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Mouse coordinates with spring inertia
    const mouse = {
      x: width / 2,
      y: height / 2,
      targetX: width / 2,
      targetY: height / 2,
    };

    const handleMouseMove = (e: MouseEvent) => {
      mouse.targetX = e.clientX;
      mouse.targetY = e.clientY;
    };

    const handleResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('resize', handleResize);

    // ─── Floating Constellation Star Nodes ─────────────────────────────────
    const nodeCount = 55;
    const nodes = Array.from({ length: nodeCount }).map(() => ({
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.35,
      vy: (Math.random() - 0.5) * 0.35,
      radius: Math.random() * 2 + 1,
      alpha: Math.random() * 0.6 + 0.2,
      pulse: Math.random() * Math.PI * 2,
    }));

    // ─── Floating Hexagonal Tech Geometry Particles ────────────────────────
    const hexCount = 14;
    const hexes = Array.from({ length: hexCount }).map(() => ({
      x: Math.random() * width,
      y: Math.random() * height,
      size: Math.random() * 28 + 14,
      rotation: Math.random() * Math.PI * 2,
      rotSpeed: (Math.random() - 0.5) * 0.008,
      vx: (Math.random() - 0.5) * 0.2,
      vy: (Math.random() - 0.5) * 0.2,
      alpha: Math.random() * 0.18 + 0.05,
    }));

    let time = 0;

    // Helper: Draw Hexagon
    const drawHexagon = (x: number, y: number, r: number, angle: number, alpha: number) => {
      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(angle);
      ctx.beginPath();
      for (let i = 0; i < 6; i++) {
        const a = (i * Math.PI) / 3;
        const hx = Math.cos(a) * r;
        const hy = Math.sin(a) * r;
        if (i === 0) ctx.moveTo(hx, hy);
        else ctx.lineTo(hx, hy);
      }
      ctx.closePath();
      ctx.strokeStyle = `rgba(139, 92, 246, ${alpha})`;
      ctx.lineWidth = 1;
      ctx.stroke();
      ctx.restore();
    };

    // ─── Render Loop ────────────────────────────────────────────────────────
    const render = () => {
      time += 0.012;

      // Mouse inertia interpolation
      mouse.x += (mouse.targetX - mouse.x) * 0.04;
      mouse.y += (mouse.targetY - mouse.y) * 0.04;

      const normMouseX = (mouse.x - width / 2) / (width / 2);
      const normMouseY = (mouse.y - height / 2) / (height / 2);

      // Deep obsidian void base
      ctx.fillStyle = '#05060A';
      ctx.fillRect(0, 0, width, height);

      // ── Layer 1: Radiant Radial Aurora Spotlights ──
      const aura1X = width * 0.35 + normMouseX * 90;
      const aura1Y = height * 0.4 + normMouseY * 70;
      const aura1 = ctx.createRadialGradient(aura1X, aura1Y, 10, aura1X, aura1Y, Math.max(width, height) * 0.55);
      aura1.addColorStop(0, 'rgba(139, 92, 246, 0.28)');
      aura1.addColorStop(0.4, 'rgba(99, 102, 241, 0.12)');
      aura1.addColorStop(0.8, 'rgba(236, 72, 153, 0.04)');
      aura1.addColorStop(1, 'transparent');
      ctx.fillStyle = aura1;
      ctx.fillRect(0, 0, width, height);

      const aura2X = width * 0.7 + Math.sin(time * 0.4) * 50;
      const aura2Y = height * 0.65 + Math.cos(time * 0.3) * 40;
      const aura2 = ctx.createRadialGradient(aura2X, aura2Y, 10, aura2X, aura2Y, Math.max(width, height) * 0.45);
      aura2.addColorStop(0, 'rgba(6, 182, 212, 0.2)');
      aura2.addColorStop(0.5, 'rgba(99, 102, 241, 0.08)');
      aura2.addColorStop(1, 'transparent');
      ctx.fillStyle = aura2;
      ctx.fillRect(0, 0, width, height);

      // ── Layer 2: Rotating Cyber Ring Gate (Center Backdrop) ──
      const gateX = width / 2 + normMouseX * 25;
      const gateY = height / 2 + normMouseY * 18;

      ctx.save();
      ctx.translate(gateX, gateY);

      // Outer Ring
      ctx.rotate(time * 0.2);
      ctx.beginPath();
      ctx.arc(0, 0, 240, 0, Math.PI * 2);
      ctx.strokeStyle = 'rgba(139, 92, 246, 0.12)';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([20, 15, 5, 15]);
      ctx.stroke();

      // Inner Ring
      ctx.rotate(-time * 0.35);
      ctx.beginPath();
      ctx.arc(0, 0, 180, 0, Math.PI * 2);
      ctx.strokeStyle = 'rgba(6, 182, 212, 0.16)';
      ctx.lineWidth = 1;
      ctx.setLineDash([40, 20]);
      ctx.stroke();

      ctx.restore();

      // ── Layer 3: Floating Cyber Hexagon Shapes ──
      hexes.forEach((h) => {
        h.x += h.vx;
        h.y += h.vy;
        h.rotation += h.rotSpeed;

        if (h.x < -40) h.x = width + 40;
        if (h.x > width + 40) h.x = -40;
        if (h.y < -40) h.y = height + 40;
        if (h.y > height + 40) h.y = -40;

        drawHexagon(h.x, h.y, h.size, h.rotation, h.alpha);
      });

      // ── Layer 4: Constellation Nodes & Glowing Rays ──
      nodes.forEach((n, i) => {
        n.x += n.vx + normMouseX * 0.15;
        n.y += n.vy + normMouseY * 0.15;
        n.pulse += 0.025;

        if (n.x < 0) n.x = width;
        if (n.x > width) n.x = 0;
        if (n.y < 0) n.y = height;
        if (n.y > height) n.y = 0;

        const currentRadius = n.radius + Math.sin(n.pulse) * 0.6;

        ctx.beginPath();
        ctx.arc(n.x, n.y, Math.max(0.5, currentRadius), 0, Math.PI * 2);
        ctx.fillStyle = `rgba(224, 231, 255, ${n.alpha})`;
        ctx.shadowBlur = 10;
        ctx.shadowColor = 'rgba(139, 92, 246, 0.7)';
        ctx.fill();
        ctx.shadowBlur = 0;

        // Connect distance lines between nearby nodes
        for (let j = i + 1; j < nodes.length; j++) {
          const n2 = nodes[j];
          const dx = n.x - n2.x;
          const dy = n.y - n2.y;
          const dist = Math.sqrt(dx * dx + dy * dy);

          if (dist < 140) {
            const alpha = (1 - dist / 140) * 0.15;
            ctx.beginPath();
            ctx.moveTo(n.x, n.y);
            ctx.lineTo(n2.x, n2.y);
            ctx.strokeStyle = `rgba(139, 92, 246, ${alpha})`;
            ctx.lineWidth = 0.8;
            ctx.stroke();
          }
        }
      });

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(animationFrameId);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      className={`fixed inset-0 w-full h-full pointer-events-none z-0 ${className}`}
    />
  );
}
