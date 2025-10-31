<script lang="ts">
  import { onMount, onDestroy } from "svelte";
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
  let dropdownOpen = $state(false);

  onMount(async () => {
    await loadTracks();
  });

  async function loadTracks() {
    try {
      loading = true;
      tracks = await trackService.getAllTracks();

      const savedData = localStorage.getItem("streamletz_last_track");
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

  function playNext() {
    if (!currentTrack || tracks.length === 0) return;

    const currentIndex = tracks.findIndex((t) => t.id === currentTrack!.id);
    if (currentIndex < tracks.length - 1) {
      currentTrack = tracks[currentIndex + 1];
    } else {
      currentTrack = tracks[0];
    }
  }

  function playPrevious() {
    if (!currentTrack || tracks.length === 0) return;

    const currentIndex = tracks.findIndex((t) => t.id === currentTrack!.id);
    if (currentIndex > 0) {
      currentTrack = tracks[currentIndex - 1];
    } else {
      currentTrack = tracks[tracks.length - 1];
    }
  }

  function handleLogoutClick() {
    dropdownOpen = false;
    onLogout();
  }

  function toggleDropdown() {
    dropdownOpen = !dropdownOpen;
  }

  function handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-dropdown')) {
      dropdownOpen = false;
    }
  }

  onMount(async () => {
    await loadTracks();
    document.addEventListener("click", handleClickOutside);
  });

  onDestroy(() => {
    document.removeEventListener("click", handleClickOutside);
  });

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
      
      <div class="search-bar">
        <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
          <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
        </svg>
        <input
          type="text"
          bind:value={searchQuery}
          onkeyup={(e) => e.key === "Enter" && handleSearch()}
          placeholder="Search for tracks, artists, or albums..."
        />
      </div>

      <div class="profile-dropdown">
        <button class="profile-btn" onclick={toggleDropdown}>
          <div class="profile-avatar">
            {user?.username?.charAt(0).toUpperCase()}
          </div>
        </button>
        
        {#if dropdownOpen}
          <div class="dropdown-menu">
            <div class="dropdown-header">
              <div class="dropdown-avatar">
                {user?.username?.charAt(0).toUpperCase()}
              </div>
              <div class="dropdown-user-info">
                <div class="dropdown-username">{user?.username}</div>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <button class="dropdown-item" onclick={() => dropdownOpen = false}>
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
              Profile
            </button>
            <button class="dropdown-item" onclick={() => dropdownOpen = false}>
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
              </svg>
              Settings
            </button>
            <div class="dropdown-divider"></div>
            <button class="dropdown-item" onclick={handleLogoutClick}>
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
              </svg>
              Logout
            </button>
          </div>
        {/if}
      </div>
    </div>
  </header>

  <main class="dashboard-content">
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

  <AudioPlayer
    track={currentTrack}
    bind:isPlaying
    onNext={playNext}
    onPrevious={playPrevious}
  />
</div>

<style lang="scss">
  @use '../styles/variables.scss' as *;

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
    padding: $spacing-md $spacing-lg;
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
      gap: $spacing-lg;

      h1 {
        font-size: 1.75rem;
        color: $text-primary;
        font-weight: 700;
        white-space: nowrap;
      }

      .search-bar {
        flex: 1;
        max-width: 500px;
        position: relative;
        display: flex;
        align-items: center;

        .search-icon {
          position: absolute;
          left: $spacing-md;
          width: 20px;
          height: 20px;
          color: $text-secondary;
          pointer-events: none;
          z-index: 1;
        }

        input {
          width: 100%;
          padding: $spacing-sm $spacing-md $spacing-sm 48px;
          background: $background-dark;
          border: 1px solid rgba(255, 255, 255, 0.1);
          border-radius: $border-radius-full;
          color: $text-primary;
          font-size: 0.95rem;
          transition: all $transition-fast;

          &::placeholder {
            color: $text-muted;
          }

          &:focus {
            outline: none;
            border-color: $primary-color;
            background: $background-light;
          }
        }
      }

      .profile-dropdown {
        position: relative;
        flex-shrink: 0;

        .profile-btn {
          background: transparent;
          border: none;
          padding: 0;
          cursor: pointer;
          transition: transform $transition-fast;

          &:hover {
            transform: scale(1.05);
          }

          .profile-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: $gradient-primary;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 1.1rem;
            color: $text-primary;
            box-shadow: $shadow-md;
          }
        }

        .dropdown-menu {
          position: absolute;
          top: calc(100% + $spacing-sm);
          right: 0;
          min-width: 240px;
          background: $background-card;
          border-radius: $border-radius-lg;
          box-shadow: $shadow-xl;
          border: 1px solid rgba(255, 255, 255, 0.1);
          overflow: hidden;
          animation: slideDown 0.2s ease;

          .dropdown-header {
            padding: $spacing-lg;
            display: flex;
            align-items: center;
            gap: $spacing-md;

            .dropdown-avatar {
              width: 48px;
              height: 48px;
              border-radius: 50%;
              background: $gradient-primary;
              display: flex;
              align-items: center;
              justify-content: center;
              font-weight: 700;
              font-size: 1.3rem;
              color: $text-primary;
            }

            .dropdown-user-info {
              flex: 1;

              .dropdown-username {
                font-weight: 600;
                color: $text-primary;
                font-size: 1rem;
              }
            }
          }

          .dropdown-divider {
            height: 1px;
            background: rgba(255, 255, 255, 0.1);
          }

          .dropdown-item {
            width: 100%;
            display: flex;
            align-items: center;
            gap: $spacing-md;
            padding: $spacing-md $spacing-lg;
            background: transparent;
            border: none;
            color: $text-primary;
            font-size: 0.95rem;
            font-weight: 500;
            cursor: pointer;
            transition: background $transition-fast;
            text-align: left;

            svg {
              width: 20px;
              height: 20px;
              color: $text-secondary;
            }

            &:hover {
              background: rgba(255, 255, 255, 0.05);
            }

            &:last-child {
              color: #f87171;

              svg {
                color: #f87171;
              }
            }
          }
        }
      }
    }
  }

  @media (max-width: $breakpoint-tablet) {
    .dashboard-header {
      padding: $spacing-sm $spacing-md;

      .header-content {
        gap: $spacing-md;

        h1 {
          font-size: 1.5rem;
        }

        .search-bar {
          max-width: 300px;

          input {
            font-size: 0.9rem;
            padding: $spacing-xs $spacing-sm $spacing-xs 40px;
          }

          .search-icon {
            left: $spacing-sm;
            width: 18px;
            height: 18px;
          }
        }

        .profile-dropdown .profile-btn .profile-avatar {
          width: 36px;
          height: 36px;
          font-size: 1rem;
        }
      }
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .dashboard-header {
      .header-content {
        flex-wrap: wrap;

        h1 {
          font-size: 1.3rem;
          flex: 0 0 auto;
        }

        .search-bar {
          flex: 1 1 100%;
          max-width: 100%;
          order: 3;
        }

        .profile-dropdown {
          flex: 0 0 auto;
        }
      }
    }
  }

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .dashboard-content {
    flex: 1;
    max-width: 1400px;
    width: 100%;
    margin: 0 auto;
    padding: $spacing-xxxl $spacing-xxl;
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
        bottom: $spacing-sm;
        right: $spacing-sm;
        width: 40px;
        height: 40px;
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
    .dashboard-content {
      padding: $spacing-xxl $spacing-lg;
    }

    .tracks-grid {
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: $spacing-lg;
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .dashboard-content {
      padding: $spacing-xl $spacing-md;
    }

    .tracks-grid {
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: $spacing-md;
    }

    .track-card {
      .track-cover {
        height: 140px;

        .play-overlay {
          width: 36px;
          height: 36px;
          bottom: $spacing-xs;
          right: $spacing-xs;

          svg {
            width: 18px;
            height: 18px;
          }
        }
      }

      .track-details {
        .track-title {
          font-size: 0.95rem;
        }

        .track-artist,
        .track-album {
          font-size: 0.85rem;
        }

        .track-meta {
          font-size: 0.75rem;
        }
      }
    }
  }
</style>