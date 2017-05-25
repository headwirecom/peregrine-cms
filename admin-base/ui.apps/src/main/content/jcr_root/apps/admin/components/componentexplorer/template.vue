<template>
    <div class="component-explorer">
        <span class="panel-title">Components</span>
        <div v-if="this.$root.$data.admin.components" class="collection">
           <span 
            v-for          = "cmp in componentList"
            v-on:dragstart = "onDragStart(cmp, $event)" 
            draggable      = "true" 
            class          = "collection-item">
                <i class="material-icons">drag_handle</i> 
                {{cmp.path.split('/')[2]}} {{cmp.name}}
            </span>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            componentList: function () {
                if(!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var allowedComponents = ['/apps/example', '/apps/pagerender'] // this.$root.$data.admin.currentPageConfig.allowedComponents
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
        },
        methods: {
            onDragStart: function(cmp, ev) {
                if(ev) {
                    ev.dataTransfer.setData('text', cmp.path)
                }
            }
        }
    }
</script>
