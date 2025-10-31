<script lang="ts">
  import { authService, type RegisterData } from '../lib/authService';

  export let onRegister: () => void;
  export let onSwitchToLogin: () => void;

  let username = '';
  let email = '';
  let password = '';
  let confirmPassword = '';
  let errors: Record<string, string> = {};
  let error = '';
  let loading = false;

  function validateForm(): boolean {
    errors = {};

    if (username.length < 3) {
      errors.username = 'Username must be at least 3 characters';
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      errors.email = 'Please enter a valid email address';
    }

    if (password.length < 6) {
      errors.password = 'Password must be at least 6 characters';
    }

    if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = '';

    if (!validateForm()) {
      return;
    }

    loading = true;

    try {
      const registerData: RegisterData = { username, email, password };
      const response = await authService.register(registerData);
      
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify({
        username: response.username,
        email: response.email
      }));

      onRegister();
    } catch (err: any) {
      error = err.response?.data?.message || 'Registration failed. Please try again.';
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

    <form on:submit={handleSubmit}>
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
        <button type="button" class="link-btn" on:click={onSwitchToLogin}>
          Login
        </button>
      </p>
    </div>
  </div>
</div>

<style lang="scss">
  .register-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, $background-dark 0%, darken($background-dark, 5%) 100%);
    padding: $spacing-lg;
  }

  .register-card {
    background: $background-light;
    border-radius: $border-radius-xl;
    padding: $spacing-xxl;
    width: 100%;
    max-width: 450px;
    box-shadow: $shadow-lg;
  }

  .register-header {
    text-align: center;
    margin-bottom: $spacing-xxl;

    h1 {
      color: $primary-color;
      font-size: 2.5rem;
      margin-bottom: $spacing-sm;
    }

    .motto {
      color: $text-secondary;
      font-size: 1rem;
      font-style: italic;
    }
  }

  form {
    button[type="submit"] {
      width: 100%;
      margin-top: $spacing-lg;
    }
  }

  .register-footer {
    text-align: center;
    margin-top: $spacing-xl;

    p {
      color: $text-secondary;
    }

    .link-btn {
      background: none;
      color: $primary-color;
      padding: 0;
      font-weight: 600;
      text-decoration: underline;

      &:hover {
        color: $accent-color;
      }
    }
  }

  @media (max-width: $breakpoint-mobile) {
    .register-card {
      padding: $spacing-xl;
    }

    .register-header h1 {
      font-size: 2rem;
    }
  }
</style>
