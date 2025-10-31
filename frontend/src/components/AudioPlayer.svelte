<script lang="ts">
  import { onMount, onDestroy } from 'svelte';
  import type { Track } from '../lib/trackService';
  import { trackService } from '../lib/trackService';

  export let track: Track | null;

  let audio: HTMLAudioElement | null = null;
  let isPlaying = false;
  let currentTime = 0;
  let duration = 0;
  let volume = 0.7;
  let buffered = 0;
  let seeking = false;

  $: if (track && audio) {
    loadTrack();
  }

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
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  onMount(() => {
    audio = new Audio();
    audio.volume = volume;
    
    audio.addEventListener('play', () => isPlaying = true);
    audio.addEventListener('pause', () => isPlaying = false);
    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('progress', handleProgress);
  });

  onDestroy(() => {
    if (audio) {
      audio.pause();
      audio.src = '';
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
      <button class="control-btn" on:click={togglePlay}>
        {#if isPlaying}
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"/>
          </svg>
        {:else}
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M8 5v14l11-7z"/>
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
            on:input={handleSeek}
            on:mousedown={() => seeking = true}
            on:mouseup={() => seeking = false}
            class="progress-bar"
          />
        </div>
        <span class="time">{formatTime(duration)}</span>
      </div>
    </div>

    <div class="player-volume">
      <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
        <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"/>
      </svg>
      <input
        type="range"
        min="0"
        max="1"
        step="0.01"
        bind:value={volume}
        on:input={handleVolumeChange}
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
  .audio-player {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: $background-light;
    padding: $spacing-lg;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.3);
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
    min-width: 250px;

    .album-art {
      width: 56px;
      height: 56px;
      border-radius: $border-radius-sm;
      overflow: hidden;
      background: $background-dark;
      display: flex;
      align-items: center;
      justify-content: center;

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
      .track-title {
        font-weight: 600;
        color: $text-primary;
        margin-bottom: $spacing-xs;
      }

      .track-artist {
        font-size: 0.875rem;
        color: $text-secondary;
      }
    }
  }

  .player-controls {
    flex: 1;
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .control-btn {
      background: $primary-color;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform $transition-fast;

      svg {
        width: 20px;
        height: 20px;
      }

      &:hover {
        transform: scale(1.1);
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
        min-width: 40px;
        text-align: center;
      }

      .progress-wrapper {
        flex: 1;
        position: relative;
        height: 4px;
        background: $background-dark;
        border-radius: 2px;

        .buffer-bar {
          position: absolute;
          height: 100%;
          background: rgba($primary-color, 0.3);
          border-radius: 2px;
          pointer-events: none;
        }

        .progress-bar {
          position: absolute;
          width: 100%;
          height: 100%;
          opacity: 0;
          cursor: pointer;

          &::-webkit-slider-thumb {
            opacity: 1;
            width: 12px;
            height: 12px;
            background: $primary-color;
            border-radius: 50%;
            cursor: pointer;
          }
        }

        &::after {
          content: '';
          position: absolute;
          height: 100%;
          background: $primary-color;
          border-radius: 2px;
          width: calc(var(--progress) * 1%);
          pointer-events: none;
        }
      }
    }
  }

  .player-volume {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    min-width: 150px;

    .volume-icon {
      width: 24px;
      height: 24px;
      color: $text-secondary;
    }

    .volume-slider {
      flex: 1;
      height: 4px;
      background: $background-dark;
      border-radius: 2px;
      outline: none;
      -webkit-appearance: none;

      &::-webkit-slider-thumb {
        -webkit-appearance: none;
        width: 12px;
        height: 12px;
        background: $primary-color;
        border-radius: 50%;
        cursor: pointer;
      }
    }
  }

  .player-placeholder {
    width: 100%;
    text-align: center;
    color: $text-secondary;
    font-style: italic;
  }

  @media (max-width: $breakpoint-tablet) {
    .audio-player {
      flex-direction: column;
      gap: $spacing-md;
      padding: $spacing-md;
    }

    .player-info {
      width: 100%;
      min-width: unset;
    }

    .player-controls {
      width: 100%;
    }

    .player-volume {
      width: 100%;
      min-width: unset;
    }
  }
</style>
