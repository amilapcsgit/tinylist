import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus } from 'lucide-react';
import { cn } from '@/lib/utils';

interface PullToAddProps {
  onAdd: (text: string) => void;
  color: string;
}

export function PullToAdd({ onAdd, color }: PullToAddProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [text, setText] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (text.trim()) {
      onAdd(text.trim());
      setText('');
      setIsOpen(false);
    }
  };

  return (
    <>
      {/* Pull indicator / Trigger area (simplified for web, just a button for now + animation) */}
      <div className="w-full flex justify-center py-2">
         <button 
           onClick={() => setIsOpen(!isOpen)}
           className="text-xs text-muted-foreground font-ui uppercase tracking-widest hover:text-white transition-colors flex items-center gap-1"
         >
            <Plus className="w-3 h-3" /> Add Item
         </button>
      </div>

      <AnimatePresence>
        {isOpen && (
          <motion.form
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            onSubmit={handleSubmit}
            className="overflow-hidden px-1"
          >
            <input
              autoFocus
              type="text"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="What needs to be done?"
              className="w-full bg-background/50 border border-white/10 rounded-md p-3 text-lg font-ui focus:outline-none focus:border-primary/50 placeholder:text-muted-foreground/50 text-white transition-all"
              style={{
                  boxShadow: `0 0 15px ${color}10` // subtle glow of list color
              }}
            />
          </motion.form>
        )}
      </AnimatePresence>
    </>
  );
}
