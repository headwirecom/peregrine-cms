<template>
    <div class="nav-content sub-nav">
        <template v-for="child in model.children">
            <li v-bind:is="child.component" v-bind:model="child"></li>
        </template>
    </div>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        }
    }
}
</script>

