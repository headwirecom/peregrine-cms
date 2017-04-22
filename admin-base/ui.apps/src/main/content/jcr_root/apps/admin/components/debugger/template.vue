<template>
<div v-bind:style="elementStyle">
<a href="#" v-if="!visible" v-on:click.stop.prevent="showDebugger(true)">show data</a>
<a href="#" v-if="visible" v-on:click.stop.prevent="showDebugger(false)" style="position: fixed; bottom: 20px; left: 0; background-color: black; color: white;">hide data</a>
<pre v-if="visible">
{{jsonview}}
</pre>
</div>
</template>

<script>
    export default {
        props: ['model']
        ,
        computed: {
            jsonview: function() {
                return JSON.stringify(this.$root.$data, true, 2)
            },
            elementStyle: function() {
                if(this.visible) {
                    return "position: fixed; bottom: 0; left: 0; top: 0; right: 0; height: 100%; width: 100%; overflow: scroll; background-color: black; color: white;"
                } else {
                    return "position: fixed; bottom: 20px; left: 0;"
                }
            }

        },
        data: function() {
            if(!this.visible) {
                this.visible = false
            }
            return { visible: this.visible }
        },
        methods: {
            showDebugger: function(show) {
                this.visible = show
            }
        }
    }
</script>
