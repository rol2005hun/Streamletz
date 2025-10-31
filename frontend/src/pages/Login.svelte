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

    // Prevent any default form behavior
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

<style lang="scss">
  @use "sass:color";
  @use "../styles/variables.scss" as *;

  .login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xl;
    position: relative;
    z-index: 1;
  }

  .login-card {
    background: rgba($background-card, 0.7);
    backdrop-filter: blur(20px);
    border: 1px solid rgba($primary-color, 0.1);
    border-radius: $border-radius-xl;
    padding: $spacing-xxxl;
    width: 100%;
    max-width: 480px;
    box-shadow: $shadow-xl;
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: $gradient-primary;
    }

    &::after {
      content: "";
      position: absolute;
      top: -50%;
      right: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(
        circle,
        rgba($primary-color, 0.05) 0%,
        transparent 70%
      );
      pointer-events: none;
    }
  }

  .login-header {
    text-align: center;
    margin-bottom: $spacing-xxxl;
    position: relative;
    z-index: 1;

    h1 {
      font-size: 3rem;
      margin-bottom: $spacing-md;
      background: $gradient-primary;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: -0.02em;
    }

    .motto {
      color: $text-secondary;
      font-size: 1.1rem;
      font-weight: 300;
      letter-spacing: 0.5px;
    }
  }

  form {
    position: relative;
    z-index: 1;

    button[type="submit"] {
      width: 100%;
      margin-top: $spacing-xl;
      padding: $spacing-lg;
      font-size: 1rem;
    }
  }

  .password-input-wrapper {
    position: relative;
    display: flex;
    align-items: center;

    input {
      flex: 1;
      padding-right: 3rem;
    }

    .password-toggle {
      position: absolute;
      right: $spacing-md;
      background: none;
      border: none;
      color: $text-muted;
      cursor: pointer;
      padding: $spacing-sm;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color $transition-fast;

      svg {
        width: 20px;
        height: 20px;
      }

      &:hover {
        color: $primary-color;
      }
    }
  }

  .signup-link {
    text-align: center;
    margin-top: $spacing-xl;
    color: $text-secondary;
    font-size: 0.95rem;

    .link-btn {
      background: none;
      color: $primary-light;
      padding: 0;
      font-weight: 600;
      font-size: 1rem;
      text-decoration: none;
      border-bottom: 2px solid transparent;
      transition: all $transition-normal;

      &:hover {
        color: $accent-light;
        border-bottom-color: $accent-light;
      }
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .login-card {
      padding: $spacing-xxl;
    }

    .login-header h1 {
      font-size: 2.5rem;
    }
  }
</style>