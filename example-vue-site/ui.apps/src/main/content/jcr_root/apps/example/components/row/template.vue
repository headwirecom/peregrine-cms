<template>
<div class="row">
    <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
    <pagerender-vue-components-placeholder v-bind:model="{ path: model.path, component: model.component }"></pagerender-vue-components-placeholder>
</div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>
