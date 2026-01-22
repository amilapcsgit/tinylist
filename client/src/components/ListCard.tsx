import React, { useState } from 'react';
import { motion, PanInfo, useMotionValue, useTransform } from 'framer-motion';
import { TodoList, useStore, COLOR_MAP as STORE_COLOR_MAP } from '@/lib/store';
import { Trash2, Edit2, GripVertical, Copy, X } from 'lucide-react';
import { useLocation } from 'wouter';
import { cn } from '@/lib/utils';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

const COLOR_MAP = STORE_COLOR_MAP;

interface ListCardProps {
  list: TodoList;
  itemCount: number;
  completedCount: number;
}

export function ListCard({ list, itemCount, completedCount }: ListCardProps) {
  const [, setLocation] = useLocation();
  const deleteList = useStore(state => state.deleteList);
  const duplicateList = useStore(state => state.duplicateList);
  const updateList = useStore(state => state.updateList);
  
  // Swipe Logic
  const x = useMotionValue(0);
  const cardOpacity = useTransform(x, [-200, -150, 0, 150, 200], [0, 1, 1, 1, 0]);
  const cardHeight = useMotionValue('auto'); 

  // Edit State
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(list.title);

  const handleDragEnd = (_: any, info: PanInfo) => {
    if (info.offset.x < -100) {
      // Swipe Left: Delete
      deleteList(list.id);
    } else if (info.offset.x > 100) {
       // Swipe Right: Edit
       setIsEditing(true);
    }
  };

  const listColor = COLOR_MAP[list.color];

  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editTitle.trim()) {
      updateList(list.id, { title: editTitle.trim() });
      setIsEditing(false);
    }
  };

  return (
    <>
    <motion.div
      layout
      style={{ x, opacity: cardOpacity, height: cardHeight }}
      drag="x"
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={0.5}
      onDragEnd={handleDragEnd}
      className="relative w-full mb-2 group"
      whileTap={{ scale: 0.995 }}
    >
      {/* Background Actions Layer */}
      <div className="absolute inset-0 flex items-center justify-between px-4 rounded overflow-hidden">
        <div className="flex items-center gap-2 text-neon-blue font-bold">
          <Edit2 className="w-6 h-6" />
          <span className="font-display tracking-wider">EDIT</span>
        </div>
        <div className="flex items-center gap-2 text-neon-red font-bold">
          <span className="font-display tracking-wider">DELETE</span>
          <Trash2 className="w-6 h-6" />
        </div>
      </div>

      {/* Card Content - Full Width Style */}
      <div 
        onClick={() => setLocation(`/list/${list.id}`)}
        className="relative z-10 w-full h-16 flex items-center justify-between px-4 shadow-md overflow-hidden cursor-pointer bg-background"
        style={{
            // Full width colored bar effect
            borderLeft: `8px solid ${listColor}`,
            borderRight: `2px solid ${listColor}40`,
            backgroundColor: `${listColor}10` // Subtle tint
        }}
      >
        <div className="flex items-center gap-3 flex-1 min-w-0">
          <GripVertical className="w-5 h-5 text-muted-foreground/30 flex-shrink-0" />
          <h3 className="font-display text-lg font-bold text-white tracking-wide truncate">
            {list.title}
          </h3>
        </div>

        <div className="flex items-center gap-3 shrink-0">
             <span className="font-ui text-2xl font-bold tabular-nums tracking-widest" style={{ color: listColor }}>
               {completedCount}<span className="text-white/20 text-lg mx-0.5">/</span>{itemCount}
             </span>
        </div>
      </div>
    </motion.div>

    {/* Edit Dialog */}
    <Dialog open={isEditing} onOpenChange={setIsEditing}>
      <DialogContent className="bg-card border-white/10 text-white">
        <DialogHeader>
          <DialogTitle className="font-display text-primary">Edit List</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleEditSubmit} className="flex flex-col gap-4 mt-2">
           <input 
              autoFocus
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
              className="bg-black/20 border border-white/10 rounded p-3 font-display text-lg text-white focus:outline-none focus:border-primary"
           />
           {/* Color picker could go here */}
           <div className="flex justify-end gap-2">
             <button type="button" onClick={() => setIsEditing(false)} className="px-4 py-2 text-sm text-muted-foreground">CANCEL</button>
             <button type="submit" className="px-4 py-2 bg-primary text-black font-bold rounded hover:bg-primary/80">SAVE</button>
           </div>
        </form>
      </DialogContent>
    </Dialog>
    </>
  );
}
