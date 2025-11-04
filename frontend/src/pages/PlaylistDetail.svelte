<script lang="ts">
  import { onMount } from "svelte";
  import { playlistService, type Playlist } from "../lib/playlistService";
  import type { Track } from "../lib/trackService";

  interface Props {
    id: string;
    currentTrack: Track | null;
    isPlaying: boolean;
    allTracks: Track[];
  }

  let {
    id,
    currentTrack = $bindable(null),
    isPlaying = $bindable(false),
    allTracks = $bindable([]),
  }: Props = $props();

  let playlist = $state<Playlist | null>(null);
  let loading = $state(true);
  let error = $state("");
  let hoveredTrack = $state<number | null>(null);

  onMount(async () => {
    await loadPlaylist();
  });

  async function loadPlaylist() {
    try {
      loading = true;
      playlist = await playlistService.getPlaylistById(parseInt(id));
      if (playlist?.tracks) {
        allTracks = playlist.tracks.map((track) => ({
          ...track,
          album: track.album || "",
          coverArtUrl: track.coverArtUrl || "",
          filePath: "",
          fileFormat: "",
        }));
      }
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to load playlist";
    } finally {
      loading = false;
    }
  }

  async function removeTrack(trackId: number) {
    if (!playlist) return;

    try {
      playlist = await playlistService.removeTrackFromPlaylist(
        playlist.id,
        trackId,
      );
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to remove track";
    }
  }

  function formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  function formatTotalDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours} hr ${minutes} min`;
    }
    return `${minutes} min`;
  }

  function playTrack(track: any) {
    const trackAsTrack: Track = {
      ...track,
      album: track.album || "",
      coverArtUrl: track.coverArtUrl || "",
      filePath: track.filePath || "",
      fileFormat: track.fileFormat || "",
    };

    if (currentTrack?.id === trackAsTrack.id) {
      isPlaying = !isPlaying;
    } else {
      currentTrack = trackAsTrack;
      isPlaying = true;
    }
  }

  function goBack() {
    window.history.pushState({}, "", "/playlists");
    window.dispatchEvent(new PopStateEvent("popstate"));
  }
</script>

{#if loading}
  <div class="loading-state">
    <div class="spinner"></div>
    <p>Loading playlist...</p>
  </div>
{:else if error}
  <div class="error">{error}</div>
{:else if playlist}
  <div class="playlist-detail">
    <button class="back-btn" onclick={goBack} title="Back to Playlists">
      ← Back
    </button>

    <div class="playlist-header">
      <div class="cover">
        {#if playlist.coverImageUrl}
          <img src={playlist.coverImageUrl} alt={playlist.name} />
        {:else}
          <div class="default-cover">
            <span>🎵</span>
          </div>
        {/if}
      </div>
      <div class="info">
        <span class="type">Playlist</span>
        <h1>{playlist.name}</h1>
        {#if playlist.description}
          <p class="description">{playlist.description}</p>
        {/if}
        <div class="meta">
          <span class="owner">{playlist.ownerUsername}</span>
          <span class="separator">•</span>
          <span>{playlist.trackCount} songs</span>
          {#if playlist.totalDuration > 0}
            <span class="separator">•</span>
            <span>{formatTotalDuration(playlist.totalDuration)}</span>
          {/if}
        </div>
      </div>
    </div>

    <div class="tracks-section">
      {#if playlist.tracks && playlist.tracks.length > 0}
        <div class="tracks-header">
          <div class="col-number">#</div>
          <div class="col-title">Title</div>
          <div class="col-album">Album</div>
          <div class="col-plays">Plays</div>
          <div class="col-duration">Duration</div>
          <div class="col-actions"></div>
        </div>

        <div class="tracks-list">
          {#each playlist.tracks as track, index}
            <div
              class="track-row"
              class:playing={currentTrack?.id === track.id}
              role="button"
              tabindex="0"
              onmouseenter={() => (hoveredTrack = track.id)}
              onmouseleave={() => (hoveredTrack = null)}
              onclick={() => playTrack(track)}
              onkeydown={(e) => {
                if (e.key === "Enter" || e.key === " ") {
                  e.preventDefault();
                  playTrack(track);
                }
              }}
            >
              <div class="col-number">
                {#if hoveredTrack === track.id || currentTrack?.id === track.id}
                  <button
                    class="play-btn"
                    onclick={(e) => {
                      e.stopPropagation();
                      playTrack(track);
                    }}
                    title="Play"
                  >
                    {#if currentTrack?.id === track.id && isPlaying}
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
                      </svg>
                    {:else}
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8 5v14l11-7z" />
                      </svg>
                    {/if}
                  </button>
                {:else}
                  {index + 1}
                {/if}
              </div>
              <div class="col-title">
                <div class="track-info">
                  {#if track.coverArtUrl}
                    <img
                      src={track.coverArtUrl.startsWith("http")
                        ? track.coverArtUrl
                        : `http://localhost:1124${track.coverArtUrl}`}
                      alt={track.album || track.title}
                    />
                  {:else}
                    <div class="track-cover placeholder">🎵</div>
                  {/if}
                  <div class="track-text">
                    <div class="track-title">{track.title}</div>
                    <div class="track-artist">{track.artist}</div>
                  </div>
                </div>
              </div>
              <div class="col-album">{track.album || "-"}</div>
              <div class="col-plays">
                {track.playCount?.toLocaleString() || 0}
              </div>
              <div class="col-duration">{formatDuration(track.duration)}</div>
              <div class="col-actions">
                <button
                  class="remove-btn"
                  onclick={(e) => {
                    e.stopPropagation();
                    removeTrack(track.id);
                  }}
                  title="Remove from playlist"
                >
                  ✕
                </button>
              </div>
            </div>
          {/each}
        </div>
      {:else}
        <div class="empty-playlist">
          <p>This playlist is empty</p>
          <p class="hint">Add tracks to start building your playlist</p>
        </div>
      {/if}
    </div>
  </div>
{/if}

<style scoped lang="scss">
  @use "../styles/pages/PlaylistDetail.scss";
</style>
