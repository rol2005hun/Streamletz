<script lang="ts">
  import { onMount } from "svelte";
  import { authService } from "../lib/authService";
  import { trackService, type Track } from "../lib/trackService";
  import AudioPlayer from "../components/AudioPlayer.svelte";

  let { onLogout }: { onLogout: () => void } = $props();

  let user = authService.getUser();
  let tracks = $state<Track[]>([]);
  let currentTrack = $state<Track | null>(null);
  let isPlaying = $state(false);
  let searchQuery = $state("");
  let loading = $state(true);
  let error = $state("");

  onMount(async () => {
    await loadTracks();
  });

  async function loadTracks() {
    try {
      loading = true;
      tracks = await trackService.getAllTracks();
    } catch (err: any) {
      error = "Failed to load tracks. Please try again.";
      console.error(err);
    } finally {
      loading = false;
    }
  }

  async function handleSearch() {
    if (!searchQuery.trim()) {
      await loadTracks();
      return;
    }

    try {
      loading = true;
      tracks = await trackService.searchTracks(searchQuery);
    } catch (err: any) {
      error = "Search failed. Please try again.";
      console.error(err);
    } finally {
      loading = false;
    }
  }

  function playTrack(track: Track) {
    currentTrack = track;
  }

  function handleLogoutClick() {
    onLogout();
  }

  function formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }
</script>

<div class="dashboard">
  <header class="dashboard-header">
    <div class="header-content">
      <h1>Streamletz</h1>
      <div class="header-actions">
        <span class="user-info">Welcome, {user?.username}!</span>
        <button class="btn btn-secondary" onclick={handleLogoutClick}>
          Logout
        </button>
      </div>
    </div>
  </header>

  <main class="dashboard-content">
    <div class="search-section">
      <div class="search-bar">
        <input
          type="text"
          bind:value={searchQuery}
          onkeyup={(e) => e.key === "Enter" && handleSearch()}
          placeholder="Search for tracks, artists, or albums..."
        />
        <button class="btn btn-primary" onclick={handleSearch}> Search </button>
      </div>
    </div>

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
          <div class="track-card" class:playing={currentTrack?.id === track.id}>
            <div class="track-cover">
              {#if track.coverArtUrl}
                <img 
                  src={track.coverArtUrl.startsWith('http') ? track.coverArtUrl : `http://localhost:8080${track.coverArtUrl}`} 
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

  <AudioPlayer track={currentTrack} bind:isPlaying />
</div>

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .dashboard {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    padding-bottom: 110px;
    position: relative;
    z-index: 1;
  }

  .dashboard-header {
    background: $background-card;
    padding: $spacing-md $spacing-xxl;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    position: sticky;
    top: 0;
    z-index: $z-header;

    .header-content {
      max-width: 1400px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;

      h1 {
        font-size: 2rem;
        color: $text-primary;
        font-weight: 700;
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: $spacing-lg;

        .user-info {
          color: $text-secondary;
          font-size: 0.95rem;
          font-weight: 500;
        }

        .btn {
          padding: $spacing-sm $spacing-lg;
          background: transparent;
          border: 1px solid $text-secondary;
          color: $text-primary;
          font-size: 0.875rem;
          font-weight: 600;
          
          &:hover {
            border-color: $text-primary;
            background: transparent;
            transform: scale(1.05);
          }
        }
      }
    }
  }

  .dashboard-content {
    flex: 1;
    max-width: 1400px;
    width: 100%;
    margin: 0 auto;
    padding: $spacing-xxxl $spacing-xxl;
  }

  .search-section {
    margin-bottom: $spacing-xxxl;

    .search-bar {
      display: flex;
      gap: $spacing-md;
      max-width: 700px;
      margin: 0 auto;

      input {
        flex: 1;
        padding: $spacing-md $spacing-xl;
        background: $background-light;
        border: none;
        border-radius: $border-radius-full;
        color: $text-primary;
        font-size: 0.95rem;
        transition: all $transition-normal;

        &:focus {
          outline: 2px solid $text-primary;
          background: $background-light;
        }

        &::placeholder {
          color: $text-muted;
        }
      }

      .btn {
        padding: $spacing-md $spacing-xxl;
        background: $primary-color;
        color: $spotify-dark;
        font-weight: 700;
        
        &:hover {
          background: $primary-light;
          transform: scale(1.05);
        }
      }
    }
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $spacing-xxxl;
    gap: $spacing-lg;

    p {
      color: $text-secondary;
      font-size: 1.1rem;
    }
  }

  .empty-state {
    text-align: center;
    padding: $spacing-xxxl;
    color: $text-secondary;

    h3 {
      color: $text-primary;
      margin-bottom: $spacing-md;
      font-size: 1.75rem;
    }

    p {
      font-size: 1.1rem;
    }
  }

  .tracks-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: $spacing-xl;
  }

  .track-card {
    background: $background-card;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    transition: all $transition-normal;
    cursor: pointer;
    position: relative;
    overflow: hidden;

    &:hover {
      background: $background-light;
      box-shadow: $shadow-lg;

      .play-overlay {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
      }
    }

    &.playing {
      .track-title {
        color: $primary-color;
      }

      .play-overlay {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
        background: $primary-color;
      }
    }

    .track-cover {
      position: relative;
      width: 100%;
      aspect-ratio: 1;
      border-radius: $border-radius-md;
      overflow: hidden;
      background: $background-dark;
      margin-bottom: $spacing-md;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .cover-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 3.5rem;
        background: $gradient-primary;
        opacity: 0.3;
      }

      .play-overlay {
        position: absolute;
        bottom: $spacing-md;
        right: $spacing-md;
        width: 48px;
        height: 48px;
        background: $primary-color;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0.9;
        transform: scale(1);
        transition: all 0.2s ease;
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.4);
        z-index: 2;

        svg {
          width: 20px;
          height: 20px;
          color: $spotify-dark;
          margin-left: 2px;
        }

        &:hover {
          transform: scale(1.1);
          background: $primary-light;
          opacity: 1;
        }
      }
    }

    .track-details {
      position: relative;
      z-index: 1;

      .track-title {
        font-size: 1.05rem;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: $spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-artist {
        font-size: 0.9rem;
        color: $text-secondary;
        margin-bottom: $spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: 500;
      }

      .track-album {
        font-size: 0.85rem;
        color: $text-muted;
        margin-bottom: $spacing-sm;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-meta {
        display: flex;
        justify-content: space-between;
        font-size: 0.8rem;
        color: $text-muted;
        padding-top: $spacing-sm;
        border-top: 1px solid rgba($primary-color, 0.1);
      }
    }
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.5;
    }
  }

  @media (max-width: $breakpoint-tablet) {
    .dashboard-header {
      .header-content {
        flex-direction: column;
        gap: $spacing-md;
        text-align: center;
      }
    }

    .tracks-grid {
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: $spacing-lg;
    }

    .dashboard-content {
      padding: $spacing-xxl $spacing-lg;
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .search-section .search-bar {
      flex-direction: column;
    }

    .tracks-grid {
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: $spacing-md;
    }

    .dashboard-content {
      padding: $spacing-xl $spacing-md;
    }
  }
</style>