<script lang="ts">
  import { onMount } from 'svelte';
  import { authService } from '../lib/authService';
  import { trackService, type Track } from '../lib/trackService';
  import AudioPlayer from '../components/AudioPlayer.svelte';

  export let onLogout: () => void;

  let user = authService.getUser();
  let tracks: Track[] = [];
  let currentTrack: Track | null = null;
  let searchQuery = '';
  let loading = true;
  let error = '';

  onMount(async () => {
    await loadTracks();
  });

  async function loadTracks() {
    try {
      loading = true;
      tracks = await trackService.getAllTracks();
    } catch (err: any) {
      error = 'Failed to load tracks. Please try again.';
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
      error = 'Search failed. Please try again.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  function playTrack(track: Track) {
    currentTrack = track;
  }

  function handleLogoutClick() {
    if (confirm('Are you sure you want to logout?')) {
      onLogout();
    }
  }

  function formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }
</script>

<div class="dashboard">
  <header class="dashboard-header">
    <div class="header-content">
      <h1>Streamletz</h1>
      <div class="header-actions">
        <span class="user-info">Welcome, {user?.username}!</span>
        <button class="btn btn-secondary" on:click={handleLogoutClick}>
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
          on:keyup={(e) => e.key === 'Enter' && handleSearch()}
          placeholder="Search for tracks, artists, or albums..."
        />
        <button class="btn btn-primary" on:click={handleSearch}>
          Search
        </button>
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
                <img src={track.coverArtUrl} alt={track.album} />
              {:else}
                <div class="cover-placeholder">🎵</div>
              {/if}
              <button class="play-overlay" on:click={() => playTrack(track)}>
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M8 5v14l11-7z"/>
                </svg>
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

  <footer class="dashboard-footer">
    <p class="motto">Your sound. Your stream. Your rules.</p>
    <p class="copyright">&copy; 2024 Streamletz. All rights reserved.</p>
  </footer>

  <AudioPlayer track={currentTrack} />
</div>

<style lang="scss">
  .dashboard {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    padding-bottom: 110px; // Space for audio player
  }

  .dashboard-header {
    background: $background-light;
    padding: $spacing-lg $spacing-xl;
    box-shadow: $shadow-sm;
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
        color: $primary-color;
        font-size: 1.75rem;
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: $spacing-lg;

        .user-info {
          color: $text-secondary;
          font-size: 0.9rem;
        }
      }
    }
  }

  .dashboard-content {
    flex: 1;
    max-width: 1400px;
    width: 100%;
    margin: 0 auto;
    padding: $spacing-xxl $spacing-xl;
  }

  .search-section {
    margin-bottom: $spacing-xxl;

    .search-bar {
      display: flex;
      gap: $spacing-md;
      max-width: 600px;

      input {
        flex: 1;
        padding: $spacing-md $spacing-lg;
        background: $background-light;
        border: 2px solid transparent;
        border-radius: $border-radius-lg;
        color: $text-primary;
        font-size: 1rem;
        transition: border-color $transition-fast;

        &:focus {
          border-color: $primary-color;
        }

        &::placeholder {
          color: $text-secondary;
        }
      }
    }
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: $spacing-xxl;
    gap: $spacing-lg;

    p {
      color: $text-secondary;
    }
  }

  .empty-state {
    text-align: center;
    padding: $spacing-xxl;
    color: $text-secondary;

    h3 {
      color: $text-primary;
      margin-bottom: $spacing-md;
    }
  }

  .tracks-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: $spacing-xl;
  }

  .track-card {
    background: $background-light;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    transition: all $transition-normal;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: $shadow-lg;

      .play-overlay {
        opacity: 1;
      }
    }

    &.playing {
      border: 2px solid $primary-color;
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
        font-size: 3rem;
      }

      .play-overlay {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 56px;
        height: 56px;
        background: $primary-color;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity $transition-fast;

        svg {
          width: 24px;
          height: 24px;
          margin-left: 3px;
        }

        &:hover {
          background: $accent-color;
        }
      }
    }

    .track-details {
      .track-title {
        font-size: 1rem;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: $spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-artist {
        font-size: 0.875rem;
        color: $text-secondary;
        margin-bottom: $spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-album {
        font-size: 0.8rem;
        color: $text-secondary;
        margin-bottom: $spacing-sm;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-meta {
        display: flex;
        justify-content: space-between;
        font-size: 0.75rem;
        color: $text-secondary;
        padding-top: $spacing-sm;
        border-top: 1px solid rgba($text-secondary, 0.2);
      }
    }
  }

  .dashboard-footer {
    background: $background-light;
    padding: $spacing-xl;
    text-align: center;
    margin-top: auto;

    .motto {
      color: $primary-color;
      font-style: italic;
      font-size: 1.1rem;
      margin-bottom: $spacing-sm;
    }

    .copyright {
      color: $text-secondary;
      font-size: 0.875rem;
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
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: $spacing-md;
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .search-section .search-bar {
      flex-direction: column;
    }

    .tracks-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
