<script lang="ts">
  import { onMount } from 'svelte';
  import Login from './pages/Login.svelte';
  import Register from './pages/Register.svelte';
  import Dashboard from './pages/Dashboard.svelte';
  import { authService } from './lib/authService';

  let currentPage: 'login' | 'register' | 'dashboard' = 'login';
  let isAuthenticated = false;

  onMount(() => {
    isAuthenticated = authService.isAuthenticated();
    if (isAuthenticated) {
      currentPage = 'dashboard';
    }
  });

  function handleLogin() {
    isAuthenticated = true;
    currentPage = 'dashboard';
  }

  function handleLogout() {
    authService.logout();
    isAuthenticated = false;
    currentPage = 'login';
  }

  function switchToRegister() {
    currentPage = 'register';
  }

  function switchToLogin() {
    currentPage = 'login';
  }
</script>

<main class="app">
  {#if currentPage === 'login'}
    <Login onLogin={handleLogin} onSwitchToRegister={switchToRegister} />
  {:else if currentPage === 'register'}
    <Register onRegister={handleLogin} onSwitchToLogin={switchToLogin} />
  {:else if currentPage === 'dashboard'}
    <Dashboard onLogout={handleLogout} />
  {/if}
</main>

<style lang="scss">
  .app {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
</style>
