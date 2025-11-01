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
  let currentPath = $state(window.location.pathname);
  let animationFrameId: number | null = null;

  function toggleSidebar() {
    collapsed = !collapsed;
    localStorage.setItem("streamletz_sidebar_collapsed", collapsed.toString());
  }

  function navigate(path: string, e: MouseEvent) {
    e.preventDefault();
    window.history.pushState({}, "", path);
    window.dispatchEvent(new PopStateEvent("popstate"));
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

    if (animationFrameId !== null) {
      cancelAnimationFrame(animationFrameId);
    }

    animationFrameId = requestAnimationFrame(() => {
      const delta = e.clientX - startX;
      const newWidth = Math.max(200, Math.min(400, startWidth + delta));
      width = newWidth;
      animationFrameId = null;
    });
  }

  function stopResize() {
    if (isResizing) {
      isResizing = false;
      document.body.style.cursor = "";
      document.body.style.userSelect = "";
      localStorage.setItem("streamletz_sidebar_width", width.toString());

      if (animationFrameId !== null) {
        cancelAnimationFrame(animationFrameId);
        animationFrameId = null;
      }
    }
  }

  function handlePathChange() {
    currentPath = window.location.pathname;
  }

  onMount(() => {
    document.addEventListener("mousemove", handleResize);
    document.addEventListener("mouseup", stopResize);
    window.addEventListener("popstate", handlePathChange);

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
    window.removeEventListener("popstate", handlePathChange);

    // Clean up any pending animation frame
    if (animationFrameId !== null) {
      cancelAnimationFrame(animationFrameId);
    }
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
            href="/dashboard"
            class="sidebar-link"
            class:active={currentPath === "/dashboard" || currentPath === "/"}
            onclick={(e) => navigate("/dashboard", e)}
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
            href="/liked"
            class="sidebar-link"
            class:active={currentPath === "/liked"}
            onclick={(e) => navigate("/liked", e)}
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
            href="/playlists"
            class="sidebar-link"
            class:active={currentPath === "/playlists"}
            onclick={(e) => navigate("/playlists", e)}
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

<style scoped lang="scss">
  @use "../styles/components/Sidebar.scss";
</style>
