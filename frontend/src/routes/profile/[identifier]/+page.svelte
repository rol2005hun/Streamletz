<script lang="ts">
    import ProfileView from "$lib/components/ProfileView.svelte";
    export let data;
</script>

<ProfileView profileData={data.userProfile} likedTracks={data.likedTracks} playlists={data.playlists} isOwnProfile={data.isOwnProfile} />
