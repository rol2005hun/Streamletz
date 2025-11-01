<script lang="ts">
    import { onMount } from "svelte";
    import Login from "./pages/Login.svelte";
    import Register from "./pages/Register.svelte";
    import Dashboard from "./pages/Dashboard.svelte";
    import Playlists from "./pages/Playlists.svelte";
    import PlaylistDetail from "./pages/PlaylistDetail.svelte";
    import LikedSongs from "./pages/LikedSongs.svelte";
    import { authService } from "./lib/authService";

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

    onMount(() => {
        isAuthenticated = authService.isAuthenticated();

        const handleHashChange = () => {
            const hash = window.location.hash.slice(1);
            if (!isAuthenticated && hash !== "/register") {
                currentPage = "login";
                return;
            }

            if (hash === "/dashboard") {
                currentPage = "dashboard";
            } else if (hash === "/playlists") {
                currentPage = "playlists";
            } else if (hash.startsWith("/playlist/")) {
                playlistId = hash.split("/")[2];
                currentPage = "playlist-detail";
            } else if (hash === "/liked") {
                currentPage = "liked-songs";
            } else if (hash === "/register") {
                currentPage = "register";
            } else if (isAuthenticated) {
                currentPage = "dashboard";
            } else {
                currentPage = "login";
            }
        };

        handleHashChange();
        window.addEventListener("hashchange", handleHashChange);

        return () => {
            window.removeEventListener("hashchange", handleHashChange);
        };
    });

    function handleLogin() {
        isAuthenticated = true;
        window.location.hash = "#/dashboard";
    }

    function handleLogout() {
        authService.logout();
        isAuthenticated = false;
        window.location.hash = "#/login";
    }

    function switchToRegister() {
        window.location.hash = "#/register";
    }

    function switchToLogin() {
        window.location.hash = "#/login";
    }
</script>

<main class="app">
    {#if currentPage === "login"}
        <Login onLogin={handleLogin} onSwitchToRegister={switchToRegister} />
    {:else if currentPage === "register"}
        <Register onRegister={handleLogin} onSwitchToLogin={switchToLogin} />
    {:else if currentPage === "dashboard"}
        <Dashboard onLogout={handleLogout} />
    {:else if currentPage === "playlists"}
        <Playlists />
    {:else if currentPage === "playlist-detail"}
        <PlaylistDetail id={playlistId} />
    {:else if currentPage === "liked-songs"}
        <LikedSongs />
    {/if}
</main>

<style lang="scss">
    @use "./styles/variables.scss" as *;

    .app {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }
</style>