<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import type { Track } from "../lib/trackService";
  import { trackService } from "../lib/trackService";

  let { 
    track = $bindable(null),
    isPlaying = $bindable(false)
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

  $effect(() => {
    if (track && audio) {
      loadTrack();
      // Automatikusan indítsd el a lejátszást
      audio.play().catch((err) => {
        console.error('Playback failed:', err);
      });
    }
  });

  function loadTrack() {
    if (!track || !audio) return;

    const streamUrl = trackService.getStreamUrl(track.id);
    audio.src = streamUrl;
    audio.load();
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
          <img src={track.coverArtUrl} alt={track.album} />
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
      <button class="control-btn" onclick={togglePlay}>
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
    background: rgba($background-card, 0.95);
    backdrop-filter: blur(30px);
    padding: $spacing-lg $spacing-xl;
    box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.4);
    border-top: 1px solid rgba($primary-color, 0.2);
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

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: $gradient-primary;
      opacity: 0.5;
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
      background: $gradient-primary;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all $transition-normal;
      box-shadow: $shadow-glow;

      svg {
        width: 24px;
        height: 24px;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
      }

      &:hover {
        transform: scale(1.1);
        box-shadow: $shadow-glow-hover;
      }

      &:active {
        transform: scale(0.95);
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
        height: 6px;
        background: rgba($background-dark, 0.5);
        border-radius: $border-radius-full;
        overflow: hidden;

        .buffer-bar {
          position: absolute;
          height: 100%;
          background: rgba($primary-color, 0.2);
          border-radius: $border-radius-full;
          pointer-events: none;
          transition: width $transition-normal;
        }

        .progress-bar {
          position: absolute;
          width: 100%;
          height: 100%;
          opacity: 0;
          cursor: pointer;
          z-index: 2;
          appearance: none;

          &::-webkit-slider-thumb {
            appearance: none;
            width: 16px;
            height: 16px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            cursor: pointer;
            box-shadow: 0 0 20px rgba(102, 102, 241, 0.3);
            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

            &:hover {
              transform: scale(1.2);
            }
          }

          &::-moz-range-thumb {
            width: 16px;
            height: 16px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            border: none;
            cursor: pointer;
            box-shadow: 0 0 20px rgba(102, 102, 241, 0.3);
            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

            &:hover {
              transform: scale(1.2);
            }
          }
        }

        &::after {
          content: "";
          position: absolute;
          height: 100%;
          background: $gradient-primary;
          border-radius: $border-radius-full;
          width: calc(var(--progress) * 1%);
          pointer-events: none;
          box-shadow: 0 0 10px rgba($primary-color, 0.5);
        }
      }
    }
  }

  .player-volume {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    min-width: 180px;

    .volume-icon {
      width: 24px;
      height: 24px;
      color: $text-secondary;
      transition: color $transition-fast;

      &:hover {
        color: $primary-light;
      }
    }

    .volume-slider {
      flex: 1;
      height: 6px;
      background: rgba($background-dark, 0.5);
      border-radius: $border-radius-full;
      outline: none;
      appearance: none;
      cursor: pointer;

      &::-webkit-slider-thumb {
        appearance: none;
        width: 14px;
        height: 14px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 50%;
        cursor: pointer;
        box-shadow: 0 0 20px rgba(102, 102, 241, 0.3);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: scale(1.2);
        }
      }

      &::-moz-range-thumb {
        width: 14px;
        height: 14px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 50%;
        border: none;
        cursor: pointer;
        box-shadow: 0 0 20px rgba(102, 102, 241, 0.3);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: scale(1.2);
        }
      }

      &::-webkit-slider-runnable-track {
        height: 6px;
        background: linear-gradient(
          to right,
          $primary-color 0%,
          $primary-color var(--volume-percent, 70%),
          rgba($background-dark, 0.5) var(--volume-percent, 70%)
        );
        border-radius: $border-radius-full;
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
