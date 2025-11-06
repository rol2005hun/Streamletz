<script lang="ts">
    import AudioPlayer from "$lib/components/AudioPlayer.svelte";
    import { currentTrack, allTracks, isPlaying, isPaused } from "$lib/stores";
    import { onMount } from "svelte";
    import { trackService } from "$lib/trackService";
    import "$styles/global.scss";
    export let data;

    onMount(async () => {
        try {
            if (!data?.isAuthenticated) return;
            const lb = data?.lastPlayback;
            if (lb && lb.trackId && !$currentTrack) {
                const t = await trackService.getTrackById(lb.trackId);
                if (t) {
                    allTracks.set([t]);
                    currentTrack.set(t);
                    if (typeof lb.wasPlaying === "boolean") {
                        isPlaying.set(!!lb.wasPlaying);
                        isPaused.set(!lb.wasPlaying);
                    }
                }
            }
        } catch (err) {
            console.error("Failed to restore last playback:", err);
        }
    });
</script>

<slot />

{#if data?.isAuthenticated}
    <AudioPlayer
        allTracks={$allTracks}
        initialPosition={data.lastPlayback?.position}
    />
{/if}
