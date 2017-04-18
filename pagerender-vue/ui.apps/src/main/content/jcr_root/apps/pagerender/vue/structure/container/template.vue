<template>
<div>
    <div v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </div>
</div>
</template>

<script>
export default {
    props: [ 'model' ]
}
</script>
