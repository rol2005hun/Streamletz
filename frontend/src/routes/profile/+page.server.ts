import { userService } from '$lib/userService';
import { likedTrackService } from '$lib/likedTrackService';
import { playlistService } from '$lib/playlistService';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const userProfile = await userService.getUserProfile();
    const likedTracks = await likedTrackService.getLikedTracks();
    const playlists = await playlistService.getUserPlaylists();

    return {
        userProfile,
        likedTracks,
        playlists
    };
};