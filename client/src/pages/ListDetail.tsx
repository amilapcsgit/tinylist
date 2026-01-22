import React, { useMemo, useState, useEffect } from 'react';
import { useRoute } from 'wouter';
import { Layout } from '@/components/Layout';
import { TaskItem } from '@/components/TaskItem';
import { PullToAdd } from '@/components/PullToAdd';
import { useStore, COLOR_MAP } from '@/lib/store';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { MoreVertical, Copy, Trash2, CheckSquare, Plus, ArrowUp, ArrowDown, Calculator, X, Check } from 'lucide-react';
import { useLocation } from 'wouter';
import { cn } from '@/lib/utils';
import { nanoid } from 'nanoid';

// Regex to find the last number in a string (integers, decimals, negatives)
// Matches "-10.5", "10", "1,000.50" (simple), actually simpler: just look for the last numeric token
const NUMERIC_REGEX = /(-?(?:\d+[.,])?\d+)(?=\D*$)/;

export default function ListDetail() {
  const [, params] = useRoute('/list/:id');
  const [, setLocation] = useLocation();
  const listId = params?.id;

  const lists = useStore(state => state.lists);
  const items = useStore(state => state.items);
  const addItem = useStore(state => state.addItem);
  const clearCompleted = useStore(state => state.clearCompleted);
  const deleteList = useStore(state => state.deleteList);
  const duplicateList = useStore(state => state.duplicateList);
  const updateItem = useStore(state => state.updateItem);

  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [includeDoneInSum, setIncludeDoneInSum] = useState(true);

  // Edit Item State
  const [editingItem, setEditingItem] = useState<{ id: string, text: string } | null>(null);

  const list = lists.find(l => l.id === listId);
  const listItems = useMemo(() => 
    items.filter(i => i.listId === listId).sort((a, b) => {
       // Sort by creation time (stable sort) as 'order' field is not on Item yet
       return a.createdAt - b.createdAt; 
    }), 
  [items, listId]);

  // Numeric Sum Calculation
  const calculation = useMemo(() => {
      let sum = 0;
      let count = 0;
      
      const targetItems = isSelectionMode 
        ? listItems.filter(i => selectedIds.has(i.id))
        : listItems;

      targetItems.forEach(item => {
          if (!includeDoneInSum && item.isDone && !isSelectionMode) return;
          
          const match = item.text.match(NUMERIC_REGEX);
          if (match) {
              // Replace comma with dot for parsing if needed
              const valStr = match[1].replace(',', '.');
              const val = parseFloat(valStr);
              if (!isNaN(val)) {
                  sum += val;
                  count++;
              }
          }
      });
      
      return { sum, count };
  }, [listItems, isSelectionMode, selectedIds, includeDoneInSum]);

  if (!list) return null;

  const listColor = COLOR_MAP[list.color];

  // Selection Handlers
  const toggleSelection = (id: string) => {
      const newSet = new Set(selectedIds);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      
      setSelectedIds(newSet);
      if (newSet.size === 0 && isSelectionMode) {
          // Optional: Exit selection mode if empty? No, keep it until explicit exit.
      }
  };

  const enterSelectionMode = (id: string) => {
      setIsSelectionMode(true);
      setSelectedIds(new Set([id]));
  };

  const handleEditItem = (e: React.FormEvent) => {
      e.preventDefault();
      if (editingItem && editingItem.text.trim()) {
          updateItem(editingItem.id, editingItem.text.trim());
          setEditingItem(null);
      }
  };

  return (
    <Layout 
      title={list.title} 
      showBack
      actions={
        <DropdownMenu>
            <DropdownMenuTrigger className="p-2 hover:bg-white/10 rounded-full transition-colors outline-none">
            <MoreVertical className="w-5 h-5 text-muted-foreground hover:text-primary transition-colors" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="bg-card border-white/10 text-foreground">
            <DropdownMenuItem onClick={() => setIsSelectionMode(!isSelectionMode)}>
                <CheckSquare className="w-4 h-4 mr-2" /> {isSelectionMode ? 'Exit Selection' : 'Select Items'}
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setIncludeDoneInSum(!includeDoneInSum)}>
                <Calculator className="w-4 h-4 mr-2" /> {includeDoneInSum ? 'Exclude Done from Sum' : 'Include Done in Sum'}
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => clearCompleted(list.id)}>
                <Trash2 className="w-4 h-4 mr-2" /> Clear Completed
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => duplicateList(list.id)}>
                <Copy className="w-4 h-4 mr-2" /> Duplicate List
            </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
      }
    >
        {/* Color Accent Bar */}
        <div className="h-1 w-full shadow-[0_0_15px_currentColor]" style={{ backgroundColor: listColor, color: listColor }} />

        <div className="pb-32"> {/* Extra padding for dual bottom bars */}
            <PullToAdd 
                onAdd={(text) => addItem(list.id, text, 'top')} 
                color={listColor}
            />

            <div className="mt-2 flex flex-col">
                {listItems.map(item => (
                    <TaskItem 
                        key={item.id} 
                        item={item} 
                        color={listColor}
                        isSelectionMode={isSelectionMode}
                        isSelected={selectedIds.has(item.id)}
                        onToggleSelection={toggleSelection}
                        onEnterSelectionMode={enterSelectionMode}
                        onEdit={(item) => setEditingItem({ id: item.id, text: item.text })}
                    />
                ))}
            </div>

            {listItems.length === 0 && (
                <div className="text-center py-20 opacity-30 font-display">
                    EMPTY LIST
                </div>
            )}
        </div>

        {/* BOTTOM TOOLBAR: Numeric Sum & Selection */}
        <div className="fixed bottom-0 left-0 right-0 z-50">
            
            {/* 1. Sum Toolbar (Always visible or conditional?) User says "Always show" */}
            <div className="bg-background/95 backdrop-blur-xl border-t border-white/10 px-4 py-3 flex items-center justify-between shadow-2xl">
                <div className="flex items-center gap-4">
                    <div className="flex flex-col">
                        <span className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold">
                            {isSelectionMode ? 'SELECTED SUM' : 'TOTAL SUM'}
                        </span>
                        <div className="flex items-baseline gap-1">
                            <span className="font-display text-2xl font-bold text-primary text-glow-sm">
                                {Number.isInteger(calculation.sum) ? calculation.sum : calculation.sum.toFixed(2)}
                            </span>
                            <span className="text-xs text-muted-foreground font-ui">
                                ({calculation.count} items)
                            </span>
                        </div>
                    </div>
                </div>

                {isSelectionMode ? (
                    <div className="flex gap-2">
                        <button 
                            onClick={() => setSelectedIds(new Set())}
                            className="p-2 rounded-full hover:bg-white/10 text-muted-foreground"
                        >
                            <X className="w-5 h-5" />
                        </button>
                        <button 
                            onClick={() => setIsSelectionMode(false)}
                            className="px-4 py-2 bg-primary/20 text-primary rounded-full font-bold text-xs"
                        >
                            DONE
                        </button>
                    </div>
                ) : (
                    <button 
                        onClick={() => setIsSelectionMode(true)}
                        className="text-xs font-bold text-muted-foreground hover:text-white border border-white/10 px-3 py-1.5 rounded-full uppercase"
                    >
                        Select
                    </button>
                )}
            </div>

            {/* 2. Add Item Input (Only when NOT in selection mode) */}
            {!isSelectionMode && (
                <div className="p-4 bg-background/50 backdrop-blur-sm border-t border-white/5 flex justify-center">
                    <div className="w-full max-w-md flex gap-2">
                        <input 
                            type="text" 
                            placeholder="Add item..."
                            className="flex-1 bg-card border border-white/10 rounded-full px-4 py-3 focus:outline-none focus:border-primary/50 text-white font-ui shadow-lg"
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    const target = e.target as HTMLInputElement;
                                    if (target.value.trim()) {
                                        addItem(list.id, target.value.trim());
                                        target.value = '';
                                    }
                                }
                            }}
                        />
                    </div>
                </div>
            )}
        </div>

        {/* Edit Item Dialog */}
        <Dialog open={!!editingItem} onOpenChange={(open) => !open && setEditingItem(null)}>
            <DialogContent className="bg-card border-white/10 text-white top-[20%] translate-y-0">
                <DialogHeader>
                    <DialogTitle>Edit Item</DialogTitle>
                </DialogHeader>
                <form onSubmit={handleEditItem} className="flex flex-col gap-4 mt-2">
                    <input 
                        autoFocus
                        value={editingItem?.text || ''}
                        onChange={(e) => setEditingItem(prev => prev ? { ...prev, text: e.target.value } : null)}
                        className="bg-black/20 border border-white/10 rounded p-3 font-ui text-lg text-white focus:outline-none focus:border-primary"
                    />
                    <div className="flex justify-end gap-2">
                         <button type="submit" className="px-4 py-2 bg-primary text-black font-bold rounded">Save</button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>

    </Layout>
  );
}
