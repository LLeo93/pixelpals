import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  define: {
    // Aggiunto questo blocco
    global: 'window', // Questa riga risolve il ReferenceError per 'global'
  },
  server: {
    port: 3000,
    https: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
