<template>
    <div v-bind:data-per-path="model.path" v-bind:class="[editorType]">
        <pagerendervue-components-placeholder
            v-bind:model="{ path: model.path, component: model.component, location: 'before' }"
            v-bind:class="{'from-template': model.fromTemplate}">
        </pagerendervue-components-placeholder>
        <template v-for="child in model.children">
            <component
                v-bind:is="child.component"
                v-bind:model="child"
                v-bind:class="{'from-template': isFromTemplate(child)}">
            </component>
        </template>
        <pagerendervue-components-placeholder
            v-bind:model="{ path: model.path, component: model.component, location: 'after' }"
            v-bind:class="{'from-template': model.fromTemplate}">
        </pagerendervue-components-placeholder>
    </div>
</template>

<script>
    export default {
        props: [ 'model' ],
        computed: {
            editorType() {
                return $peregrineApp.getAdminAppNode('/state/contentview/editor/type');
            }
        },
        methods: {
            isFromTemplate(m) {
                return (!m.children || m.children.length <= 0) && m.fromTemplate;
            }
        }
    }
</script>
<style>
    div.page-editor .from-template {
        background-image: repeating-linear-gradient(135deg, #ffffff 0px, #ffffff 0px, #80808015 10px, #80808015 20px, #ffffff 20px);
    }
</style>
