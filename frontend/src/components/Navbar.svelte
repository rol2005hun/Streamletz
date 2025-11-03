<script lang="ts">
  import { onMount, onDestroy } from "svelte";

  let {
    user,
    searchQuery = $bindable(""),
    onLogout,
  }: {
    user: { username: string; profileImage?: string | null } | null;
    searchQuery: string;
    onLogout: () => void;
  } = $props();

  let dropdownOpen = $state(false);
  let mobileSearchOpen = $state(false);
  let wasMobileSearchOpen = $state(false);

  function toggleDropdown() {
    dropdownOpen = !dropdownOpen;
  }

  function toggleMobileSearch() {
    mobileSearchOpen = !mobileSearchOpen;
    if (mobileSearchOpen) {
      setTimeout(() => {
        const input = document.querySelector(".mobile-search-modal input");
        if (input) (input as HTMLInputElement).focus();
      }, 100);
    }
  }

  function handleResize() {
    const isMobile = window.innerWidth <= 480;

    if (!isMobile && mobileSearchOpen) {
      wasMobileSearchOpen = true;
      mobileSearchOpen = false;
    } else if (isMobile && wasMobileSearchOpen && !mobileSearchOpen) {
      mobileSearchOpen = true;
      wasMobileSearchOpen = false;
      setTimeout(() => {
        const input = document.querySelector(".mobile-search-modal input");
        if (input) (input as HTMLInputElement).focus();
      }, 100);
    }
  }

  function handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest(".profile-dropdown")) {
      dropdownOpen = false;
    }
    if (
      !target.closest(".mobile-search-modal") &&
      !target.closest(".mobile-search-btn")
    ) {
      mobileSearchOpen = false;
    }
  }

  function handleLogoutClick() {
    dropdownOpen = false;
    onLogout();
  }

  function navigate(path: string) {
    dropdownOpen = false;
    window.history.pushState({}, "", path);
    window.dispatchEvent(new PopStateEvent("popstate"));
  }

  onMount(() => {
    document.addEventListener("click", handleClickOutside);
    window.addEventListener("resize", handleResize);
  });

  onDestroy(() => {
    document.removeEventListener("click", handleClickOutside);
    window.removeEventListener("resize", handleResize);
  });
</script>

<header class="navbar">
  <div class="navbar-content">
    <div class="navbar-brand">
      <svg class="logo-icon" viewBox="0 0 24 24" fill="currentColor">
        <path
          d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
        />
      </svg>
      <h1>Streamletz</h1>
    </div>

    <div class="search-bar">
      <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
        <path
          d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
        />
      </svg>
      <input
        type="text"
        bind:value={searchQuery}
        placeholder="Search for tracks, artists, or albums..."
      />
    </div>

    <button
      class="mobile-search-btn"
      onclick={toggleMobileSearch}
      aria-label="Search"
    >
      <svg viewBox="0 0 24 24" fill="currentColor">
        <path
          d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
        />
      </svg>
    </button>

    <div class="profile-dropdown">
      <button class="profile-btn" onclick={toggleDropdown}>
        <div class="profile-avatar">
          {#if user?.profileImage}
            <img
              src={user.profileImage}
              alt={user.username}
              class="avatar-img"
            />
          {:else}
            {user?.username?.charAt(0).toUpperCase()}
          {/if}
        </div>
        <span class="profile-name">{user?.username}</span>
        <svg
          class="dropdown-icon"
          class:open={dropdownOpen}
          viewBox="0 0 24 24"
          fill="currentColor"
        >
          <path d="M7 10l5 5 5-5z" />
        </svg>
      </button>

      {#if dropdownOpen}
        <div class="dropdown-menu">
          <div class="dropdown-header">
            <div class="dropdown-avatar">
              {#if user?.profileImage}
                <img
                  src={user.profileImage}
                  alt={user.username}
                  class="avatar-img"
                />
              {:else}
                {user?.username?.charAt(0).toUpperCase()}
              {/if}
            </div>
            <div class="dropdown-user-info">
              <div class="dropdown-username">{user?.username}</div>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <button class="dropdown-item" onclick={() => navigate("/profile")}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
              />
            </svg>
            Profile
          </button>
          <button class="dropdown-item" onclick={() => navigate("/settings")}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"
              />
            </svg>
            Settings
          </button>
          <div class="dropdown-divider"></div>
          <button class="dropdown-item logout" onclick={handleLogoutClick}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"
              />
            </svg>
            Logout
          </button>
        </div>
      {/if}
    </div>
  </div>

  {#if mobileSearchOpen}
    <div class="mobile-search-modal">
      <div class="mobile-search-content">
        <button
          class="close-search-btn"
          onclick={toggleMobileSearch}
          aria-label="Close search"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
            />
          </svg>
        </button>
        <div class="mobile-search-input">
          <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
            />
          </svg>
          <input
            type="text"
            bind:value={searchQuery}
            placeholder="Search for tracks, artists, or albums..."
          />
        </div>
      </div>
    </div>
  {/if}
</header>

<style scoped lang="scss">
  @use "../styles/components/Navbar.scss";
</style>
