import React, { useMemo, useState } from 'react';
import { useRoute } from 'wouter';
import { motion } from 'framer-motion';
import { Layout } from '@/components/Layout';
import { TaskItem } from '@/components/TaskItem';
import { useStore, COLOR_MAP, LIST_COLORS, ListColor, TodoItem } from '@/lib/store';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { MoreVertical, Copy, Trash2, CheckSquare, Plus, Calculator, X } from 'lucide-react';
import { useLocation } from 'wouter';
import { cn } from '@/lib/utils';

const NUMERIC_REGEX = /(-?(?:\d+[.,])?\d+)(?=\D*$)/;

export default function ListDetail() {
  const [, params] = useRoute('/list/:id');
  const [, setLocation] = useLocation();
  const listId = params?.id;

  const lists = useStore(state => state.lists);
  const items = useStore(state => state.items);
  const addItem = useStore(state => state.addItem);
  const deleteItem = useStore(state => state.deleteItem);
  const clearCompleted = useStore(state => state.clearCompleted);
  const duplicateList = useStore(state => state.duplicateList);
  const updateItem = useStore(state => state.updateItem);

  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [isDeletingItem, setIsDeletingItem] = useState<TodoItem | null>(null);
  const [isAddingItem, setIsAddingItem] = useState(false);
  const [newItemText, setNewItemText] = useState('');
  const [newItemColor, setNewItemColor] = useState<ListColor>('green');

  const list = lists.find(l => l.id === listId);
  const listItems = useMemo(() => 
    items.filter(i => i.listId === listId).sort((a, b) => a.createdAt - b.createdAt), 
  [items, listId]);

  const calculation = useMemo(() => {
      let sum = 0;
      let count = 0;
      const isSelectionMode = selectedIds.size > 0;
      const targetItems = isSelectionMode 
        ? listItems.filter(i => selectedIds.has(i.id))
        : listItems;

      targetItems.forEach(item => {
          const match = item.text.match(NUMERIC_REGEX);
          if (match) {
              const valStr = match[1].replace(',', '.');
              const val = parseFloat(valStr);
              if (!isNaN(val)) {
                  sum += val;
                  count++;
              }
          }
      });
      return { sum, count, isSelectionMode };
  }, [listItems, selectedIds]);

  const toggleSelection = (id: string) => {
      const newSet = new Set(selectedIds);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      setSelectedIds(newSet);
  };

  const handleAddItem = (e: React.FormEvent) => {
      e.preventDefault();
      if (newItemText.trim()) {
          addItem(list.id, newItemText.trim(), newItemColor);
          setNewItemText('');
          setIsAddingItem(false);
      }
  };

  const confirmDelete = () => {
      if (isDeletingItem) {
          deleteItem(isDeletingItem.id);
          setIsDeletingItem(null);
      }
  };

  if (!list) return null;
  const listColor = COLOR_MAP[list.color];

  const enterSelectionMode = (id: string) => {
      setSelectedIds(new Set([id]));
  };

  const longPressProps = useLongPress(() => {
    setIsAddingItem(true);
    if (navigator.vibrate) navigator.vibrate(50);
  });

  const handleEditItem = (e: React.FormEvent) => {
      e.preventDefault();
      if (editingItem && editingItem.text.trim()) {
          updateItem(editingItem.id, { text: editingItem.text.trim() });
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
            <DropdownMenuItem onClick={() => setSelectedIds(new Set())}>
                <X className="w-4 h-4 mr-2" /> Clear Selection
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
        <div className="h-1 w-full shadow-[0_0_15px_currentColor]" style={{ backgroundColor: listColor, color: listColor }} />

        <div 
          className="pb-32 min-h-[calc(100vh-64px)]"
          {...longPressProps}
        >
            <div className="mt-2 flex flex-col">
                {listItems.map(item => (
                    <TaskItem 
                        key={item.id} 
                        item={item} 
                        color={listColor}
                        isSelectionMode={selectedIds.size > 0}
                        isSelected={selectedIds.has(item.id)}
                        onToggleSelection={toggleSelection}
                        onEnterSelectionMode={toggleSelection}
                        onEdit={() => {}} 
                        onDeleteRequest={(item) => setIsDeletingItem(item)}
                    />
                ))}
            </div>

            {listItems.length === 0 && (
                <div className="text-center py-20 opacity-30 font-display">EMPTY LIST</div>
            )}
        </div>

        {/* BOTTOM TOOLBAR: Numeric Sum */}
        <div className="fixed bottom-0 left-0 right-0 z-50">
            <div className="bg-background/95 backdrop-blur-xl border-t border-white/10 px-4 py-3 flex items-center justify-between shadow-2xl">
                <div className="flex flex-col">
                    <span className="text-[10px] text-muted-foreground uppercase tracking-wider font-bold">
                        {calculation.isSelectionMode ? 'SELECTED SUM' : 'TOTAL SUM'}
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
                
                {calculation.isSelectionMode && (
                  <button 
                    onClick={() => setSelectedIds(new Set())}
                    className="p-2 rounded-full hover:bg-white/10 text-muted-foreground"
                  >
                    <X className="w-5 h-5" />
                  </button>
                )}
            </div>
        </div>

        {/* FAB for Add Item */}
        <motion.button
            whileTap={{ scale: 0.9 }}
            onClick={() => setIsAddingItem(true)}
            className="fixed bottom-24 right-6 w-14 h-14 bg-primary text-black rounded-full shadow-lg flex items-center justify-center z-50"
        >
            <Plus className="w-8 h-8" />
        </motion.button>

        {/* Add Item Dialog */}
        <Dialog open={isAddingItem} onOpenChange={setIsAddingItem}>
            <DialogContent className="bg-card border-white/10 text-white w-full max-w-md rounded-none p-0 overflow-hidden">
                <div 
                  className="w-full h-16 transition-colors duration-300"
                  style={{ backgroundColor: COLOR_MAP[newItemColor] }}
                >
                  <div className="h-full flex items-center px-6 justify-between">
                     <span className="text-white font-display text-lg tracking-widest uppercase opacity-80">New Item</span>
                     <X className="w-6 h-6 cursor-pointer" onClick={() => setIsAddingItem(false)} />
                  </div>
                </div>

                <div className="flex flex-col">
                    {/* 10 Colors Grid */}
                    <div className="grid grid-cols-5 gap-0">
                        {LIST_COLORS.map(color => (
                            <button
                                key={color}
                                type="button"
                                onClick={() => setNewItemColor(color)}
                                className={cn(
                                    "aspect-square transition-all duration-200 relative",
                                    newItemColor === color ? "z-10" : "hover:opacity-80"
                                )}
                                style={{ backgroundColor: COLOR_MAP[color] }}
                            >
                                {newItemColor === color && (
                                    <div className="absolute inset-0 border-4 border-white z-20" />
                                )}
                            </button>
                        ))}
                    </div>

                    <form onSubmit={handleAddItem} className="p-6 bg-background">
                        <input 
                            autoFocus
                            placeholder="What needs to be done?"
                            value={newItemText}
                            onChange={(e) => setNewItemText(e.target.value)}
                            className="w-full bg-black/40 border border-white/10 p-4 font-ui text-xl text-white focus:outline-none focus:border-primary"
                        />
                        
                        <div className="flex justify-end gap-3 mt-8">
                             <button 
                                type="button"
                                onClick={() => setIsAddingItem(false)}
                                className="px-6 py-2 text-sm text-muted-foreground font-bold"
                             >
                                CANCEL
                             </button>
                             <button 
                                type="submit" 
                                className="px-10 py-3 bg-primary text-black font-bold font-display tracking-wider"
                             >
                                ADD ITEM
                             </button>
                        </div>
                    </form>
                </div>
            </DialogContent>
        </Dialog>

        {/* Delete Confirmation */}
        <Dialog open={!!isDeletingItem} onOpenChange={() => setIsDeletingItem(null)}>
            <DialogContent className="bg-card border-white/10 text-white w-[80%] rounded-xl">
                <DialogHeader>
                    <DialogTitle>Delete Item?</DialogTitle>
                </DialogHeader>
                <div className="py-4 text-muted-foreground font-ui">
                    "{isDeletingItem?.text}" will be permanently removed.
                </div>
                <DialogFooter className="flex gap-2">
                    <button onClick={() => setIsDeletingItem(null)} className="px-4 py-2 text-sm">CANCEL</button>
                    <button onClick={confirmDelete} className="px-4 py-2 bg-red-600 text-white rounded font-bold">DELETE</button>
                </DialogFooter>
            </DialogContent>
        </Dialog>

    </Layout>
  );
}
