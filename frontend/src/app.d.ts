/// <reference types="svelte" />
/// <reference types="vite/client" />

// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		interface Locals {
			isAuthenticated: boolean;
			user?: import('$lib/authService').User | null;
		}
		// interface Error {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
}

// Ezt a két interfészt adtuk hozzá a API_BASE_URL típusának definíálásához
interface ImportMetaEnv {
	readonly API_BASE_URL: string;
}

interface ImportMeta {
	readonly env: ImportMetaEnv;
}

export { };