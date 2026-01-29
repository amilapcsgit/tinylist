import React, { useState, useMemo } from 'react';
import { Layout } from '@/components/Layout';
import { useStore, COLOR_MAP } from '@/lib/store';
import { Search as SearchIcon } from 'lucide-react';
import { Link } from 'wouter';
import { cn } from '@/lib/utils';

export default function Search() {
  const [query, setQuery] = useState('');
  const lists = useStore(state => state.lists);
  const items = useStore(state => state.items);

  const results = useMemo(() => {
    if (!query.trim()) return [];
    
    const lowerQuery = query.toLowerCase();
    
    // Find matching lists
    const matchedLists = lists.filter(l => l.title.toLowerCase().includes(lowerQuery));
    
    // Find matching items
    const matchedItems = items.filter(i => i.text.toLowerCase().includes(lowerQuery));
    
    return { lists: matchedLists, items: matchedItems };
  }, [query, lists, items]);

  // Helper to get list info for an item
  const getListForItem = (listId: string) => lists.find(l => l.id === listId);

  return (
    <Layout title="SEARCH" showBack>
      <div className="p-4">
        <div className="relative mb-6">
            <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input 
                autoFocus
                type="text"
                placeholder="Search lists and items..."
                className="w-full bg-card border border-white/10 rounded-lg pl-10 pr-4 py-4 text-lg font-ui text-white focus:outline-none focus:border-primary/50 placeholder:text-muted-foreground/50 transition-all"
                value={query}
                onChange={e => setQuery(e.target.value)}
            />
        </div>

        <div className="flex flex-col gap-6">
            {/* List Matches */}
            {results.lists && results.lists.length > 0 && (
                <section>
                    <h3 className="font-display text-primary text-sm mb-2 px-2 opacity-70">MATCHING LISTS</h3>
                    <div className="flex flex-col gap-2">
                        {results.lists.map(list => (
                            <Link key={list.id} href={`/list/${list.id}`}>
                                <div className="bg-card border border-white/5 p-4 rounded-lg flex items-center justify-between hover:border-primary/30 transition-colors group cursor-pointer">
                                    <span className="font-display font-bold">{list.title}</span>
                                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: COLOR_MAP[list.color], boxShadow: `0 0 5px ${COLOR_MAP[list.color]}` }} />
                                </div>
                            </Link>
                        ))}
                    </div>
                </section>
            )}

            {/* Item Matches */}
            {results.items && results.items.length > 0 && (
                <section>
                    <h3 className="font-display text-primary text-sm mb-2 px-2 opacity-70">MATCHING ITEMS</h3>
                    <div className="flex flex-col gap-2">
                        {results.items.map(item => {
                            const parentList = getListForItem(item.listId);
                            if (!parentList) return null;
                            const color = COLOR_MAP[parentList.color];
                            
                            return (
                                <Link key={item.id} href={`/list/${parentList.id}`}>
                                    <div className="bg-card border border-white/5 p-4 rounded-lg flex flex-col gap-1 hover:border-primary/30 transition-colors cursor-pointer">
                                        <div className="flex items-center gap-2">
                                            <div className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: color }} />
                                            <span className="text-xs text-muted-foreground font-ui uppercase">{parentList.title}</span>
                                        </div>
                                        <span className={cn("font-ui text-lg", item.isDone && "line-through opacity-50")}>
                                            {item.text}
                                            <span className="bg-yellow-500/20 text-yellow-200">
                                              {/* Highlight match logic omitted for brevity/perf */}
                                            </span>
                                        </span>
                                    </div>
                                </Link>
                            );
                        })}
                    </div>
                </section>
            )}
            
            {query && (!results.lists?.length && !results.items?.length) && (
                <div className="text-center py-10 opacity-30 font-display">NO MATCHES FOUND</div>
            )}
        </div>
      </div>
    </Layout>
  );
}
