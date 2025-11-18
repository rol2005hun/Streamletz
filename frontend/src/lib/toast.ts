import { writable } from 'svelte/store';

export type ToastType = 'info' | 'success' | 'error' | 'warning';

export interface Toast {
    id: number;
    message: string;
    type: ToastType;
    timeout?: number;
}

export const toasts = writable<Toast[]>([]);

let toastId = 0;

export function showToast(message: string, type: ToastType = 'info', timeout = 400000) {
    toastId += 1;
    const toast: Toast = { id: toastId, message, type, timeout };
    toasts.update((all) => [...all, toast]);
    setTimeout(() => {
        toasts.update((all) => all.filter((t) => t.id !== toast.id));
    }, timeout);
}