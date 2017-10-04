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
    <div class="component-explorer">
        <span class="panel-title">Components</span>
            <ul class="collapsible" data-collapsible="expandable" ref="groups">
                <li v-for="(group, key) in componentList" >
                    <div class="collapsible-header">
                        <span>{{key}}</span>
                        <i class="material-icons">arrow_drop_down</i>
                    </div>
                    <div class="collapsible-body">
                        <ul class="collection">
                            <li 
                                class="collection-item"
                                v-for="component in group"
                                v-on:dragstart="onDragStart(component, $event)" 
                                draggable="true">
                                <div>
                                    <i class="material-icons">drag_handle</i>
                                    <span>{{displayName(component)}}</span>
                                </div>
                                <img v-if="component.thumbnail" v-bind:src="component.thumbnail">
                            </li>
                        </ul>
                    </div>
                </li>
            </ul>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        mounted() {
            $(this.$refs.groups).collapsible({ accordion: false })
        },
        beforeDestroy() {
            $(this.$refs.groups).collapsible('destroy')
        },
        computed: {
            componentList: function () {
                if(!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/'+componentPath[3]] // this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if(!list || !allowedComponents) return {}

                var ret = {}
                for(var i = 0; i < list.length; i++) {
                    var path = list[i].path
                    if(list[i].group === '.hidden') continue;
                    for(var j = 0; j < allowedComponents.length; j++) {
                        if(path.startsWith(allowedComponents[j])) {
                            let groupName = list[i].group
                            if(!groupName) { groupName = 'General' }
                            if(!ret[groupName]) {
                                Vue.set(ret, groupName, [])
                            }
                            ret[groupName].push(list[i])
                            break;
                        }
                    }
                }
                return ret
            }
        },
        methods: {
            displayName(component) {
                if(component.title) {
                    return component.title
                } else {
                    return component.path.split('/')[2] + ' ' + component.name
                }
            },
            onDragStart: function(component, ev) {
                if(ev) {
                    ev.dataTransfer.setData('text', component.path)
                }
            }
        }
    }
</script>