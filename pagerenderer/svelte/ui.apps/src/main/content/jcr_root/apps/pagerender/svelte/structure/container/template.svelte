<script>
    export let model;
</script>

{#each model.children as child}
    <svelte:component this={window.$peregrineApp.resolveComponent(child.component)} model={child}/>
{/each}
