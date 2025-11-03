import api from "./api";

export interface UserProfile {
    id: number;
    username: string;
    email: string;
    profileImage: string | null;
    createdAt: string;
}

export interface UpdateProfileResponse extends UserProfile {
    newToken?: string | null;  // New token if username changed
}

export interface UpdateProfileRequest {
    username?: string;
    email?: string;
    profileImage?: string;
}

export interface UpdatePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export const userService = {
    async getUserProfile(): Promise<UserProfile> {
        const response = await api.get("/user/profile");
        return response.data;
    },

    async getPublicProfile(identifier: string | number): Promise<UserProfile> {
        const response = await api.get(`/user/profile/${identifier}`);
        return response.data;
    },

    async updateProfile(data: UpdateProfileRequest): Promise<UpdateProfileResponse> {
        const response = await api.put("/user/profile", data);
        return response.data;
    },

    async changePassword(data: UpdatePasswordRequest): Promise<void> {
        await api.put("/user/password", data);
    }
};