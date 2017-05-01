<template>
    <div class="peregrine-workspace">
        <component 
            v-bind:is    ="getChildByPath('contentview').component"
            v-bind:model ="getChildByPath('contentview')">
        </component>

        <component 
            class        ="z-depth-2" 
            style        ="padding: 0 0.75rem;" 
            v-bind:style ="getEditorStyle()" 
            v-bind:is    ="getChildByPath('editor').component"
            v-bind:model ="getChildByPath('editor')">
        </component>

        <component 
            v-bind:class ="getComponentExplorerClasses()" 
            v-bind:is    ="getChildByPath('components').component"
            v-bind:model ="getChildByPath('components')">
        </component>
    </div>
</template>

<script>
    export default {
        props: ['model'],

        methods: {
            getChildByPath(childName) {
                var path = this.model.path+'/'+childName
                for(var i = 0; i < this.model.children.length; i++) {
                    if(this.model.children[i].path === path) {
                        var ret = this.model.children[i]
                        ret.classes = 'col fullheight s4'
                        return ret
                    }
                }
                return null
            },

            getEditorStyle() {
                if(perAdminView.state.editor && perAdminView.state.editor.dialog) {
                    return 'flex: 1;'
                } else {
                    return 'width: 0px; display: none'
                }
            },

            getStyleForComponent(name) {
                if(perAdminView.state[name] === undefined) { this.$root.$set(perAdminView.state, name, true) }
                return perAdminView.state[name] ? 'flex: 1; height: 100%; padding: 0 0.75rem;' : 'width: 0px;'
            },

            // maybe rename to "toggleStateProp"
            showHide(me, name) {
                console.log('showHide of', name, 'called')
                perAdminView.state[name] = !perAdminView.state[name]
            },
  
            getComponentExplorerClasses() {
                // componentExplorerVisible: true/false
                if(perAdminView.state.componentExplorerVisible === undefined) { 
                    this.$root.$set(perAdminView.state, 'componentExplorerVisible', true) 
                }

                var classes = 'component-explorer blue-grey lighten-5 z-depth-2'
                if(perAdminView.state.componentExplorerVisible){
                    classes = classes + ' visible'
                }
                return classes
            }
        }
    }
</script>
