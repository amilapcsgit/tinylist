import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Layout } from '@/components/Layout';
import { ListCard } from '@/components/ListCard';
import { useStore, LIST_COLORS, COLOR_MAP, ListColor } from '@/lib/store';
import { Plus, X, ArrowUpAZ, ArrowDown01, MoreVertical, RotateCcw } from 'lucide-react';
import { cn } from '@/lib/utils';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export default function Home() {
  const lists = useStore(state => state.lists);
  const items = useStore(state => state.items);
  const addList = useStore(state => state.addList);
  const undo = useStore(state => state.undo);
  const history = useStore(state => state.history);
  
  const [isCreating, setIsCreating] = useState(false);
  const [newListTitle, setNewListTitle] = useState('');
  const [newListColor, setNewListColor] = useState<ListColor>('green');
  const [sortMode, setSortMode] = useState<'az' | 'manual' | 'completion'>('manual');

  // Computed: Get item counts per list
  const getListStats = (listId: string) => {
    const listItems = items.filter(i => i.listId === listId);
    return {
      total: listItems.length,
      completed: listItems.filter(i => i.isDone).length
    };
  };

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (newListTitle.trim()) {
      addList(newListTitle.trim(), newListColor);
      setNewListTitle('');
      setIsCreating(false);
    }
  };

  const sortedLists = [...lists].sort((a, b) => {
    if (sortMode === 'az') return a.title.localeCompare(b.title);
    if (sortMode === 'completion') {
        const statsA = getListStats(a.id);
        const statsB = getListStats(b.id);
        const ratioA = statsA.total ? statsA.completed / statsA.total : 0;
        const ratioB = statsB.total ? statsB.completed / statsB.total : 0;
        return ratioB - ratioA; // Most completed first
    }
    return a.order - b.order;
  });

  return (
    <Layout 
      title="NEON LISTS"
      actions={
        <div className="flex items-center gap-1">
          {history.length > 0 && (
              <button 
                onClick={undo}
                className="p-2 hover:bg-white/10 rounded-full transition-colors text-neon-yellow"
                title="Undo"
              >
                  <RotateCcw className="w-5 h-5" />
              </button>
          )}
          <DropdownMenu>
            <DropdownMenuTrigger className="p-2 hover:bg-white/10 rounded-full transition-colors outline-none">
              <MoreVertical className="w-5 h-5 text-muted-foreground hover:text-primary transition-colors" />
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="bg-card border-white/10 text-foreground">
              <DropdownMenuItem onClick={() => setSortMode('az')}>
                <ArrowUpAZ className="w-4 h-4 mr-2" /> Sort A-Z
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setSortMode('completion')}>
                 <ArrowDown01 className="w-4 h-4 mr-2" /> Sort by Completion
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setSortMode('manual')}>
                 <ArrowDown01 className="w-4 h-4 mr-2" /> Manual Order
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      }
    >
      <div className="flex flex-col">
        {/* List Rows */}
        <AnimatePresence>
          {sortedLists.map(list => {
            const stats = getListStats(list.id);
            return (
              <ListCard 
                key={list.id} 
                list={list} 
                itemCount={stats.total}
                completedCount={stats.completed}
              />
            );
          })}
        </AnimatePresence>

        {/* Empty State */}
        {lists.length === 0 && !isCreating && (
          <div className="flex flex-col items-center justify-center py-20 text-muted-foreground opacity-50">
            <p className="font-display tracking-widest text-lg">NO DATA</p>
            <p className="text-sm font-ui">Tap + to initialize new list</p>
          </div>
        )}

        {/* Creator Panel */}
        <AnimatePresence>
          {isCreating && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="bg-card/50 border-t border-b border-white/10 p-4 overflow-hidden"
            >
              <form onSubmit={handleCreate} className="flex flex-col gap-4">
                <input
                  autoFocus
                  type="text"
                  placeholder="LIST TITLE"
                  className="bg-transparent border-b border-white/20 p-2 text-xl font-display text-white placeholder:text-white/20 focus:outline-none focus:border-primary transition-colors"
                  value={newListTitle}
                  onChange={e => setNewListTitle(e.target.value)}
                />
                
                <div className="flex gap-2 flex-wrap justify-between">
                  {LIST_COLORS.map(color => (
                    <button
                      key={color}
                      type="button"
                      onClick={() => setNewListColor(color)}
                      className={cn(
                        "w-8 h-8 rounded-full border-2 transition-all duration-200 hover:scale-110",
                        newListColor === color ? "border-white scale-110 shadow-[0_0_10px_currentColor]" : "border-transparent opacity-50 hover:opacity-100"
                      )}
                      style={{ backgroundColor: COLOR_MAP[color], color: COLOR_MAP[color] }}
                    />
                  ))}
                </div>

                <div className="flex justify-end gap-2 mt-2">
                  <button 
                    type="button"
                    onClick={() => setIsCreating(false)}
                    className="px-4 py-2 text-sm font-ui text-muted-foreground hover:text-white transition-colors"
                  >
                    CANCEL
                  </button>
                  <button 
                    type="submit"
                    className="px-6 py-2 bg-primary/20 hover:bg-primary/40 text-primary border border-primary/50 rounded-md font-display text-sm tracking-wider transition-all hover:box-glow"
                  >
                    CREATE
                  </button>
                </div>
              </form>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* FAB */}
      <motion.button
        layout
        onClick={() => setIsCreating(true)}
        className="fixed bottom-6 right-6 w-14 h-14 bg-primary text-black rounded-full shadow-[0_0_20px_var(--color-primary)] flex items-center justify-center z-50 hover:scale-110 active:scale-95 transition-transform"
        whileHover={{ rotate: 90 }}
      >
        <Plus className="w-8 h-8" />
      </motion.button>
    </Layout>
  );
}
