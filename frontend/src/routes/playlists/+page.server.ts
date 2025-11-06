import type { PageServerLoad } from './$types';
import { playlistService, type Playlist } from '$lib/playlistService';

export const load: PageServerLoad = async () => {
    let playlists: Playlist[] = [];

    try {
        playlists = await playlistService.getUserPlaylists();
    } catch {
        console.error('Failed to fetch user playlists');
    }
    return {
        playlists
    };
};