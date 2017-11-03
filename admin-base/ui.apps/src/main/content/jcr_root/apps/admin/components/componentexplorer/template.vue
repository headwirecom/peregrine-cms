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
        {{filteredList}}
        <span class="panel-title">Components</span>
            <input type="text" v-model="state.filter" placeholder="Filter components" tabindex="1" autofocus/>
            <select class="browser-default" v-model="state.group">
                <option value="">All Groups</option>
                <option v-for="(group, key) in allGroups" v-bind:value="key">{{key}}</option>
            </select>
            <ul>
                <li 
                    v-for="(group, key) in groups">
                    <div>
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
//        beforeCreate() {
//            let perState = $perAdminApp.getNodeFromViewOrNull('/state');
//            perState.componentExplorer = perState.componentExplorer || {
//                accordion: {},
//                filter: ""
//            }
//        },
//
//        data() {
//            return {
//                state: $perAdminApp.getNodeFromViewOrNull('/state/componentExplorer'),
//            }
//        },
        
//        mounted() {
//            $(this.$refs.groups).collapsible({
//                accordion: false,
//                onOpen: (el) => { Vue.set(this.state.accordion, el[0].dataset.groupIndex, true) },
//                onClose: (el) => { Vue.set(this.state.accordion, el[0].dataset.groupIndex, false) }
//            })
//        },

//        beforeDestroy() {
//            $(this.$refs.groups).collapsible('destroy')
//        },

        computed: {
            state() {
                const state = $perAdminApp.getNodeFromView('/state/componentExplorer')
                if(state) {
                    return state
                }
                Vue.set($perAdminApp.getView().state, 'componentExplorer', {filter: ''})
                return $perAdminApp.getNodeFromView('/state/componentExplorer')
            },
            filteredList: function() {
                if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/' + componentPath[3]] // this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if (!list || !allowedComponents) return {}

                console.log(allowedComponents)
                // Filter list to local components and with local filter
                return list.filter(component => {
                    console.log(component.title, component.path)
                    if (component.group === '.hidden') return false;
                    if((this.state.group && this.state.group !== '') && component.group !== this.state.group) return false;
                    if (component.title.toLowerCase().indexOf(this.state.filter.toLowerCase()) == -1) return false;
                    return component.path.startsWith(allowedComponents);

                })
            },
            groupList: function() {
                if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/' + componentPath[3]] // this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if (!list || !allowedComponents) return {}

                // Filter list to local components
                const ret = list.filter(component => {
                    if (component.group === '.hidden') return false;
//                    if (component.title.toLowerCase().indexOf(this.state.filter.toLowerCase()) == -1) return false;
                    return component.path.startsWith(allowedComponents);

                })
                return ret
            },
            groups: function () {
                return this.filteredList.reduce( ( obj, current ) => {
                    if ( !current.group ) current.group = 'General';
                    if ( !obj[ current.group ]) Vue.set(obj, current.group, []);
                    obj[ current.group ].push( current ); 
                    return obj;
                }, {})
            },
            allGroups: function () {
                const ret = this.groupList.reduce( ( obj, current ) => {
                    if ( !current.group ) current.group = 'General';
                    if ( !obj[ current.group ]) Vue.set(obj, current.group, []);
                    obj[ current.group ].push( current );
                    return obj;
                }, {})

                // make sure the currently selected group is an actual group
                if(!ret[this.state.group]) { this.state.group = ''}
                return ret
            }
        },
        methods: {
            isActive( key, groupChildren) {
                return (
                    this.state.accordion[ key ]
                )
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