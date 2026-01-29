import React, { useState } from 'react';
import { motion, AnimatePresence, Reorder } from 'framer-motion';
import { Layout } from '@/components/Layout';
import { ListCard } from '@/components/ListCard';
import { useStore, LIST_COLORS, COLOR_MAP, ListColor, TodoList } from '@/lib/store';
import { Plus, ArrowUpAZ, ArrowDown01, MoreVertical, RotateCcw } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useLongPress } from '@/hooks/use-mobile';
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
  const reorderLists = useStore(state => state.reorderLists);
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

  // Sort logic
  const sortedLists = [...lists].sort((a, b) => {
    if (sortMode === 'az') return a.title.localeCompare(b.title);
    if (sortMode === 'completion') {
        const statsA = getListStats(a.id);
        const statsB = getListStats(b.id);
        const ratioA = statsA.total ? statsA.completed / statsA.total : 0;
        const ratioB = statsB.total ? statsB.completed / statsB.total : 0;
        return ratioB - ratioA;
    }
    return a.order - b.order;
  });

  const handleReorder = (newOrder: TodoList[]) => {
    if (sortMode === 'manual') {
      const updatedLists = newOrder.map((list, index) => ({
        ...list,
        order: index
      }));
      reorderLists(updatedLists);
    }
  };

  // Explicit long press to open creator
  const longPressProps = useLongPress(() => {
    setIsCreating(true);
    if (navigator.vibrate) navigator.vibrate(50);
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
      <div 
        className="flex flex-col pb-24 min-h-[calc(100vh-64px)]"
        {...longPressProps}
      >
        {/* Lists with Reorder Support */}
        {sortMode === 'manual' ? (
             <Reorder.Group axis="y" values={sortedLists} onReorder={handleReorder} className="flex flex-col">
                {sortedLists.map(list => {
                    const stats = getListStats(list.id);
                    return (
                    <Reorder.Item key={list.id} value={list} dragListener={true}>
                        <ListCard 
                            list={list} 
                            itemCount={stats.total}
                            completedCount={stats.completed}
                        />
                    </Reorder.Item>
                    );
                })}
             </Reorder.Group>
        ) : (
            <div className="flex flex-col">
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
            </div>
        )}

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
              className="bg-card/90 border-t border-b border-white/10 overflow-hidden"
            >
              {/* Visual color selection preview like TinyApp */}
              <div 
                className="w-full h-12 transition-colors duration-300"
                style={{ backgroundColor: COLOR_MAP[newListColor] }}
              >
                <div className="h-full flex items-center px-4 justify-between">
                   <span className="text-white font-display text-sm tracking-widest uppercase opacity-70">New List</span>
                   <span className="text-white font-ui text-sm">0/0</span>
                </div>
              </div>

              <div className="flex flex-col">
                {/* 10 Colors Grid (TinyApp style) */}
                <div className="grid grid-cols-5 gap-0">
                  {LIST_COLORS.map(color => (
                    <button
                      key={color}
                      type="button"
                      onClick={() => setNewListColor(color)}
                      className={cn(
                        "aspect-square transition-all duration-200 relative",
                        newListColor === color ? "z-10" : "hover:opacity-80"
                      )}
                      style={{ backgroundColor: COLOR_MAP[color] }}
                    >
                      {newListColor === color && (
                        <div className="absolute inset-0 border-4 border-white z-20" />
                      )}
                    </button>
                  ))}
                </div>

                <form onSubmit={handleCreate} className="p-4 bg-background">
                  <input
                    autoFocus
                    type="text"
                    placeholder="LIST TITLE"
                    className="w-full bg-black/40 border border-white/10 p-4 text-xl font-display text-white placeholder:text-white/20 focus:outline-none focus:border-primary transition-colors"
                    value={newListTitle}
                    onChange={e => setNewListTitle(e.target.value)}
                  />

                  <div className="flex justify-end gap-2 mt-4">
                    <button 
                      type="button"
                      onClick={() => setIsCreating(false)}
                      className="px-6 py-2 text-sm font-ui text-muted-foreground hover:text-white transition-colors"
                    >
                      CANCEL
                    </button>
                    <button 
                      type="submit"
                      className="px-10 py-3 bg-primary text-black font-display font-bold text-sm tracking-wider transition-all hover:bg-primary/80"
                    >
                      CREATE
                    </button>
                  </div>
                </form>
              </div>
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
