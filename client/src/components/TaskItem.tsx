import React from 'react';
import { motion, PanInfo, useMotionValue } from 'framer-motion';
import { TodoItem, useStore } from '@/lib/store';
import { Trash2, Check, X } from 'lucide-react';
import { cn } from '@/lib/utils';

interface TaskItemProps {
  item: TodoItem;
  color: string;
}

export function TaskItem({ item, color }: TaskItemProps) {
  const toggleItem = useStore(state => state.toggleItem);
  const deleteItem = useStore(state => state.deleteItem);
  const x = useMotionValue(0);

  const handleDragEnd = (_: any, info: PanInfo) => {
    if (info.offset.x < -80) {
      deleteItem(item.id);
    } 
  };

  return (
    <motion.div
      layout
      style={{ x }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      onDragEnd={handleDragEnd}
      className="relative w-full mb-2 group touch-pan-y"
    >
      {/* Background Actions */}
      <div className="absolute inset-0 flex items-center justify-end px-4 rounded-md bg-destructive/20 border border-destructive/30">
        <Trash2 className="w-5 h-5 text-destructive" />
      </div>

      {/* Content */}
      <motion.div 
        className={cn(
            "relative z-10 flex items-center gap-3 p-4 rounded-md border backdrop-blur-sm transition-colors duration-200",
            item.isDone ? "bg-card/50 border-white/5" : "bg-card border-white/10 hover:border-white/20"
        )}
        onClick={() => toggleItem(item.id)}
      >
        {/* Checkbox */}
        <div 
            className={cn(
                "w-6 h-6 rounded border-2 flex items-center justify-center transition-all duration-300",
                item.isDone ? "border-transparent" : "border-muted-foreground/50"
            )}
            style={{ 
                backgroundColor: item.isDone ? color : 'transparent',
                borderColor: item.isDone ? color : undefined,
                boxShadow: item.isDone ? `0 0 10px ${color}` : 'none'
            }}
        >
            {item.isDone && <Check className="w-4 h-4 text-black font-bold" />}
        </div>

        {/* Text */}
        <span 
            className={cn(
                "flex-1 font-ui text-lg transition-all duration-300 select-none break-all",
                item.isDone ? "text-muted-foreground line-through decoration-white/20 decoration-2" : "text-foreground"
            )}
        >
            {item.text}
        </span>

      </motion.div>
    </motion.div>
  );
}
