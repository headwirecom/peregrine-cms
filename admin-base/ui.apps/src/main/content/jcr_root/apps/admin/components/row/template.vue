<template>
<div class="row">
    <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
</div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>
