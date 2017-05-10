<template>
    <div class="tooling-page">
        <template v-for="child in model.children">
            <component v-bind:is="child.component" v-bind:model="child"></component>
        </template>
        <admin-components-notifyuser>I am a Notification</admin-components-notifyuser>
        <admin-components-pathbrowser>I am a Notification</admin-components-pathbrowser>
    </div>
</template>

<script>
export default {
    props: [ 'model' ],
    methods: {
        selectPath: function(me, target) {
            $perAdminApp.loadContent(target+'.html')
        },
        editPage: function(me, target) {
            console.log('edit page')
        }
    }
}
</script>
