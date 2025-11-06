import type { PageServerLoad } from './$types';
import { playlistService } from '$lib/playlistService';

export const load: PageServerLoad = async ({ params }) => {
    const id = parseInt(params.id);
    try {
        const playlist = await playlistService.getPlaylistById(id);
        return { playlist };
    } catch (err) {
        return { playlist: null };
    }
};