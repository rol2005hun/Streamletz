<script lang="ts">
    import { goto } from "$app/navigation";
    import { likedTrackService } from "$lib/likedTrackService";
    import type { Track } from "$lib/trackService";
    import { currentTrack, isPlaying, allTracks } from "$lib/stores";

    const { data } = $props();
    let tracks = $state<Track[]>(Array.isArray(data.tracks) ? data.tracks : []);
    let error = $state("");
    let hoveredTrack = $state<number | null>(null);
    $effect(() => {
        allTracks.set(tracks);
    });

    async function unlikeTrack(trackId: number) {
        try {
            await likedTrackService.unlikeTrack(trackId);
            tracks = tracks.filter((t: Track) => t.id !== trackId);
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
        return hours > 0 ? `${hours} hr ${minutes} min` : `${minutes} min`;
    }

    function playTrack(track: Track) {
        if ($currentTrack?.id === track.id) {
            isPlaying.set(!$isPlaying);
        } else {
            currentTrack.set(track);
            isPlaying.set(true);
        }
    }

    function goBack() {
        goto("/dashboard");
    }
</script>

<div class="liked-songs">
    <button class="back-btn" onclick={goBack} title="Back to Dashboard"
        >← Back</button
    >

    <div class="header">
        <div class="cover"><span class="heart-icon">❤️</span></div>
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
        {#if error}
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
                        class:playing={$currentTrack?.id === track.id}
                        role="button"
                        tabindex="0"
                        onmouseenter={() => (hoveredTrack = track.id)}
                        onmouseleave={() => (hoveredTrack = null)}
                        onclick={() => playTrack(track)}
                        onkeydown={(e) => {
                            if (e.key === "Enter" || e.key === " ") {
                                e.preventDefault();
                                playTrack(track);
                            }
                        }}
                    >
                        <div class="col-number">
                            {#if hoveredTrack === track.id || $currentTrack?.id === track.id}
                                <button
                                    class="play-btn"
                                    onclick={(e) => {
                                        e.stopPropagation();
                                        playTrack(track);
                                    }}
                                    title="Play"
                                >
                                    {#if $currentTrack?.id === track.id && $isPlaying}
                                        <svg
                                            viewBox="0 0 24 24"
                                            fill="currentColor"
                                        >
                                            <path
                                                d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"
                                            />
                                        </svg>
                                    {:else}
                                        <svg
                                            viewBox="0 0 24 24"
                                            fill="currentColor"
                                        >
                                            <path d="M8 5v14l11-7z" />
                                        </svg>
                                    {/if}
                                </button>
                            {:else}
                                {index + 1}
                            {/if}
                        </div>

                        <div class="col-title">
                            <div class="track-info">
                                {#if track.coverArtUrl}
                                    <img
                                        src={track.coverArtUrl.startsWith(
                                            "http",
                                        )
                                            ? track.coverArtUrl
                                            : import.meta.env.VITE_API_BASE_URL + track.coverArtUrl}
                                        alt={track.album || track.title}
                                    />
                                {:else}
                                    <div class="track-cover placeholder">
                                        🎵
                                    </div>
                                {/if}
                                <div class="track-text">
                                    <div class="track-title">{track.title}</div>
                                    <div class="track-artist">
                                        {track.artist}
                                    </div>
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
                                onclick={(e) => {
                                    e.stopPropagation();
                                    unlikeTrack(track.id);
                                }}
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
    @use "$styles/pages/LikedSongs.scss";
</style>
