import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const srcPath = path.resolve(__dirname, 'src');

/** @type {import('@sveltejs/kit').Config} */
const config = {
    preprocess: vitePreprocess({
        style: {
            scss: {
                // Make SCSS @use imports like: @use "styles/pages/Dashboard" work
                // from Svelte <style lang="scss"> blocks.
                loadPaths: [srcPath]
            }
        }
    }),

    kit: {
        adapter: adapter(),
        alias: {
            $styles: 'src/styles'
        }
    }
};

export default config;