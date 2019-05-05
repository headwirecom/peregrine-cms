<script>
    export let model;

    export function refresh(newModel) {
        if(newModel) {
            model = newModel;
        } else {
            model = model;
        }
    }
</script>

{#each model.children as child}
    <svelte:component this={window.$peregrineApp.resolveComponent(child.component)} model={child}/>
{/each}
