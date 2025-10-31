<script lang="ts">
  import { authService, type RegisterData } from '../lib/authService';

  let { onRegister, onSwitchToLogin }: { onRegister: () => void; onSwitchToLogin: () => void } = $props();

  let username = $state('');
  let email = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let errors = $state<Record<string, string>>({});
  let error = $state('');
  let loading = $state(false);

  function validateForm(): boolean {
    errors = {};

    if (username.length < 3) {
      errors.username = "Username must be at least 3 characters";
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      errors.email = "Please enter a valid email address";
    }

    if (password.length < 6) {
      errors.password = "Password must be at least 6 characters";
    }

    if (password !== confirmPassword) {
      errors.confirmPassword = "Passwords do not match";
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = "";

    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      const registerData: RegisterData = { username, email, password };
      const response = await authService.register(registerData);

      localStorage.setItem("token", response.token);
      localStorage.setItem(
        "user",
        JSON.stringify({
          username: response.username,
          email: response.email,
        }),
      );

      onRegister();
    } catch (err: any) {
      error =
        err.response?.data?.message || "Registration failed. Please try again.";
    } finally {
      loading = false;
    }
  }
</script>

<div class="register-container">
  <div class="register-card">
    <div class="register-header">
      <h1>Join Streamletz</h1>
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
          placeholder="Choose a username"
          required
        />
        {#if errors.username}
          <div class="error">{errors.username}</div>
        {/if}
      </div>

      <div class="form-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          bind:value={email}
          placeholder="Enter your email"
          required
        />
        {#if errors.email}
          <div class="error">{errors.email}</div>
        {/if}
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input
          type="password"
          id="password"
          bind:value={password}
          placeholder="Create a password"
          required
        />
        {#if errors.password}
          <div class="error">{errors.password}</div>
        {/if}
      </div>

      <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input
          type="password"
          id="confirmPassword"
          bind:value={confirmPassword}
          placeholder="Confirm your password"
          required
        />
        {#if errors.confirmPassword}
          <div class="error">{errors.confirmPassword}</div>
        {/if}
      </div>

      <button type="submit" class="btn btn-primary" disabled={loading}>
        {#if loading}
          <span class="loading"></span>
        {:else}
          Sign Up
        {/if}
      </button>
    </form>

    <div class="register-footer">
      <p>
        Already have an account?
        <button type="button" class="link-btn" onclick={onSwitchToLogin}>
          Login
        </button>
      </p>
    </div>
  </div>
</div>

<style lang="scss">
  @use 'sass:color';
  @use '../styles/variables.scss' as *;

  .register-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xl;
    position: relative;
    z-index: 1;
  }

  .register-card {
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
      background: $gradient-secondary;
    }

    &::after {
      content: '';
      position: absolute;
      bottom: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba($accent-color, 0.05) 0%, transparent 70%);
      pointer-events: none;
    }
  }

  .register-header {
    text-align: center;
    margin-bottom: $spacing-xxxl;
    position: relative;
    z-index: 1;

    h1 {
      font-size: 2.75rem;
      margin-bottom: $spacing-md;
      background: $gradient-secondary;
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

  .register-footer {
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
    .register-card {
      padding: $spacing-xxl;
    }

    .register-header h1 {
      font-size: 2.25rem;
    }
  }
</style>