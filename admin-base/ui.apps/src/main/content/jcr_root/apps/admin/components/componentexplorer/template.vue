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
        <div v-if="this.$root.$data.admin.components" class="collection">
           <span
                   class          = "collection-title"
                   v-for          = "(group, key) in componentList">
               {{key}}
               <span
                   v-for          = "cmp in group"
                v-on:dragstart = "onDragStart(cmp, $event)"
                draggable      = "true"
                class          = "collection-item">
                    <i class="material-icons">drag_handle</i>
                    {{displayName(cmp)}}
                    <img v-if="cmp.thumbnail" v-bind:src="cmp.thumbnail" style="max-width: 260px;">
                </span>
            </span>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
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
            displayName(cmp) {
                if(cmp.title) {
                    return cmp.title
                } else {
                    return cmp.path.split('/')[2] + ' ' + cmp.name
                }
            },
            onDragStart: function(cmp, ev) {
                if(ev) {
                    ev.dataTransfer.setData('text', cmp.path)
                }
            }
        }
    }
</script>
