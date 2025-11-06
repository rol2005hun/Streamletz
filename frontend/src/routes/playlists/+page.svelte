<script lang="ts">
    import { goto } from "$app/navigation";
    import { playlistService, type Playlist } from "$lib/playlistService";
    export let data;

    let playlists: Playlist[] = data.playlists ?? [];
    let showCreateModal = false;
    let newPlaylistName = "";
    let newPlaylistDescription = "";
    let newPlaylistIsPublic = false;
    let loading = false;
    let error = "";

    async function createPlaylist() {
        if (!newPlaylistName.trim()) {
            error = "Playlist name is required";
            return;
        }

        try {
            await playlistService.createPlaylist({
                name: newPlaylistName,
                description: newPlaylistDescription || undefined,
                isPublic: newPlaylistIsPublic,
            });

            playlists = await playlistService.getUserPlaylists();

            newPlaylistName = "";
            newPlaylistDescription = "";
            newPlaylistIsPublic = false;
            showCreateModal = false;
        } catch (err: any) {
            error = err.response?.data?.message || "Failed to create playlist";
        }
    }

    async function deletePlaylist(id: number) {
        if (!confirm("Are you sure you want to delete this playlist?")) return;

        try {
            await playlistService.deletePlaylist(id);
            goto("/playlists");
        } catch (err: any) {
            error = err.response?.data?.message || "Failed to delete playlist";
        }
    }

    function formatDuration(seconds: number): string {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return hours > 0 ? `${hours} hr ${minutes} min` : `${minutes} min`;
    }

    function goBack() {
        goto("/dashboard");
    }

    function navigateToPlaylist(id: number, e: MouseEvent) {
        e.preventDefault();
        goto(`/playlist/${id}`);
    }
</script>

<div class="playlists-container">
    <button class="back-btn" on:click={goBack} title="Back to Dashboard"
        >← Back</button
    >

    <div class="header">
        <h1>Your Playlists</h1>
        <button class="create-btn" on:click={() => (showCreateModal = true)}
            >+ Create Playlist</button
        >
    </div>

    {#if loading}
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading playlists...</p>
        </div>
    {:else if error}
        <div class="error">{error}</div>
    {:else if playlists.length === 0}
        <div class="empty">
            <p>You don't have any playlists yet.</p>
            <button on:click={() => (showCreateModal = true)}
                >Create Your First Playlist</button
            >
        </div>
    {:else}
        <div class="playlists-grid">
            {#each playlists as playlist}
                <div class="playlist-card">
                    <a
                        href={`/playlist/${playlist.id}`}
                        class="playlist-link"
                        on:click={(e) => navigateToPlaylist(playlist.id, e)}
                    >
                        <div class="playlist-cover">
                            {#if playlist.coverImageUrl}
                                <img
                                    src={playlist.coverImageUrl}
                                    alt={playlist.name}
                                />
                            {:else}
                                <div class="default-cover"><span>🎵</span></div>
                            {/if}
                        </div>
                        <div class="playlist-info">
                            <h3>{playlist.name}</h3>
                            {#if playlist.description}
                                <p class="description">
                                    {playlist.description}
                                </p>
                            {/if}
                            <div class="playlist-meta">
                                <span>{playlist.trackCount} tracks</span>
                                {#if playlist.totalDuration > 0}
                                    <span
                                        >• {formatDuration(
                                            playlist.totalDuration,
                                        )}</span
                                    >
                                {/if}
                            </div>
                            {#if playlist.isPublic}
                                <span class="public-badge">Public</span>
                            {/if}
                        </div>
                    </a>
                    <button
                        class="delete-btn"
                        on:click={(e) => {
                            e.preventDefault();
                            deletePlaylist(playlist.id);
                        }}>🗑️</button
                    >
                </div>
            {/each}
        </div>
    {/if}
</div>

{#if showCreateModal}
    <div
        class="modal-overlay"
        on:click={() => (showCreateModal = false)}
        on:keydown={(e) => e.key === "Escape" && (showCreateModal = false)}
        role="button"
        tabindex="-1"
        aria-label="Close modal"
    >
        <div
            class="modal"
            on:click|stopPropagation
            on:keydown|stopPropagation
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            tabindex="-1"
        >
            <div class="modal-header">
                <div class="modal-icon">🎵</div>
                <h2 id="modal-title">Create New Playlist</h2>
                <p class="modal-subtitle">
                    Build your perfect music collection
                </p>
            </div>

            <form
                on:submit={(e) => {
                    e.preventDefault();
                    createPlaylist();
                }}
            >
                <div class="form-group">
                    <label for="name"
                        ><span class="label-icon">📝</span> Playlist Name *</label
                    >
                    <input
                        id="name"
                        type="text"
                        bind:value={newPlaylistName}
                        placeholder="My Awesome Playlist"
                        required
                    />
                </div>

                <div class="form-group">
                    <label for="description"
                        ><span class="label-icon">💬</span> Description</label
                    >
                    <textarea
                        id="description"
                        bind:value={newPlaylistDescription}
                        placeholder="Tell us about your playlist..."
                        rows="3"
                    ></textarea>
                </div>

                <div class="form-group checkbox">
                    <label>
                        <input
                            type="checkbox"
                            bind:checked={newPlaylistIsPublic}
                        />
                        <span class="checkbox-text">
                            <span class="checkbox-label"
                                >🌐 Make playlist public</span
                            >
                            <span class="checkbox-hint"
                                >Everyone can see and listen</span
                            >
                        </span>
                    </label>
                </div>

                <div class="modal-actions">
                    <button
                        type="button"
                        class="cancel-btn"
                        on:click={() => (showCreateModal = false)}
                        >Cancel</button
                    >
                    <button type="submit" class="submit-btn"
                        >✨ Create Playlist</button
                    >
                </div>
            </form>
        </div>
    </div>
{/if}

<style scoped lang="scss">
    @use "$styles/pages/Playlists.scss";
</style>
