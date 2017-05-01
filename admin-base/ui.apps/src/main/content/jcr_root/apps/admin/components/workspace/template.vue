<template>
    <div class="peregrine-workspace">
        <component 
            v-bind:is    = "getChildByPath('contentview').component"
            v-bind:model = "getChildByPath('contentview')">
        </component>

        <div :class= "getRightPanelClasses()">

            <admin-components-action v-bind:model="{ 
                classes: 'toggle-right-panel',
                target: 'rightPanelVisible', 
                command: 'showHide' 
            }">
                <i class="material-icons">{{isVisible ? 'keyboard_arrow_right' : 'keyboard_arrow_left'}}</i>
            </admin-components-action>

            <component 
                v-if         = "editorVisible"
                v-bind:is    = "getChildByPath('editor').component"
                v-bind:model = "getChildByPath('editor')">
            </component>

            <component 
                v-else
                v-bind:is    = "getChildByPath('components').component"
                v-bind:model = "getChildByPath('components')">
            </component>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        beforeMount(){
            this.$root.$set(perAdminView.state, 'rightPanelVisible', true)
            this.$root.$set(perAdminView.state, 'editorVisible', false)
        },
        computed: {
            editorVisible(){
                return perAdminView.state.editorVisible
            }
        },
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

            // maybe rename to "toggleStateProp"
            showHide(me, name) {
                console.log('showHide of ', name, ' called')
                perAdminView.state[name] = !perAdminView.state[name]
            },
  
            getRightPanelClasses() {
                // rightPanelVisible: true/false
                return `right-panel ${perAdminView.state.rightPanelVisible ? 'visible' : ''}`
            }
        }
    }
</script>
