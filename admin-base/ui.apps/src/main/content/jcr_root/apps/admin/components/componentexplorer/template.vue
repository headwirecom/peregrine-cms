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
            <vue-form-generator v-bind:schema="groupSchema" v-bind:model="state">
            </vue-form-generator>
        </div>
        <input type="text" v-model="state.filter" placeholder="Filter components" tabindex="1" autofocus/>
        <ul class="collection" data-collapsible="expandable" ref="groups">
            <li class="collection-item" v-for="(item,i) in filteredList" :key="i" draggable="true" v-on:dragstart="onDragStart(item, $event)">
                <div><b>{{item.title}}</b></div>
                <div>{{item.group}}</div>
                <img v-if="item.thumbnail" v-bind:src="item.thumbnail"></img>
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
                group: "All",
                filter: "",
                componentList: []
            }
        },

        data() {
            return {
                state: $perAdminApp.getNodeFromViewOrNull('/state/componentExplorer'),
                groups: [],
                groupSchema: null
            }
        },
        
        mounted() {
            //Initialize the component list for this page
            if ( this.state.componentList.length === 0) {
                // if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                const componentPath = this.$root.$data.pageView.path.split('/')
                const allowedComponents = ['/apps/' + componentPath[3]] // this.$root.$data.admin.currentPageConfig.allowedComponents
                const list = this.$root.$data.admin.components.data
                this.state.componentList = 
                    list.filter( component => component.path.startsWith(allowedComponents) )
            }

            this.groups = this.state.componentList.reduce( ( groups, current ) => {
                if ( groups.indexOf(current.group) == -1 ) groups.push(current.group);
                return groups;
            }, ['All', 'General'])
            this.groups = this.groups.filter( group => group != '.hidden')

            this.groupSchema = {
                "fields": [{
                    "type": "material-select",
                    "label": "Select",
                    "model": "group",
                    "values": this.groups.map( group => ({ 'name': group, 'value': group }))
                }]
            }

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
                return this.state.componentList.filter( component => {
                    if (!component.group) component.group = 'General';
                    if (component.group === '.hidden') return false;
                    if (this.state.group !== 'All') {
                        if (component.group !== this.state.group ) return false;
                    }
                    if (component.title.toLowerCase().indexOf(this.state.filter.toLowerCase()) == -1) return false;
                    return true;
                })
            },
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