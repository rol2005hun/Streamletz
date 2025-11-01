<script lang="ts">
  import { onMount, onDestroy } from "svelte";

  let {
    collapsed = $bindable(false),
    width = $bindable(280),
  }: {
    collapsed: boolean;
    width: number;
  } = $props();

  let isResizing = $state(false);
  let startX = 0;
  let startWidth = 0;
  let currentPath = $state(window.location.hash);

  function toggleSidebar() {
    collapsed = !collapsed;
    localStorage.setItem("streamletz_sidebar_collapsed", collapsed.toString());
  }

  function startResize(e: MouseEvent) {
    isResizing = true;
    startX = e.clientX;
    startWidth = width;
    document.body.style.cursor = "col-resize";
    document.body.style.userSelect = "none";
  }

  function handleResize(e: MouseEvent) {
    if (!isResizing) return;

    const delta = e.clientX - startX;
    const newWidth = Math.max(200, Math.min(400, startWidth + delta));
    width = newWidth;
  }

  function stopResize() {
    if (isResizing) {
      isResizing = false;
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
      localStorage.setItem("streamletz_sidebar_width", width.toString());
    }
  }

  function handleHashChange() {
    currentPath = window.location.hash;
  }

  onMount(() => {
    document.addEventListener("mousemove", handleResize);
    document.addEventListener("mouseup", stopResize);
    window.addEventListener("hashchange", handleHashChange);

    const savedWidth = localStorage.getItem("streamletz_sidebar_width");
    const savedCollapsed = localStorage.getItem("streamletz_sidebar_collapsed");

    if (savedWidth) {
      width = parseInt(savedWidth);
    }
    if (savedCollapsed === "true") {
      collapsed = true;
    }
  });

  onDestroy(() => {
    document.removeEventListener("mousemove", handleResize);
    document.removeEventListener("mouseup", stopResize);
    window.removeEventListener("hashchange", handleHashChange);
  });
</script>

{#if !collapsed}
  <nav class="sidebar" style="width: {width}px">
    <div class="sidebar-inner">
      <div class="sidebar-header">
        <div class="library-badge">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-1 9H9V9h10v2zm-4 4H9v-2h6v2zm4-8H9V5h10v2z"
            />
          </svg>
          <span>Your Library</span>
        </div>
        <button
          class="collapse-btn"
          onclick={toggleSidebar}
          title="Collapse sidebar"
        >
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z" />
          </svg>
        </button>
      </div>

      <div class="sidebar-content">
        <div class="nav-section">
          <a
            href="#/dashboard"
            class="sidebar-link"
            class:active={currentPath === "#/dashboard" || currentPath === ""}
          >
            <div class="link-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" />
              </svg>
            </div>
            <div class="link-content">
              <span class="link-title">Home</span>
              <span class="link-subtitle">Browse all tracks</span>
            </div>
          </a>

          <a
            href="#/liked"
            class="sidebar-link"
            class:active={currentPath === "#/liked"}
          >
            <div class="link-icon liked">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                />
              </svg>
            </div>
            <div class="link-content">
              <span class="link-title">Liked Songs</span>
              <span class="link-subtitle">Your favorite tracks</span>
            </div>
          </a>

          <a
            href="#/playlists"
            class="sidebar-link"
            class:active={currentPath === "#/playlists"}
          >
            <div class="link-icon playlists">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z"
                />
              </svg>
            </div>
            <div class="link-content">
              <span class="link-title">Your Playlists</span>
              <span class="link-subtitle">Create and manage</span>
            </div>
          </a>
        </div>
      </div>
    </div>

    <div
      class="resize-handle"
      class:resizing={isResizing}
      onmousedown={startResize}
      role="slider"
      aria-orientation="vertical"
      aria-label="Resize sidebar"
      aria-valuemin="200"
      aria-valuemax="400"
      aria-valuenow={width}
      tabindex="0"
    ></div>
  </nav>
{:else}
  <button class="sidebar-toggle" onclick={toggleSidebar} title="Show sidebar">
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z" />
    </svg>
  </button>
{/if}

<style lang="scss">
  @use "../styles/variables.scss" as *;

  .sidebar {
    min-width: 200px;
    max-width: 400px;
    background: linear-gradient(180deg, #1a1a1a 0%, #0a0a0a 100%);
    flex-shrink: 0;
    position: fixed;
    left: 0;
    top: 73px;
    height: calc(100vh - 73px);
    transition: width 0.2s ease;
    border-right: 1px solid rgba(255, 255, 255, 0.05);
    z-index: $z-sidebar;
    overflow: hidden;
  }

  .sidebar-inner {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    padding: $spacing-lg;

    &::-webkit-scrollbar {
      width: 8px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }

  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xl;
    padding-bottom: $spacing-md;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .library-badge {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      color: $text-primary;
      font-weight: 700;
      font-size: 1rem;

      svg {
        width: 24px;
        height: 24px;
        color: $primary-color;
      }
    }

    .collapse-btn {
      background: rgba(255, 255, 255, 0.05);
      border: none;
      color: $text-secondary;
      cursor: pointer;
      padding: $spacing-sm;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all $transition-fast;
      width: 32px;
      height: 32px;

      svg {
        width: 20px;
        height: 20px;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: $text-primary;
        transform: scale(1.05);
      }
    }
  }

  .sidebar-content {
    flex: 1;
  }

  .nav-section {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  .sidebar-link {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    color: $text-secondary;
    text-decoration: none;
    border-radius: $border-radius-md;
    transition: all $transition-fast;
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: $primary-color;
      transform: scaleY(0);
      transition: transform $transition-fast;
    }

    .link-icon {
      width: 48px;
      height: 48px;
      border-radius: $border-radius-md;
      background: rgba(255, 255, 255, 0.05);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all $transition-fast;

      svg {
        width: 24px;
        height: 24px;
        transition: transform $transition-fast;
      }

      &.liked {
        background: linear-gradient(135deg, #e11d48 0%, #be123c 100%);
      }

      &.playlists {
        background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
      }
    }

    .link-content {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex: 1;
      min-width: 0;

      .link-title {
        font-weight: 600;
        font-size: 0.95rem;
        color: inherit;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .link-subtitle {
        font-size: 0.8rem;
        color: $text-muted;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    &:hover {
      color: $text-primary;
      background: rgba(255, 255, 255, 0.05);

      .link-icon {
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

        svg {
          transform: scale(1.1);
        }

        &.liked,
        &.playlists {
          box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
        }
      }
    }

    &.active {
      color: $text-primary;
      background: rgba(59, 130, 246, 0.1);

      &::before {
        transform: scaleY(1);
      }

      .link-icon {
        background: $gradient-primary;
        box-shadow: $shadow-glow;

        &.liked {
          background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%);
        }

        &.playlists {
          background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
        }
      }

      .link-subtitle {
        color: $text-secondary;
      }
    }
  }

  .resize-handle {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    cursor: col-resize;
    background: transparent;
    transition: background $transition-fast;
    z-index: 10;

    &:hover,
    &.resizing {
      background: $primary-color;
    }
  }

  .sidebar-toggle {
    position: fixed;
    left: $spacing-md;
    top: calc(73px + $spacing-md);
    background: $background-card;
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: $text-secondary;
    cursor: pointer;
    padding: $spacing-md;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all $transition-fast;
    z-index: $z-sidebar;
    box-shadow: $shadow-md;

    svg {
      width: 20px;
      height: 20px;
    }

    &:hover {
      background: $primary-color;
      color: $text-primary;
      border-color: $primary-color;
      transform: scale(1.1);
      box-shadow: $shadow-glow;
    }
  }
</style>
