<template>
    <a v-bind:href="model.target" v-on:click.stop.prevent="action" v-bind:class="model.classes">{{model.title}}<slot></slot></a>
</template>

<script>
export default {
    props: ['model' ],
    methods: {
        action: function(e) {
            console.log(this.model)
            perHelperAction(this, this.model.command, this.model.target)
        }
    }
}
</script>
