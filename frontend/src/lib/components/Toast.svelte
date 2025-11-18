<script lang="ts">
    import { toasts } from "$lib/toast";
    import { fly, fade } from "svelte/transition";

    function closeToast(index: number) {
        toasts.update((list) => {
            list.splice(index, 1);
            return [...list];
        });
    }
</script>

<div class="toast-container">
    {#each $toasts as toast, i (toast.id)}
        <div
            class="toast {toast.type}"
            in:fly={{ y: 20, duration: 200 }}
            out:fade={{ duration: 400 }}
        >
            <span>{toast.message}</span>
            <button
                class="close-btn"
                aria-label="Bezárás"
                on:click={() => closeToast(i)}
            >
                <span class="close-icon">&times;</span>
            </button>
        </div>
    {/each}
</div>

<style scoped lang="scss">
    @use "$styles/components/Toast";
</style>