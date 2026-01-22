import React from 'react';
import { motion, PanInfo, useMotionValue, useTransform } from 'framer-motion';
import { TodoList, useStore, COLOR_MAP as STORE_COLOR_MAP } from '@/lib/store';
import { Trash2, Edit2, GripVertical } from 'lucide-react';
import { useLocation } from 'wouter';

// Fix: Use COLOR_MAP from store directly
const COLOR_MAP = STORE_COLOR_MAP;

interface ListCardProps {
  list: TodoList;
  itemCount: number;
  completedCount: number;
}

export function ListCard({ list, itemCount, completedCount }: ListCardProps) {
  const [, setLocation] = useLocation();
  const deleteList = useStore(state => state.deleteList);
  
  // Swipe Logic
  const x = useMotionValue(0);
  const cardOpacity = useTransform(x, [-200, -150, 0, 150, 200], [0, 1, 1, 1, 0]);
  const cardHeight = useMotionValue('auto'); 

  const handleDragEnd = (_: any, info: PanInfo) => {
    if (info.offset.x < -100) {
      deleteList(list.id);
    } 
  };

  const listColor = COLOR_MAP[list.color];

  return (
    <motion.div
      layout
      style={{ x, opacity: cardOpacity, height: cardHeight }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={0.5}
      onDragEnd={handleDragEnd}
      className="relative w-full mb-3 group"
      whileTap={{ scale: 0.99 }}
    >
      {/* Background Actions Layer */}
      <div className="absolute inset-0 flex items-center justify-between px-4 rounded-lg overflow-hidden">
        <div className="flex items-center gap-2 text-neon-blue font-bold opacity-0">
          <Edit2 className="w-5 h-5" />
        </div>
        <div className="flex items-center gap-2 text-neon-red font-bold">
          <span className="font-display text-sm tracking-wider">DELETE</span>
          <Trash2 className="w-5 h-5" />
        </div>
      </div>

      {/* Card Content */}
      <div 
        onClick={() => setLocation(`/list/${list.id}`)}
        className="relative z-10 bg-card border border-white/5 rounded-lg p-4 flex items-center justify-between h-24 shadow-lg backdrop-blur-sm overflow-hidden cursor-pointer"
        style={{
            boxShadow: `inset 6px 0 0 0 ${listColor}`
        }}
      >
        {/* Glow effect on hover */}
        <div 
            className="absolute inset-0 opacity-0 group-hover:opacity-5 transition-opacity duration-300 pointer-events-none"
            style={{ backgroundColor: listColor }}
        />

        <div className="flex flex-col gap-1 ml-3 flex-1 min-w-0">
          <h3 className="font-display text-xl font-bold text-white tracking-wide truncate">
            {list.title}
          </h3>
          <span className="text-xs text-muted-foreground font-ui tracking-wider uppercase opacity-60">
            {new Date(list.createdAt).toLocaleDateString()}
          </span>
        </div>

        <div className="flex items-center gap-4 pl-4 shrink-0">
          <div className="flex flex-col items-end">
             <span className="font-ui text-3xl font-bold tabular-nums tracking-widest leading-none" style={{ color: listColor, textShadow: `0 0 10px ${listColor}40` }}>
               {completedCount}<span className="text-white/20 text-xl mx-1">/</span>{itemCount}
             </span>
             {/* Progress bar */}
             <div className="w-20 h-1.5 bg-black/40 rounded-full mt-2 overflow-hidden border border-white/5">
                <div 
                    className="h-full rounded-full transition-all duration-500 relative"
                    style={{ 
                        width: `${itemCount === 0 ? 0 : (completedCount / itemCount) * 100}%`,
                        backgroundColor: listColor,
                        boxShadow: `0 0 8px ${listColor}`
                    }}
                />
             </div>
          </div>
          <GripVertical className="w-6 h-6 text-white/10" />
        </div>
      </div>
    </motion.div>
  );
}
