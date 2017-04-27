<template>
    <div style="height: 90%; ">
        <p>components</p>
        <div v-if="this.$root.$data.admin.components" class="collection" style="height: 100%; overflow: auto;">
           <a draggable="true" v-on:dragstart="onDragStart(cmp, $event)" class="collection-item" v-for="cmp in componentList()">{{cmp.path.split('/')[2]}} {{cmp.name}}</a>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
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