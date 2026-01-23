import React, { useRef, useState } from 'react';
import { motion, PanInfo, useMotionValue, useTransform } from 'framer-motion';
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
  const [isDragging, setIsDragging] = useState(false);

  // Transformations for swipe actions
  const deleteOpacity = useTransform(x, [-100, -50], [1, 0]);
  const editOpacity = useTransform(x, [50, 100], [0, 1]);

  const handleDragEnd = (_: any, info: PanInfo) => {
    setIsDragging(false);
    if (info.offset.x < -100) {
      // Swipe Left: Delete
      deleteItem(item.id);
    } else if (info.offset.x > 100) {
      // Swipe Right: Edit
      onEdit(item);
    }
  };

  const handleClick = () => {
      if (isDragging) return;
      if (isSelectionMode) {
          onToggleSelection(item.id);
      } else {
          toggleItem(item.id);
      }
  };

  // Explicit Long Press for Selection Mode
  const timeoutRef = useRef<NodeJS.Timeout>(undefined);

  const handleTouchStart = () => {
      timeoutRef.current = setTimeout(() => {
          if (!isSelectionMode) {
            onEnterSelectionMode(item.id);
            if (navigator.vibrate) navigator.vibrate(50);
          }
      }, 500);
  };

  const handleTouchEnd = () => {
      clearTimeout(timeoutRef.current);
  };

  return (
    <div className="relative w-full mb-1 group select-none">
      {/* Background Actions */}
      <div className="absolute inset-0 flex items-center justify-between px-4 overflow-hidden bg-background">
        <div className="flex items-center gap-2 text-blue-400 font-bold bg-blue-900/20 w-1/2 h-full justify-start pl-4">
             <motion.div style={{ opacity: editOpacity }}>
                <Edit2 className="w-5 h-5" />
             </motion.div>
        </div>
        <div className="flex items-center gap-2 text-red-500 font-bold bg-red-900/20 w-1/2 h-full justify-end pr-4">
             <motion.div style={{ opacity: deleteOpacity }}>
                <Trash2 className="w-5 h-5" />
             </motion.div>
        </div>
      </div>

      {/* Content */}
      <motion.div 
        style={{ x }}
        drag="x"
        dragConstraints={{ left: 0, right: 0 }}
        dragElastic={0.2}
        onDragStart={() => setIsDragging(true)}
        onDragEnd={handleDragEnd}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
        onMouseDown={handleTouchStart}
        onMouseUp={handleTouchEnd}
        onClick={handleClick}
        className={cn(
            "relative z-10 flex items-center gap-3 p-3 border-b border-white/5 bg-card backdrop-blur-sm transition-colors duration-200",
            isSelected && "bg-primary/10 border-primary/30" // Selection highlight
        )}
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
      </motion.div>
    </div>
  );
}
