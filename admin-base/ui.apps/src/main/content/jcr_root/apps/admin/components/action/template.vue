<template>
    <span>
        <a 
            v-if                    = "!model.type" 
            v-bind:href             = "model.target" 
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
            <i class="material-icons">{{model.icon ? model.icon : model.title}}<slot></slot></i>
        </a>
    </span>
</template>

<script>
export default {
    props: ['model' ],
    methods: {
        action: function(e) {
            perHelperAction(this, this.model.command, this.model.target)
        }
    }
}
</script>
