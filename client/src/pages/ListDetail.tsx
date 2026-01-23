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

  if (!list) return null;
  const listColor = COLOR_MAP[list.color];

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

        <div className="pb-32">
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
            <DialogContent className="bg-card border-white/10 text-white w-[90%] rounded-xl">
                <DialogHeader>
                    <DialogTitle className="font-display text-primary">New Item</DialogTitle>
                </DialogHeader>
                <form onSubmit={handleAddItem} className="flex flex-col gap-6 mt-2">
                    <input 
                        autoFocus
                        placeholder="Item text (e.g. Milk 2.50)"
                        value={newItemText}
                        onChange={(e) => setNewItemText(e.target.value)}
                        className="w-full bg-black/20 border border-white/10 rounded p-3 font-ui text-lg text-white focus:outline-none focus:border-primary"
                    />
                    
                    <div className="space-y-2">
                        <label className="text-xs font-ui text-muted-foreground uppercase">Color</label>
                        <div className="flex flex-wrap gap-3">
                            {LIST_COLORS.map(color => (
                                <button
                                    key={color}
                                    type="button"
                                    onClick={() => setNewItemColor(color)}
                                    className={cn(
                                        "w-8 h-8 rounded-full border-2 transition-all",
                                        newItemColor === color ? "border-white scale-110 shadow-[0_0_10px_currentColor]" : "border-transparent opacity-50"
                                    )}
                                    style={{ backgroundColor: COLOR_MAP[color], color: COLOR_MAP[color] }}
                                />
                            ))}
                        </div>
                    </div>

                    <div className="flex justify-end gap-2">
                         <button type="submit" className="px-6 py-2 bg-primary text-black font-bold rounded font-display tracking-wider">ADD</button>
                    </div>
                </form>
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
