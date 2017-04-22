<template>
    <a v-bind:href="target" v-on:click.stop.prevent="action" v-bind:class="classes">{{title}}<slot></slot></a>
</template>

<script>
export default {
    props: ['title', 'command', 'target', 'classes' ],
    methods: {
        action: function(e) {
            perHelperAction(this, this.command, this.target)
        }
    }
}
</script>
