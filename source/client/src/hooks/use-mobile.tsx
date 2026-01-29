import * as React from "react"

const MOBILE_BREAKPOINT = 768

export function useIsMobile() {
  const [isMobile, setIsMobile] = React.useState<boolean | undefined>(undefined)

  React.useEffect(() => {
    const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`)
    const onChange = () => {
      setIsMobile(window.innerWidth < MOBILE_BREAKPOINT)
    }
    mql.addEventListener("change", onChange)
    setIsMobile(window.innerWidth < MOBILE_BREAKPOINT)
    return () => mql.removeEventListener("change", onChange)
  }, [])

  return !!isMobile
}

// Simple useLongPress hook
export function useLongPress(callback: () => void, ms = 500) {
  const [startLongPress, setStartLongPress] = React.useState(false);
  const timeout = React.useRef<NodeJS.Timeout>();

  React.useEffect(() => {
    if (startLongPress) {
      timeout.current = setTimeout(() => {
        callback();
        setStartLongPress(false);
      }, ms);
    } else {
      clearTimeout(timeout.current);
    }

    return () => clearTimeout(timeout.current);
  }, [callback, ms, startLongPress]);

  return {
    onMouseDown: () => setStartLongPress(true),
    onMouseUp: () => setStartLongPress(false),
    onMouseLeave: () => setStartLongPress(false),
    onTouchStart: () => setStartLongPress(true),
    onTouchEnd: () => setStartLongPress(false),
  };
}
