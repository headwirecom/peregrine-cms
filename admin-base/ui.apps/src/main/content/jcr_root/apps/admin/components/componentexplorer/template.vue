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
        <span class="panel-title">{{ $i18n('Components') }}</span>
            <input type="text" v-model="state.filter" v-bind:placeholder="$i18n('filterComponents')" tabindex="1" autofocus/>
            <select class="browser-default" v-model="state.group">
                <option value="">{{ $i18n('allGroups') }}</option>
                <option v-for="(group, key) in allGroups" v-bind:value="key" v-bind:key="key">{{ key }}</option>
            </select>
            <ul>
                <li 
                    v-for="(group, key) in groups" v-bind:key="key">
                    <div>
                        <ul class="collection">
                            <li
                                class="collection-item"
                                v-for="component in group"
                                v-bind:key="componentKey(component)"
                                v-on:dragstart="onDragStart(component, $event)"
                                v-on:dragend="onDragEnd(component, $event)"
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
    import { IgnoreContainers } from '../../../../../../js/constants.js';

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
                Vue.set($perAdminApp.getView().state, 'componentExplorer', {filter: '', group: ''})
                return $perAdminApp.getNodeFromView('/state/componentExplorer')
            },
            filteredList: function() {
                var currentGroup = this.state.group
                if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/' + componentPath[2]+ '/'] // this.$root.$data.admin.currentPageConfig.allowedComponents
                var list = this.$root.$data.admin.components.data
                if (!list || !allowedComponents) return {}

                var sorted = list.sort(function( left, right) {
                    const leftName = (left.group + '-' + left.title).toLowerCase();
                    const rightName = (right.group + '-' + right.title).toLowerCase();
                    if(leftName < rightName) return -1;
                    if(leftName > rightName) return 1;
                    return 0;
                })

                // Filter list to local components and with local filter
                return sorted.filter(component => {
                    if (component.group === '.hidden') return false;
                    if((currentGroup && currentGroup !== '') && component.group !== currentGroup) return false;
                    if (component.title.toLowerCase().indexOf(this.state.filter.toLowerCase()) == -1) return false;
                    return component.path.startsWith(allowedComponents);

                })
            },
            groupList: function() {
                if (!this.$root.$data.admin.components) return {}
                // if(!this.$root.$data.admin.currentPageConfig) return {}
                var componentPath = this.$root.$data.pageView.path.split('/')
                var allowedComponents = ['/apps/' + componentPath[2]] // this.$root.$data.admin.currentPageConfig.allowedComponents
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
            },
            isIgnoreContainersEnabled() {
                let view = $perAdminApp.getView();
                return view.state.tools
                    && view.state.tools.workspace
                    && view.state.tools.workspace.ignoreContainers === IgnoreContainers.ENABLED;
            }
        },
        methods: {
            componentKey( component ) {
                if(component.variation) {
                    return component.path+":"+component.variation
                } else {
                    return component.path
                }
            },
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
                    if(component.variation) {
                        ev.dataTransfer.setData('text', component.path+":"+component.variation)
                    } else {
                        ev.dataTransfer.setData('text', component.path)
                    }
                    let view = $perAdminApp.getView();
                    if (this.isIgnoreContainersEnabled) {
                        Vue.set(view.state.tools.workspace, 'ignoreContainers', IgnoreContainers.ON_HOLD);
                        Vue.set(view.pageView, 'view', view.state.tools.workspace.view);
                    }
                }
            },
            onDragEnd: function(component, ev) {
                let view = $perAdminApp.getView();
                if (this.isIgnoreContainersEnabled) {
                    Vue.set(view.state.tools.workspace, 'ignoreContainers', IgnoreContainers.ENABLED);
                    Vue.set(view.pageView, 'view', IgnoreContainers.ENABLED);

                }
            }
        }
    }
</script>