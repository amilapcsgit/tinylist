import React, { useState } from 'react';
import { motion, PanInfo, useMotionValue, useTransform } from 'framer-motion';
import { TodoList, useStore, COLOR_MAP as STORE_COLOR_MAP } from '@/lib/store';
import { Trash2, Edit2, GripVertical, Check } from 'lucide-react';
import { useLocation } from 'wouter';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from '@/lib/utils';

const COLOR_MAP = STORE_COLOR_MAP;

interface ListCardProps {
  list: TodoList;
  itemCount: number;
  completedCount: number;
  dragControls?: any; // For reorder handle
}

export function ListCard({ list, itemCount, completedCount, dragControls }: ListCardProps) {
  const [, setLocation] = useLocation();
  const deleteList = useStore(state => state.deleteList);
  const updateList = useStore(state => state.updateList);
  
  // Swipe Logic
  const x = useMotionValue(0);
  const cardHeight = useMotionValue('auto'); 
  const [isDragging, setIsDragging] = useState(false);

  // Transformations for swipe actions
  // Reveal Delete (Red) on Left Swipe (x < 0)
  const deleteOpacity = useTransform(x, [-100, -50], [1, 0]);
  
  // Reveal Edit (Blue) on Right Swipe (x > 0)
  const editOpacity = useTransform(x, [50, 100], [0, 1]);

  // Edit State
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(list.title);
  const [editColor, setEditColor] = useState(list.color);

  const handleDragEnd = (_: any, info: PanInfo) => {
    setIsDragging(false);
    if (info.offset.x < -100) {
      // Swipe Left Threshold: Delete
      deleteList(list.id);
    } else if (info.offset.x > 100) {
       // Swipe Right Threshold: Edit
       setIsEditing(true);
    }
  };

  const listColor = COLOR_MAP[list.color];

  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editTitle.trim()) {
      updateList(list.id, { title: editTitle.trim(), color: editColor });
      setIsEditing(false);
    }
  };

  return (
    <>
    <div className="relative w-full mb-3 h-16 group select-none">
      {/* Background Actions Layer - Strictly behind */}
      <div className="absolute inset-0 flex items-center justify-between rounded-lg overflow-hidden z-0">
        {/* Left Side (Revealed on Right Swipe -> Edit) */}
        <div className="flex items-center gap-2 pl-4 w-1/2 bg-blue-900/50 h-full justify-start">
           <motion.div style={{ opacity: editOpacity }} className="flex items-center gap-2 text-blue-400 font-bold">
              <Edit2 className="w-6 h-6" />
              <span className="font-display tracking-wider text-sm">EDIT</span>
           </motion.div>
        </div>

        {/* Right Side (Revealed on Left Swipe -> Delete) */}
        <div className="flex items-center gap-2 pr-4 w-1/2 bg-red-900/50 h-full justify-end">
           <motion.div style={{ opacity: deleteOpacity }} className="flex items-center gap-2 text-red-500 font-bold">
              <span className="font-display tracking-wider text-sm">DELETE</span>
              <Trash2 className="w-6 h-6" />
           </motion.div>
        </div>
      </div>

      {/* Foreground Card */}
      <motion.div
        style={{ x, height: cardHeight }}
        drag="x"
        dragConstraints={{ left: 0, right: 0 }}
        dragElastic={0.2} // Stiffer resistance
        onDragStart={() => setIsDragging(true)}
        onDragEnd={handleDragEnd}
        onClick={() => !isDragging && setLocation(`/list/${list.id}`)}
        className="relative z-10 w-full h-full flex items-center justify-between shadow-lg overflow-hidden cursor-pointer bg-background rounded-lg border-l-8"
        // Ensure background is opaque to hide actions
        animate={{ x: 0 }} // Snap back on release if not deleted
      >
         {/* Colored Bar + Content */}
         <div 
            className="absolute inset-0 w-full h-full"
            style={{ 
                backgroundColor: `${listColor}`,
                opacity: 0.15 
            }} 
         />
         
         {/* Border Accent */}
         <div className="absolute left-0 top-0 bottom-0 w-2" style={{ backgroundColor: listColor }} />

         <div className="relative flex items-center justify-between w-full px-4 z-20">
            <div className="flex items-center gap-3 flex-1 min-w-0">
              {/* Reorder Handle - Pass explicit control if needed, or just visual */}
              {/* <div onPointerDown={(e) => dragControls?.start(e)} className="cursor-grab active:cursor-grabbing touch-none">
                <GripVertical className="w-5 h-5 text-muted-foreground/30 flex-shrink-0" />
              </div> */}
              <h3 className="font-display text-lg font-bold text-white tracking-wide truncate">
                {list.title}
              </h3>
            </div>

            <div className="flex items-center gap-3 shrink-0">
                <span className="font-ui text-xl font-bold tabular-nums tracking-widest" style={{ color: listColor }}>
                  {completedCount}<span className="text-muted-foreground/50 text-base mx-0.5">/</span>{itemCount}
                </span>
            </div>
         </div>
      </motion.div>
    </div>

    {/* Edit Dialog */}
    <Dialog open={isEditing} onOpenChange={setIsEditing}>
      <DialogContent className="bg-card border-white/10 text-white w-[90%] rounded-xl">
        <DialogHeader>
          <DialogTitle className="font-display text-primary">Edit List</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleEditSubmit} className="flex flex-col gap-6 mt-2">
           <div className="space-y-2">
             <label className="text-xs font-ui text-muted-foreground uppercase">Title</label>
             <input 
                autoFocus
                value={editTitle}
                onChange={(e) => setEditTitle(e.target.value)}
                className="w-full bg-black/20 border border-white/10 rounded p-3 font-display text-lg text-white focus:outline-none focus:border-primary"
             />
           </div>
           
           <div className="space-y-2">
             <label className="text-xs font-ui text-muted-foreground uppercase">Color</label>
             <div className="flex flex-wrap gap-3">
               {Object.entries(COLOR_MAP).map(([name, colorValue]) => (
                  <button
                    key={name}
                    type="button"
                    onClick={() => setEditColor(name as any)}
                    className={cn(
                      "w-8 h-8 rounded-full border-2 transition-all",
                      editColor === name ? "border-white scale-110 shadow-[0_0_10px_currentColor]" : "border-transparent opacity-50"
                    )}
                    style={{ backgroundColor: colorValue, color: colorValue }}
                  />
               ))}
             </div>
           </div>

           <div className="flex justify-end gap-2 mt-2">
             <button type="button" onClick={() => setIsEditing(false)} className="px-4 py-2 text-sm text-muted-foreground font-ui font-bold">CANCEL</button>
             <button type="submit" className="px-6 py-2 bg-primary text-black font-bold rounded font-display tracking-wider hover:bg-primary/80">SAVE</button>
           </div>
        </form>
      </DialogContent>
    </Dialog>
    </>
  );
}
