import { nanoid } from 'nanoid';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

export type ListColor = 'red' | 'blue' | 'green' | 'yellow' | 'purple' | 'cyan' | 'orange' | 'pink';

export const LIST_COLORS: ListColor[] = [
  'red', 'orange', 'yellow', 'green', 'cyan', 'blue', 'purple', 'pink'
];

export const COLOR_MAP: Record<ListColor, string> = {
  red: 'var(--neon-red)',
  orange: 'var(--neon-orange)',
  yellow: 'var(--neon-yellow)',
  green: 'var(--neon-green)',
  cyan: 'var(--neon-cyan)',
  blue: 'var(--neon-blue)',
  purple: 'var(--neon-purple)',
  pink: 'var(--neon-pink)',
};

export interface TodoItem {
  id: string;
  listId: string;
  text: string;
  isDone: boolean;
  createdAt: number;
}

export interface TodoList {
  id: string;
  title: string;
  color: ListColor;
  createdAt: number;
  order: number;
}

interface StoreState {
  lists: TodoList[];
  items: TodoItem[];
  
  // List Actions
  addList: (title: string, color: ListColor) => void;
  updateList: (id: string, updates: Partial<TodoList>) => void;
  deleteList: (id: string) => void;
  reorderLists: (newOrder: TodoList[]) => void;
  
  // Item Actions
  addItem: (listId: string, text: string, position?: 'top' | 'bottom') => void;
  toggleItem: (itemId: string) => void;
  updateItem: (itemId: string, text: string) => void;
  deleteItem: (itemId: string) => void;
  clearCompleted: (listId: string) => void;
  reorderItems: (newItems: TodoItem[]) => void; // Simple reorder for now
  
  // Undo/Redo support could be added here, but keeping it simple for v1
  lastDeletedList: TodoList | null;
  lastDeletedItems: TodoItem[] | null; // For restoring a list
  restoreLastDeletedList: () => void;
  
  lastDeletedItem: TodoItem | null;
  restoreLastDeletedItem: () => void;
  
  duplicateList: (listId: string) => void;
}

export const useStore = create<StoreState>()(
  persist(
    (set, get) => ({
      lists: [
        { id: '1', title: 'Todos', color: 'red', createdAt: Date.now(), order: 0 },
        { id: '2', title: 'Groceries', color: 'green', createdAt: Date.now(), order: 1 },
        { id: '3', title: 'Ideas', color: 'cyan', createdAt: Date.now(), order: 2 },
      ],
      items: [
        { id: '101', listId: '1', text: 'Welcome to NeonList', isDone: false, createdAt: Date.now() },
        { id: '102', listId: '1', text: 'Swipe right to edit', isDone: false, createdAt: Date.now() },
        { id: '103', listId: '1', text: 'Swipe left to delete', isDone: true, createdAt: Date.now() },
      ],
      
      lastDeletedList: null,
      lastDeletedItems: null,
      lastDeletedItem: null,

      addList: (title, color) => set((state) => ({
        lists: [
          ...state.lists,
          { 
            id: nanoid(), 
            title, 
            color, 
            createdAt: Date.now(), 
            order: state.lists.length 
          }
        ]
      })),

      updateList: (id, updates) => set((state) => ({
        lists: state.lists.map(l => l.id === id ? { ...l, ...updates } : l)
      })),

      deleteList: (id) => set((state) => {
        const listToDelete = state.lists.find(l => l.id === id);
        const itemsToDelete = state.items.filter(i => i.listId === id);
        if (!listToDelete) return state;

        return {
          lists: state.lists.filter(l => l.id !== id),
          items: state.items.filter(i => i.listId !== id),
          lastDeletedList: listToDelete,
          lastDeletedItems: itemsToDelete
        };
      }),

      reorderLists: (newOrder) => set({ lists: newOrder }),

      addItem: (listId, text, position = 'bottom') => set((state) => {
        const newItem = {
          id: nanoid(),
          listId,
          text,
          isDone: false,
          createdAt: Date.now()
        };
        return {
          items: position === 'top' 
            ? [newItem, ...state.items] 
            : [...state.items, newItem]
        };
      }),

      toggleItem: (itemId) => set((state) => ({
        items: state.items.map(i => i.id === itemId ? { ...i, isDone: !i.isDone } : i)
      })),

      updateItem: (itemId, text) => set((state) => ({
        items: state.items.map(i => i.id === itemId ? { ...i, text } : i)
      })),

      deleteItem: (itemId) => set((state) => {
        const item = state.items.find(i => i.id === itemId);
        return {
          items: state.items.filter(i => i.id !== itemId),
          lastDeletedItem: item || null
        };
      }),

      clearCompleted: (listId) => set((state) => ({
        items: state.items.filter(i => !(i.listId === listId && i.isDone))
      })),
      
      reorderItems: (newItems) => set((state) => {
        // This assumes newItems contains all items, or at least we need to merge carefully
        // Ideally we only reorder the subset displayed, but for simplicity in this global store:
        const otherItems = state.items.filter(i => !newItems.find(ni => ni.id === i.id));
        return { items: [...otherItems, ...newItems] };
      }),

      restoreLastDeletedList: () => set((state) => {
        if (!state.lastDeletedList) return state;
        return {
          lists: [...state.lists, state.lastDeletedList],
          items: [...state.items, ...(state.lastDeletedItems || [])],
          lastDeletedList: null,
          lastDeletedItems: null
        };
      }),

      restoreLastDeletedItem: () => set((state) => {
        if (!state.lastDeletedItem) return state;
        return {
          items: [...state.items, state.lastDeletedItem],
          lastDeletedItem: null
        };
      }),
      
      duplicateList: (listId) => set((state) => {
        const originalList = state.lists.find(l => l.id === listId);
        if (!originalList) return state;
        
        const newListId = nanoid();
        const newList: TodoList = {
          ...originalList,
          id: newListId,
          title: `${originalList.title} Copy`,
          createdAt: Date.now(),
          order: state.lists.length
        };
        
        const originalItems = state.items.filter(i => i.listId === listId);
        const newItems = originalItems.map(i => ({
          ...i,
          id: nanoid(),
          listId: newListId,
          createdAt: Date.now()
        }));
        
        return {
          lists: [...state.lists, newList],
          items: [...state.items, ...newItems]
        };
      })

    }),
    {
      name: 'neonlist-storage', // name of the item in the storage (must be unique)
      storage: createJSONStorage(() => localStorage), // (optional) by default, 'localStorage' is used
    }
  )
);
