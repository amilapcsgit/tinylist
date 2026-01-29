import { nanoid } from 'nanoid';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

export type ListColor = 'red' | 'orange' | 'yellow' | 'lime' | 'green' | 'teal' | 'cyan' | 'blue' | 'purple' | 'pink';

export const LIST_COLORS: ListColor[] = [
  'red', 'orange', 'yellow', 'lime', 'green', 'teal', 'cyan', 'blue', 'purple', 'pink'
];

export const COLOR_MAP: Record<ListColor, string> = {
  red: 'var(--neon-red)',
  orange: 'var(--neon-orange)',
  yellow: 'var(--neon-yellow)',
  lime: 'var(--neon-lime)',
  green: 'var(--neon-green)',
  teal: 'var(--neon-teal)',
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
  color: ListColor; // Added item-specific color
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
  addItem: (listId: string, text: string, color: ListColor, position?: 'top' | 'bottom') => void;
  toggleItem: (itemId: string) => void;
  updateItem: (itemId: string, updates: Partial<TodoItem>) => void;
  deleteItem: (itemId: string) => void;
  clearCompleted: (listId: string) => void;
  reorderItems: (newItems: TodoItem[]) => void;
  
  // Undo/Redo support
  history: {
    type: 'list' | 'item';
    action: 'delete' | 'complete' | 'edit';
    data: any;
    timestamp: number;
  }[];
  
  addToHistory: (entry: { type: 'list' | 'item', action: 'delete' | 'complete' | 'edit', data: any }) => void;
  undo: () => void;
  
  duplicateList: (listId: string) => void;
  duplicateItem: (itemId: string) => void;
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
        { id: '101', listId: '1', text: 'Welcome to NeonList', isDone: false, color: 'red', createdAt: Date.now() },
        { id: '102', listId: '1', text: 'Swipe right to edit', isDone: false, color: 'blue', createdAt: Date.now() },
        { id: '103', listId: '1', text: 'Swipe left to delete', isDone: true, color: 'green', createdAt: Date.now() },
        { id: '104', listId: '2', text: 'Milk 2.50', isDone: false, color: 'green', createdAt: Date.now() },
        { id: '105', listId: '2', text: 'Bread 1.20', isDone: false, color: 'orange', createdAt: Date.now() },
      ],
      
      history: [],
      lastDeletedList: null,
      lastDeletedItems: null,
      lastDeletedItem: null,

      addToHistory: (entry) => set((state) => ({
        history: [...state.history.slice(-10), { ...entry, timestamp: Date.now() }]
      })),

      undo: () => set((state) => {
        if (state.history.length === 0) return state;
        const lastAction = state.history[state.history.length - 1];
        const newHistory = state.history.slice(0, -1);

        if (lastAction.type === 'list' && lastAction.action === 'delete') {
           const { list, items } = lastAction.data;
           return {
             lists: [...state.lists, list],
             items: [...state.items, ...items],
             history: newHistory
           };
        }
        
        if (lastAction.type === 'item' && lastAction.action === 'delete') {
            const item = lastAction.data;
            return {
                items: [...state.items, item],
                history: newHistory
            };
        }
        
        if (lastAction.type === 'item' && lastAction.action === 'complete') {
             const { id, wasDone } = lastAction.data;
             return {
                 items: state.items.map(i => i.id === id ? { ...i, isDone: wasDone } : i),
                 history: newHistory
             };
        }

        return { history: newHistory };
      }),

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

        const newHistory = [...state.history.slice(-10), {
            type: 'list' as const,
            action: 'delete' as const,
            data: { list: listToDelete, items: itemsToDelete },
            timestamp: Date.now()
        }];

        return {
          lists: state.lists.filter(l => l.id !== id),
          items: state.items.filter(i => i.listId !== id),
          history: newHistory
        };
      }),

      reorderLists: (newOrder) => set({ lists: newOrder }),

      addItem: (listId, text, color, position = 'bottom') => set((state) => {
        const newItem = {
          id: nanoid(),
          listId,
          text,
          isDone: false,
          color,
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

      updateItem: (itemId, updates) => set((state) => ({
        items: state.items.map(i => i.id === itemId ? { ...i, ...updates } : i)
      })),

      deleteItem: (itemId) => set((state) => {
        const item = state.items.find(i => i.id === itemId);
        if (!item) return state;

        const newHistory = [...state.history.slice(-10), {
            type: 'item' as const,
            action: 'delete' as const,
            data: item,
            timestamp: Date.now()
        }];

        return {
          items: state.items.filter(i => i.id !== itemId),
          history: newHistory
        };
      }),

      clearCompleted: (listId) => set((state) => ({
        items: state.items.filter(i => !(i.listId === listId && i.isDone))
      })),
      
      reorderItems: (newItems) => set((state) => {
        const otherItems = state.items.filter(i => !newItems.find(ni => ni.id === i.id));
        return { items: [...otherItems, ...newItems] };
      }),

      restoreLastDeletedList: () => get().undo(),
      restoreLastDeletedItem: () => get().undo(),
      
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
      }),

      duplicateItem: (itemId) => set((state) => {
          const item = state.items.find(i => i.id === itemId);
          if (!item) return state;
          
          const newItem = {
              ...item,
              id: nanoid(),
              createdAt: Date.now()
          };
          
          const index = state.items.findIndex(i => i.id === itemId);
          const newItems = [...state.items];
          newItems.splice(index + 1, 0, newItem);
          
          return { items: newItems };
      })

    }),
    {
      name: 'neonlist-storage', // name of the item in the storage (must be unique)
      storage: createJSONStorage(() => localStorage), // (optional) by default, 'localStorage' is used
    }
  )
);
