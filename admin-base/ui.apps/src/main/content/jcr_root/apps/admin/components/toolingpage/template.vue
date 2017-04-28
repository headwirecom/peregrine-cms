<template>
    <div class="tooling-page">
        <template v-for="child in model.children">
            <component v-bind:is="child.component" v-bind:model="child"></component>
        </template>
    </div>
</template>

<script>
export default {
    props: [ 'model' ],
    methods: {
        selectPath: function(me, target) {
            loadContent(target+'.html')
        },
        editPage: function(me, target) {
            console.log('edit page')
        }
    }
}
</script>
