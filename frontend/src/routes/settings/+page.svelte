<script lang="ts">
    import { goto } from "$app/navigation";
    import { authService } from "$lib/authService";
    import {
        userService,
        type UserProfile,
        type UpdatePasswordRequest,
    } from "$lib/userService";

    const { data } = $props();
    let message = $state("");
    let messageType = $state("");
    let profileLoading = $state(false);
    let passwordLoading = $state(false);
    let loading = false;

    let userProfile = $state<UserProfile | null>(data.userProfile);

    function getProfileForm() {
        return {
            username: userProfile?.username || "",
            email: userProfile?.email || "",
            profileImage: userProfile?.profileImage || "",
        };
    }
    let profileForm = getProfileForm();

    $effect(() => {
        if (userProfile) {
            profileForm.username = userProfile.username || "";
            profileForm.email = userProfile.email || "";
            profileForm.profileImage = userProfile.profileImage || "";
        }
    });

    let passwordForm = $state<UpdatePasswordRequest>({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
    });

    let settings = {
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
    async function updateProfile() {
        profileLoading = true;
        try {
            const updatedProfile = await userService.updateProfile({
                username:
                    profileForm.username !== userProfile?.username
                        ? profileForm.username
                        : undefined,
                email:
                    profileForm.email !== userProfile?.email
                        ? profileForm.email
                        : undefined,
                profileImage:
                    profileForm.profileImage !== userProfile?.profileImage
                        ? profileForm.profileImage
                        : undefined,
            });
            userProfile = updatedProfile;
            if (updatedProfile.newToken) {
                authService.setAuth(updatedProfile.newToken, {
                    username: updatedProfile.username,
                    email: updatedProfile.email,
                    profileImage: updatedProfile.profileImage,
                });
            } else {
                const token = authService.getToken();
                if (token) {
                    authService.setAuth(token, {
                        username: updatedProfile.username,
                        email: updatedProfile.email,
                        profileImage: updatedProfile.profileImage,
                    });
                }
            }
            messageType = "success";
            message = "Profile updated successfully!";
        } catch (error) {
            messageType = "error";
            message = "Failed to update profile.";
        } finally {
            profileLoading = false;
            setTimeout(() => {
                message = "";
                messageType = "";
            }, 3000);
        }
    }

    async function changePassword() {
        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            messageType = "error";
            message = "New passwords do not match";
            setTimeout(() => {
                message = "";
                messageType = "";
            }, 3000);
            return;
        }

        passwordLoading = true;
        try {
            await userService.changePassword(passwordForm);
            messageType = "success";
            message = "Password changed successfully!";
            passwordForm = {
                currentPassword: "",
                newPassword: "",
                confirmPassword: "",
            };
        } catch (error: any) {
            messageType = "error";
            message =
                error.response?.data?.message || "Failed to change password";
        } finally {
            passwordLoading = false;
            setTimeout(() => {
                message = "";
                messageType = "";
            }, 3000);
        }
    }

    function saveSettings() {
        try {
            document.cookie = `streamletz_settings=${encodeURIComponent(JSON.stringify(settings))}; path=/; max-age=31536000`;
            messageType = "success";
            message = "Settings saved successfully!";
        } catch {
            messageType = "error";
            message = "Failed to save settings";
        } finally {
            setTimeout(() => {
                message = "";
                messageType = "";
            }, 3000);
        }
    }

    function goBack() {
        goto("/dashboard");
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
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"
                            ></path>
                            <circle cx="12" cy="7" r="4"></circle>
                        </svg>
                    </div>
                    <div>
                        <h2>Account Settings</h2>
                        <p class="section-description">
                            Manage your profile and account information
                        </p>
                    </div>
                </div>

                <div class="settings-group">
                    <div class="user-id-display">
                        <div class="id-label">
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="2"
                            >
                                <path
                                    d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"
                                ></path>
                                <path
                                    d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"
                                ></path>
                            </svg>
                            <span>User ID</span>
                        </div>
                        <div class="id-value">
                            <code>#{userProfile?.id || "..."}</code>
                            <button
                                type="button"
                                class="copy-id-btn"
                                onclick={() => {
                                    if (userProfile?.id) {
                                        navigator.clipboard.writeText(
                                            userProfile.id.toString(),
                                        );
                                        messageType = "success";
                                        message =
                                            "User ID copied to clipboard!";
                                        setTimeout(() => {
                                            message = "";
                                            messageType = "";
                                        }, 2000);
                                    }
                                }}
                                title="Copy ID"
                            >
                                <svg
                                    width="16"
                                    height="16"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    stroke-width="2"
                                >
                                    <rect
                                        x="9"
                                        y="9"
                                        width="13"
                                        height="13"
                                        rx="2"
                                        ry="2"
                                    ></rect>
                                    <path
                                        d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
                                    ></path>
                                </svg>
                            </button>
                        </div>
                        <p class="id-hint">
                            Your unique identifier - share this to let others
                            view your profile
                        </p>
                    </div>

                    <form
                        onsubmit={(e) => {
                            e.preventDefault();
                            updateProfile();
                        }}
                    >
                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="username">Username</label>
                                <span class="setting-hint"
                                    >Your public display name</span
                                >
                            </div>
                            <input
                                type="text"
                                id="username"
                                bind:value={profileForm.username}
                                placeholder="Enter username"
                                class="form-input"
                                autocomplete="username"
                            />
                        </div>

                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="email">Email</label>
                                <span class="setting-hint"
                                    >Your email address</span
                                >
                            </div>
                            <input
                                type="email"
                                id="email"
                                bind:value={profileForm.email}
                                placeholder="Enter email"
                                class="form-input"
                                autocomplete="email"
                            />
                        </div>

                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="profile-image"
                                    >Profile Image URL</label
                                >
                                <span class="setting-hint"
                                    >URL to your profile picture</span
                                >
                            </div>
                            <input
                                type="text"
                                id="profile-image"
                                bind:value={profileForm.profileImage}
                                placeholder="https://example.com/image.jpg"
                                class="form-input"
                                autocomplete="photo"
                            />
                        </div>

                        <button
                            type="submit"
                            class="save-profile-btn"
                            disabled={profileLoading}
                        >
                            {#if profileLoading}
                                <span class="btn-spinner"></span>
                                Saving...
                            {:else}
                                Save Profile
                            {/if}
                        </button>
                    </form>
                </div>

                <div class="section-divider"></div>

                <div class="settings-group">
                    <h3 class="subsection-title">Change Password</h3>

                    <form
                        onsubmit={(e) => {
                            e.preventDefault();
                            changePassword();
                        }}
                    >
                        <input
                            type="text"
                            name="username"
                            value={userProfile?.username || ""}
                            autocomplete="username"
                            style="display: none;"
                            readonly
                        />

                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="current-password"
                                    >Current Password</label
                                >
                            </div>
                            <input
                                type="password"
                                id="current-password"
                                bind:value={passwordForm.currentPassword}
                                placeholder="Enter current password"
                                class="form-input"
                                autocomplete="current-password"
                            />
                        </div>

                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="new-password">New Password</label>
                            </div>
                            <input
                                type="password"
                                id="new-password"
                                bind:value={passwordForm.newPassword}
                                placeholder="Enter new password"
                                class="form-input"
                                autocomplete="new-password"
                            />
                        </div>

                        <div class="setting-item form-item">
                            <div class="setting-info">
                                <label for="confirm-password"
                                    >Confirm New Password</label
                                >
                            </div>
                            <input
                                type="password"
                                id="confirm-password"
                                bind:value={passwordForm.confirmPassword}
                                placeholder="Confirm new password"
                                class="form-input"
                                autocomplete="new-password"
                            />
                        </div>

                        <button
                            type="submit"
                            class="save-profile-btn"
                            disabled={passwordLoading}
                        >
                            {#if passwordLoading}
                                <span class="btn-spinner"></span>
                                Changing...
                            {:else}
                                Change Password
                            {/if}
                        </button>
                    </form>
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
        </div>
    {/if}
</div>

<style scoped lang="scss">
    @use "$styles/pages/Settings.scss";
</style>
