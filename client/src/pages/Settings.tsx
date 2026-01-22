import React from 'react';
import { Layout } from '@/components/Layout';
import { useStore } from '@/lib/store';
import { Download, Moon, Sun, Monitor, Trash, Database } from 'lucide-react';

export default function Settings() {
  const lists = useStore(state => state.lists);
  const items = useStore(state => state.items);

  const handleExport = () => {
    const data = { lists, items, exportedAt: new Date().toISOString() };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `neonlist-backup-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <Layout title="SETTINGS" showBack>
      <div className="p-4 flex flex-col gap-6">
        
        <section className="bg-card border border-white/5 rounded-lg overflow-hidden">
            <div className="p-4 border-b border-white/5 bg-white/5">
                <h3 className="font-display text-primary">APPEARANCE</h3>
            </div>
            <div className="p-4 flex flex-col gap-4">
                <div className="flex items-center justify-between">
                    <span className="font-ui">Theme</span>
                    <div className="flex bg-black/20 rounded-lg p-1 border border-white/5">
                        <button className="p-2 rounded bg-primary/20 text-primary"><Moon className="w-4 h-4" /></button>
                        <button className="p-2 rounded opacity-50 hover:opacity-100 transition-opacity"><Sun className="w-4 h-4" /></button>
                    </div>
                </div>
                <p className="text-xs text-muted-foreground">Currently locked to Cyberpunk Dark Mode.</p>
            </div>
        </section>

        <section className="bg-card border border-white/5 rounded-lg overflow-hidden">
            <div className="p-4 border-b border-white/5 bg-white/5">
                <h3 className="font-display text-primary">DATA</h3>
            </div>
            <div className="p-4 flex flex-col gap-4">
                <button 
                    onClick={handleExport}
                    className="flex items-center justify-between w-full p-3 rounded hover:bg-white/5 transition-colors text-left"
                >
                    <div className="flex flex-col">
                        <span className="font-ui font-bold">Export Backup</span>
                        <span className="text-xs text-muted-foreground">Save your lists as JSON file</span>
                    </div>
                    <Download className="w-5 h-5 text-muted-foreground" />
                </button>
                
                <div className="h-px bg-white/5" />
                
                <div className="p-3">
                    <div className="flex items-center justify-between mb-2">
                        <span className="font-ui font-bold">Storage Stats</span>
                        <Database className="w-4 h-4 text-muted-foreground" />
                    </div>
                    <div className="grid grid-cols-2 gap-4 text-sm">
                        <div className="bg-black/20 p-2 rounded">
                            <span className="block text-xs text-muted-foreground">LISTS</span>
                            <span className="font-display text-lg text-primary">{lists.length}</span>
                        </div>
                        <div className="bg-black/20 p-2 rounded">
                            <span className="block text-xs text-muted-foreground">ITEMS</span>
                            <span className="font-display text-lg text-secondary-foreground">{items.length}</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>
        
        <div className="mt-8 text-center">
            <h1 className="font-display text-2xl text-white/10 tracking-widest font-black">NEON LIST</h1>
            <p className="text-xs text-white/20 mt-2 font-mono">v1.0.0 // REPLIT BUILD</p>
        </div>

      </div>
    </Layout>
  );
}
