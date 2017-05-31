<template>
    <div :class="`peregrine-workspace ${state.rightPanelVisible ? 'right-panel-visible' : ''}`">
        <component
                v-bind:is    = "getChildByPath('contentview').component"
                v-bind:model = "getChildByPath('contentview')">
        </component>

        <admin-components-action v-if="!state.rightPanelVisible" v-bind:model="{
            classes: 'show-right-panel',
            target: 'rightPanelVisible',
            command: 'showHide'
            }"><i class="material-icons">keyboard_arrow_left</i>
        </admin-components-action>

        <div class="right-panel">
            <admin-components-action v-if="!state.editorVisible" v-bind:model="{
                classes: 'hide-right-panel',
                target: 'rightPanelVisible',
                command: 'showHide'
            }">
                <i class="material-icons">highlight_off</i>
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
    </div>
</template>

<script>
    export default {
        props: ['model'],
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
            },

            showComponentEdit(me, target) {
//                if(!me.state.editorVisible) {
                $perAdminApp.stateAction('editComponent', target)

//                }
            },

        }
    }
</script>
