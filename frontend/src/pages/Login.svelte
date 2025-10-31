<script lang="ts">
  import { authService, type LoginData } from '../lib/authService';

  let { onLogin, onSwitchToRegister }: { onLogin: () => void; onSwitchToRegister: () => void } = $props();

  let username = $state('');
  let password = $state('');
  let error = $state('');
  let loading = $state(false);

  async function handleSubmit(e: Event) {
    e.preventDefault();
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
    } finally {
      loading = false;
    }
  }
</script>

<div class="login-container">
  <div class="login-card">
    <div class="login-header">
      <h1>Streamletz</h1>
      <p class="motto">Your sound. Your stream. Your rules.</p>
    </div>

    <form onsubmit={handleSubmit}>
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
        <input
          type="password"
          id="password"
          bind:value={password}
          placeholder="Enter your password"
          required
        />
      </div>

      <button type="submit" class="btn btn-primary" disabled={loading}>
        {#if loading}
          <span class="loading"></span>
        {:else}
          Login
        {/if}
      </button>
    </form>

    <div class="login-footer">
      <p>
        Don't have an account?
        <button type="button" class="link-btn" onclick={onSwitchToRegister}>
          Sign up
        </button>
      </p>
    </div>
  </div>
</div>

<style lang="scss">
  @use 'sass:color';
  @use '../styles/variables.scss' as *;

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
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: $gradient-primary;
    }

    &::after {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba($primary-color, 0.05) 0%, transparent 70%);
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

    button[type='submit'] {
      width: 100%;
      margin-top: $spacing-xl;
      padding: $spacing-lg;
      font-size: 1rem;
    }
  }

  .login-footer {
    text-align: center;
    margin-top: $spacing-xl;
    position: relative;
    z-index: 1;

    p {
      color: $text-secondary;
      font-size: 0.95rem;
    }

    .link-btn {
      background: none;
      color: $primary-light;
      padding: 0;
      font-weight: 600;
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