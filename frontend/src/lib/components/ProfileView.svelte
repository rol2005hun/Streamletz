<script lang="ts">
    import { goto } from "$app/navigation";
    import type { Track } from "$lib/trackService";
    import { currentTrack, isPlaying, allTracks } from "$lib/stores";
    import type { UserProfile } from "$lib/userService";

    export let profileData: UserProfile | null = null;
    export let likedTracks: Track[] = [];
    export let playlists: import("$lib/playlistService").Playlist[] = [];
    export let isOwnProfile: boolean = false;

    let error = "";
    let hoveredTrack: number | null = null;

    $: stats = {
        totalLikedTracks: likedTracks.length,
        totalPlaylists: playlists.length,
        recentlyPlayed: likedTracks.slice(0, 5),
    };

    if (isOwnProfile && likedTracks.length > 0) {
        allTracks.set(
            likedTracks.map((track) => ({
                ...track,
                album: track.album || "",
                coverArtUrl: track.coverArtUrl || "",
                filePath: "",
                fileFormat: "",
            })),
        );
    }

    function playTrack(track: Track) {
        if ($currentTrack?.id === track.id) {
            isPlaying.set(!$isPlaying);
        } else {
            currentTrack.set(track);
            isPlaying.set(true);
        }
    }

    function formatDuration(seconds: number): string {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, "0")}`;
    }

    function getUserInitials(username: string): string {
        return username
            .split(" ")
            .map((n) => n[0])
            .join("")
            .toUpperCase()
            .slice(0, 2);
    }

    function goBack() {
        goto("/dashboard");
    }
</script>

<div class="profile-page">
    {#if error}
        <div class="error-container">
            <div class="error-icon">⚠️</div>
            <p class="error-message">{error}</p>
            <button class="back-btn" on:click={goBack}>← Go Back</button>
        </div>
    {:else if profileData}
        <div class="profile-header">
            <div class="profile-banner">
                <button
                    class="back-btn"
                    on:click={goBack}
                    title="Back to Dashboard">← Back</button
                >
                {#if !isOwnProfile}
                    <span class="public-badge">Public Profile</span>
                {/if}
            </div>
            <div class="profile-info">
                <div class="avatar-container">
                    <div class="avatar-large">
                        {#if profileData.profileImage}
                            <img
                                src={profileData.profileImage}
                                alt={profileData.username}
                                class="avatar-img"
                            />
                        {:else}
                            {getUserInitials(profileData.username)}
                        {/if}
                    </div>
                </div>
                <div class="user-details">
                    <h1 class="username">{profileData.username}</h1>
                    {#if isOwnProfile}
                        <div class="user-stats">
                            <span class="stat-item"
                                ><strong>{stats.totalPlaylists}</strong> Playlists</span
                            >
                            <span class="stat-divider">•</span>
                            <span class="stat-item"
                                ><strong>{stats.totalLikedTracks}</strong> Liked
                                Songs</span
                            >
                        </div>
                    {/if}
                </div>
            </div>
        </div>

        <div class="profile-content">
            {#if isOwnProfile}
                {#if stats.recentlyPlayed.length > 0}
                    <div class="profile-section">
                        <h2 class="section-title">Recently Played</h2>
                        <div class="tracks-section">
                            <div class="tracks-header">
                                <div class="col-number">#</div>
                                <div class="col-title">Title</div>
                                <div class="col-album">Album</div>
                                <div class="col-duration">Duration</div>
                            </div>

                            <div class="tracks-list">
                                {#each stats.recentlyPlayed as track, index}
                                    <div
                                        class="track-row"
                                        class:playing={$currentTrack?.id ===
                                            track.id}
                                        role="button"
                                        tabindex="0"
                                        on:mouseenter={() =>
                                            (hoveredTrack = track.id)}
                                        on:mouseleave={() =>
                                            (hoveredTrack = null)}
                                        on:click={() => playTrack(track)}
                                        on:keydown={(e) => {
                                            if (
                                                e.key === "Enter" ||
                                                e.key === " "
                                            ) {
                                                e.preventDefault();
                                                playTrack(track);
                                            }
                                        }}
                                    >
                                        <div class="col-number">
                                            {#if hoveredTrack === track.id || $currentTrack?.id === track.id}
                                                <button
                                                    class="play-btn"
                                                    on:click|stopPropagation={() =>
                                                        playTrack(track)}
                                                    title="Play"
                                                >
                                                    {#if $currentTrack?.id === track.id && $isPlaying}
                                                        <svg
                                                            width="16"
                                                            height="16"
                                                            viewBox="0 0 24 24"
                                                            fill="currentColor"
                                                        >
                                                            <path
                                                                d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"
                                                            />
                                                        </svg>
                                                    {:else}
                                                        <svg
                                                            width="16"
                                                            height="16"
                                                            viewBox="0 0 24 24"
                                                            fill="currentColor"
                                                        >
                                                            <path
                                                                d="M8 5v14l11-7z"
                                                            />
                                                        </svg>
                                                    {/if}
                                                </button>
                                            {:else}
                                                {index + 1}
                                            {/if}
                                        </div>
                                        <div class="col-title">
                                            <div class="track-info-cell">
                                                {#if track.coverArtUrl}
                                                    <img
                                                        src={track.coverArtUrl.startsWith(
                                                            "http",
                                                        )
                                                            ? track.coverArtUrl
                                                            : import.meta.env.VITE_API_BASE_URL + track.coverArtUrl}
                                                        alt={track.album ||
                                                            track.title}
                                                        class="track-cover"
                                                    />
                                                {:else}
                                                    <div
                                                        class="track-cover placeholder"
                                                    >
                                                        🎵
                                                    </div>
                                                {/if}
                                                <div class="track-text">
                                                    <div class="track-title">
                                                        {track.title}
                                                    </div>
                                                    <div class="track-artist">
                                                        {track.artist}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-album">
                                            {track.album || "Unknown"}
                                        </div>
                                        <div class="col-duration">
                                            {formatDuration(track.duration)}
                                        </div>
                                    </div>
                                {/each}
                            </div>
                        </div>
                    </div>
                {/if}

                <div class="profile-section">
                    <h2 class="section-title">Music Statistics</h2>
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-icon">
                                <svg
                                    width="28"
                                    height="28"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    stroke-width="2"
                                >
                                    <path
                                        d="M21 15V6M18.5 18a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"
                                    />
                                    <path
                                        d="M12 18V3l9 3M12 18a2.5 2.5 0 1 0-2.5-2.5A2.5 2.5 0 0 0 12 18Z"
                                    ></path>
                                </svg>
                            </div>
                            <div class="stat-value">{stats.totalPlaylists}</div>
                            <div class="stat-label">Total Playlists</div>
                        </div>

                        <div class="stat-card">
                            <div class="stat-icon liked">
                                <svg
                                    width="28"
                                    height="28"
                                    viewBox="0 0 24 24"
                                    fill="currentColor"
                                >
                                    <path
                                        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                                    />
                                </svg>
                            </div>
                            <div class="stat-value">
                                {stats.totalLikedTracks}
                            </div>
                            <div class="stat-label">Liked Songs</div>
                        </div>
                    </div>
                </div>
            {:else}
                <div class="profile-section">
                    <div class="public-profile-info">
                        <p class="info-text">
                            This is a public profile view. Only basic
                            information is displayed.
                        </p>
                        <p class="member-since">
                            Member since {new Date(
                                profileData.createdAt,
                            ).toLocaleDateString()}
                        </p>
                    </div>
                </div>
            {/if}
        </div>
    {/if}
</div>

<style scoped lang="scss">
    @use "$styles/pages/Profile.scss";
</style>
