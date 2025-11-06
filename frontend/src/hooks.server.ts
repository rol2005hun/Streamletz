import type { Handle } from '@sveltejs/kit';
import { redirect } from '@sveltejs/kit';
import { setServerToken } from '$lib/api';

export const handle: Handle = async ({ event, resolve }) => {
    const token = event.cookies.get('token');
    const isAuthenticated = !!token;

    event.locals.isAuthenticated = isAuthenticated;

    setServerToken(token ?? null);

    const publicRoutes = ['/login', '/register'];
    const isPublic = publicRoutes.some((route) =>
        event.url.pathname.startsWith(route)
    );

    if (isAuthenticated && isPublic) {
        throw redirect(303, '/dashboard');
    }

    if (!isAuthenticated && !isPublic) {
        throw redirect(303, '/login');
    }

    if (event.url.pathname === '/') {
        if (isAuthenticated) {
            throw redirect(303, '/dashboard');
        } else {
            throw redirect(303, '/login');
        }
    }

    return resolve(event);
};