import React, { useRef } from 'react';
import { motion, PanInfo, useMotionValue } from 'framer-motion';
import { TodoItem, useStore } from '@/lib/store';
import { Trash2, Edit2, Copy, Check } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useLongPress } from "@/hooks/use-mobile";

interface TaskItemProps {
  item: TodoItem;
  color: string;
  isSelectionMode: boolean;
  isSelected: boolean;
  onToggleSelection: (id: string) => void;
  onEnterSelectionMode: (id: string) => void;
  onEdit: (item: TodoItem) => void;
}

export function TaskItem({ 
    item, 
    color, 
    isSelectionMode, 
    isSelected, 
    onToggleSelection, 
    onEnterSelectionMode,
    onEdit
}: TaskItemProps) {
  const toggleItem = useStore(state => state.toggleItem);
  const deleteItem = useStore(state => state.deleteItem);
  const duplicateItem = useStore(state => state.duplicateItem);
  const x = useMotionValue(0);

  const handleDragEnd = (_: any, info: PanInfo) => {
    if (info.offset.x < -80) {
      // Swipe Left: Delete
      deleteItem(item.id);
    } else if (info.offset.x > 80) {
      if (info.offset.y > 30) {
          // Swipe Right + Down (approx): Duplicate
          duplicateItem(item.id);
      } else {
          // Swipe Right: Edit
          onEdit(item);
      }
    }
  };

  const handleClick = () => {
      if (isSelectionMode) {
          onToggleSelection(item.id);
      } else {
          toggleItem(item.id);
      }
  };

  // Simple long press detection
  const timeoutRef = useRef<NodeJS.Timeout>(undefined);

  const handleTouchStart = () => {
      timeoutRef.current = setTimeout(() => {
          if (!isSelectionMode) {
            onEnterSelectionMode(item.id);
            // Vibrate if available
            if (navigator.vibrate) navigator.vibrate(50);
          }
      }, 500);
  };

  const handleTouchEnd = () => {
      clearTimeout(timeoutRef.current);
  };

  return (
    <motion.div
      layout
      style={{ x }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      onDragEnd={handleDragEnd}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
      onMouseDown={handleTouchStart} // For mouse users testing
      onMouseUp={handleTouchEnd}
      className="relative w-full mb-1 group touch-pan-y"
    >
      {/* Background Actions */}
      <div className="absolute inset-0 flex items-center justify-between px-4 rounded-md overflow-hidden bg-background">
        <div className="flex items-center gap-2 text-neon-blue font-bold">
             <Edit2 className="w-5 h-5" />
        </div>
        <div className="flex items-center gap-2 text-destructive font-bold">
             <Trash2 className="w-5 h-5" />
        </div>
      </div>

      {/* Content */}
      <motion.div 
        className={cn(
            "relative z-10 flex items-center gap-3 p-3 rounded-none border-b border-white/5 bg-card backdrop-blur-sm transition-colors duration-200",
            isSelected && "bg-white/10 border-primary/30" // Selection highlight
        )}
        onClick={handleClick}
      >
        {/* Selection / Checkbox */}
        <div 
            className={cn(
                "w-5 h-5 flex-shrink-0 rounded-sm border flex items-center justify-center transition-all duration-300",
                item.isDone ? "border-transparent" : "border-muted-foreground/40",
                isSelected ? "border-primary bg-primary/20" : ""
            )}
            style={{ 
                backgroundColor: item.isDone ? color : (isSelected ? `${color}20` : 'transparent'),
                borderColor: isSelected ? color : (item.isDone ? color : undefined),
                boxShadow: item.isDone ? `0 0 8px ${color}` : 'none'
            }}
        >
            {item.isDone && <Check className="w-3.5 h-3.5 text-black font-bold" />}
            {isSelected && !item.isDone && <div className="w-2 h-2 rounded-full" style={{ backgroundColor: color }} />}
        </div>

        {/* Text */}
        <span 
            className={cn(
                "flex-1 font-ui text-lg leading-tight transition-all duration-300 select-none break-words",
                item.isDone ? "text-muted-foreground line-through decoration-white/20" : "text-foreground"
            )}
        >
            {item.text}
        </span>

        {/* Drag Handle (visible in reorder mode, but let's just show it subtly) */}
        {/* <GripVertical className="w-4 h-4 text-muted-foreground/20" /> */}

      </motion.div>
    </motion.div>
  );
}
