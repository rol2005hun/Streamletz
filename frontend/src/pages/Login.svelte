<script lang="ts">
  import { authService, type LoginData } from "../lib/authService";

  let {
    onLogin,
    onSwitchToRegister,
  }: { onLogin: () => void; onSwitchToRegister: () => void } = $props();

  let username = $state("");
  let password = $state("");
  let error = $state("");
  let loading = $state(false);
  let showPassword = $state(false);

  async function handleSubmit(e: Event) {
    e.preventDefault();
    e.stopPropagation();

    if (e.target instanceof HTMLFormElement) {
      e.target.setAttribute("action", "javascript:void(0);");
    }

    error = "";
    loading = true;

    try {
      const loginData: LoginData = { username, password };
      const response = await authService.login(loginData);

      localStorage.setItem("token", response.token);
      localStorage.setItem(
        "user",
        JSON.stringify({
          username: response.username,
          email: response.email,
        }),
      );

      onLogin();
    } catch (err: any) {
      error = err.response?.data?.message || "Login failed. Please try again.";
      console.error("Login error:", err);
    } finally {
      loading = false;
    }

    return false;
  }

  function togglePasswordVisibility() {
    showPassword = !showPassword;
  }
</script>

<div class="login-container">
  <div class="background-decoration">
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>
    <div class="circle circle-3"></div>
  </div>

  <div class="login-card">
    <div class="login-header">
      <div class="logo-container">
        <svg viewBox="0 0 24 24" fill="currentColor" class="logo-icon">
          <path
            d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
          />
        </svg>
      </div>
      <h1>Welcome Back</h1>
      <p class="motto">Sign in to continue to Streamletz</p>
    </div>

    <form onsubmit={handleSubmit} action="javascript:void(0);">
      {#if error}
        <div class="error-message">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
            />
          </svg>
          {error}
        </div>
      {/if}

      <div class="form-group">
        <label for="username">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
            />
          </svg>
          Username
        </label>
        <input
          type="text"
          id="username"
          bind:value={username}
          placeholder="Enter your username"
          autocomplete="username"
          required
        />
      </div>

      <div class="form-group">
        <label for="password">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
            />
          </svg>
          Password
        </label>
        <div class="password-input-wrapper">
          <input
            type={showPassword ? "text" : "password"}
            id="password"
            bind:value={password}
            placeholder="Enter your password"
            autocomplete="current-password"
            required
          />
          <button
            type="button"
            class="password-toggle"
            onclick={togglePasswordVisibility}
            aria-label={showPassword ? "Hide password" : "Show password"}
          >
            {#if showPassword}
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            {:else}
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
            {/if}
          </button>
        </div>
      </div>

      <button type="submit" class="btn btn-primary" disabled={loading}>
        {#if loading}
          <span class="loading"></span>
          <span>Signing in...</span>
        {:else}
          <span>Sign In</span>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"
            />
          </svg>
        {/if}
      </button>

      <div class="divider">
        <span>or</span>
      </div>

      <p class="signup-link">
        Don't have an account?
        <button type="button" class="link-btn" onclick={onSwitchToRegister}>
          Create account
        </button>
      </p>
    </form>
  </div>
</div>

<style scoped lang="scss">
  @use "../styles/pages/Login.scss";
</style>