import api, { API_BASE_URL } from "./api";

export interface Track {
  id: number;
  title: string;
  artist: string;
  album: string;
  duration: number;
  coverArtUrl: string;
  filePath: string;
  fileFormat: string;
  playCount: number;
}

export interface TrackBrowseResponse {
  items: Track[];
  nextCursor: string | null;
  hasMore: boolean;
}

export interface TrackCountResponse {
  count: number;
}

export const trackService = {
  async browseTracks(limit: number = 50, cursor?: string | null): Promise<TrackBrowseResponse> {
    const response = await api.get("/tracks/browse", {
      params: { limit, cursor: cursor || undefined }
    });
    return response.data;
  },

  async browseTracksPage(limit: number = 50, page: number = 0): Promise<TrackBrowseResponse> {
    const response = await api.get("/tracks/browse/page", {
      params: { limit, page }
    });
    return response.data;
  },

  async getTrackCount(): Promise<number> {
    const response = await api.get<TrackCountResponse>("/tracks/count");
    return response.data.count;
  },

  getAllTracks: async (): Promise<Track[]> => {
    const response = await api.get("/tracks");
    return response.data;
  },

  async getTrackById(id: number): Promise<Track> {
    const response = await api.get(`/tracks/${id}`);
    return response.data;
  },

  async searchTracks(query: string): Promise<Track[]> {
    const response = await api.get("/tracks/search", { params: { query } });
    return response.data;
  },

  getStreamUrl(trackId: number): string {
    return `${API_BASE_URL}/tracks/stream/${trackId}`;
  },

  async incrementPlayCount(trackId: number): Promise<void> {
    await api.post(`/tracks/${trackId}/play`);
  },

  async downloadFromExternal(source: string, url: string): Promise<void> {
    await api.post("/tracks/download", null, {
      params: { source, url }
    });
  }
};