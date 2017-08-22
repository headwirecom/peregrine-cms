<template>
    <div v-bind:data-per-path="model.path">
        <template v-for="child in layout.children">
            <component v-bind:is="child.component" v-bind:model="child"></component>
        </template>
    </div>
</template>

<script>
    export default {
        props: [ 'model' ],
        computed: {
            layout() {
                for(let i = 0; i < this.model.children.length; i++) {
                    if(this.model.children[i].name === 'layout') {
                        return this.model.children[i]
                    }
                }
            }
        }
    }
</script>
