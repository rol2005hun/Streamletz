<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import { authService } from "../lib/authService";
  import { trackService, type Track } from "../lib/trackService";
  import { playlistService, type Playlist } from "../lib/playlistService";
  import { likedTrackService } from "../lib/likedTrackService";
  import Navbar from "../components/Navbar.svelte";
  import Sidebar from "../components/Sidebar.svelte";

  let {
    onLogout,
    currentTrack = $bindable(),
    isPlaying = $bindable(),
    allTracks = $bindable(),
  }: {
    onLogout: () => void;
    currentTrack: Track | null;
    isPlaying: boolean;
    allTracks: Track[];
  } = $props();

  let user = authService.getUser();
  let tracks = $state<Track[]>([]);
  let searchQuery = $state("");
  let loading = $state(true);
  let error = $state("");
  let searchTimeout: ReturnType<typeof setTimeout> | null = null;
  let isFirstLoad = true;
  let sidebarCollapsed = $state(false);
  let sidebarWidth = $state(280);
  let showPlaylistModal = $state(false);
  let selectedTrackForPlaylist = $state<Track | null>(null);
  let userPlaylists = $state<Playlist[]>([]);
  let playlistsLoading = $state(false);
  let likedTracks = $state<Set<number>>(new Set());

  onMount(async () => {
    await loadTracks();
    await loadLikedTracks();
    isFirstLoad = false;
  });

  $effect(() => {
    const currentQuery = searchQuery;

    if (isFirstLoad) return;

    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }

    searchTimeout = setTimeout(async () => {
      if (!currentQuery.trim()) {
        try {
          loading = true;
          tracks = await trackService.getAllTracks();
        } catch (err: any) {
          error = "Failed to load tracks. Please try again.";
          console.error(err);
        } finally {
          loading = false;
        }
      } else {
        try {
          loading = true;
          tracks = await trackService.searchTracks(currentQuery);
        } catch (err: any) {
          error = "Search failed. Please try again.";
          console.error(err);
        } finally {
          loading = false;
        }
      }
    }, 300);
  });

  async function loadTracks() {
    try {
      loading = true;
      tracks = await trackService.getAllTracks();
      allTracks = tracks;
    } catch (err: any) {
      error = "Failed to load tracks. Please try again.";
      console.error(err);
    } finally {
      loading = false;
    }
  }

  async function loadLikedTracks() {
    try {
      const liked = await likedTrackService.getLikedTracks();
      likedTracks = new Set(liked.map((track) => track.id));
    } catch (err: any) {
      console.error("Failed to load liked tracks:", err);
    }
  }

  function playTrack(track: Track) {
    if (currentTrack?.id === track.id) {
      isPlaying = !isPlaying;
    } else {
      currentTrack = track;
      isPlaying = true;
    }
  }

  onMount(async () => {
    await loadTracks();
  });

  onDestroy(() => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
  });

  function formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  async function openAddToPlaylistModal(track: Track) {
    selectedTrackForPlaylist = track;
    showPlaylistModal = true;
    playlistsLoading = true;
    try {
      userPlaylists = await playlistService.getUserPlaylists();
    } catch (err: any) {
      error = "Failed to load playlists";
      console.error(err);
    } finally {
      playlistsLoading = false;
    }
  }

  async function addToPlaylist(playlistId: number) {
    if (!selectedTrackForPlaylist) return;

    try {
      await playlistService.addTrackToPlaylist(
        playlistId,
        selectedTrackForPlaylist.id,
      );
      showPlaylistModal = false;
      selectedTrackForPlaylist = null;
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to add track to playlist";
      console.error(err);
    }
  }

  function closePlaylistModal() {
    showPlaylistModal = false;
    selectedTrackForPlaylist = null;
  }

  async function toggleLike(track: Track, e: MouseEvent) {
    e.stopPropagation();
    const isLiked = likedTracks.has(track.id);

    try {
      await likedTrackService.toggleLike(track.id, isLiked);

      if (isLiked) {
        const newSet = new Set(likedTracks);
        newSet.delete(track.id);
        likedTracks = newSet;
      } else {
        const newSet = new Set(likedTracks);
        newSet.add(track.id);
        likedTracks = newSet;
      }
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to update like status";
      console.error(err);
    }
  }
</script>

<div class="dashboard">
  <Navbar {user} bind:searchQuery {onLogout} />

  <div class="main-view">
    <Sidebar bind:collapsed={sidebarCollapsed} bind:width={sidebarWidth} />

    <main
      class="dashboard-content"
      style="margin-left: {sidebarCollapsed ? 0 : sidebarWidth}px"
    >
      {#if error}
        <div class="error-message">{error}</div>
      {/if}

      {#if loading}
        <div class="loading-container">
          <div class="loading"></div>
          <p>Loading tracks...</p>
        </div>
      {:else if tracks.length === 0}
        <div class="empty-state">
          <h3>No tracks found</h3>
          <p>Try a different search or check back later for new content.</p>
        </div>
      {:else}
        <div class="tracks-grid">
          {#each tracks as track (track.id)}
            <div
              class="track-card"
              class:playing={currentTrack?.id === track.id}
            >
              <div class="track-cover">
                {#if track.coverArtUrl}
                  <img
                    src={track.coverArtUrl.startsWith("http")
                      ? track.coverArtUrl
                      : `http://localhost:8080${track.coverArtUrl}`}
                    alt={track.album || track.title}
                  />
                {:else}
                  <div class="cover-placeholder">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
                      />
                    </svg>
                  </div>
                {/if}
                <button
                  class="like-btn"
                  class:liked={likedTracks.has(track.id)}
                  onclick={(e) => toggleLike(track, e)}
                  title={likedTracks.has(track.id) ? "Unlike" : "Like"}
                >
                  {#if likedTracks.has(track.id)}
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                      />
                    </svg>
                  {:else}
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"
                      />
                    </svg>
                  {/if}
                </button>
                <button
                  class="play-overlay"
                  onclick={() => {
                    playTrack(track);
                  }}
                  aria-label="Play {track.title}"
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
              </div>
              <div class="track-details">
                <h3 class="track-title">{track.title}</h3>
                <p class="track-artist">{track.artist}</p>
                {#if track.album}
                  <p class="track-album">{track.album}</p>
                {/if}
                <div class="track-meta">
                  <span class="duration">{formatDuration(track.duration)}</span>
                  <span class="plays">{track.playCount} plays</span>
                </div>
                <button
                  class="add-to-playlist-btn"
                  onclick={() => openAddToPlaylistModal(track)}
                  title="Add to playlist"
                >
                  + Add to Playlist
                </button>
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </main>
  </div>

  {#if showPlaylistModal}
    <div
      class="modal-overlay"
      onclick={closePlaylistModal}
      onkeydown={(e) => e.key === "Escape" && closePlaylistModal()}
      role="button"
      tabindex="-1"
      aria-label="Close modal"
    >
      <div
        class="modal"
        onclick={(e) => e.stopPropagation()}
        onkeydown={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        tabindex="-1"
      >
        <div class="modal-header">
          <h2 id="modal-title">Add to Playlist</h2>
          <p class="modal-subtitle">
            {selectedTrackForPlaylist?.title} - {selectedTrackForPlaylist?.artist}
          </p>
        </div>

        <div class="modal-content">
          {#if playlistsLoading}
            <div class="loading-state">
              <div class="spinner"></div>
              <p>Loading playlists...</p>
            </div>
          {:else if userPlaylists.length === 0}
            <div class="empty-state">
              <p>You don't have any playlists yet.</p>
            </div>
          {:else}
            <div class="playlists-list">
              {#each userPlaylists as playlist}
                <button
                  class="playlist-item"
                  onclick={() => addToPlaylist(playlist.id)}
                >
                  <div class="playlist-icon">🎵</div>
                  <div class="playlist-info">
                    <div class="playlist-name">{playlist.name}</div>
                    <div class="playlist-count">
                      {playlist.trackCount} tracks
                    </div>
                  </div>
                </button>
              {/each}
            </div>
          {/if}
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" onclick={closePlaylistModal}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  {/if}
</div>

<style scoped lang="scss">
  @use "../styles/pages/Dashboard.scss";
</style>
