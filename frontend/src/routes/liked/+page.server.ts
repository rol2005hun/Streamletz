import type { PageServerLoad } from './$types';
import { likedTrackService } from '$lib/likedTrackService';
import type { Track } from '$lib/trackService';

export const load: PageServerLoad = async () => {
    let tracks: Track[] = [];
    try {
        tracks = await likedTrackService.getLikedTracks();
    } catch {
        tracks = [];
    }
    return { tracks };
};