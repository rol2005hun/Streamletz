<script lang="ts">
    import { onMount } from "svelte";
    import { authService, type User } from "../lib/authService";
    import { userService, type UserProfile } from "../lib/userService";
    import type { Track } from "../lib/trackService";
    import { likedTrackService } from "../lib/likedTrackService";
    import { playlistService } from "../lib/playlistService";

    interface Props {
        identifier?: string;
        currentTrack?: Track | null;
        isPlaying?: boolean;
        allTracks?: Track[];
    }

    let {
        identifier,
        currentTrack = $bindable(null),
        isPlaying = $bindable(false),
        allTracks = $bindable([]),
    }: Props = $props();

    let profileData = $state<UserProfile | null>(null);
    let currentUser = $state<User | null>(null);
    let isOwnProfile = $state(false);
    let stats = $state({
        totalLikedTracks: 0,
        totalPlaylists: 0,
        recentlyPlayed: [] as Track[],
    });
    let loading = $state(true);
    let error = $state("");
    let hoveredTrack = $state<number | null>(null);

    onMount(async () => {
        currentUser = authService.getUser();
        await loadProfile();
        loading = false;
    });

    async function loadProfile() {
        try {
            if (identifier) {
                profileData = await userService.getPublicProfile(identifier);
                isOwnProfile = currentUser?.username === profileData.username;
            } else {
                if (!currentUser) {
                    error = "Please log in to view your profile";
                    return;
                }
                profileData = await userService.getUserProfile();
                isOwnProfile = true;
            }

            if (isOwnProfile) {
                await loadUserStats();
            }
        } catch (err: any) {
            console.error("Failed to load profile:", err);
            error = err.response?.data?.message || "Failed to load profile";
        }
    }

    async function loadUserStats() {
        try {
            const [likedTracks, playlists] = await Promise.all([
                likedTrackService.getLikedTracks(),
                playlistService.getUserPlaylists(),
            ]);

            stats = {
                totalLikedTracks: likedTracks.length,
                totalPlaylists: playlists.length,
                recentlyPlayed: likedTracks.slice(0, 5),
            };

            // Set all tracks for the audio player
            if (likedTracks.length > 0) {
                allTracks = likedTracks.map((track) => ({
                    ...track,
                    album: track.album || "",
                    coverArtUrl: track.coverArtUrl || "",
                    filePath: "",
                    fileFormat: "",
                }));
            }
        } catch (error) {
            console.error("Failed to load user stats:", error);
        }
    }

    function playTrack(track: Track) {
        if (currentTrack?.id === track.id) {
            isPlaying = !isPlaying;
        } else {
            currentTrack = track;
            isPlaying = true;
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
        window.history.pushState({}, "", "/dashboard");
        window.dispatchEvent(new PopStateEvent("popstate"));
    }
</script>

<div class="profile-page">
    {#if loading}
        <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading profile...</p>
        </div>
    {:else if error}
        <div class="error-container">
            <div class="error-icon">⚠️</div>
            <p class="error-message">{error}</p>
            <button class="back-btn" onclick={goBack}>← Go Back</button>
        </div>
    {:else if profileData}
        <div class="profile-header">
            <div class="profile-banner">
                <button
                    class="back-btn"
                    onclick={goBack}
                    title="Back to Dashboard"
                >
                    ← Back
                </button>
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
                            <span class="stat-item">
                                <strong>{stats.totalPlaylists}</strong> Playlists
                            </span>
                            <span class="stat-divider">•</span>
                            <span class="stat-item">
                                <strong>{stats.totalLikedTracks}</strong> Liked Songs
                            </span>
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
                                        class:playing={currentTrack?.id ===
                                            track.id}
                                        role="button"
                                        tabindex="0"
                                        onmouseenter={() =>
                                            (hoveredTrack = track.id)}
                                        onmouseleave={() =>
                                            (hoveredTrack = null)}
                                        onclick={() => playTrack(track)}
                                        onkeydown={(e) => {
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
                                            {#if hoveredTrack === track.id || currentTrack?.id === track.id}
                                                <button
                                                    class="play-btn"
                                                    onclick={(e) => {
                                                        e.stopPropagation();
                                                        playTrack(track);
                                                    }}
                                                    title="Play"
                                                >
                                                    {#if currentTrack?.id === track.id && isPlaying}
                                                        <svg
                                                            width="16"
                                                            height="16"
                                                            viewBox="0 0 24 24"
                                                            fill="currentColor"
                                                        >
                                                            <path
                                                                d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"
                                                            ></path>
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
                                                            ></path>
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
                                                            : `http://localhost:8080${track.coverArtUrl}`}
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
                                        <div class="col-album">{track.album || "Unknown"}</div>
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
                                    ></path>
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
                                    ></path>
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

<style lang="scss">
    @use "../styles/pages/Profile.scss";
</style>
