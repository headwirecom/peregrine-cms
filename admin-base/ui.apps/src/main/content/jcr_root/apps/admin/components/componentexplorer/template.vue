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
            <div class="input-field">
                <select 
                    v-model="state.group" 
                    ref="select">
                    <option value="all" selected>All Components</option>
                    <option v-for="group in groups" v-bind:value="group">{{group}}</option>
                </select>
            </div>
            <input type="text" v-model="state.filter" placeholder="Filter components" tabindex="1" autofocus/>
            <ul class="collection" data-collapsible="expandable" ref="groups">
                <li class="collection-item" v-for="item in filteredList">
                    <div>{{item.title}}</div><div>{{item.group}}</div>
                </li>
            </ul>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        beforeCreate() {
            let perState = $perAdminApp.getNodeFromViewOrNull('/state'); 
            perState.componentExplorer = perState.componentExplorer || {
                group: "all",
                filter: "" 
            }
        },

        data() {
            return {
                state: $perAdminApp.getNodeFromViewOrNull('/state/componentExplorer'),
            }
        },
        
        mounted() {
            $(this.$refs.select).material_select();
            $(this.$refs.select).change((e) => {
                this.updateGroup(e.target.value);
            });
        },
        beforeDestroy() {
            $(this.$refs.select).material_select('destroy');
        },

        computed: {
            filteredList: function() {
                if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/' + componentPath[3]] // this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if (!list || !allowedComponents) return {}

                // Filter list to local components and with local filter
                return list.filter(component => {
                    if (!component.group) component.group = 'General';
                    if (component.group === '.hidden') return false;
                    if (this.state.group !== 'all') {
                        console.log(component.group, this.state.group)
                        if (component.group !== this.state.group ) return false;
                    }
                    if (component.title.toLowerCase().indexOf(this.state.filter.toLowerCase()) == -1) return false;
                    return component.path.startsWith(allowedComponents);

                })
            },
            groups: function () {
                return this.filteredList.reduce( ( groups, current ) => {
                    if ( groups.indexOf(current.group) == -1 ) groups.push(current.group);
                    return groups;
                }, ['General'])
            }
        },

        methods: {
            updateGroup(value) {
                this.state.group = value;
            },
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