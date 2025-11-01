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
        "streamletz_last_playback",
        JSON.stringify({
          trackId: track.id,
          position: currentTime,
          timestamp: Date.now(),
        }),
      );
    }
  });

  let previousTrackId = $state<number | null>(null);
  let isInitialLoad = $state(true);

  $effect(() => {
    if (track && audio) {
      const isNewTrack = previousTrackId !== track.id;
      if (isNewTrack) {
        previousTrackId = track.id;
      }
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

    if (isInitialLoad) {
      const savedData = localStorage.getItem("streamletz_last_playback");
      if (savedData) {
        try {
          const { trackId, position } = JSON.parse(savedData);
          if (trackId === track.id && position > 0) {
            audio.addEventListener(
              "loadedmetadata",
              () => {
                if (position < audio!.duration - 5) {
                  audio!.currentTime = position;
                  currentTime = position;
                }
              },
              { once: true },
            );
          }
        } catch (e) {
          console.error("Failed to restore playback position:", e);
        }
      }
      isInitialLoad = false;
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

  function handleEnded() {
    if (onNext) {
      onNext();
    }
  }

  onMount(() => {
    audio = new Audio();
    audio.volume = volume;

    audio.addEventListener("play", () => (isPlaying = true));
    audio.addEventListener("pause", () => (isPlaying = false));
    audio.addEventListener("timeupdate", handleTimeUpdate);
    audio.addEventListener("loadedmetadata", handleLoadedMetadata);
    audio.addEventListener("progress", handleProgress);
    audio.addEventListener("ended", handleEnded);
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

    <div class="player-center">
      <div class="player-controls">
        <button
          class="control-btn shuffle"
          aria-label="Shuffle"
          title="Shuffle"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M13.151.922a.75.75 0 10-1.06 1.06L13.109 3H11.16a3.75 3.75 0 00-2.873 1.34l-6.173 7.356A2.25 2.25 0 01.39 12.5H0V14h.391a3.75 3.75 0 002.873-1.34l6.173-7.356a2.25 2.25 0 011.724-.804h1.947l-1.017 1.018a.75.75 0 001.06 1.06L15.98 3.75 13.15.922zM.391 3.5H0V2h.391c1.109 0 2.16.49 2.873 1.34L4.89 5.277l-.979 1.167-1.796-2.14A2.25 2.25 0 00.39 3.5z"
            />
            <path
              d="M7.5 10.723l.98-1.167.957 1.14a2.25 2.25 0 001.724.804h1.947l-1.017-1.018a.75.75 0 111.06-1.06l2.829 2.828-2.829 2.828a.75.75 0 11-1.06-1.06L13.109 13H11.16a3.75 3.75 0 01-2.873-1.34l-.787-.938z"
            />
          </svg>
        </button>

        <button
          class="control-btn"
          onclick={onPrevious}
          disabled={!onPrevious}
          aria-label="Previous track"
          title="Previous"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path d="M3.3 1a.7.7 0 01.7.7v5.15l9.95-5.744a.7.7 0 011.05.606v12.575a.7.7 0 01-1.05.607L4 9.149V14.3a.7.7 0 01-.7.7H1.7a.7.7 0 01-.7-.7V1.7a.7.7 0 01.7-.7h1.6z" />
          </svg>
        </button>

        <button class="control-btn play-pause" onclick={togglePlay} title={isPlaying ? "Pause" : "Play"}>
          {#if isPlaying}
            <svg viewBox="0 0 16 16" fill="currentColor">
              <path d="M2.7 1a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7H2.7zm8 0a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-2.6z" />
            </svg>
          {:else}
            <svg viewBox="0 0 16 16" fill="currentColor">
              <path d="M3 1.713a.7.7 0 011.05-.607l10.89 6.288a.7.7 0 010 1.212L4.05 14.894A.7.7 0 013 14.288V1.713z" />
            </svg>
          {/if}
        </button>

        <button
          class="control-btn"
          onclick={onNext}
          disabled={!onNext}
          aria-label="Next track"
          title="Next"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path d="M12.7 1a.7.7 0 00-.7.7v5.15L2.05 1.107A.7.7 0 001 1.712v12.575a.7.7 0 001.05.607L12 9.149V14.3a.7.7 0 00.7.7h1.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-1.6z" />
          </svg>
        </button>

        <button
          class="control-btn repeat"
          aria-label="Repeat"
          title="Repeat"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M0 4.75A3.75 3.75 0 013.75 1h8.5A3.75 3.75 0 0116 4.75v5a3.75 3.75 0 01-3.75 3.75H9.81l1.018 1.018a.75.75 0 11-1.06 1.06L6.939 12.75l2.829-2.828a.75.75 0 111.06 1.06L9.811 12h2.439a2.25 2.25 0 002.25-2.25v-5a2.25 2.25 0 00-2.25-2.25h-8.5a2.25 2.25 0 00-2.25 2.25v5A2.25 2.25 0 003.75 12H5v1.5H3.75A3.75 3.75 0 010 9.75v-5z"
            />
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
            style="--progress-percent: {duration > 0
              ? (currentTime / duration) * 100
              : 0}%"
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
    padding: $spacing-sm $spacing-md;
    box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.4);
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    z-index: $z-player;
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    align-items: center;
    gap: $spacing-lg;
    height: 90px;

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
    min-width: 180px;

    .album-art {
      width: 56px;
      height: 56px;
      border-radius: $border-radius-sm;
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
        font-weight: 400;
        font-size: 0.875rem;
        color: $text-primary;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }

      .track-artist {
        font-size: 0.75rem;
        color: $text-secondary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
          color: $text-primary;
        }
      }
    }
  }

  .player-center {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    align-items: center;
    width: 100%;
    max-width: 722px;
    margin: 0 auto;
  }

  .player-controls {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    justify-content: center;

    .control-btn {
      background: transparent;
      border: none;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      color: #b3b3b3;
      cursor: pointer;
      padding: 0;

      svg {
        width: 16px;
        height: 16px;
      }

      &:hover:not(:disabled) {
        color: $text-primary;
        transform: scale(1.06);
      }

      &:active:not(:disabled) {
        color: $text-primary;
        transform: scale(0.95);
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }

      &.shuffle,
      &.repeat {
        svg {
          width: 16px;
          height: 16px;
        }
      }

      &.play-pause {
        background: $text-primary;
        color: #000;
        width: 32px;
        height: 32px;
        border-radius: 50%;

        svg {
          width: 16px;
          height: 16px;
        }

        &:hover {
          background: $text-primary;
          transform: scale(1.06);
        }

        &:active {
          transform: scale(0.97);
        }
      }
    }
  }

  .player-volume {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    justify-content: flex-end;

    .volume-icon {
      width: 16px;
      height: 16px;
      color: #b3b3b3;
      flex-shrink: 0;
      cursor: pointer;

      &:hover {
        color: $text-primary;
      }
    }

    .volume-slider {
      width: 93px;
      height: 12px;
      background: transparent;
      outline: none;
      appearance: none;
      cursor: pointer;
      display: flex;
      align-items: center;

      &::-webkit-slider-runnable-track {
        width: 100%;
        height: 4px;
        background: linear-gradient(
          to right,
          #fff 0%,
          #fff var(--volume-percent, 70%),
          #4d4d4d var(--volume-percent, 70%)
        );
        border-radius: 2px;
        transition: background 0.1s ease;
      }

      &::-moz-range-track {
        width: 100%;
        height: 4px;
        background: #4d4d4d;
        border-radius: 2px;
      }

      &::-moz-range-progress {
        background: #fff;
        height: 4px;
        border-radius: 2px;
        transition: background 0.1s ease;
      }

      &:hover {
        &::-webkit-slider-runnable-track {
          background: linear-gradient(
            to right,
            $primary-color 0%,
            $primary-color var(--volume-percent, 70%),
            #4d4d4d var(--volume-percent, 70%)
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
        background: #fff;
        border-radius: 50%;
        cursor: pointer;
        opacity: 0;
        transition:
          opacity 0.2s ease,
          transform 0.1s ease;
        margin-top: -4px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);

        &:hover {
          transform: scale(1.15);
        }

        &:active {
          transform: scale(1);
        }
      }

      &::-moz-range-thumb {
        width: 12px;
        height: 12px;
        background: #fff;
        border-radius: 50%;
        border: none;
        cursor: pointer;
        opacity: 0;
        transition:
          opacity 0.2s ease,
          transform 0.1s ease;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);

        &:hover {
          transform: scale(1.15);
        }

        &:active {
          transform: scale(1);
        }
      }
    }
  }

  .progress-section {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    width: 100%;

    .time {
      font-size: 0.6875rem;
      color: #a7a7a7;
      min-width: 40px;
      text-align: center;
      font-weight: 400;
      font-variant-numeric: tabular-nums;
      flex-shrink: 0;
    }

    .progress-wrapper {
      flex: 1;
      position: relative;
      height: 12px;
      display: flex;
      align-items: center;
      cursor: pointer;

      &:hover {
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
            #4d4d4d var(--progress-percent, 0%)
          );
        }

        .progress-bar::-moz-range-progress {
          background: $primary-color;
        }
      }

      .buffer-bar {
        position: absolute;
        height: 4px;
        background: #4d4d4d;
        border-radius: 2px;
        pointer-events: none;
        transition: width $transition-normal;
        z-index: 1;
      }

      .progress-bar {
        position: relative;
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
          background: linear-gradient(
            to right,
            #fff 0%,
            #fff var(--progress-percent, 0%),
            #4d4d4d var(--progress-percent, 0%)
          );
          border-radius: 2px;
          transition: background 0.1s ease;
        }

        &::-moz-range-track {
          width: 100%;
          height: 4px;
          background: #4d4d4d;
          border-radius: 2px;
        }

        &::-moz-range-progress {
          background: #fff;
          height: 4px;
          border-radius: 2px;
          transition: background 0.1s ease;
        }

        &::-webkit-slider-thumb {
          appearance: none;
          width: 12px;
          height: 12px;
          background: #fff;
          border-radius: 50%;
          cursor: pointer;
          opacity: 0;
          transition:
            opacity 0.2s ease,
            transform 0.1s ease;
          margin-top: -4px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);

          &:hover {
            transform: scale(1.15);
          }

          &:active {
            transform: scale(1);
          }
        }

        &::-moz-range-thumb {
          width: 12px;
          height: 12px;
          background: #fff;
          border-radius: 50%;
          border: none;
          cursor: pointer;
          opacity: 0;
          transition:
            opacity 0.2s ease,
            transform 0.1s ease;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);

          &:hover {
            transform: scale(1.15);
          }

          &:active {
            transform: scale(1);
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
      grid-template-columns: 1fr;
      height: auto;
      padding: $spacing-sm;
      gap: $spacing-xs;
    }

    .player-info {
      width: 100%;
    }

    .player-center {
      width: 100%;
      order: 2;
    }

    .player-volume {
      width: 100%;
      order: 3;
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .audio-player {
      padding: $spacing-sm;
    }

    .control-btn {
      &.shuffle,
      &.repeat {
        display: none !important;
      }
    }
  }
</style>
