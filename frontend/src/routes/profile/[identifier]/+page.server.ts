import { userService } from '$lib/userService';
import { likedTrackService } from '$lib/likedTrackService';
import { playlistService } from '$lib/playlistService';
import type { Track } from '$lib/trackService';
import type { Playlist } from '$lib/playlistService';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async ({ params }) => {
    const identifier = params.identifier;
    let userProfile;
    let likedTracks: Track[] = [];
    let playlists: Playlist[] = [];
    let isOwnProfile = false;

    const loggedInUser = await userService.getUserProfile();

    if (identifier) {
        if (identifier === loggedInUser.username) {
            userProfile = loggedInUser;
            likedTracks = await likedTrackService.getLikedTracks();
            playlists = await playlistService.getUserPlaylists();
            isOwnProfile = true;
        } else {
            userProfile = await userService.getPublicProfile(identifier);
        }
    } else {
        userProfile = loggedInUser;
        likedTracks = await likedTrackService.getLikedTracks();
        playlists = await playlistService.getUserPlaylists();
        isOwnProfile = true;
    }

    return { userProfile, identifier, likedTracks, playlists, isOwnProfile };
};