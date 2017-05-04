<template>
<div v-bind:style="elementStyle" class="debugger z-depth-5">
    <a href="#" v-if="!visible" v-on:click.stop.prevent="showDebugger(true)" title="show data" class="toggle-debugger"><i class="material-icons">keyboard_arrow_right</i></a>
    <a href="#" v-if="visible" v-on:click.stop.prevent="showDebugger(false)" title="hide data" class="toggle-debugger-visible"><i class="material-icons">keyboard_arrow_left</i></a>
    <div v-if="visible">
        <div>
            <b>loggers</b>&nbsp;|&nbsp;<span v-for="(logger, key) of getLoggers()"><a v-on:click.stop.prevent="changeLogLevel(logger.name)">{{logger.name}} {{levelToName(logger.level)}}</a>&nbsp;|&nbsp;</span>
        </div>
        <div>
            <b>root objects</b>&nbsp;|&nbsp;<span v-for="(value, key) of this.$root.$data"><a v-on:click.stop.prevent="select(key)">{{key}}</a>&nbsp;|&nbsp;</span>
        </div>
<pre>
{{this.$root.$data[this.selected]}}
</pre>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model']
        ,
        beforeMount() {
          this.selected = 'state'
        },
        computed: {
            jsonview: function() {
                if(this.selected) {
                    return JSON.stringify(this.$root.$data[this.selected], true, 2)
                }
                return JSON.stringify(this.$root.$data, true, 2)
            },
            elementStyle: function() {
                if(this.visible) {
                    return "position: fixed; bottom: 0; left: 0; top: 0; right: 0; height: 100%; width: 100%; overflow: scroll; background-color: #37474f; color: white;"
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
            levelToName: function(level) {
                return ['off', 'error', 'warn', 'info', 'debug', 'fine'][level]
            },
            getLoggers: function() {
                let ret = []
                let loggers = $perAdminApp.getLoggers()
                for(var key in loggers) {
                    ret.push({ name: loggers[key].name, level: loggers[key].level })
                }
                return ret
            },
            showDebugger: function(show) {
                this.visible = show
            },
            select: function(name) {
                this.selected = name
                this.$forceUpdate()
            },
            changeLogLevel: function(name) {
                let logger = $perAdminApp.getLogger(name)
                logger.level = ( logger.level + 1) % 6
                this.$forceUpdate()
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

.debugger {
    z-index: 2;
    color: white;
}

.debugger a {
    color: white;
}
</style>