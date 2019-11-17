<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
<div v-bind:class="`debugger ${elementStyle}`">
    <a href="#" v-if="!visible" v-on:click.stop.prevent="showDebugger(true)" v-bind:title="$i18n('showDebugData')" class="toggle-debugger show-debugger"  v-bind:style="consoleErrors"><i class="material-icons">bug_report</i></a>
    <a href="#" v-if="visible" v-on:click.stop.prevent="showDebugger(false)" v-bind:title="$i18n('hideDebugData')" class="toggle-debugger hide-debugger"><i class="material-icons">highlight_off</i></a>
    <div v-if="visible" class="debugger-content">
        <div class="row">
            <div class="col s12 m4 l3 debugger-levels">
                <h5>Loggers</h5>
                <ul class="collection">
                    <li v-for="(logger, key) of getLoggers()" class="collection-item right-align">
                        <span class="logger-name">{{logger.name}}:</span> 
                        <a  class="logger-level" 
                            v-bind:title="`${$i18n('set')} ${logger.name} ${$i18n('loggingLevel')}: ${levelToName(logger.level)}`"
                            v-on:click.stop.prevent="changeLogLevel(logger.name)">
                            {{levelToName(logger.level)}}
                        </a>
                    </li>
                </ul>
            </div>
            <div class="col s12 m8 l9 debugger-object-view">
                <h5>Root Objects</h5>
                <ul class="list-inline">
                    <li v-for="(value, key) of this.$root.$data">
                        <a v-bind:title="`${$i18n('show')} '${key}' ${$i18n('debugData')}`" v-bind:class="selected === key ? 'active' : ''" v-on:click.stop.prevent="select(key)">{{key}}</a>
                    </li>
                </ul>
                <code><pre>{{this.$root.$data[this.selected]}}</pre></code>
            </div>
        </div>
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
                    return "visible"
                } else {
                    return ""
                }
            },
            consoleErrors: function() {
                return $perAdminApp.getView().admin.consoleErrors === true ? 'background-color: red;' : ''
            }

        },
        data: function() {
            if(!this.visible) {
                this.visible = false
            }
            return { 
                visible: this.visible
            }
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
