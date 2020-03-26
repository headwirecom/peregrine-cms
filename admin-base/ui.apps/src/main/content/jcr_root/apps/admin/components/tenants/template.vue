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

    <div class="row" style="border: solid silver 2px; box-shadow: 3px 3px 4px lightgray; margin-right: 10px;">
        <div class="col s12 m6 l4 icon-action" v-for="child in children" v-bind:key="child.name">
            <div class="card blue-grey darken-3">
                <div class="card-content white-text">
                    <span class="card-title">{{child.title ? child.title : child.name}}</span>
                    <p>{{child.description}}</p>
                </div>
                <div class="card-action">
                    <admin-components-action
                        v-bind:model="{
                            target: child.name,
                            command: 'selectTenant',
                            tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                        }">{{`${$i18n('edit')} '${child.title || child.name}'`}}
                    </admin-components-action>

                    <admin-components-action
                        v-bind:model="{
                            target: { path: '/content', name: child.name },
                            command: 'configureSite',
                            tooltipTitle: `${$i18n('configure')} '${child.title || child.name}'`
                        }">
                        <i class="material-icons">settings</i>
                    </admin-components-action>

                    <admin-components-action
                        v-bind:model="{
                            target: { path: '/content', name: child.name },
                            command: 'deleteSite',
                            tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`
                        }">
                        <i class="material-icons">delete</i>
                    </admin-components-action>
                </div>
            </div>
        </div>

        <div class="col s12 m6 l4 icon-action">
            <div class="card blue-grey darken-3">
                <div class="card-content white-text">
                    <span class="card-title">{{`${$i18n('new site')}`}}</span>
                    <p>create your own website</p>
                </div>
                <div class="card-action">
                    <admin-components-action
                        v-bind:model="{
                            target: '/content/admin/pages/pages/createsite',
                            command: 'selectPath',
                            tooltipTitle: $i18n('create tenant'),
                        }">{{$i18n('create website')}}
                    </admin-components-action>
                </div>
            </div>
        </div>

        <!-- older variation 
        <p>
        <admin-components-action
            v-bind:model="{
                target: '/content/admin/pages/pages/createsite',
                command: 'selectPath',
                tooltipTitle: $i18n('create tenant'),
            }"><button>{{$i18n('create website')}}</button>
        </admin-components-action>
        </p>
        <fieldset class="vue-form-generator" v-if="children.length > 0">
            <div class="form-group required">
                <div class="row">
                    <div class="col m12">
                        <label>Select Tenant</label>
                        <ul class="collection">
                            <li class="collection-item" v-for="child in children" v-bind:key="child.name">
                                <admin-components-action
                                    v-bind:model="{
                                        target: child.name,
                                        command: 'selectTenant',
                                        tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                                    }">{{child.title ? child.title : child.name}}
                                </admin-components-action>

                                <admin-components-action
                                    v-bind:model="{
                                        target: { path: '/content', name: child.name },
                                        command: 'deleteSite',
                                        tooltipTitle: `${$i18n('delete')} '${child.title || child.name}'`
                                    }">
                                    <i class="material-icons">delete</i>
                                </admin-components-action>
                           </li>
                        </ul>
                    </div>
                </div>
            </div>
        </fieldset> -->
    </div>

</template>

<script>
    import {set} from '../../../../../../js/utils';
    export default {
        props: ['model'],
        data(){
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0
            }
        },
        computed: {
            children: function() {
                const tenants = $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants');
                if(tenants) {
                    return tenants.filter( (t) => !t.template && !t.internal);
                }
                return [];
            }
        },
        created() {
        },
        methods: {
            selectTenant(me, target) {
                $perAdminApp.stateAction('setTenant', { name: target}).then( () => {
                    $perAdminApp.loadContent('/content/admin/pages/welcome.html');
                });
            },

            deleteSite: function(me, target) {
                $perAdminApp.askUser('Delete Site', me.$i18n('Are you sure you want to delete this site, its children, and generated content and components?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteSite', target)
                    }
                })
            }
        }
    }
</script>

<style scoped>
fieldset {
    border: none;
}
</style>