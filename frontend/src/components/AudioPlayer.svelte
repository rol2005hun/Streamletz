<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import type { Track } from "../lib/trackService";
  import { trackService } from "../lib/trackService";

  let {
    track = $bindable(null),
    isPlaying = $bindable(false),
  }: {
    track: Track | null;
    isPlaying: boolean;
  } = $props();

  let audio: HTMLAudioElement | null = null;
  let currentTime = $state(0);
  let duration = $state(0);
  let volume = $state(0.7);
  let buffered = $state(0);
  let seeking = $state(false);

  // Load saved volume and track on mount
  onMount(() => {
    const savedVolume = localStorage.getItem('streamletz_volume');
    if (savedVolume) {
      volume = parseFloat(savedVolume);
    }
  });

  // Save volume to localStorage when it changes
  $effect(() => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('streamletz_volume', volume.toString());
    }
  });

  // Save current track and playback position
  $effect(() => {
    if (track && currentTime > 0 && typeof window !== 'undefined') {
      localStorage.setItem('streamletz_last_track', JSON.stringify({
        trackId: track.id,
        position: currentTime
      }));
    }
  });

  $effect(() => {
    if (track && audio) {
      loadTrack();
    }
  });

  async function loadTrack() {
    if (!track || !audio) return;

    audio.pause();
    currentTime = 0;
    isPlaying = false;

    const streamUrl = trackService.getStreamUrl(track.id);
    audio.src = streamUrl;
    audio.load();

    // Check if we should restore playback position
    const savedData = localStorage.getItem('streamletz_last_track');
    if (savedData) {
      try {
        const { trackId, position } = JSON.parse(savedData);
        if (trackId === track.id && position > 0 && position < duration - 5) {
          audio.currentTime = position;
        }
      } catch (e) {
        console.error('Failed to restore playback position:', e);
      }
    }

    try {
      await audio.play();
      isPlaying = true;
    } catch (err) {
      console.error("Playback failed:", err);
      isPlaying = false;
    }
  }

  function togglePlay() {
    if (!audio) return;

    if (isPlaying) {
      audio.pause();
    } else {
      audio.play();
    }
  }

  function handleTimeUpdate() {
    if (!audio || seeking) return;
    currentTime = audio.currentTime;
  }

  function handleLoadedMetadata() {
    if (!audio) return;
    duration = audio.duration;
  }

  function handleProgress() {
    if (!audio || audio.buffered.length === 0) return;
    buffered = (audio.buffered.end(audio.buffered.length - 1) / duration) * 100;
  }

  function handleSeek(e: Event) {
    if (!audio) return;
    const target = e.target as HTMLInputElement;
    currentTime = parseFloat(target.value);
    audio.currentTime = currentTime;
  }

  function handleVolumeChange(e: Event) {
    if (!audio) return;
    const target = e.target as HTMLInputElement;
    volume = parseFloat(target.value);
    audio.volume = volume;
  }

  function formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  onMount(() => {
    audio = new Audio();
    audio.volume = volume;

    audio.addEventListener("play", () => (isPlaying = true));
    audio.addEventListener("pause", () => (isPlaying = false));
    audio.addEventListener("timeupdate", handleTimeUpdate);
    audio.addEventListener("loadedmetadata", handleLoadedMetadata);
    audio.addEventListener("progress", handleProgress);
  });

  onDestroy(() => {
    if (audio) {
      audio.pause();
      audio.src = "";
    }
  });
</script>

<div class="audio-player" class:active={track !== null}>
  {#if track}
    <div class="player-info">
      <div class="album-art">
        {#if track.coverArtUrl}
          <img
            src={track.coverArtUrl.startsWith("http")
              ? track.coverArtUrl
              : `http://localhost:8080${track.coverArtUrl}`}
            alt={track.album}
          />
        {:else}
          <div class="placeholder">🎵</div>
        {/if}
      </div>
      <div class="track-info">
        <div class="track-title">{track.title}</div>
        <div class="track-artist">{track.artist}</div>
      </div>
    </div>

    <div class="player-controls">
      <button class="control-btn play-pause" onclick={togglePlay}>
        {#if isPlaying}
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
          </svg>
        {:else}
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M8 5v14l11-7z" />
          </svg>
        {/if}
      </button>

      <div class="progress-container">
        <span class="time">{formatTime(currentTime)}</span>
        <div class="progress-wrapper">
          <div class="buffer-bar" style="width: {buffered}%"></div>
          <input
            type="range"
            min="0"
            max={duration || 0}
            step="0.1"
            bind:value={currentTime}
            oninput={handleSeek}
            onmousedown={() => (seeking = true)}
            onmouseup={() => (seeking = false)}
            class="progress-bar"
          />
        </div>
        <span class="time">{formatTime(duration)}</span>
      </div>
    </div>

    <div class="player-volume">
      <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
        <path
          d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"
        />
      </svg>
      <input
        type="range"
        min="0"
        max="1"
        step="0.01"
        bind:value={volume}
        oninput={handleVolumeChange}
        class="volume-slider"
        style="--volume-percent: {volume * 100}%"
      />
    </div>
  {:else}
    <div class="player-placeholder">
      <p>Select a track to start playing</p>
    </div>
  {/if}
</div>

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .audio-player {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: $background-card;
    padding: $spacing-sm $spacing-xl;
    box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.4);
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    align-items: center;
    gap: $spacing-xl;
    z-index: $z-player;
    min-height: 90px;

    &:not(.active) {
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  .player-info {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    min-width: 280px;

    .album-art {
      width: 64px;
      height: 64px;
      border-radius: $border-radius-md;
      overflow: hidden;
      background: $gradient-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-md;
      position: relative;

      &::after {
        content: "";
        position: absolute;
        inset: 0;
        border: 2px solid rgba($primary-color, 0.3);
        border-radius: $border-radius-md;
        animation: pulse 2s infinite;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .placeholder {
        font-size: 2rem;
      }
    }

    .track-info {
      flex: 1;

      .track-title {
        font-weight: 700;
        font-size: 1.05rem;
        color: $text-primary;
        margin-bottom: $spacing-xs;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-artist {
        font-size: 0.9rem;
        color: $text-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .player-controls {
    flex: 1;
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .control-btn {
      background: transparent;
      border: 2px solid transparent;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      color: $text-secondary;

      svg {
        width: 20px;
        height: 20px;
      }

      &:hover {
        color: $text-primary;
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }

      &.play-pause {
        background: $text-primary;
        color: $spotify-dark;
        width: 36px;
        height: 36px;

        &:hover {
          background: $text-primary;
          transform: scale(1.08);
        }
      }
    }

    .progress-container {
      flex: 1;
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .time {
        font-size: 0.875rem;
        color: $text-secondary;
        min-width: 45px;
        text-align: center;
        font-weight: 500;
        font-variant-numeric: tabular-nums;
      }

      .progress-wrapper {
        flex: 1;
        position: relative;
        height: 4px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: $border-radius-full;
        overflow: visible;
        cursor: pointer;

        &:hover {
          height: 6px;
        }

        .buffer-bar {
          position: absolute;
          height: 100%;
          background: rgba(255, 255, 255, 0.15);
          border-radius: $border-radius-full;
          pointer-events: none;
          transition: width $transition-normal;
        }

        .progress-bar {
          position: absolute;
          width: 100%;
          height: 100%;
          opacity: 1;
          cursor: pointer;
          z-index: 2;
          appearance: none;
          background: transparent;

          &::-webkit-slider-runnable-track {
            width: 100%;
            height: 4px;
            background: transparent;
          }

          &::-moz-range-track {
            width: 100%;
            height: 4px;
            background: transparent;
          }

          &::-webkit-slider-thumb {
            appearance: none;
            width: 12px;
            height: 12px;
            background: $text-primary;
            border-radius: 50%;
            cursor: pointer;
            opacity: 1;
            transition: transform 0.2s ease;
            margin-top: -4px;

            &:hover {
              transform: scale(1.2);
            }
          }

          &::-moz-range-thumb {
            width: 12px;
            height: 12px;
            background: $text-primary;
            border-radius: 50%;
            border: none;
            cursor: pointer;
            opacity: 1;
            transition: transform 0.2s ease;

            &:hover {
              transform: scale(1.2);
            }
          }
        }

        &::after {
          content: "";
          position: absolute;
          height: 100%;
          background: $primary-color;
          border-radius: $border-radius-full;
          width: calc(var(--progress) * 1%);
          pointer-events: none;
        }
      }
    }
  }

  .player-volume {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: $spacing-md;
    min-width: 180px;

    .volume-icon {
      width: 24px;
      height: 24px;
      color: $text-secondary;
      transition: color $transition-fast;

      &:hover {
        color: $text-primary;
      }
    }

    .volume-slider {
      flex: 1;
      height: 4px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: $border-radius-full;
      outline: none;
      appearance: none;
      cursor: pointer;

      &::-webkit-slider-runnable-track {
        width: 100%;
        height: 4px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: $border-radius-full;
      }

      &::-moz-range-track {
        width: 100%;
        height: 4px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: $border-radius-full;
      }

      &::-webkit-slider-thumb {
        appearance: none;
        width: 12px;
        height: 12px;
        background: $text-primary;
        border-radius: 50%;
        cursor: pointer;
        transition: all 0.2s ease;
        margin-top: -4px;

        &:hover {
          transform: scale(1.2);
        }
      }

      &::-moz-range-thumb {
        width: 12px;
        height: 12px;
        background: $text-primary;
        border-radius: 50%;
        border: none;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
          transform: scale(1.2);
        }
      }

      &::-webkit-slider-runnable-track {
        background: linear-gradient(
          to right,
          $primary-color 0%,
          $primary-color var(--volume-percent, 70%),
          rgba(255, 255, 255, 0.1) var(--volume-percent, 70%)
        );
      }

      &::-moz-range-track {
        background: linear-gradient(
          to right,
          $primary-color 0%,
          $primary-color var(--volume-percent, 70%),
          rgba(255, 255, 255, 0.1) var(--volume-percent, 70%)
        );
      }
    }
  }

  .player-placeholder {
    width: 100%;
    text-align: center;
    color: $text-muted;
    font-size: 1.05rem;
    font-weight: 500;
    letter-spacing: 0.5px;
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 0.6;
    }
    50% {
      opacity: 0.2;
    }
  }

  @media (max-width: $breakpoint-tablet) {
    .audio-player {
      flex-direction: column;
      gap: $spacing-md;
      padding: $spacing-md $spacing-lg;
      min-height: auto;
    }

    .player-info {
      width: 100%;
      min-width: unset;
    }

    .player-controls {
      width: 100%;

      .progress-container .time {
        min-width: 40px;
      }
    }

    .player-volume {
      width: 100%;
      min-width: unset;
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .player-info .album-art {
      width: 56px;
      height: 56px;
    }

    .player-controls .control-btn {
      width: 44px;
      height: 44px;

      svg {
        width: 20px;
        height: 20px;
      }
    }
  }
</style>
