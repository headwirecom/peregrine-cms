<template>
    <div class="component-explorer">
        <span class="panel-title">Components</span>
        <div v-if="this.$root.$data.admin.components" class="collection">
           <a draggable="true" v-on:dragstart="onDragStart(cmp, $event)" class="collection-item" v-for="cmp in componentList()"><i class="material-icons">drag_handle</i> {{cmp.path.split('/')[2]}} {{cmp.name}}</a>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            isVisible: function() {
                return this.$root.$data.state.componentExplorerVisible
            }
            // isPinned: function() {
            //     return this.$root.$data.state.componentExplorerPinned
            // }
        },
        methods: {
            onDragStart: function(cmp, ev) {
                if(ev) {
                    ev.dataTransfer.setData('component', cmp.path)
                }
            },
            componentList: function() {
                if(!this.$root.$data.admin.components) return {}
                if(!this.$root.$data.admin.currentPageConfig) return {}
                var allowedComponents = this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if(!list || !allowedComponents) return {}

                var ret = []
                for(var i = 0; i < list.length; i++) {
                    var path = list[i].path
                    for(var j = 0; j < allowedComponents.length; j++) {
                        if(path.startsWith(allowedComponents[j])) {
                            ret.push(list[i])
                            break;
                        }
                    }
                }
                return ret
            }
        }
    }
</script>
