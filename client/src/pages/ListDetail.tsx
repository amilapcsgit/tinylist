import React, { useMemo } from 'react';
import { useRoute } from 'wouter';
import { Layout } from '@/components/Layout';
import { TaskItem } from '@/components/TaskItem';
import { PullToAdd } from '@/components/PullToAdd';
import { useStore, COLOR_MAP } from '@/lib/store';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { MoreVertical, Copy, Trash2, CheckSquare } from 'lucide-react';
import { useLocation } from 'wouter';

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

  const list = lists.find(l => l.id === listId);
  const listItems = useMemo(() => 
    items.filter(i => i.listId === listId).sort((a, b) => {
       if (a.isDone === b.isDone) return b.createdAt - a.createdAt;
       return a.isDone ? 1 : -1;
    }), 
  [items, listId]);

  if (!list) {
    return (
        <Layout title="Not Found" showBack>
            <div className="p-8 text-center text-muted-foreground">List not found</div>
        </Layout>
    );
  }

  const listColor = COLOR_MAP[list.color];

  const handleDelete = () => {
    if (confirm('Delete this list?')) {
        deleteList(list.id);
        setLocation('/');
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
            <DropdownMenuItem onClick={() => clearCompleted(list.id)}>
                <CheckSquare className="w-4 h-4 mr-2" /> Clear Completed
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => duplicateList(list.id)}>
                <Copy className="w-4 h-4 mr-2" /> Duplicate List
            </DropdownMenuItem>
            <DropdownMenuItem onClick={handleDelete} className="text-destructive focus:text-destructive">
                <Trash2 className="w-4 h-4 mr-2" /> Delete List
            </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
      }
    >
        {/* Color Accent Bar */}
        <div className="h-1 w-full shadow-[0_0_15px_currentColor]" style={{ backgroundColor: listColor, color: listColor }} />

        <div className="p-4 pb-24">
            <PullToAdd 
                onAdd={(text) => addItem(list.id, text, 'top')} 
                color={listColor}
            />

            <div className="mt-4 flex flex-col gap-1">
                {listItems.map(item => (
                    <TaskItem key={item.id} item={item} color={listColor} />
                ))}
            </div>

            {listItems.length === 0 && (
                <div className="text-center py-20 opacity-30 font-display">
                    EMPTY LIST
                </div>
            )}
        </div>
        
        {/* Floating Input at Bottom (Standard Add) */}
        <div className="fixed bottom-0 left-0 right-0 p-4 bg-background/80 backdrop-blur-xl border-t border-white/5 flex justify-center">
            <div className="w-full max-w-md flex gap-2">
                <input 
                    type="text" 
                    placeholder="Add item..."
                    className="flex-1 bg-card border border-white/10 rounded-full px-4 py-3 focus:outline-none focus:border-primary/50 text-white font-ui"
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

    </Layout>
  );
}
