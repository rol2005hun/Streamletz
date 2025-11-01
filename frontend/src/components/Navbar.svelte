<script lang="ts">
  import { onMount, onDestroy } from "svelte";

  let {
    user,
    searchQuery = $bindable(""),
    onLogout,
  }: {
    user: { username: string } | null;
    searchQuery: string;
    onLogout: () => void;
  } = $props();

  let dropdownOpen = $state(false);

  function toggleDropdown() {
    dropdownOpen = !dropdownOpen;
  }

  function handleClickOutside(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest(".profile-dropdown")) {
      dropdownOpen = false;
    }
  }

  function handleLogoutClick() {
    dropdownOpen = false;
    onLogout();
  }

  onMount(() => {
    document.addEventListener("click", handleClickOutside);
  });

  onDestroy(() => {
    document.removeEventListener("click", handleClickOutside);
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

    <div class="profile-dropdown">
      <button class="profile-btn" onclick={toggleDropdown}>
        <div class="profile-avatar">
          {user?.username?.charAt(0).toUpperCase()}
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
              {user?.username?.charAt(0).toUpperCase()}
            </div>
            <div class="dropdown-user-info">
              <div class="dropdown-username">{user?.username}</div>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <button class="dropdown-item" onclick={() => (dropdownOpen = false)}>
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
              />
            </svg>
            Profile
          </button>
          <button class="dropdown-item" onclick={() => (dropdownOpen = false)}>
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
</header>

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .navbar {
    background: $background-card;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    position: sticky;
    top: 0;
    z-index: $z-header;
    flex-shrink: 0;
    backdrop-filter: blur(20px);
    background: rgba(24, 24, 24, 0.95);
  }

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md $spacing-lg;
    gap: $spacing-lg;
  }

  .navbar-brand {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    min-width: 200px;

    .logo-icon {
      width: 32px;
      height: 32px;
      color: $primary-color;
    }

    h1 {
      font-size: 1.5rem;
      font-weight: 700;
      background: $gradient-primary;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .search-bar {
    flex: 1;
    max-width: 600px;
    position: relative;

    .search-icon {
      position: absolute;
      left: $spacing-md;
      top: 50%;
      transform: translateY(-50%);
      width: 20px;
      height: 20px;
      color: $text-secondary;
      pointer-events: none;
    }

    input {
      width: 100%;
      padding: $spacing-md $spacing-md $spacing-md 48px;
      background: $background-light;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: $border-radius-full;
      color: $text-primary;
      font-size: 0.95rem;
      transition: all $transition-fast;

      &::placeholder {
        color: $text-secondary;
      }

      &:focus {
        outline: none;
        border-color: $primary-color;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        background: $background-medium;
      }
    }
  }

  .profile-dropdown {
    position: relative;

    .profile-btn {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      padding: $spacing-sm $spacing-md;
      background: $background-light;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: $border-radius-full;
      color: $text-primary;
      cursor: pointer;
      transition: all $transition-fast;

      &:hover {
        background: $background-medium;
        border-color: rgba(255, 255, 255, 0.2);
      }

      .profile-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: $gradient-primary;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 0.9rem;
      }

      .profile-name {
        font-weight: 600;
        font-size: 0.95rem;
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        @media (max-width: $breakpoint-tablet) {
          display: none;
        }
      }

      .dropdown-icon {
        width: 20px;
        height: 20px;
        transition: transform $transition-fast;

        &.open {
          transform: rotate(180deg);
        }
      }
    }

    .dropdown-menu {
      position: absolute;
      top: calc(100% + $spacing-sm);
      right: 0;
      width: 240px;
      background: $background-card;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: $border-radius-md;
      padding: $spacing-sm;
      box-shadow: $shadow-lg;
      animation: dropdownSlide 0.2s ease;
      z-index: $z-dropdown;
    }

    @keyframes dropdownSlide {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .dropdown-header {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      padding: $spacing-md;

      .dropdown-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: $gradient-primary;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 1rem;
      }

      .dropdown-user-info {
        flex: 1;

        .dropdown-username {
          font-weight: 600;
          color: $text-primary;
        }
      }
    }

    .dropdown-divider {
      height: 1px;
      background: rgba(255, 255, 255, 0.1);
      margin: $spacing-sm 0;
    }

    .dropdown-item {
      width: 100%;
      display: flex;
      align-items: center;
      gap: $spacing-md;
      padding: $spacing-md;
      background: none;
      border: none;
      color: $text-secondary;
      text-align: left;
      cursor: pointer;
      border-radius: $border-radius-sm;
      transition: all $transition-fast;
      font-size: 0.95rem;

      svg {
        width: 20px;
        height: 20px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.05);
        color: $text-primary;
      }

      &.logout {
        color: $error-color;

        svg {
          color: $error-color;
        }

        &:hover {
          background: rgba(239, 68, 68, 0.1);
          color: #fca5a5;
        }
      }
    }
  }
</style>
