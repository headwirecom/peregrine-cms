<template>
    <div class="peregrine-workspace">


        <component
                v-bind:is    = "getChildByPath('contentview').component"
                v-bind:model = "getChildByPath('contentview')">
        </component>

        <div :class="getRightPanelClasses">

            <admin-components-action v-bind:model="{
                classes: 'toggle-right-panel',
                target: 'rightPanelVisible',
                command: 'showHide'
            }">
                <i class="material-icons">{{state.rightPanelVisible ? 'keyboard_arrow_right' : 'keyboard_arrow_left'}}</i>
            </admin-components-action>

            <component
                    v-if         = "state.editorVisible"
                    v-bind:is    = "getChildByPath('editor').component"
                    v-bind:model = "getChildByPath('editor')">
            </component>

            <component
                    v-else
                    v-bind:is    = "getChildByPath('components').component"
                    v-bind:model = "getChildByPath('components')">
            </component>
        </div>

        <!--
        <component
            v-bind:is    = "getChildByPath('contentview').component"
            v-bind:model = "getChildByPath('contentview')">
        </component>

        <div :class= "getRightPanelClasses">
            {{this.$root.$data.state}}
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
        -->
    </div>
</template>

<script>
    export default {
        props: ['model'],
//        beforeMount(){
//
//            $perAdminApp.getNodeFromView('/state')['rightPanelVisible'] = true // .$set($perAdminApp.getView().state,'rightPanelVisible', true)
//            this.$root.$data.state.rightPanelVisible = true
////            $perAdminView.state.rightPanelVisible = true
////            $perAdminApp.getNodeFromViewWithDefault('/state/editorVisible', true)
////            this.$root.$set(perAdminView.state, 'rightPanelVisible', true)
////            this.$root.$set(perAdminView.state, 'editorVisible', false)
//        },
        computed: {
            state: function() {
                return $perAdminApp.getView().state
            },
            editorVisible: function() {
                return $perAdminApp.getNodeFromView('/state/editorVisible')
            },
            getRightPanelClasses: function() {
                // rightPanelVisible: true/false
                return `right-panel ${$perAdminView.state.rightPanelVisible ? 'visible' : ''}`
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
                if($perAdminApp.getView().state.rightPanelVisible === undefined) {
                    $perAdminApp.getApp().$set($perAdminApp.getView().state,'rightPanelVisible', true)
                    return
                }
                $perAdminView.state.rightPanelVisible = $perAdminView.state.rightPanelVisible ? false: true
            }
  
        }
    }
</script>
