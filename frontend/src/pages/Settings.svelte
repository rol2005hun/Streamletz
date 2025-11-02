<script lang="ts">
    import { onMount } from "svelte";
    import { authService } from "../lib/authService";

    let loading = $state(true);
    let saving = $state(false);
    let message = $state("");
    let messageType = $state<"success" | "error" | "">("");

    let settings = $state({
        notifications: {
            emailNotifications: true,
            pushNotifications: false,
            newReleases: true,
            playlistUpdates: true,
        },
        playback: {
            autoplay: true,
            crossfade: false,
            gaplessPlayback: true,
            normalizeVolume: false,
        },
        appearance: {
            theme: "dark",
            compactMode: false,
            showAlbumArt: true,
        },
        privacy: {
            showProfile: true,
            showActivity: true,
            allowExplicit: true,
        },
    });

    onMount(() => {
        authService.getUser();
        loadSettings();
        loading = false;
    });

    function loadSettings() {
        const savedSettings = localStorage.getItem("streamletz_settings");
        if (savedSettings) {
            try {
                settings = JSON.parse(savedSettings);
            } catch (error) {
                console.error("Failed to load settings:", error);
            }
        }
    }

    function saveSettings() {
        saving = true;
        try {
            localStorage.setItem(
                "streamletz_settings",
                JSON.stringify(settings),
            );
            messageType = "success";
            message = "Settings saved successfully!";
        } catch (error) {
            messageType = "error";
            message = "Failed to save settings";
        } finally {
            saving = false;
            setTimeout(() => {
                message = "";
                messageType = "";
            }, 3000);
        }
    }

    function resetSettings() {
        if (
            confirm("Are you sure you want to reset all settings to default?")
        ) {
            settings = {
                notifications: {
                    emailNotifications: true,
                    pushNotifications: false,
                    newReleases: true,
                    playlistUpdates: true,
                },
                playback: {
                    autoplay: true,
                    crossfade: false,
                    gaplessPlayback: true,
                    normalizeVolume: false,
                },
                appearance: {
                    theme: "dark",
                    compactMode: false,
                    showAlbumArt: true,
                },
                privacy: {
                    showProfile: true,
                    showActivity: true,
                    allowExplicit: true,
                },
            };
            saveSettings();
        }
    }

    function goBack() {
        window.history.pushState({}, "", "/dashboard");
        window.dispatchEvent(new PopStateEvent("popstate"));
    }
</script>

<div class="settings-page">
    {#if loading}
        <div class="loading-container">
            <div class="spinner"></div>
            <p>Loading settings...</p>
        </div>
    {:else}
        <button class="back-btn" onclick={goBack} title="Back to Dashboard">
            ← Back
        </button>

        <div class="settings-header">
            <h1>Settings</h1>
            <p class="settings-subtitle">
                Manage your account settings and preferences
            </p>
        </div>

        {#if message}
            <div class="message {messageType}">
                {message}
            </div>
        {/if}

        <div class="settings-content">
            <!-- Notifications Section -->
            <div class="settings-section">
                <div class="section-header">
                    <div class="section-icon">
                        <svg
                            width="24"
                            height="24"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                        >
                            <path
                                d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
                            ></path>
                            <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
                        </svg>
                    </div>
                    <div>
                        <h2>Notifications</h2>
                        <p class="section-description">
                            Manage how you receive notifications
                        </p>
                    </div>
                </div>

                <div class="settings-group">
                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="email-notif">Email Notifications</label>
                            <span class="setting-hint"
                                >Receive updates via email</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="email-notif"
                                bind:checked={
                                    settings.notifications.emailNotifications
                                }
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="push-notif">Push Notifications</label>
                            <span class="setting-hint"
                                >Get notified about important updates</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="push-notif"
                                bind:checked={
                                    settings.notifications.pushNotifications
                                }
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="new-releases">New Releases</label>
                            <span class="setting-hint"
                                >Notify about new music releases</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="new-releases"
                                bind:checked={
                                    settings.notifications.newReleases
                                }
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="playlist-updates"
                                >Playlist Updates</label
                            >
                            <span class="setting-hint"
                                >Get notified when playlists change</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="playlist-updates"
                                bind:checked={
                                    settings.notifications.playlistUpdates
                                }
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="settings-section">
                <div class="section-header">
                    <div class="section-icon">
                        <svg
                            width="24"
                            height="24"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                        >
                            <circle cx="12" cy="12" r="10"></circle>
                            <polygon points="10 8 16 12 10 16 10 8"></polygon>
                        </svg>
                    </div>
                    <div>
                        <h2>Playback</h2>
                        <p class="section-description">
                            Control your music playback experience
                        </p>
                    </div>
                </div>

                <div class="settings-group">
                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="autoplay">Autoplay</label>
                            <span class="setting-hint"
                                >Automatically play similar tracks when your
                                music ends</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="autoplay"
                                bind:checked={settings.playback.autoplay}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="crossfade">Crossfade</label>
                            <span class="setting-hint"
                                >Smooth transitions between tracks</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="crossfade"
                                bind:checked={settings.playback.crossfade}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="gapless">Gapless Playback</label>
                            <span class="setting-hint"
                                >Seamless playback without pauses</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="gapless"
                                bind:checked={settings.playback.gaplessPlayback}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="normalize">Normalize Volume</label>
                            <span class="setting-hint"
                                >Keep consistent volume across tracks</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="normalize"
                                bind:checked={settings.playback.normalizeVolume}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>
                </div>
            </div>

            <!-- Appearance Section -->
            <div class="settings-section">
                <div class="section-header">
                    <div class="section-icon">
                        <svg
                            width="24"
                            height="24"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                        >
                            <path
                                d="M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454z"
                            ></path>
                        </svg>
                    </div>
                    <div>
                        <h2>Appearance</h2>
                        <p class="section-description">
                            Customize how Streamletz looks
                        </p>
                    </div>
                </div>

                <div class="settings-group">
                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="compact">Compact Mode</label>
                            <span class="setting-hint"
                                >Use a more compact interface layout</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="compact"
                                bind:checked={settings.appearance.compactMode}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="album-art">Show Album Art</label>
                            <span class="setting-hint"
                                >Display album artwork throughout the app</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="album-art"
                                bind:checked={settings.appearance.showAlbumArt}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>
                </div>
            </div>

            <!-- Privacy Section -->
            <div class="settings-section">
                <div class="section-header">
                    <div class="section-icon">
                        <svg
                            width="24"
                            height="24"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                        >
                            <path
                                d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"
                            ></path>
                        </svg>
                    </div>
                    <div>
                        <h2>Privacy</h2>
                        <p class="section-description">
                            Control your privacy settings
                        </p>
                    </div>
                </div>

                <div class="settings-group">
                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="show-profile">Public Profile</label>
                            <span class="setting-hint"
                                >Make your profile visible to others</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="show-profile"
                                bind:checked={settings.privacy.showProfile}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="show-activity">Show Activity</label>
                            <span class="setting-hint"
                                >Let others see what you're listening to</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="show-activity"
                                bind:checked={settings.privacy.showActivity}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="setting-item">
                        <div class="setting-info">
                            <label for="explicit">Allow Explicit Content</label>
                            <span class="setting-hint"
                                >Play tracks with explicit content</span
                            >
                        </div>
                        <label class="toggle-switch">
                            <input
                                type="checkbox"
                                id="explicit"
                                bind:checked={settings.privacy.allowExplicit}
                                onchange={() => saveSettings()}
                            />
                            <span class="slider"></span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="settings-actions">
                <button class="btn-secondary" onclick={resetSettings}>
                    Reset to Default
                </button>
                <button
                    class="btn-primary"
                    onclick={saveSettings}
                    disabled={saving}
                >
                    {saving ? "Saving..." : "Save Changes"}
                </button>
            </div>
        </div>
    {/if}
</div>

<style lang="scss">
    @use "../styles/pages/Settings.scss";
</style>
