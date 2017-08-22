<template>
  <component v-bind:is="deref.component" v-bind:model="deref"></component>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            deref() {
                const ref = this.model.ref
                const page = window['$peregrineApp'] ? window['$peregrineApp'].getView().page : $perAdminApp.getView().pageView.page
                return this.findNodeFromPath(page, ref)
            }
        },
        methods: {
            findNodeFromPath(node, path) {
                if(node.path === path) return node;
                if(node.children) {
                    for(let i = 0; i < node.children.length; i++) {
                        const ret = this.findNodeFromPath(node.children[i], path)
                        if(ret) {
                            return ret
                        }
                    }
                }
                return undefined
            }
        }
    }
</script>

