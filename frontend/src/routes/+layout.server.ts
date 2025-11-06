import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ locals, cookies }) => {
    let user = null;
    let lastPlayback = null;
    const userCookie = cookies.get('user');
    if (userCookie) {
        try {
            user = JSON.parse(decodeURIComponent(userCookie));
        } catch {
            console.error('Failed to parse user cookie');
        }
    }

    const lastPlaybackCookie = cookies.get('streamletz_last_playback');
    if (lastPlaybackCookie) {
        try {
            lastPlayback = JSON.parse(decodeURIComponent(lastPlaybackCookie));
        } catch {
            try {
                lastPlayback = JSON.parse(lastPlaybackCookie);
            } catch {
                console.error('Failed to parse last playback cookie');
            }
        }
    }

    let sidebarCollapsed = false;
    let sidebarWidth = 280;
    const sidebarCollapsedCookie = cookies.get('streamletz_sidebar_collapsed');
    const sidebarWidthCookie = cookies.get('streamletz_sidebar_width');
    if (sidebarCollapsedCookie === 'true') sidebarCollapsed = true;
    if (sidebarWidthCookie && !isNaN(Number(sidebarWidthCookie))) sidebarWidth = Number(sidebarWidthCookie);

    return {
        isAuthenticated: locals.isAuthenticated,
        user,
        lastPlayback,
        sidebarCollapsed,
        sidebarWidth
    };
};