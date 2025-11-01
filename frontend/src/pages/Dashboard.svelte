<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import { authService } from "../lib/authService";
  import { trackService, type Track } from "../lib/trackService";
  import AudioPlayer from "../components/AudioPlayer.svelte";
  import Navbar from "../components/Navbar.svelte";
  import Sidebar from "../components/Sidebar.svelte";
  import "../styles/pages/Dashboard.scss";

  let { onLogout }: { onLogout: () => void } = $props();

  let user = authService.getUser();
  let tracks = $state<Track[]>([]);
  let currentTrack = $state<Track | null>(null);
  let isPlaying = $state(false);
  let searchQuery = $state("");
  let loading = $state(true);
  let error = $state("");
  let searchTimeout: ReturnType<typeof setTimeout> | null = null;
  let isFirstLoad = true;
  let sidebarCollapsed = $state(false);
  let sidebarWidth = $state(280);

  onMount(async () => {
    await loadTracks();
    isFirstLoad = false;
  });

  $effect(() => {
    console.log("Search query changed: ", searchQuery);

    if (isFirstLoad) return;

    const currentQuery = searchQuery;

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

      const savedData = localStorage.getItem("streamletz_last_playback");
      if (savedData && tracks.length > 0) {
        try {
          const { trackId } = JSON.parse(savedData);
          const lastTrack = tracks.find((t) => t.id === trackId);
          if (lastTrack) {
            currentTrack = lastTrack;
          }
        } catch (e) {
          console.error("Failed to restore last track: ", e);
        }
      }
    } catch (err: any) {
      error = "Failed to load tracks. Please try again.";
      console.error(err);
    } finally {
      loading = false;
    }
  }

  function playTrack(track: Track) {
    currentTrack = track;
    isPlaying = true;
  }

  function playNext() {
    if (!currentTrack || tracks.length === 0) return;

    const wasPlaying = isPlaying;
    const currentIndex = tracks.findIndex((t) => t.id === currentTrack!.id);
    if (currentIndex < tracks.length - 1) {
      currentTrack = tracks[currentIndex + 1];
    } else {
      currentTrack = tracks[0];
    }
    isPlaying = wasPlaying;
  }

  function playPrevious() {
    if (!currentTrack || tracks.length === 0) return;

    const wasPlaying = isPlaying;
    const currentIndex = tracks.findIndex((t) => t.id === currentTrack!.id);
    if (currentIndex > 0) {
      currentTrack = tracks[currentIndex - 1];
    } else {
      currentTrack = tracks[tracks.length - 1];
    }
    isPlaying = wasPlaying;
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
                  class="play-overlay"
                  onclick={() => playTrack(track)}
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
              </div>
            </div>
          {/each}
        </div>
      {/if}
    </main>
  </div>

  <AudioPlayer
    track={currentTrack}
    bind:isPlaying
    onNext={playNext}
    onPrevious={playPrevious}
  />
</div>