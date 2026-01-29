import React, { useRef, useState } from 'react';
import { motion, PanInfo, useMotionValue, useTransform } from 'framer-motion';
import { TodoItem, useStore, COLOR_MAP as STORE_COLOR_MAP } from '@/lib/store';
import { Trash2, Edit2, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

const COLOR_MAP = STORE_COLOR_MAP;

interface TaskItemProps {
  item: TodoItem;
  color: string;
  isSelectionMode: boolean;
  isSelected: boolean;
  onToggleSelection: (id: string) => void;
  onEnterSelectionMode: (id: string) => void;
  onEdit: (item: TodoItem) => void;
  onDeleteRequest: (item: TodoItem) => void;
}

export function TaskItem({ 
    item, 
    color, 
    isSelectionMode, 
    isSelected, 
    onToggleSelection, 
    onEnterSelectionMode,
    onEdit,
    onDeleteRequest
}: TaskItemProps) {
  const toggleItem = useStore(state => state.toggleItem);
  const x = useMotionValue(0);
  const [isDragging, setIsDragging] = useState(false);

  // Transformations for swipe actions
  const deleteOpacity = useTransform(x, [-100, -50], [1, 0]);
  const editOpacity = useTransform(x, [50, 100], [0, 1]);

  const handleDragEnd = (_: any, info: PanInfo) => {
    setIsDragging(false);
    if (info.offset.x < -100) {
      // Swipe Left: Request Delete Confirmation
      onDeleteRequest(item);
    } else if (info.offset.x > 100) {
      // Swipe Right: Toggle Completion (remove checkbox style)
      toggleItem(item.id);
    }
  };

  const handleClick = () => {
      if (isDragging) return;
      // Always selection mode basically if we want tap to toggle selection
      onToggleSelection(item.id);
  };

  return (
    <div className="relative w-full mb-1 group select-none h-16">
      {/* Background Actions */}
      <div className="absolute inset-0 flex items-center justify-between px-4 overflow-hidden bg-background">
        <div className="flex items-center gap-2 text-green-400 font-bold bg-green-900/20 w-1/2 h-full justify-start pl-4">
             <motion.div style={{ opacity: editOpacity }} className="flex items-center gap-2">
                <Check className="w-5 h-5" />
                <span className="text-xs uppercase font-display">Done</span>
             </motion.div>
        </div>
        <div className="flex items-center gap-2 text-red-500 font-bold bg-red-900/20 w-1/2 h-full justify-end pr-4">
             <motion.div style={{ opacity: deleteOpacity }} className="flex items-center gap-2">
                <span className="text-xs uppercase font-display">Delete</span>
                <Trash2 className="w-5 h-5" />
             </motion.div>
        </div>
      </div>

      {/* Content */}
      <motion.div 
        style={{ x, borderLeftColor: STORE_COLOR_MAP[item.color] || color }}
        drag="x"
        dragConstraints={{ left: 0, right: 0 }}
        dragElastic={0.2}
        onDragStart={() => setIsDragging(true)}
        onDragEnd={handleDragEnd}
        onClick={handleClick}
        className={cn(
            "relative z-10 flex items-center gap-3 p-4 h-full border-l-8 backdrop-blur-sm transition-colors duration-200 cursor-pointer",
            isSelected ? "bg-black/40" : "bg-card/90"
        )}
      >
        {/* Selection Overlay (Dark transparent when selected) */}
        {isSelected && (
          <div className="absolute inset-0 bg-black/40 z-0" />
        )}

        <div className="relative z-10 flex-1 flex items-center justify-between">
            <span 
                className={cn(
                    "font-ui text-lg leading-tight transition-all duration-300 select-none break-words",
                    item.isDone ? "text-muted-foreground line-through opacity-50" : "text-foreground"
                )}
            >
                {item.text}
            </span>
            
            {item.isDone && <Check className="w-5 h-5 text-green-500 opacity-50" />}
        </div>
      </motion.div>
    </div>
  );
}
