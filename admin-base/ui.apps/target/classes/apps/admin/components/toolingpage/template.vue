<template>
<div style="position: fixed; width: 100%; height: 100%; top: 0; bottom: 0; left: 0; right: 0; overflow: scroll;">
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
