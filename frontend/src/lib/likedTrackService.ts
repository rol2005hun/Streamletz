import api from "./api";
import type { Track } from "./trackService";

export const likedTrackService = {
  // Like a track
  likeTrack: async (trackId: number): Promise<void> => {
    await api.post(`/liked/tracks/${trackId}`);
  },

  // Unlike a track
  unlikeTrack: async (trackId: number): Promise<void> => {
    await api.delete(`/liked/tracks/${trackId}`);
  },

  // Get all liked tracks
  getLikedTracks: async (): Promise<Track[]> => {
    const response = await api.get("/liked/tracks");
    return response.data;
  },

  // Check if track is liked
  isTrackLiked: async (trackId: number): Promise<boolean> => {
    const response = await api.get(`/liked/tracks/${trackId}/status`);
    return response.data.isLiked;
  },

  // Get liked tracks count
  getLikedTracksCount: async (): Promise<number> => {
    const response = await api.get("/liked/tracks/count");
    return response.data.count;
  },

  // Toggle like status
  toggleLike: async (trackId: number, isLiked: boolean): Promise<void> => {
    if (isLiked) {
      await likedTrackService.unlikeTrack(trackId);
    } else {
      await likedTrackService.likeTrack(trackId);
    }
  },
};
