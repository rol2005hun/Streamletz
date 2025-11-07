<script lang="ts">
  import { onMount, onDestroy } from "svelte";
  import { trackService } from "$lib/trackService";
  import { isPlaying, isPaused, currentTrack } from "$lib/stores";
  import { get } from "svelte/store";

  let { allTracks = $bindable([]), initialPosition = undefined } = $props();
  let isLoading = $state(true);
  const track = $derived(() => $currentTrack);
  let audio: HTMLAudioElement | null = null;
  let currentTime = $state(0);
  let duration = $state(0);
  let volume = $state(0.7);
  let buffered = $state(0);
  let seeking = $state(false);
  let isMuted = $state(false);
  let unmutedVolume = $state(0.7);
  let playCountIncremented = $state(false);

  onMount(() => {
    if (typeof window !== "undefined") {
      const matchPaused = document.cookie.match(
        /(?:^|; )streamletz_isPaused=([^;]*)/,
      );
      if (matchPaused) {
        try {
          isPaused.set(JSON.parse(decodeURIComponent(matchPaused[1])));
        } catch {}
      }
    }
    audio = new Audio();
    try {
      const match = document.cookie.match(/(?:^|; )streamletz_volume=([^;]*)/);
      if (match) {
        volume = parseFloat(decodeURIComponent(match[1]));
      }
      const matchUnmuted = document.cookie.match(
        /(?:^|; )streamletz_unmutedVolume=([^;]*)/,
      );
      if (matchUnmuted) {
        unmutedVolume = parseFloat(decodeURIComponent(matchUnmuted[1]));
      }
      if (volume === 0) {
        isMuted = true;
      } else {
        isMuted = false;
      }
    } catch {
      console.error("Failed to parse audio settings");
    }

    audio.volume = volume;

    audio.addEventListener("play", () => {
      isPlaying.set(true);
      isPaused.set(false);
    });

    audio.addEventListener("pause", () => {
      isPlaying.set(false);
      isPaused.set(true);
    });

    audio.addEventListener("timeupdate", handleTimeUpdate);
    audio.addEventListener("loadedmetadata", handleLoadedMetadata);
    audio.addEventListener("progress", handleProgress);
    audio.addEventListener("ended", handleEnded);

    if (typeof initialPosition === "number" && initialPosition > 0) {
      audio.currentTime = initialPosition;
    }

    let playPauseQueue: Promise<void> = Promise.resolve();
    async function handlePlayPause(playing: boolean) {
      if (!audio) return;

      if (playing && audio.paused) {
        try {
          await audio.play();
        } catch (err) {
          console.error("[AudioPlayer] Playback failed:", err);
        }
      } else if (!playing && !audio.paused) {
        audio.pause();
      }
    }

    const unsub = isPlaying.subscribe((playing) => {
      playPauseQueue = playPauseQueue.then(() => handlePlayPause(playing));
    });

    onDestroy(() => {
      unsub();
      if (audio) {
        audio.pause();
        audio.src = "";
      }
    });
  });

  $effect(() => {
    if (!track() && allTracks && allTracks.length > 0) {
      let lastTrackId: number | null = null;
      if (typeof document !== "undefined") {
        const cookieMatch = document.cookie.match(
          /streamletz_last_playback=([^;]*)/,
        );
        if (cookieMatch) {
          try {
            const playback = JSON.parse(decodeURIComponent(cookieMatch[1]));
            if (playback && playback.trackId) {
              lastTrackId = playback.trackId;
            }
          } catch {}
        }
      }
      let found = null;
      if (lastTrackId != null) {
        found = allTracks.find((t) => t.id === lastTrackId);
      }
      currentTrack.set(found || allTracks[0]);
    }
  });

  $effect(() => {
    if (typeof document !== "undefined") {
      document.cookie = `streamletz_volume=${encodeURIComponent(volume.toString())}; path=/; max-age=31536000`;
      document.cookie = `streamletz_unmutedVolume=${encodeURIComponent(unmutedVolume.toString())}; path=/; max-age=31536000`;
    }
  });

  let previousTrackId = $state<number | null>(null);
  let restoredFromSession = false;

  let unsubWasPlaying: (() => void) | undefined;
  $effect(() => {
    if (unsubWasPlaying) unsubWasPlaying();
    const t = track();
    if (t && typeof document !== "undefined") {
      unsubWasPlaying = isPlaying.subscribe((playing) => {
        const cookieMatch = document.cookie.match(
          /streamletz_last_playback=([^;]*)/,
        );
        let last: Record<string, any> = {};
        if (cookieMatch) {
          try {
            last = JSON.parse(decodeURIComponent(cookieMatch[1]));
          } catch {}
        }
        const value = JSON.stringify({
          trackId: t.id ?? null,
          position: last.position ?? currentTime,
          timestamp: Date.now(),
          wasPlaying: playing,
        });
        document.cookie = `streamletz_last_playback=${encodeURIComponent(value)}; path=/; max-age=604800`;
      });
    }
    return () => {
      if (unsubWasPlaying) unsubWasPlaying();
    };
  });

  $effect(() => {
    const t = track();
    if (t && typeof document !== "undefined") {
      const cookieMatch = document.cookie.match(
        /streamletz_last_playback=([^;]*)/,
      );
      let last: Record<string, any> = {};
      if (cookieMatch) {
        try {
          last = JSON.parse(decodeURIComponent(cookieMatch[1]));
        } catch {}
      }
      const value = JSON.stringify({
        trackId: t.id ?? null,
        position: currentTime,
        timestamp: Date.now(),
        wasPlaying: last.wasPlaying ?? get(isPlaying),
      });
      document.cookie = `streamletz_last_playback=${encodeURIComponent(value)}; path=/; max-age=604800`;
    }
  });

  $effect(() => {
    const t = track();
    if (t && audio && t.id != null) {
      const isNewTrack = previousTrackId !== t.id;
      if (isNewTrack) {
        previousTrackId = t.id;
        playCountIncremented = false;

        let shouldPlay = true;
        if (typeof document !== "undefined") {
          const cookieMatch = document.cookie.match(
            /streamletz_last_playback=([^;]*)/,
          );
          if (cookieMatch) {
            try {
              const playback = JSON.parse(decodeURIComponent(cookieMatch[1]));
              if (playback && playback.wasPlaying === false) {
                shouldPlay = false;
              }
            } catch {}
          }
        }
        isLoading = true;
        loadTrack(shouldPlay).finally(() => {
          isLoading = false;
        });
      }
    } else {
      isLoading = false;
    }
  });

  $effect(() => {
    const playing = get(isPlaying);
    const currentAudio = audio;
    const t = track();
    if (currentAudio && t) {
      if (playing && currentAudio.paused) {
        currentAudio.play().catch((err) => {
          console.error("Failed to play:", err);
          isPlaying.set(false);
        });
      } else if (!playing && !currentAudio.paused) {
        currentAudio.pause();
      }
    }
  });

  async function loadTrack(shouldPlay = true) {
    const t = track();
    if (!t || !audio || t.id == null) {
      return;
    }

    audio.pause();
    audio.src = "";
    audio.load();
    let startPosition = 0;
    if (
      typeof initialPosition === "number" &&
      initialPosition > 0 &&
      !restoredFromSession
    ) {
      startPosition = initialPosition;
      restoredFromSession = true;
    }
    currentTime = startPosition;
    const streamUrl = trackService.getStreamUrl(t.id);
    audio.src = streamUrl;
    audio.load();
    audio.addEventListener(
      "loadedmetadata",
      () => {
        audio!.currentTime = startPosition;
        currentTime = startPosition;
      },
      { once: true },
    );

    const playing = get(isPlaying);
    const paused = get(isPaused);

    if (shouldPlay && playing && !paused) {
      audio.addEventListener(
        "canplay",
        async () => {
          try {
            await audio!.play();
          } catch (err) {
            console.error("[AudioPlayer] Playback failed:", err);
            isPlaying.set(false);
          }
        },
        { once: true },
      );
    }
  }

  function togglePlay() {
    if (!audio) return;
    const playing = get(isPlaying);
    if (playing) {
      isPlaying.set(false);
      isPaused.set(true);
    } else {
      isPlaying.set(true);
      isPaused.set(false);
    }
    // Save isPaused to cookie
    document.cookie = `streamletz_isPaused=${encodeURIComponent(JSON.stringify(get(isPaused)))}; path=/; max-age=31536000`;
  }

  function handleTimeUpdate() {
    if (!audio || seeking) return;
    currentTime = audio.currentTime;
    const t = track();
    if (t && typeof document !== "undefined" && t.id != null) {
      const cookieMatch = document.cookie.match(
        /streamletz_last_playback=([^;]*)/,
      );
      let last: any = {};
      if (cookieMatch) {
        try {
          last = JSON.parse(decodeURIComponent(cookieMatch[1]));
        } catch {}
      }
      const value = JSON.stringify({
        trackId: t.id,
        position: currentTime,
        timestamp: Date.now(),
        wasPlaying: last.wasPlaying ?? get(isPlaying),
      });
      document.cookie = `streamletz_last_playback=${encodeURIComponent(value)}; path=/; max-age=604800`;
    }
    if (!playCountIncremented && t && duration > 0 && t.id != null) {
      const progressPercent = (currentTime / duration) * 100;
      if (progressPercent >= 90) {
        playCountIncremented = true;
        trackService.incrementPlayCount(t.id).catch((err) => {
          console.error("Failed to increment play count: ", err);
          console.error("Error details: ", err.response?.data);
          console.error("Status: ", err.response?.status);
        });
      }
    }
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
    if (volume > 0) {
      isMuted = false;
      unmutedVolume = volume;
    }
    // Save volume and unmutedVolume to cookies
    document.cookie = `streamletz_volume=${encodeURIComponent(volume.toString())}; path=/; max-age=31536000`;
    document.cookie = `streamletz_unmutedVolume=${encodeURIComponent(unmutedVolume.toString())}; path=/; max-age=31536000`;
  }

  function toggleMute() {
    if (!audio) return;
    if (isMuted) {
      volume = unmutedVolume > 0 ? unmutedVolume : 0.7;
      audio.volume = volume;
      isMuted = false;
    } else {
      if (volume > 0) {
        unmutedVolume = volume;
      }
      volume = 0;
      audio.volume = 0;
      isMuted = true;
    }
    // Save volume and unmutedVolume to cookies
    document.cookie = `streamletz_volume=${encodeURIComponent(volume.toString())}; path=/; max-age=31536000`;
    document.cookie = `streamletz_unmutedVolume=${encodeURIComponent(unmutedVolume.toString())}; path=/; max-age=31536000`;
  }

  function formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  }

  function handleNext() {
    if (allTracks.length === 0) return;
    const t = track();
    const currentIndex = allTracks.findIndex(
      (tr) => t && t.id != null && tr.id === t.id,
    );
    if (currentIndex >= allTracks.length - 1) {
      currentTrack.set(allTracks[0]);
    } else {
      currentTrack.set(allTracks[currentIndex + 1]);
    }
    isPlaying.set(true);
    if (audio) {
      audio.currentTime = 0;
    }
    currentTime = 0;
  }

  function handlePrevious() {
    if (allTracks.length === 0) return;
    const t = track();
    const currentIndex = allTracks.findIndex(
      (tr) => t && t.id != null && tr.id === t.id,
    );
    if (currentIndex <= 0) {
      currentTrack.set(allTracks[allTracks.length - 1]);
    } else {
      currentTrack.set(allTracks[currentIndex - 1]);
    }
    isPlaying.set(true);
    if (audio) {
      audio.currentTime = 0;
    }
    currentTime = 0;
  }

  function handleEnded() {
    handleNext();
  }

  onDestroy(() => {
    if (audio) {
      audio.pause();
      audio.src = "";
    }
  });
</script>

<div class="audio-player" class:active={track() !== null}>
  {#if isLoading}
    <div class="skeleton-player-horizontal">
      <div class="skeleton-left">
        <div class="skeleton-album-art skeleton"></div>
        <div class="skeleton-track-info">
          <div class="skeleton-title skeleton"></div>
          <div class="skeleton-artist skeleton"></div>
        </div>
      </div>
      <div class="skeleton-center">
        <div class="skeleton-controls">
          <div class="skeleton-btn skeleton shuffle"></div>
          <div class="skeleton-btn skeleton previous"></div>
          <div class="skeleton-btn skeleton play"></div>
          <div class="skeleton-btn skeleton next"></div>
          <div class="skeleton-btn skeleton repeat"></div>
        </div>
        <div class="skeleton-progress">
          <div class="skeleton-time skeleton"></div>
          <div class="skeleton-bar skeleton"></div>
          <div class="skeleton-time skeleton"></div>
        </div>
      </div>
      <div class="skeleton-right">
        <div class="skeleton-volume">
          <div class="skeleton-volicon skeleton"></div>
          <div class="skeleton-volbar skeleton"></div>
        </div>
      </div>
    </div>
  {:else if (() => {
    const t = track();
    return t && t.id != null;
  })()}
    <div class="player-info">
      <div class="album-art">
        {#if (() => {
          const t = track();
          return t && t.coverArtUrl;
        })()}
          {#key (() => {
            const t = track();
            return t ? t.id : 0;
          })()}
            {#if (() => {
              const t = track();
              return t && t.coverArtUrl;
            })()}
              {#await Promise.resolve(track()) then t}
                {#if t && t.coverArtUrl}
                  <img
                    src={t.coverArtUrl.startsWith("http")
                      ? t.coverArtUrl
                      : import.meta.env.VITE_API_BASE_URL + t.coverArtUrl}
                    alt={t.album}
                  />
                {/if}
              {/await}
            {/if}
          {/key}
        {:else}
          <div class="placeholder">🎵</div>
        {/if}
      </div>
      <div class="track-info">
        {#if (() => {
          const t = track();
          return t && t.title;
        })()}
          {#await Promise.resolve(track()) then t}
            {#if t && t.title}
              <div class="track-title">{t.title}</div>
            {/if}
          {/await}
        {/if}
        {#if (() => {
          const t = track();
          return t && t.artist;
        })()}
          {#await Promise.resolve(track()) then t}
            {#if t && t.artist}
              <div class="track-artist">{t.artist}</div>
            {/if}
          {/await}
        {/if}
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
          onclick={handlePrevious}
          disabled={allTracks.length === 0}
          aria-label="Previous track"
          title="Previous"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M3.3 1a.7.7 0 01.7.7v5.15l9.95-5.744a.7.7 0 011.05.606v12.575a.7.7 0 01-1.05.607L4 9.149V14.3a.7.7 0 01-.7.7H1.7a.7.7 0 01-.7-.7V1.7a.7.7 0 01.7-.7h1.6z"
            />
          </svg>
        </button>

        <button
          class="control-btn play-pause"
          onclick={togglePlay}
          title={$isPlaying && !$isPaused ? "Pause" : "Play"}
        >
          {#if $isPlaying && !$isPaused}
            <svg viewBox="0 0 16 16" fill="currentColor">
              <path
                d="M2.7 1a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7H2.7zm8 0a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-2.6z"
              />
            </svg>
          {:else}
            <svg viewBox="0 0 16 16" fill="currentColor">
              <path
                d="M3 1.713a.7.7 0 011.05-.607l10.89 6.288a.7.7 0 010 1.212L4.05 14.894A.7.7 0 013 14.288V1.713z"
              />
            </svg>
          {/if}
        </button>

        <button
          class="control-btn"
          onclick={handleNext}
          disabled={allTracks.length === 0}
          aria-label="Next track"
          title="Next"
        >
          <svg viewBox="0 0 16 16" fill="currentColor">
            <path
              d="M12.7 1a.7.7 0 00-.7.7v5.15L2.05 1.107A.7.7 0 001 1.712v12.575a.7.7 0 001.05.607L12 9.149V14.3a.7.7 0 00.7.7h1.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-1.6z"
            />
          </svg>
        </button>

        <button class="control-btn repeat" aria-label="Repeat" title="Repeat">
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
      <button
        class="volume-icon-btn"
        onclick={toggleMute}
        aria-label={isMuted ? "Unmute" : "Mute"}
        title={isMuted ? "Unmute" : "Mute"}
      >
        {#if isMuted || volume === 0}
          <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
            <path
              d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"
            />
          </svg>
        {:else if volume < 0.2}
          <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
            <path d="M3 9v6h4l5 5V4L7 9H3z" />
          </svg>
        {:else if volume < 0.5}
          <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
            <path
              d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"
            />
          </svg>
        {:else}
          <svg viewBox="0 0 24 24" fill="currentColor" class="volume-icon">
            <path
              d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM16.5 12c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z"
            />
            <path
              d="M19 12c0-2.99-1.73-5.58-4.24-6.79v1.53c1.72.94 2.88 2.76 2.88 4.89s-1.16 3.95-2.88 4.89v1.53C17.27 17.58 19 14.99 19 12z"
            />
          </svg>
        {/if}
      </button>
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

<style scoped lang="scss">
  @use "$styles/components/AudioPlayer.scss";
</style>
