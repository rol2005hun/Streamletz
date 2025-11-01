<script lang="ts">
  import { onMount } from "svelte";
  import { playlistService, type Playlist } from "../lib/playlistService";

  interface Props {
    id: string;
  }

  let { id }: Props = $props();
  let playlist = $state<Playlist | null>(null);
  let loading = $state(true);
  let error = $state("");

  onMount(async () => {
    await loadPlaylist();
  });

  async function loadPlaylist() {
    try {
      loading = true;
      playlist = await playlistService.getPlaylistById(parseInt(id));
    } catch (err: any) {
      error = err.response?.data?.message || "Failed to load playlist";
    } finally {
      loading = false;
    }
  }

  async function removeTrack(trackId: number) {
    if (!playlist) return;

    try {
      playlist = await playlistService.removeTrackFromPlaylist(playlist.id, trackId);
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
</script>

{#if loading}
  <div class="loading">Loading playlist...</div>
{:else if error}
  <div class="error">{error}</div>
{:else if playlist}
  <div class="playlist-detail">
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
              <div class="col-duration">{formatDuration(track.duration)}</div>
              <div class="col-actions">
                <button class="remove-btn" onclick={() => removeTrack(track.id)} title="Remove from playlist">
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

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .loading,
  .error {
    text-align: center;
    padding: 2rem;
    color: $text-secondary;
  }

  .error {
    color: $error-color;
  }

  .playlist-detail {
    padding: 2rem;
    max-width: 1400px;
    margin: 0 auto;
  }

  .playlist-header {
    display: flex;
    gap: 2rem;
    margin-bottom: 2rem;
    padding: 2rem;
    background: linear-gradient(180deg, rgba($primary-color, 0.3) 0%, transparent 100%);
    border-radius: $border-radius-md;

    .cover {
      flex-shrink: 0;
      width: 232px;
      height: 232px;
      border-radius: $border-radius-md;
      overflow: hidden;
      box-shadow: $shadow-lg;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .default-cover {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, $primary-color, darken($primary-color, 20%));
        display: flex;
        align-items: center;
        justify-content: center;

        span {
          font-size: 6rem;
        }
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

      .description {
        color: $text-secondary;
        margin: 0.5rem 0;
      }

      .meta {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        color: $text-primary;
        font-size: 0.875rem;
        font-weight: 600;

        .owner {
          font-weight: 700;
        }

        .separator {
          color: $text-secondary;
        }
      }
    }
  }

  .tracks-section {
    background: $background-card;
    border-radius: $border-radius-md;
    padding: 1rem;
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

        .remove-btn {
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

        .remove-btn {
          background: none;
          border: none;
          color: $text-secondary;
          cursor: pointer;
          font-size: 1.25rem;
          opacity: 0;
          transition: all $transition-fast;
          padding: 0.25rem;
          width: 32px;
          height: 32px;
          border-radius: 50%;

          &:hover {
            color: $text-primary;
            background: rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }

  .empty-playlist {
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

  @media (max-width: $breakpoint-tablet) {
    .playlist-header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .cover {
        width: 180px;
        height: 180px;
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
