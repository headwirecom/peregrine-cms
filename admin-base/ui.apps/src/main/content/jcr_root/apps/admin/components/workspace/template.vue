<template>
<div class="" style="width: 100%; height: 90%; display: flex; margin-bottom: 0px">

    <component style="flex: 4; height: 100%;" v-bind:is="getChildByPath('contentview').component"
               v-bind:model="getChildByPath('contentview')">
    </component>

    <component style="flex: 1;" v-bind:is="getChildByPath('editor').component"
               v-bind:model="getChildByPath('editor')">
    </component>

    <component style="flex: 1;" v-bind:is="getChildByPath('components').component"
               v-bind:model="getChildByPath('components')">
    </component>
</div>
</template>

<script>
    export default {
        props: ['model'],
        methods: {

            getChildByPath: function(childName) {
                var path = this.model.path+'/'+childName
                for(var i = 0; i < this.model.children.length; i++) {
                    if(this.model.children[i].path === path) {
                        var ret = this.model.children[i]
                        ret.classes = 'col fullheight s4'
                        return ret
                    }
                }
                return null
            }

        }
    }
</script>
