import { useEffect } from 'react';
import Lenis from 'lenis';

let lenisInstance: Lenis | null = null;

/**
 * Initializes Lenis smooth scrolling globally.
 * Call once in a top-level component (App.tsx or index.tsx).
 */
export function useLenis() {
  useEffect(() => {
    const lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
    });

    lenisInstance = lenis;

    let raf: number;
    function animate(time: number) {
      lenis.raf(time);
      raf = requestAnimationFrame(animate);
    }
    raf = requestAnimationFrame(animate);

    return () => {
      cancelAnimationFrame(raf);
      lenis.destroy();
      lenisInstance = null;
    };
  }, []);
}

export { lenisInstance };
