<script lang="ts">
  import { authService, type LoginData } from "../lib/authService";
  import "../styles/pages/Login.scss";

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
  <div class="login-card">
    <div class="login-header">
      <h1>Streamletz</h1>
      <p class="motto">Your sound. Your stream. Your rules.</p>
    </div>

    <form onsubmit={handleSubmit} action="javascript:void(0);">
      {#if error}
        <div class="error-message">{error}</div>
      {/if}

      <div class="form-group">
        <label for="username">Username</label>
        <input
          type="text"
          id="username"
          bind:value={username}
          placeholder="Enter your username"
          required
        />
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <div class="password-input-wrapper">
          <input
            type={showPassword ? "text" : "password"}
            id="password"
            bind:value={password}
            placeholder="Enter your password"
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
        {:else}
          Login
        {/if}
      </button>

      <p class="signup-link">
        Don't have an account?
        <button type="button" class="link-btn" onclick={onSwitchToRegister}>
          Sign up
        </button>
      </p>
    </form>
  </div>
</div>