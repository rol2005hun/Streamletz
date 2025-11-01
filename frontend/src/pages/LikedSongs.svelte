<script lang="ts">
  import LikedSongs from "./LikedSongs.svelte";
  import { onMount } from "svelte";
  import { likedTrackService } from "../lib/likedTrackService";
  import type { Track } from "../lib/trackService";

  let tracks = $state<Track[]>([]);
  let loading = $state(true);
  let error = $state("");
  let hoveredTrack = $state<number | null>(null);

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
    const totalSeconds = tracks.reduce(
      (sum, track) => sum + (track.duration || 0),
      0,
    );
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);

    if (hours > 0) {
      return `${hours} hr ${minutes} min`;
    }
    return `${minutes} min`;
  }

  function playTrack(trackId: number) {
    console.log("Playing track:", trackId);
  }

  function goBack() {
    window.history.pushState({}, "", "/dashboard");
    window.dispatchEvent(new PopStateEvent("popstate"));
  }
</script>

<div class="liked-songs">
  <button class="back-btn" onclick={goBack} title="Back to Dashboard">
    ← Back
  </button>

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
      <div class="loading-state">
        <div class="spinner"></div>
        <p>Loading liked songs...</p>
      </div>
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
          <div
            class="track-row"
            role="button"
            tabindex="0"
            onmouseenter={() => (hoveredTrack = track.id)}
            onmouseleave={() => (hoveredTrack = null)}
          >
            <div class="col-number">
              {#if hoveredTrack === track.id}
                <button
                  class="play-btn"
                  onclick={() => playTrack(track.id)}
                  title="Play"
                >
                  ▶️
                </button>
              {:else}
                {index + 1}
              {/if}
            </div>
            <div class="col-title">
              <div class="track-info">
                {#if track.coverArtUrl}
                  <img
                    src={track.coverArtUrl}
                    alt={track.title}
                    class="track-cover"
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
            <div class="col-duration">
              {formatDuration(track.duration || 0)}
            </div>
            <div class="col-actions">
              <button
                class="unlike-btn"
                onclick={() => unlikeTrack(track.id)}
                title="Remove from liked songs"
              >
                ❤️
              </button>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>

<style scoped lang="scss">
  @use "../styles/pages/LikedSongs.scss";
</style>
