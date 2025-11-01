<script lang="ts">
    import { onMount } from "svelte";
    import { playlistService, type Playlist } from "../lib/playlistService";

    let playlists = $state<Playlist[]>([]);
    let showCreateModal = $state(false);
    let newPlaylistName = $state("");
    let newPlaylistDescription = $state("");
    let newPlaylistIsPublic = $state(false);
    let loading = $state(true);
    let error = $state("");

    onMount(async () => {
        await loadPlaylists();
    });

    async function loadPlaylists() {
        try {
            loading = true;
            playlists = await playlistService.getUserPlaylists();
        } catch (err: any) {
            error = err.response?.data?.message || "Failed to load playlists";
        } finally {
            loading = false;
        }
    }

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

            // Reset form
            newPlaylistName = "";
            newPlaylistDescription = "";
            newPlaylistIsPublic = false;
            showCreateModal = false;

            // Reload playlists
            await loadPlaylists();
        } catch (err: any) {
            error = err.response?.data?.message || "Failed to create playlist";
        }
    }

    async function deletePlaylist(id: number) {
        if (!confirm("Are you sure you want to delete this playlist?")) {
            return;
        }

        try {
            await playlistService.deletePlaylist(id);
            await loadPlaylists();
        } catch (err: any) {
            error = err.response?.data?.message || "Failed to delete playlist";
        }
    }

    function formatDuration(seconds: number): string {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);

        if (hours > 0) {
            return `${hours} hr ${minutes} min`;
        }
        return `${minutes} min`;
    }
</script>

<div class="playlists-container">
    <div class="header">
        <h1>Your Playlists</h1>
        <button class="create-btn" onclick={() => (showCreateModal = true)}>
            + Create Playlist
        </button>
    </div>

    {#if loading}
        <div class="loading">Loading playlists...</div>
    {:else if error}
        <div class="error">{error}</div>
    {:else if playlists.length === 0}
        <div class="empty">
            <p>You don't have any playlists yet.</p>
            <button onclick={() => (showCreateModal = true)}
                >Create Your First Playlist</button
            >
        </div>
    {:else}
        <div class="playlists-grid">
            {#each playlists as playlist}
                <div class="playlist-card">
                    <a href={`#/playlist/${playlist.id}`} class="playlist-link">
                        <div class="playlist-cover">
                            {#if playlist.coverImageUrl}
                                <img
                                    src={playlist.coverImageUrl}
                                    alt={playlist.name}
                                />
                            {:else}
                                <div class="default-cover">
                                    <span>🎵</span>
                                </div>
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
                        onclick={(e) => {
                            e.preventDefault();
                            deletePlaylist(playlist.id);
                        }}
                    >
                        🗑️
                    </button>
                </div>
            {/each}
        </div>
    {/if}
</div>

{#if showCreateModal}
    <div
        class="modal-overlay"
        onclick={() => (showCreateModal = false)}
        onkeydown={(e) => e.key === "Escape" && (showCreateModal = false)}
        role="button"
        tabindex="-1"
        aria-label="Close modal"
    >
        <div
            class="modal"
            onclick={(e) => e.stopPropagation()}
            onkeydown={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            tabindex="-1"
        >
            <h2 id="modal-title">Create New Playlist</h2>
            <form
                onsubmit={(e) => {
                    e.preventDefault();
                    createPlaylist();
                }}
            >
                <div class="form-group">
                    <label for="name">Playlist Name *</label>
                    <input
                        id="name"
                        type="text"
                        bind:value={newPlaylistName}
                        placeholder="My Awesome Playlist"
                        required
                    />
                </div>

                <div class="form-group">
                    <label for="description">Description</label>
                    <textarea
                        id="description"
                        bind:value={newPlaylistDescription}
                        placeholder="Add a description..."
                        rows="3"
                    ></textarea>
                </div>

                <div class="form-group checkbox">
                    <label>
                        <input
                            type="checkbox"
                            bind:checked={newPlaylistIsPublic}
                        />
                        Make playlist public
                    </label>
                </div>

                <div class="modal-actions">
                    <button
                        type="button"
                        class="cancel-btn"
                        onclick={() => (showCreateModal = false)}
                    >
                        Cancel
                    </button>
                    <button type="submit" class="submit-btn">Create</button>
                </div>
            </form>
        </div>
    </div>
{/if}

<style lang="scss">
    @use "../styles/variables.scss" as *;

    .playlists-container {
        padding: 2rem;
        max-width: 1400px;
        margin: 0 auto;
    }

    .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;

        h1 {
            font-size: 2rem;
            font-weight: 700;
            color: $text-primary;
        }

        .create-btn {
            background: $primary-color;
            color: white;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 500px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
                background: lighten($primary-color, 10%);
                transform: scale(1.05);
            }
        }
    }

    .loading,
    .error {
        text-align: center;
        padding: 2rem;
        color: $text-secondary;
    }

    .error {
        color: #f44336;
    }

    .empty {
        text-align: center;
        padding: 4rem 2rem;

        p {
            font-size: 1.2rem;
            color: $text-secondary;
            margin-bottom: 1.5rem;
        }

        button {
            background: $primary-color;
            color: white;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 500px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
                background: lighten($primary-color, 10%);
                transform: scale(1.05);
            }
        }
    }

    .playlists-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 1.5rem;
    }

    .playlist-card {
        background: $card-background;
        border-radius: 8px;
        padding: 1rem;
        transition: background 0.2s;
        position: relative;

        &:hover {
            background: lighten($card-background, 5%);
        }

        .playlist-link {
            text-decoration: none;
            color: inherit;
            display: block;
        }

        .playlist-cover {
            width: 100%;
            aspect-ratio: 1;
            border-radius: 8px;
            overflow: hidden;
            margin-bottom: 1rem;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .default-cover {
                width: 100%;
                height: 100%;
                background: linear-gradient(
                    135deg,
                    $primary-color,
                    darken($primary-color, 20%)
                );
                display: flex;
                align-items: center;
                justify-content: center;

                span {
                    font-size: 4rem;
                }
            }
        }

        .playlist-info {
            h3 {
                font-size: 1rem;
                font-weight: 700;
                color: $text-primary;
                margin-bottom: 0.5rem;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .description {
                font-size: 0.875rem;
                color: $text-secondary;
                margin-bottom: 0.5rem;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
            }

            .playlist-meta {
                font-size: 0.875rem;
                color: $text-secondary;
            }

            .public-badge {
                display: inline-block;
                background: rgba($primary-color, 0.2);
                color: $primary-color;
                padding: 0.25rem 0.5rem;
                border-radius: 4px;
                font-size: 0.75rem;
                font-weight: 600;
                margin-top: 0.5rem;
            }
        }

        .delete-btn {
            position: absolute;
            top: 1rem;
            right: 1rem;
            background: rgba(0, 0, 0, 0.6);
            border: none;
            border-radius: 50%;
            width: 32px;
            height: 32px;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.2s;
            font-size: 1rem;

            &:hover {
                background: rgba(244, 67, 54, 0.8);
            }
        }

        &:hover .delete-btn {
            opacity: 1;
        }
    }

    .modal-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }

    .modal {
        background: $card-background;
        border-radius: 8px;
        padding: 2rem;
        max-width: 500px;
        width: 90%;

        h2 {
            color: $text-primary;
            margin-bottom: 1.5rem;
        }

        .form-group {
            margin-bottom: 1.5rem;

            label {
                display: block;
                color: $text-primary;
                font-weight: 600;
                margin-bottom: 0.5rem;
            }

            input[type="text"],
            textarea {
                width: 100%;
                padding: 0.75rem;
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 4px;
                background: rgba(0, 0, 0, 0.2);
                color: $text-primary;
                font-family: inherit;
                font-size: 1rem;

                &:focus {
                    outline: none;
                    border-color: $primary-color;
                }
            }

            textarea {
                resize: vertical;
            }

            &.checkbox {
                label {
                    display: flex;
                    align-items: center;
                    cursor: pointer;

                    input[type="checkbox"] {
                        margin-right: 0.5rem;
                        width: 18px;
                        height: 18px;
                        cursor: pointer;
                    }
                }
            }
        }

        .modal-actions {
            display: flex;
            gap: 1rem;
            justify-content: flex-end;

            button {
                padding: 0.75rem 1.5rem;
                border: none;
                border-radius: 500px;
                font-weight: 700;
                cursor: pointer;
                transition: all 0.2s;

                &.cancel-btn {
                    background: transparent;
                    color: $text-secondary;

                    &:hover {
                        color: $text-primary;
                    }
                }

                &.submit-btn {
                    background: $primary-color;
                    color: white;

                    &:hover {
                        background: lighten($primary-color, 10%);
                        transform: scale(1.05);
                    }
                }
            }
        }
    }
</style>
