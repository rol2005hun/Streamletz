import type { PageServerLoad } from './$types';
import { playlistService, type Playlist } from '$lib/playlistService';
import { trackService, type Track, type TrackBrowseResponse } from '$lib/trackService';
import { likedTrackService } from '$lib/likedTrackService';

export const load: PageServerLoad = async ({ locals, cookies }) => {
    let user = null;
    let playlists: Playlist[] = [];
    let trackBrowse: TrackBrowseResponse | null = null;
    let tracks: Track[] = [];
    let nextCursor: string | null = null;
    let hasMore = false;
    let likedTrackIds: number[] = [];

    if (locals.isAuthenticated) {
        const userCookie = cookies.get('user');
        const tokenCookie = cookies.get('streamletz-token');

        if (userCookie) {
            try {
                user = JSON.parse(decodeURIComponent(userCookie));
            } catch {
                console.error('Failed to parse user cookie');
            }
        }
        if (tokenCookie) {
            try {
                trackBrowse = await trackService.browseTracks(60, null);
                tracks = trackBrowse.items ?? [];
                nextCursor = trackBrowse.nextCursor ?? null;
                hasMore = !!trackBrowse.hasMore;
            } catch {
                tracks = [];
                nextCursor = null;
                hasMore = false;
            }

            try {
                playlists = await playlistService.getUserPlaylists();
            } catch {
                playlists = [];
            }

            try {
                likedTrackIds = await likedTrackService.getLikedStatus(tracks.map((t) => t.id));
            } catch {
                likedTrackIds = [];
            }
        }

    }
    return {
        user,
        playlists,
        tracks,
        nextCursor,
        hasMore,
        likedTrackIds
    };
};