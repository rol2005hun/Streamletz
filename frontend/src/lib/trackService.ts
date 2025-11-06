import type { AxiosInstance } from 'axios';

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

export function createTrackService(api: AxiosInstance, baseUrl: string) {
  return {
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
      return `${baseUrl}/tracks/stream/${trackId}`;
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
}