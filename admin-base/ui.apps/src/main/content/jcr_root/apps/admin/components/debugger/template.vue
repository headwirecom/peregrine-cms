<template>
<div v-bind:style="elementStyle">
<a href="#" v-if="!visible" v-on:click.stop.prevent="showDebugger(true)" title="show data" class="toggle-debugger"><i class="material-icons">keyboard_arrow_right</i></a>
<a href="#" v-if="visible" v-on:click.stop.prevent="showDebugger(false)" title="hide data" class="toggle-debugger-visible"><i class="material-icons">keyboard_arrow_left</i></a>
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
                    return "position: fixed; bottom: 60px; left: 0;"
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

<style>
.toggle-debugger {
    width: 2rem;
    height: 40px;
    position: absolute;
    border: 1px solid #cfd8dc;
    border-left: 0;
    background-color: #eceff1 !important;
}

.toggle-debugger-visible {
    width: 2rem;
    height: 40px;
    bottom: 6px;
    position: absolute;
    border: 1px solid #cfd8dc;
    border-left: 0;
    background-color: #eceff1 !important;
}

</style>