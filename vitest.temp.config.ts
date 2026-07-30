import { defineConfig } from 'vitest/config';
import path from 'path';

export default defineConfig({
  resolve: {
    alias: {
      app: path.resolve('src/main/webapp/app'),
      environments: path.resolve('src/main/webapp/environments'),
      content: path.resolve('src/main/webapp/content'),
      i18n: path.resolve('src/test/javascript/mocks/i18n.ts'),
    },
  },
  define: {
    __VERSION__: JSON.stringify('0.0.0'),
  },
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['src/main/webapp/app/layouts/navbar/navbar.spec.ts'],
    setupFiles: ['src/test/javascript/mocks/test-setup.ts'],
  },
});
