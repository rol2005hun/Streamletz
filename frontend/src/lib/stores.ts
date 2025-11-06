import { writable } from 'svelte/store';
import type { Track } from './trackService';

export const currentTrack = writable<Track | null>(null);
export const isPlaying = writable(false);
export const isPaused = writable(false);
export const allTracks = writable<Track[]>([]);