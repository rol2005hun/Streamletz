<script lang="ts">
    import { goto, invalidateAll } from "$app/navigation";
    import { tick } from "svelte";
    import { authService } from "$lib/authService";
    import { API_BASE_URL } from "$lib/api";
    import { trackService, type Track } from "$lib/trackService";
    import { playlistService, type Playlist } from "$lib/playlistService";
    import { likedTrackService } from "$lib/likedTrackService";
    import { currentTrack, isPlaying, allTracks } from "$lib/stores";
    import Navbar from "$lib/components/Navbar.svelte";
    import Sidebar from "$lib/components/Sidebar.svelte";

    const props = $props();
    let data = $derived(props.data);

    let initialTrackId = $derived(data.lastPlayback?.trackId);

    let user = $derived(data.user);
    let playlists: Playlist[] = $derived(data.playlists ?? []);
    const PAGE_SIZE = 60;
    const MAX_TRACKS_IN_MEMORY = 240;

    let browseTracks: Track[] = $state([]);
    let browseNextCursor: string | null = $state(null);
    let browseHasMore: boolean = $state(false);
    let browseLoading = $state(false);
    let didTryInitialBrowseLoad = $state(false);

    let searchResults: Track[] = $state([]);
    let searchLoading = $state(false);
    let searchQuery = $state("");

    let tracks: Track[] = $derived(
        searchQuery.trim() ? searchResults : browseTracks,
    );

    let error = $state("");
    let searchTimeout: ReturnType<typeof setTimeout> | null = null;
    let sidebarCollapsed = $state(false);
    let sidebarWidth = $state(280);
    let showPlaylistModal = $state(false);
    let selectedTrackForPlaylist: Track | null = $state(null);
    let userPlaylists: Playlist[] = $state([]);
    let playlistsLoading = $state(false);
    let likedTracks = $state(new Set<number>());

    let didInitFromServerData = $state(false);

    let scrollEl: HTMLElement | null = $state(null);
    let bottomSentinel: HTMLElement | null = $state(null);
    let bottomObserver: IntersectionObserver | null = null;

    $effect(() => {
        allTracks.set(tracks);
    });

    $effect(() => {
        if (didInitFromServerData) return;

        const serverTracks = (data as any).tracks as Track[] | undefined;
        browseTracks = serverTracks ?? [];
        browseNextCursor = (data as any).nextCursor ?? null;
        browseHasMore = !!(data as any).hasMore;
        error = (data as any).trackLoadError ?? "";

        sidebarCollapsed = data.sidebarCollapsed ?? false;
        sidebarWidth = data.sidebarWidth ?? 280;
        likedTracks = new Set<number>(data.likedTrackIds ?? []);

        didInitFromServerData = true;
    });

    $effect(() => {
        // If SSR didn't provide tracks (or failed), try a client-side first page.
        if (didTryInitialBrowseLoad) return;
        if (searchQuery.trim()) return;
        if (!user) return;
        if (browseTracks.length > 0) return;

        didTryInitialBrowseLoad = true;
        void (async () => {
            browseLoading = true;
            try {
                const response = await trackService.browseTracks(
                    PAGE_SIZE,
                    null,
                );
                const newItems = response.items ?? [];
                browseTracks = newItems;
                browseNextCursor = response.nextCursor ?? null;
                browseHasMore = !!response.hasMore;

                try {
                    const likedIds = await likedTrackService.getLikedStatus(
                        newItems.map((t) => t.id),
                    );
                    likedTracks = new Set<number>(likedIds);
                } catch {
                    // ignore liked status failures for initial load
                }
            } catch {
                error = "Failed to load tracks.";
            } finally {
                browseLoading = false;
            }
        })();
    });

    $effect(() => {
        if (!scrollEl || !bottomSentinel) return;
        if (bottomObserver) bottomObserver.disconnect();

        bottomObserver = new IntersectionObserver(
            (entries) => {
                if (searchQuery.trim()) return;
                const entry = entries[0];
                if (entry?.isIntersecting) {
                    void loadNextBrowsePage({
                        keepLoadingWhileNearBottom: true,
                        remaining: 5,
                    });
                }
            },
            {
                root: scrollEl,
                rootMargin: "600px 0px",
                threshold: 0,
            },
        );

        bottomObserver.observe(bottomSentinel);

        return () => {
            bottomObserver?.disconnect();
            bottomObserver = null;
        };
    });

    $effect(() => {
        if (!$currentTrack && tracks.length > 0) {
            if (initialTrackId) {
                const found = tracks.find((t) => t.id === initialTrackId);
                if (found) {
                    currentTrack.set(found);
                } else {
                    currentTrack.set(tracks[0]);
                }
            } else {
                currentTrack.set(tracks[0]);
            }
        }
    });

    $effect(() => {
        if (searchQuery.trim()) {
            if (searchTimeout) clearTimeout(searchTimeout);
            searchTimeout = setTimeout(async () => {
                try {
                    searchLoading = true;
                    const results =
                        await trackService.searchTracks(searchQuery);
                    searchResults = results;

                    const likedIds = await likedTrackService.getLikedStatus(
                        results.map((t) => t.id),
                    );
                    likedTracks = new Set<number>(likedIds);
                } catch {
                    error = "Search failed. Please try again.";
                } finally {
                    searchLoading = false;
                }
            }, 300);
        } else {
            searchLoading = false;
        }
    });

    function isNearBottom(el: HTMLElement, thresholdPx: number = 200): boolean {
        return el.scrollTop + el.clientHeight >= el.scrollHeight - thresholdPx;
    }

    async function loadNextBrowsePage(
        options: {
            keepLoadingWhileNearBottom?: boolean;
            remaining?: number;
        } = {},
    ) {
        if (browseLoading) return;
        if (!browseHasMore) return;
        if (!browseNextCursor) return;
        if (!scrollEl) return;

        const keepLoadingWhileNearBottom = !!options.keepLoadingWhileNearBottom;
        const remaining = options.remaining ?? 0;
        const wasNearBottom = isNearBottom(scrollEl, 300);

        browseLoading = true;
        try {
            const response = await trackService.browseTracks(
                PAGE_SIZE,
                browseNextCursor,
            );
            const newItems = response.items ?? [];

            if (newItems.length === 0) {
                browseHasMore = false;
                browseNextCursor = null;
                return;
            }

            // append
            browseTracks = [...browseTracks, ...newItems];
            browseNextCursor = response.nextCursor ?? null;
            browseHasMore = !!response.hasMore;

            // update liked set for appended items
            try {
                const likedIds = await likedTrackService.getLikedStatus(
                    newItems.map((t) => t.id),
                );
                if (likedIds.length) {
                    const merged = new Set(likedTracks);
                    for (const id of likedIds) merged.add(id);
                    likedTracks = merged;
                }
            } catch {
                // ignore liked status failures for background paging
            }

            await tick();

            if (wasNearBottom) {
                scrollEl.scrollTop = scrollEl.scrollHeight;
                await tick();
            }

            // memory cap: drop oldest items and compensate scroll
            if (browseTracks.length > MAX_TRACKS_IN_MEMORY) {
                const dropCount = browseTracks.length - MAX_TRACKS_IN_MEMORY;
                const beforeHeight = scrollEl.scrollHeight;
                const beforeTop = scrollEl.scrollTop;

                browseTracks = browseTracks.slice(dropCount);
                await tick();

                if (wasNearBottom) {
                    scrollEl.scrollTop = scrollEl.scrollHeight;
                } else {
                    const afterHeight = scrollEl.scrollHeight;
                    const heightDelta = beforeHeight - afterHeight;
                    scrollEl.scrollTop = Math.max(0, beforeTop - heightDelta);
                }
            }
        } catch {
            error = "Failed to load more tracks.";
        } finally {
            browseLoading = false;
        }

        if (keepLoadingWhileNearBottom && remaining > 0 && scrollEl) {
            if (
                !searchQuery.trim() &&
                browseHasMore &&
                browseNextCursor &&
                isNearBottom(scrollEl, 600)
            ) {
                await loadNextBrowsePage({
                    keepLoadingWhileNearBottom: true,
                    remaining: remaining - 1,
                });
            }
        }
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
        const secs = Math.floor(seconds % 60);
        return `${mins}:${secs.toString().padStart(2, "0")}`;
    }

    async function openAddToPlaylistModal(track: Track) {
        selectedTrackForPlaylist = track;
        showPlaylistModal = true;
        playlistsLoading = true;
        try {
            userPlaylists = await playlistService.getUserPlaylists();
        } catch {
            error = "Failed to load playlists";
        } finally {
            playlistsLoading = false;
        }
    }

    async function addToPlaylist(playlistId: number) {
        if (!selectedTrackForPlaylist) return;
        try {
            await playlistService.addTrackToPlaylist(
                playlistId,
                selectedTrackForPlaylist.id,
            );
            showPlaylistModal = false;
            selectedTrackForPlaylist = null;
        } catch (err: any) {
            error =
                err.response?.data?.message ||
                "Failed to add track to playlist";
        }
    }

    function closePlaylistModal() {
        showPlaylistModal = false;
        selectedTrackForPlaylist = null;
    }

    async function toggleLike(track: Track, e: MouseEvent) {
        e.stopPropagation();
        const isLiked = likedTracks.has(track.id);
        try {
            await likedTrackService.toggleLike(track.id, isLiked);
            const newSet = new Set(likedTracks);
            isLiked ? newSet.delete(track.id) : newSet.add(track.id);
            likedTracks = newSet;
        } catch (err: any) {
            error =
                err.response?.data?.message || "Failed to update like status";
        }
    }

    async function handleLogout() {
        authService.logout();

        await invalidateAll();

        goto("/login");
    }
</script>

<div class="dashboard">
    <Navbar {user} bind:searchQuery onLogout={handleLogout} />

    <div class="main-view">
        <Sidebar
            bind:collapsed={sidebarCollapsed}
            bind:width={sidebarWidth}
            {playlists}
        />

        <main
            class="dashboard-content"
            style="margin-left: {sidebarCollapsed ? 0 : sidebarWidth}px"
            bind:this={scrollEl}
        >
            {#if error}
                <div class="error-message">{error}</div>
            {/if}

            {#if searchLoading && searchQuery.trim()}
                <div class="loading-container">
                    <div class="loading"></div>
                    <p>Loading tracks...</p>
                </div>
            {:else if !searchQuery.trim() && tracks.length === 0 && !didTryInitialBrowseLoad}
                <div class="tracks-grid">
                    {#each Array.from({ length: 12 }) as _}
                        <div class="track-card skeleton" aria-hidden="true">
                            <div class="track-cover skeleton-block"></div>
                            <div class="track-details">
                                <div class="skeleton-line title"></div>
                                <div class="skeleton-line"></div>
                                <div class="skeleton-line short"></div>
                            </div>
                        </div>
                    {/each}
                </div>
            {:else if !searchQuery.trim() && browseLoading && tracks.length === 0}
                <div class="tracks-grid">
                    {#each Array.from({ length: 12 }) as _}
                        <div class="track-card skeleton" aria-hidden="true">
                            <div class="track-cover skeleton-block"></div>
                            <div class="track-details">
                                <div class="skeleton-line title"></div>
                                <div class="skeleton-line"></div>
                                <div class="skeleton-line short"></div>
                            </div>
                        </div>
                    {/each}
                </div>
            {:else if tracks.length === 0}
                <div class="empty-state">
                    <h3>No tracks found</h3>
                    <p>
                        Try a different search or check back later for new
                        content.
                    </p>
                </div>
            {:else}
                <div class="tracks-grid">
                    {#each tracks as track (track.id)}
                        <div
                            class="track-card"
                            class:playing={$currentTrack?.id === track.id}
                        >
                            <div class="track-cover">
                                {#if track.coverArtUrl}
                                    <img
                                        src={track.coverArtUrl.startsWith(
                                            "http",
                                        )
                                            ? track.coverArtUrl
                                            : API_BASE_URL + track.coverArtUrl}
                                        alt={track.album || track.title}
                                    />
                                {:else}
                                    <div class="cover-placeholder">
                                        <svg
                                            viewBox="0 0 24 24"
                                            fill="currentColor"
                                        >
                                            <path
                                                d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
                                            />
                                        </svg>
                                    </div>
                                {/if}
                                <button
                                    class="like-btn"
                                    class:liked={likedTracks.has(track.id)}
                                    onclick={(e) => toggleLike(track, e)}
                                    title={likedTracks.has(track.id)
                                        ? "Unlike"
                                        : "Like"}
                                >
                                    {#if likedTracks.has(track.id)}
                                        <svg
                                            viewBox="0 0 24 24"
                                            fill="currentColor"
                                        >
                                            <path
                                                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                                            />
                                        </svg>
                                    {:else}
                                        <svg
                                            viewBox="0 0 24 24"
                                            fill="currentColor"
                                        >
                                            <path
                                                d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"
                                            />
                                        </svg>
                                    {/if}
                                </button>
                                <button
                                    class="play-overlay"
                                    onclick={() => playTrack(track)}
                                    aria-label="Play {track.title}"
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
                            </div>
                            <div class="track-details">
                                <h3 class="track-title">{track.title}</h3>
                                <p class="track-artist">{track.artist}</p>
                                {#if track.album}
                                    <p class="track-album">{track.album}</p>
                                {/if}
                                <div class="track-meta">
                                    <span class="duration"
                                        >{formatDuration(track.duration)}</span
                                    >
                                    <span class="plays"
                                        >{track.playCount} plays</span
                                    >
                                </div>
                                <button
                                    class="add-to-playlist-btn"
                                    onclick={() =>
                                        openAddToPlaylistModal(track)}
                                    title="Add to playlist"
                                >
                                    + Add to Playlist
                                </button>
                            </div>
                        </div>
                    {/each}

                    {#if !searchQuery.trim() && browseLoading}
                        {#each Array.from({ length: 12 }) as _}
                            <div class="track-card skeleton" aria-hidden="true">
                                <div class="track-cover skeleton-block"></div>
                                <div class="track-details">
                                    <div class="skeleton-line title"></div>
                                    <div class="skeleton-line"></div>
                                    <div class="skeleton-line short"></div>
                                </div>
                            </div>
                        {/each}
                    {/if}
                </div>

                {#if !searchQuery.trim()}
                    <div
                        class="infinite-sentinel"
                        bind:this={bottomSentinel}
                    ></div>
                {/if}
            {/if}
        </main>
    </div>

    {#if showPlaylistModal}
        <div
            class="modal-overlay"
            onclick={closePlaylistModal}
            onkeydown={(e) => e.key === "Escape" && closePlaylistModal()}
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
                <div class="modal-header">
                    <h2 id="modal-title">Add to Playlist</h2>
                    <p class="modal-subtitle">
                        {selectedTrackForPlaylist?.title} – {selectedTrackForPlaylist?.artist}
                    </p>
                </div>

                <div class="modal-content">
                    {#if playlistsLoading}
                        <div class="loading-state">
                            <div class="spinner"></div>
                            <p>Loading playlists...</p>
                        </div>
                    {:else if userPlaylists.length === 0}
                        <div class="empty-state">
                            <p>You don't have any playlists yet.</p>
                        </div>
                    {:else}
                        <div class="playlists-list">
                            {#each userPlaylists as playlist}
                                <button
                                    class="playlist-item"
                                    onclick={() => addToPlaylist(playlist.id)}
                                >
                                    <div class="playlist-icon">🎵</div>
                                    <div class="playlist-info">
                                        <div class="playlist-name">
                                            {playlist.name}
                                        </div>
                                        <div class="playlist-count">
                                            {playlist.trackCount} tracks
                                        </div>
                                    </div>
                                </button>
                            {/each}
                        </div>
                    {/if}
                </div>

                <div class="modal-footer">
                    <button class="btn-cancel" onclick={closePlaylistModal}
                        >Cancel</button
                    >
                </div>
            </div>
        </div>
    {/if}
</div>

<style scoped lang="scss">
    @use "styles/pages/Dashboard";
</style>
