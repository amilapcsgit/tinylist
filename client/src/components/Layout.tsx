import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { cn } from '@/lib/utils';
import { Link, useLocation } from 'wouter';
import { Settings, ArrowLeft, Search as SearchIcon } from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
  title?: string;
  showBack?: boolean;
  actions?: React.ReactNode;
}

export function Layout({ children, title, showBack, actions }: LayoutProps) {
  const [location] = useLocation();

  return (
    <div className="min-h-screen bg-background text-foreground overflow-hidden flex justify-center">
      <div className="w-full max-w-md h-screen flex flex-col relative glass-panel sm:border-x border-white/5">
        
        {/* Header */}
        <header className="h-16 shrink-0 flex items-center justify-between px-4 border-b border-white/10 bg-background/50 backdrop-blur-xl z-50">
          <div className="flex items-center gap-3">
            {showBack && (
              <Link href="/">
                <button className="p-2 hover:bg-white/10 rounded-full transition-colors active:scale-95">
                  <ArrowLeft className="w-6 h-6 text-primary" />
                </button>
              </Link>
            )}
            <h1 className="font-display text-xl tracking-wider text-primary text-glow font-bold uppercase truncate max-w-[200px]">
              {title || 'NeonList'}
            </h1>
          </div>
          
          <div className="flex items-center gap-1">
            {actions}
            {!showBack && (
               <Link href="/search">
                 <button className="p-2 hover:bg-white/10 rounded-full transition-colors">
                   <SearchIcon className="w-5 h-5 text-muted-foreground hover:text-primary transition-colors" />
                 </button>
               </Link>
            )}
            <Link href="/settings">
              <button className="p-2 hover:bg-white/10 rounded-full transition-colors">
                <Settings className="w-5 h-5 text-muted-foreground hover:text-primary transition-colors" />
              </button>
            </Link>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto overflow-x-hidden relative scrollbar-hide">
          <AnimatePresence mode="wait">
            <motion.div
              key={location}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
              transition={{ duration: 0.2 }}
              className="h-full pb-20" // padding for FAB or bottom actions
            >
              {children}
            </motion.div>
          </AnimatePresence>
        </main>
        
      </div>
    </div>
  );
}
