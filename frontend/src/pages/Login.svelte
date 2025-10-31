<script lang="ts">
  import { authService, type LoginData } from '../lib/authService';

  export let onLogin: () => void;
  export let onSwitchToRegister: () => void;

  let username = '';
  let password = '';
  let error = '';
  let loading = false;

  async function handleSubmit(e: Event) {
    e.preventDefault();
    error = '';
    loading = true;

    try {
      const loginData: LoginData = { username, password };
      const response = await authService.login(loginData);
      
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify({
        username: response.username,
        email: response.email
      }));

      onLogin();
    } catch (err: any) {
      error = err.response?.data?.message || 'Login failed. Please try again.';
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
        <button type="button" class="link-btn" on:click={onSwitchToRegister}>
          Sign up
        </button>
      </p>
    </div>
  </div>
</div>

<style lang="scss">
  .login-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, $background-dark 0%, darken($background-dark, 5%) 100%);
    padding: $spacing-lg;
  }

  .login-card {
    background: $background-light;
    border-radius: $border-radius-xl;
    padding: $spacing-xxl;
    width: 100%;
    max-width: 450px;
    box-shadow: $shadow-lg;
  }

  .login-header {
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

  .login-footer {
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
    .login-card {
      padding: $spacing-xl;
    }

    .login-header h1 {
      font-size: 2rem;
    }
  }
</style>
