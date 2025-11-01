<script lang="ts">
    import { onMount } from "svelte";
    import Login from "./pages/Login.svelte";
    import Register from "./pages/Register.svelte";
    import Dashboard from "./pages/Dashboard.svelte";
    import Playlists from "./pages/Playlists.svelte";
    import PlaylistDetail from "./pages/PlaylistDetail.svelte";
    import LikedSongs from "./pages/LikedSongs.svelte";
    import { authService } from "./lib/authService";
    import type { Track } from "./lib/trackService";

    let currentPage = $state<
        | "login"
        | "register"
        | "dashboard"
        | "playlists"
        | "playlist-detail"
        | "liked-songs"
    >("login");
    let isAuthenticated = $state(false);
    let playlistId = $state<string>("");
    
    let currentTrack = $state<Track | null>(null);
    let isPlaying = $state(false);
    let allTracks = $state<Track[]>([]);

    onMount(() => {
        isAuthenticated = authService.isAuthenticated();

        const handleRouteChange = () => {
            const path = window.location.pathname;
            if (!isAuthenticated && path !== "/register") {
                currentPage = "login";
                return;
            }

            if (path === "/dashboard" || path === "/") {
                currentPage = "dashboard";
            } else if (path === "/playlists") {
                currentPage = "playlists";
            } else if (path.startsWith("/playlist/")) {
                playlistId = path.split("/")[2];
                currentPage = "playlist-detail";
            } else if (path === "/liked") {
                currentPage = "liked-songs";
            } else if (path === "/register") {
                currentPage = "register";
            } else if (path === "/login") {
                currentPage = "login";
            } else if (isAuthenticated) {
                currentPage = "dashboard";
            } else {
                currentPage = "login";
            }
        };

        handleRouteChange();
        window.addEventListener("popstate", handleRouteChange);

        return () => {
            window.removeEventListener("popstate", handleRouteChange);
        };
    });

    function handleLogin() {
        isAuthenticated = true;
        window.history.pushState({}, "", "/dashboard");
        currentPage = "dashboard";
    }

    function handleLogout() {
        authService.logout();
        isAuthenticated = false;
        window.history.pushState({}, "", "/login");
        currentPage = "login";
    }

    function switchToRegister() {
        window.history.pushState({}, "", "/register");
        currentPage = "register";
    }

    function switchToLogin() {
        window.history.pushState({}, "", "/login");
        currentPage = "login";
    }
</script>

<main class="app">
    {#if currentPage === "login"}
        <Login onLogin={handleLogin} onSwitchToRegister={switchToRegister} />
    {:else if currentPage === "register"}
        <Register onRegister={handleLogin} onSwitchToLogin={switchToLogin} />
    {:else if currentPage === "dashboard"}
        <Dashboard 
            onLogout={handleLogout} 
            bind:currentTrack 
            bind:isPlaying 
            bind:allTracks
        />
    {:else if currentPage === "playlists"}
        <Playlists 
            bind:currentTrack 
            bind:isPlaying 
            bind:allTracks
        />
    {:else if currentPage === "playlist-detail"}
        <PlaylistDetail 
            id={playlistId} 
            bind:currentTrack 
            bind:isPlaying 
            bind:allTracks
        />
    {:else if currentPage === "liked-songs"}
        <LikedSongs 
            bind:currentTrack 
            bind:isPlaying 
            bind:allTracks
        />
    {/if}
</main>

<style scoped lang="scss">
    @use "./styles/variables.scss" as *;

    .app {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }
</style>