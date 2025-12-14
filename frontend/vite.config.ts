import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const srcPath = path.resolve(__dirname, 'src');

export default defineConfig({
  plugins: [sveltekit()],
  css: {
    preprocessorOptions: {
      scss: {
        // Allow Sass @use imports like: @use "styles/pages/Dashboard";
        // (SvelteKit alias like $styles isn't resolved by Sass in <style> blocks.)
        loadPaths: [srcPath]
      }
    }
  },
  server: {
    port: 5173,
    host: true
  },
  preview: {
    port: 5173
  }
});