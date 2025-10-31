<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import type { Track } from "../lib/trackService";
  import { trackService } from "../lib/trackService";

  let {
    track = $bindable(null),
    isPlaying = $bindable(false),
    onNext,
    onPrevious,
  }: {
    track: Track | null;
    isPlaying: boolean;
    onNext?: () => void;
    onPrevious?: () => void;
  } = $props();

  let audio: HTMLAudioElement | null = null;
  let currentTime = $state(0);
  let duration = $state(0);
  let volume = $state(0.7);
  let buffered = $state(0);
  let seeking = $state(false);

  onMount(() => {
    const savedVolume = localStorage.getItem("streamletz_volume");
    if (savedVolume) {
      volume = parseFloat(savedVolume);
    }
  });

  $effect(() => {
    if (typeof window !== "undefined") {
      localStorage.setItem("streamletz_volume", volume.toString());
    }
  });

  $effect(() => {
    if (track && currentTime > 0 && typeof window !== "undefined") {
      localStorage.setItem(
        "streamletz_last_track",
        JSON.stringify({
          trackId: track.id,
          position: currentTime,
        }),
      );
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

    const savedData = localStorage.getItem("streamletz_last_track");
    if (savedData) {
      try {
        const { trackId, position } = JSON.parse(savedData);
        if (trackId === track.id && position > 0 && position < duration - 5) {
          audio.currentTime = position;
        }
      } catch (e) {
        console.error("Failed to restore playback position:", e);
      }
    }

    try {
      await audio.play();
      isPlaying = true;
    } catch (err) {
      console.error("Playback failed: ", err);
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
      <button
        class="control-btn"
        onclick={onPrevious}
        disabled={!onPrevious}
        aria-label="Previous track"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z" />
        </svg>
      </button>

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

      <button
        class="control-btn"
        onclick={onNext}
        disabled={!onNext}
        aria-label="Next track"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z" />
        </svg>
      </button>
    </div>

    <div class="progress-section">
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
          style="--progress-percent: {duration > 0 ? (currentTime / duration) * 100 : 0}%"
        />
      </div>
      <span class="time">{formatTime(duration)}</span>
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
    z-index: $z-player;
    display: flex;
    align-items: center;
    gap: $spacing-lg;

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
    min-width: 220px;
    max-width: 280px;
    flex: 0 0 auto;

    .album-art {
      width: 56px;
      height: 56px;
      border-radius: $border-radius-md;
      overflow: hidden;
      background: $gradient-primary;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-md;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .placeholder {
        font-size: 1.5rem;
      }
    }

    .track-info {
      min-width: 0;
      flex: 1;

      .track-title {
        font-weight: 700;
        font-size: 0.95rem;
        color: $text-primary;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .track-artist {
        font-size: 0.85rem;
        color: $text-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .player-controls {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    justify-content: center;
    flex: 0 0 auto;

    .control-btn {
      background: transparent;
      border: none;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      color: $text-secondary;
      cursor: pointer;

      svg {
        width: 18px;
        height: 18px;
      }

      &:hover:not(:disabled) {
        transform: scale(1.05);
      }

      &:active:not(:disabled) {
        transform: scale(0.95);
      }

      &:disabled {
        opacity: 0.3;
        cursor: not-allowed;
      }

      &.play-pause {
        background: $text-primary;
        color: $spotify-dark;
        width: 40px;
        height: 40px;

        svg {
          width: 20px;
          height: 20px;
        }

        &:hover {
          background: $text-primary;
          transform: scale(1.05);
        }

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }

  .player-volume {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    min-width: 140px;
    max-width: 200px;
    flex: 0 0 auto;
    justify-content: flex-end;

    .volume-icon {
      width: 20px;
      height: 20px;
      color: $text-secondary;
      flex-shrink: 0;
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
        background: linear-gradient(
          to right,
          rgba(255, 255, 255, 0.7) 0%,
          rgba(255, 255, 255, 0.7) var(--volume-percent, 70%),
          rgba(255, 255, 255, 0.1) var(--volume-percent, 70%)
        );
        border-radius: $border-radius-full;
        transition: background 0.2s ease;
      }

      &::-moz-range-track {
        width: 100%;
        height: 4px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: $border-radius-full;
      }

      &::-moz-range-progress {
        background: rgba(255, 255, 255, 0.7);
        height: 4px;
        border-radius: $border-radius-full;
        transition: background 0.2s ease;
      }

      &:hover {
        &::-webkit-slider-runnable-track {
          background: linear-gradient(
            to right,
            $primary-color 0%,
            $primary-color var(--volume-percent, 70%),
            rgba(255, 255, 255, 0.1) var(--volume-percent, 70%)
          );
        }

        &::-moz-range-progress {
          background: $primary-color;
        }

        &::-webkit-slider-thumb {
          opacity: 1;
        }

        &::-moz-range-thumb {
          opacity: 1;
        }
      }

      &::-webkit-slider-thumb {
        appearance: none;
        width: 12px;
        height: 12px;
        background: $text-primary;
        border-radius: 50%;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s ease, transform 0.2s ease;
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
        opacity: 0;
        transition: opacity 0.2s ease, transform 0.2s ease;

        &:hover {
          transform: scale(1.2);
        }
      }
    }
  }

  .progress-section {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex: 1;

    .time {
      font-size: 0.75rem;
      color: $text-secondary;
      min-width: 40px;
      text-align: center;
      font-weight: 500;
      font-variant-numeric: tabular-nums;
      flex-shrink: 0;
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

        .progress-bar::-webkit-slider-thumb {
          opacity: 1;
        }

        .progress-bar::-moz-range-thumb {
          opacity: 1;
        }

        .progress-bar::-webkit-slider-runnable-track {
          background: linear-gradient(
            to right,
            $primary-color 0%,
            $primary-color var(--progress-percent, 0%),
            rgba(255, 255, 255, 0.1) var(--progress-percent, 0%)
          );
        }

        .progress-bar::-moz-range-progress {
          background: $primary-color;
        }
      }

      .buffer-bar {
        position: absolute;
        height: 100%;
        background: rgba(255, 255, 255, 0.15);
        border-radius: $border-radius-full;
        pointer-events: none;
        transition: width $transition-normal;
        z-index: 1;
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
        top: 0;
        left: 0;

        &::-webkit-slider-runnable-track {
          width: 100%;
          height: 4px;
          background: linear-gradient(
            to right,
            rgba(255, 255, 255, 0.7) 0%,
            rgba(255, 255, 255, 0.7) var(--progress-percent, 0%),
            rgba(255, 255, 255, 0.1) var(--progress-percent, 0%)
          );
          border-radius: $border-radius-full;
          transition: background 0.2s ease;
        }

        &::-moz-range-track {
          width: 100%;
          height: 4px;
          background: rgba(255, 255, 255, 0.1);
          border-radius: $border-radius-full;
        }

        &::-moz-range-progress {
          background: rgba(255, 255, 255, 0.7);
          height: 4px;
          border-radius: $border-radius-full;
          transition: background 0.2s ease;
        }

        &::-webkit-slider-thumb {
          appearance: none;
          width: 12px;
          height: 12px;
          background: $text-primary;
          border-radius: 50%;
          cursor: pointer;
          opacity: 0;
          transition: opacity 0.2s ease, transform 0.2s ease;
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
          opacity: 0;
          transition: opacity 0.2s ease, transform 0.2s ease;          &:hover {
            transform: scale(1.2);
          }
        }
      }
    }
  }

  .player-placeholder {
    width: 100%;
    text-align: center;
    color: $text-muted;
    font-size: 1rem;
    padding: $spacing-md 0;

    p {
      margin: 0;
    }
  }

  @media (max-width: $breakpoint-tablet) {
    .audio-player {
      padding: $spacing-md;
      flex-wrap: wrap;
      gap: $spacing-sm;
    }

    .player-info {
      flex: 1 1 auto;
      min-width: 180px;
      max-width: 240px;

      .album-art {
        width: 48px;
        height: 48px;
      }

      .track-info {
        .track-title {
          font-size: 0.9rem;
        }

        .track-artist {
          font-size: 0.8rem;
        }
      }
    }

    .player-controls {
      flex: 0 0 auto;
      gap: $spacing-xs;

      .control-btn {
        width: 32px;
        height: 32px;

        svg {
          width: 16px;
          height: 16px;
        }

        &.play-pause {
          width: 36px;
          height: 36px;

          svg {
            width: 18px;
            height: 18px;
          }
        }
      }
    }

    .progress-section {
      flex: 1 1 100%;
      order: 3;
      gap: $spacing-sm;

      .time {
        font-size: 0.7rem;
        min-width: 36px;
      }
    }

    .player-volume {
      flex: 1 1 auto;
      min-width: 120px;
      max-width: 160px;

      .volume-icon {
        width: 18px;
        height: 18px;
      }
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .audio-player {
      padding: $spacing-sm $spacing-md;
    }

    .player-info {
      flex: 1 1 100%;
      max-width: 100%;
      min-width: unset;
      order: 1;
    }

    .player-controls {
      order: 2;
    }

    .player-volume {
      flex: 1 1 auto;
      min-width: unset;
      max-width: unset;
      order: 3;
    }

    .progress-section {
      order: 4;
    }
  }
</style>