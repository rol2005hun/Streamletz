<script lang="ts">
  import { onMount } from "svelte";
  import { likedTrackService } from "../lib/likedTrackService";
  import type { Track } from "../lib/trackService";

  let tracks = $state<Track[]>([]);
  let loading = $state(true);
  let error = $state("");

  onMount(async () => {
    await loadLikedTracks();
  });

  async function loadLikedTracks() {
    try {
      loading = true;
      tracks = await likedTrackService.getLikedTracks();
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to load liked tracks";
    } finally {
      loading = false;
    }
  }

  async function unlikeTrack(trackId: number) {
    try {
      await likedTrackService.unlikeTrack(trackId);
      tracks = tracks.filter((t) => t.id !== trackId);
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to unlike track";
    }
  }

  function formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  function getTotalDuration(): string {
    const totalSeconds = tracks.reduce((sum, track) => sum + (track.duration || 0), 0);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);

    if (hours > 0) {
      return `${hours} hr ${minutes} min`;
    }
    return `${minutes} min`;
  }
</script>

<div class="liked-songs">
  <div class="header">
    <div class="cover">
      <span class="heart-icon">❤️</span>
    </div>
    <div class="info">
      <span class="type">Playlist</span>
      <h1>Liked Songs</h1>
      <div class="meta">
        <span>{tracks.length} songs</span>
        {#if tracks.length > 0}
          <span class="separator">•</span>
          <span>{getTotalDuration()}</span>
        {/if}
      </div>
    </div>
  </div>

  <div class="tracks-section">
    {#if loading}
      <div class="loading">Loading liked songs...</div>
    {:else if error}
      <div class="error">{error}</div>
    {:else if tracks.length === 0}
      <div class="empty">
        <p>You haven't liked any songs yet</p>
        <p class="hint">Find songs you love and tap the heart icon</p>
      </div>
    {:else}
      <div class="tracks-header">
        <div class="col-number">#</div>
        <div class="col-title">Title</div>
        <div class="col-album">Album</div>
        <div class="col-plays">Plays</div>
        <div class="col-duration">Duration</div>
        <div class="col-actions"></div>
      </div>

      <div class="tracks-list">
        {#each tracks as track, index}
          <div class="track-row">
            <div class="col-number">{index + 1}</div>
            <div class="col-title">
              <div class="track-info">
                {#if track.coverArtUrl}
                  <img src={track.coverArtUrl} alt={track.title} class="track-cover" />
                {/if}
                <div>
                  <div class="track-title">{track.title}</div>
                  <div class="track-artist">{track.artist}</div>
                </div>
              </div>
            </div>
            <div class="col-album">{track.album || "-"}</div>
            <div class="col-plays">{track.playCount || 0}</div>
            <div class="col-duration">{formatDuration(track.duration || 0)}</div>
            <div class="col-actions">
              <button class="unlike-btn" onclick={() => unlikeTrack(track.id)} title="Remove from liked songs">
                ❤️
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .liked-songs {
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
  }

  .header {
    display: flex;
    gap: 2rem;
    margin-bottom: 2rem;
    padding: 2rem;
    background: linear-gradient(135deg, #5f27cd 0%, #341f97 100%);
    border-radius: $border-radius-md;

    .cover {
      flex-shrink: 0;
      width: 232px;
      height: 232px;
      background: linear-gradient(135deg, #ee5a6f 0%, #f29263 100%);
      border-radius: $border-radius-md;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-lg;

      .heart-icon {
        font-size: 8rem;
        filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.5));
      }
    }

    .info {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      gap: 0.5rem;

      .type {
        font-size: 0.875rem;
        font-weight: 700;
        text-transform: uppercase;
        color: $text-primary;
      }

      h1 {
        font-size: 3rem;
        font-weight: 900;
        color: $text-primary;
        margin: 0;
        line-height: 1.2;
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: $text-primary;
        font-size: 0.875rem;
        font-weight: 600;

        .separator {
          color: rgba(255, 255, 255, 0.7);
        }
      }
    }
  }

  .tracks-section {
    background: $background-card;
    border-radius: $border-radius-md;
    padding: 1rem;
  }

  .loading,
  .error {
    text-align: center;
    padding: 2rem;
    color: $text-secondary;
  }

  .error {
    color: $error-color;
  }

  .empty {
    text-align: center;
    padding: 4rem 2rem;

    p {
      font-size: 1.2rem;
      color: $text-secondary;
      margin-bottom: 0.5rem;
    }

    .hint {
      font-size: 0.875rem;
      color: $text-muted;
    }
  }

  .tracks-header {
    display: grid;
    grid-template-columns: 50px 1fr 1fr 100px 100px 50px;
    gap: 1rem;
    padding: 0.5rem 1rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    color: $text-secondary;
    font-size: 0.875rem;
    font-weight: 600;
    text-transform: uppercase;
  }

  .tracks-list {
    .track-row {
      display: grid;
      grid-template-columns: 50px 1fr 1fr 100px 100px 50px;
      gap: 1rem;
      padding: 0.5rem 1rem;
      border-radius: $border-radius-sm;
      align-items: center;
      transition: background $transition-fast;

      &:hover {
        background: rgba(255, 255, 255, 0.05);

        .unlike-btn {
          opacity: 1;
        }
      }

      .col-number {
        color: $text-secondary;
        font-size: 0.875rem;
      }

      .col-title {
        .track-info {
          display: flex;
          align-items: center;
          gap: 0.75rem;

          .track-cover {
            width: 40px;
            height: 40px;
            border-radius: 4px;
            object-fit: cover;
          }

          .track-title {
            color: $text-primary;
            font-weight: 600;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .track-artist {
            color: $text-secondary;
            font-size: 0.875rem;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      .col-album {
        color: $text-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .col-plays {
        color: $text-secondary;
      }

      .col-duration {
        color: $text-secondary;
      }

      .col-actions {
        display: flex;
        justify-content: center;

        .unlike-btn {
          background: none;
          border: none;
          cursor: pointer;
          font-size: 1.25rem;
          opacity: 0;
          transition: all $transition-fast;
          padding: 0.25rem;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          filter: grayscale(0);

          &:hover {
            filter: grayscale(100%);
            transform: scale(1.1);
          }
        }
      }
    }
  }

  @media (max-width: $breakpoint-tablet) {
    .header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .cover {
        width: 180px;
        height: 180px;

        .heart-icon {
          font-size: 6rem;
        }
      }

      .info h1 {
        font-size: 2rem;
      }
    }

    .tracks-header,
    .track-row {
      grid-template-columns: 30px 1fr 80px 40px;

      .col-album,
      .col-plays {
        display: none;
      }
    }
  }
</style>
