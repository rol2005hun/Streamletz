import type { PageServerLoad } from './$types';
import { playlistService, type Playlist } from '$lib/playlistService';
import { trackService, type Track } from '$lib/trackService';
import { likedTrackService } from '$lib/likedTrackService';

export const load: PageServerLoad = async ({ locals, cookies }) => {
    let user = null;
    let playlists: Playlist[] = [];
    let tracks: Track[] = [];
    let likedTrackIds: number[] = [];

    if (locals.isAuthenticated) {
        const userCookie = cookies.get('user');
        const tokenCookie = cookies.get('token');
        if (userCookie) {
            try {
                user = JSON.parse(decodeURIComponent(userCookie));
            } catch {
                console.error('Failed to parse user cookie');
            }
        }
        if (tokenCookie) {
            try {
                tracks = await trackService.getAllTracks();
            } catch {
                tracks = [];
            }

            try {
                playlists = await playlistService.getUserPlaylists();
            } catch {
                playlists = [];
            }

            try {
                const likedTracks = await likedTrackService.getLikedTracks();
                likedTrackIds = likedTracks.map((t) => t.id);
            } catch {
                likedTrackIds = [];
            }
        }

    }
    return {
        user,
        playlists,
        tracks,
        likedTrackIds
    };
};