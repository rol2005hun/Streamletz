import api from "./api";

export interface Playlist {
  id: number;
  name: string;
  description?: string;
  ownerUsername: string;
  isPublic: boolean;
  coverImageUrl?: string;
  trackCount: number;
  totalDuration: number;
  createdAt: string;
  updatedAt: string;
  tracks?: PlaylistTrack[];
}

export interface PlaylistTrack {
  id: number;
  title: string;
  artist: string;
  album?: string;
  duration: number;
  coverArtUrl?: string;
  playCount: number;
}

export interface CreatePlaylistRequest {
  name: string;
  description?: string;
  isPublic?: boolean;
}

export interface UpdatePlaylistRequest {
  name: string;
  description?: string;
  isPublic?: boolean;
}

export const playlistService = {
  createPlaylist: async (request: CreatePlaylistRequest): Promise<Playlist> => {
    const response = await api.post("/playlists", request);
    return response.data;
  },

  getUserPlaylists: async (): Promise<Playlist[]> => {
    const response = await api.get("/playlists");
    return response.data;
  },

  getPublicPlaylists: async (): Promise<Playlist[]> => {
    const response = await api.get("/playlists/public");
    return response.data;
  },

  getPlaylistById: async (id: number): Promise<Playlist> => {
    const response = await api.get(`/playlists/${id}`);
    return response.data;
  },

  updatePlaylist: async (id: number, request: UpdatePlaylistRequest): Promise<Playlist> => {
    const response = await api.put(`/playlists/${id}`, request);
    return response.data;
  },

  deletePlaylist: async (id: number): Promise<void> => {
    await api.delete(`/playlists/${id}`);
  },

  addTrackToPlaylist: async (playlistId: number, trackId: number): Promise<Playlist> => {
    const response = await api.post(`/playlists/${playlistId}/tracks/${trackId}`);
    return response.data;
  },

  removeTrackFromPlaylist: async (playlistId: number, trackId: number): Promise<Playlist> => {
    const response = await api.delete(`/playlists/${playlistId}/tracks/${trackId}`);
    return response.data;
  },

  reorderTracks: async (playlistId: number, trackIds: number[]): Promise<Playlist> => {
    const response = await api.put(`/playlists/${playlistId}/reorder`, { trackIds });
    return response.data;
  },

  searchPlaylists: async (query: string): Promise<Playlist[]> => {
    const response = await api.get(`/playlists/search`, { params: { query } });
    return response.data;
  },
};