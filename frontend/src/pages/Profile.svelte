<script lang="ts">
    import { onMount } from "svelte";
    import { authService, type User } from "../lib/authService";
    import type { Track } from "../lib/trackService";
    import { likedTrackService } from "../lib/likedTrackService";
    import { playlistService } from "../lib/playlistService";

    let user = $state<User | null>(null);
    let stats = $state({
        totalLikedTracks: 0,
        totalPlaylists: 0,
        recentlyPlayed: [] as Track[],
    });
    let loading = $state(true);

    onMount(async () => {
        user = authService.getUser();
        await loadUserStats();
        loading = false;
    });

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
        } catch (error) {
            console.error("Failed to load user stats:", error);
        }
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
    {:else if user}
        <div class="profile-header">
            <div class="profile-banner">
                <button
                    class="back-btn"
                    onclick={goBack}
                    title="Back to Dashboard"
                >
                    ← Back
                </button>
            </div>
            <div class="profile-info">
                <div class="avatar-container">
                    <div class="avatar-large">
                        {#if user.profileImage}
                            <img src={user.profileImage} alt={user.username} class="avatar-img" />
                        {:else}
                            {getUserInitials(user.username)}
                        {/if}
                    </div>
                </div>
                <div class="user-details">
                    <h1 class="username">{user.username}</h1>
                    <div class="user-stats">
                        <span class="stat-item">
                            <strong>{stats.totalPlaylists}</strong> Playlists
                        </span>
                        <span class="stat-divider">•</span>
                        <span class="stat-item">
                            <strong>{stats.totalLikedTracks}</strong> Liked Songs
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <div class="profile-content">
            {#if stats.recentlyPlayed.length > 0}
                <div class="profile-section">
                    <h2 class="section-title">Recently Played</h2>
                    <div class="recent-tracks">
                        {#each stats.recentlyPlayed as track}
                            <div class="recent-track-item">
                                <div class="track-icon">
                                    <svg
                                        width="20"
                                        height="20"
                                        viewBox="0 0 24 24"
                                        fill="currentColor"
                                    >
                                        <path
                                            d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 14.5v-9l6 4.5-6 4.5z"
                                        ></path>
                                    </svg>
                                </div>
                                <div class="track-info">
                                    <p class="track-title">{track.title}</p>
                                    <p class="track-artist">{track.artist}</p>
                                </div>
                            </div>
                        {/each}
                    </div>
                </div>
            {/if}

            <div class="profile-section">
                <h2 class="section-title">Statistics</h2>
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon playlists">
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
                        <div class="stat-value">{stats.totalLikedTracks}</div>
                        <div class="stat-label">Liked Songs</div>
                    </div>
                </div>
            </div>
        </div>
    {/if}
</div>

<style lang="scss">
    @use "../styles/pages/Profile.scss";
</style>
