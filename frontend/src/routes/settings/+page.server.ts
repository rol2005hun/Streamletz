import { userService } from '$lib/userService';
import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
    const userProfile = await userService.getUserProfile();
    return { userProfile };
};