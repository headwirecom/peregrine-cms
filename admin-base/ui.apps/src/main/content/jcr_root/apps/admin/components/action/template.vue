<template>
    <span>
        <a 
            v-if                    = "!model.type" 
            v-bind:href             = "model.target +'.html'"
            v-on:click.stop.prevent = "action" 
            v-bind:class            = "model.classes">
            {{model.title}}
            <slot></slot>
        </a>
        <a 
            v-if                    = "model.type === 'icon'" 
            v-bind:title            = "model.title" 
            v-bind:href             = "model.target" 
            v-on:click.stop.prevent = "action" 
            class                   = "btn-floating waves-effect waves-light" 
            v-bind:class            = "model.classes">
            <i class="material-icons" v-bind:class="isSelected ? 'actionSelected' : ''">
                {{model.icon ? model.icon : model.title}}
                <slot></slot>
            </i>
        </a>
    </span>
</template>

<script>
export default {
    props: ['model' ],
    computed: {
        isSelected() {
            if(!this.model.stateFrom) return false
            let currentState = $perAdminApp.getNodeFromViewOrNull(this.model.stateFrom)
            if(!currentState) {
                if(this.model.stateFromDefault) {
                    return true
                }
            } else if(currentState === this.model.target) {
                return true
            }
            return false
        }
    },
    methods: {
        action: function(e) {
            $perAdminApp.action(this, this.model.command, this.model.target)
        }
    }
}
</script>

<style>
    .actionSelected {
        background-color: white !important;
        color: #37474f !important;
    }
</style>
