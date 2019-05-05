<script>
    export let model;
</script>

<div data-per-path="{model.path}">
    <div>begin</div>
    {#each model.children as child}
        <svelte:component this={window.$peregrineApp.resolveComponent(child.component)} model={child}/>
    {/each}
    <div>end</div>
</div>
