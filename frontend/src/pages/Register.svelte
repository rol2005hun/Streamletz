<script lang="ts">
  import { authService, type RegisterData } from "../lib/authService";
  import "../styles/pages/Register.scss";

  let {
    onRegister,
    onSwitchToLogin,
  }: { onRegister: () => void; onSwitchToLogin: () => void } = $props();

  let username = $state("");
  let email = $state("");
  let password = $state("");
  let confirmPassword = $state("");
  let errors = $state<Record<string, string>>({});
  let error = $state("");
  let loading = $state(false);
  let showPassword = $state(false);
  let showConfirmPassword = $state(false);

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
    e.stopPropagation();

    // Prevent any default form behavior
    if (e.target instanceof HTMLFormElement) {
      e.target.setAttribute("action", "javascript:void(0);");
    }

    error = "";

    if (!validateForm()) {
      return false;
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
      console.error("Registration error:", err);
    } finally {
      loading = false;
    }

    return false;
  }

  function togglePasswordVisibility() {
    showPassword = !showPassword;
  }

  function toggleConfirmPasswordVisibility() {
    showConfirmPassword = !showConfirmPassword;
  }
</script>

<div class="register-container">
  <div class="background-decoration">
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>
    <div class="circle circle-3"></div>
  </div>

  <div class="register-card">
    <div class="register-header">
      <div class="logo-container">
        <svg viewBox="0 0 24 24" fill="currentColor" class="logo-icon">
          <path
            d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"
          />
        </svg>
      </div>
      <h1>Create Account</h1>
      <p class="motto">Join Streamletz and start streaming</p>
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
          placeholder="Choose a username"
          required
        />
        {#if errors.username}
          <div class="error">{errors.username}</div>
        {/if}
      </div>

      <div class="form-group">
        <label for="email">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
            />
          </svg>
          Email
        </label>
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
            placeholder="Create a password"
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
        {#if errors.password}
          <div class="error">{errors.password}</div>
        {/if}
      </div>

      <div class="form-group">
        <label for="confirmPassword">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M9 11.24V7.5C9 6.12 10.12 5 11.5 5S14 6.12 14 7.5v3.74c1.21-.81 2-2.18 2-3.74C16 5.01 13.99 3 11.5 3S7 5.01 7 7.5c0 1.56.79 2.93 2 3.74zm9.84 4.63l-4.54-2.26c-.17-.07-.35-.11-.54-.11H13v-6c0-.83-.67-1.5-1.5-1.5S10 6.67 10 7.5v10.74l-3.43-.72c-.08-.01-.15-.03-.24-.03-.31 0-.59.13-.79.33l-.79.8 4.94 4.94c.27.27.65.44 1.06.44h6.79c.75 0 1.33-.55 1.44-1.28l.75-5.27c.01-.07.02-.14.02-.2 0-.62-.38-1.16-.91-1.38z"
            />
          </svg>
          Confirm Password
        </label>
        <div class="password-input-wrapper">
          <input
            type={showConfirmPassword ? "text" : "password"}
            id="confirmPassword"
            bind:value={confirmPassword}
            placeholder="Confirm your password"
            required
          />
          <button
            type="button"
            class="password-toggle"
            onclick={toggleConfirmPasswordVisibility}
            aria-label={showConfirmPassword ? "Hide password" : "Show password"}
          >
            {#if showConfirmPassword}
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
        {#if errors.confirmPassword}
          <div class="error">{errors.confirmPassword}</div>
        {/if}
      </div>

      <button type="submit" class="btn btn-primary" disabled={loading}>
        {#if loading}
          <span class="loading"></span>
          <span>Creating account...</span>
        {:else}
          <span>Create Account</span>
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z" />
          </svg>
        {/if}
      </button>

      <div class="divider">
        <span>or</span>
      </div>

      <p class="login-link">
        Already have an account?
        <button type="button" class="link-btn" onclick={onSwitchToLogin}>
          Sign in
        </button>
      </p>
    </form>
  </div>
</div>