function createStorageMock(): Storage {
  let store = new Map<string, string>();
  return {
    get length() {
      return store.size;
    },
    clear(): void {
      store.clear();
    },
    getItem(key: string): string | null {
      return store.has(key) ? store.get(key)! : null;
    },
    key(index: number): string | null {
      return Array.from(store.keys())[index] ?? null;
    },
    removeItem(key: string): void {
      store.delete(key);
    },
    setItem(key: string, value: string): void {
      store.set(key, String(value));
    },
  };
}

Object.defineProperty(globalThis, 'localStorage', {
  value: createStorageMock(),
  configurable: true,
});

Object.defineProperty(globalThis, 'sessionStorage', {
  value: createStorageMock(),
  configurable: true,
});

if (typeof globalThis.DragEvent === 'undefined') {
  class DragEventMock extends Event {
    dataTransfer: DataTransfer | null = null;
    constructor(type: string, init?: EventInit) {
      super(type, init);
    }
  }
  Object.defineProperty(globalThis, 'DragEvent', {
    value: DragEventMock,
    configurable: true,
  });
}
