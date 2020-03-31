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
        <div class="tenant-filters">
            <label> Show template-tenants </label>
            <admin-components-materializeswitch
                :on-label="$i18n('yes')"
                :off-label="$i18n('no')"
                @update="onShowTemplateTenantsUpdate"/>
        </div>
        <div class="col s12 m6 l6 icon-action" v-for="child in children" v-bind:key="child.name">
            <div class="card blue-grey darken-3" :class="{'template-tenant': child.template}">
                <div class="card-content white-text tenant-link" @click="onCardContentClick(child.name)">
                    <span class="card-title">{{child.title ? child.title : child.name}}</span>
                    <p>{{child.description}}</p>
                </div>
                <div class="card-action">
                    <admin-components-action
                        v-bind:model="{
                            target: child.name,
                            command: 'selectTenant',
                            tooltipTitle: `${$i18n('edit')} '${child.title || child.name}'`
                        }">
                        <i class="material-icons">edit</i>
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

        <div class="col s12 m6 l6 icon-action">
            <div class="card blue-grey darken-3">
                <div class="card-content white-text tenant-link" @click="onCreateNewSiteClick">
                    <span class="card-title">{{`${$i18n('create new site')}`}}</span>
                    <p>Click to create your own website</p>
                </div>
                <div class="card-action">
                    <admin-components-action
                        v-bind:model="{
                            target: '/content/admin/pages/pages/createsite',
                            command: 'selectPath',
                            tooltipTitle: $i18n('create tenant'),
                        }">
                        <i class="material-icons">note_add</i>
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
    export default {
        props: ['model'],
        data(){
            return {
                isDraggingFile: false,
                isDraggingUiEl: false,
                isFileUploadVisible: false,
                uploadProgress: 0,
                showTemplateTenants: false
            }
        },
        computed: {
            children: function() {
                const tenants = $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants')
                if(tenants) {
                    if (this.showTemplateTenants) {
                        return tenants.filter( (t) => !t.internal)
                    } else {
                        return tenants.filter( (t) => !t.template && !t.internal)
                    }
                }
                return [];
            }
        },
        created() {
        },
        methods: {
            selectTenant(vm, name) {
                $perAdminApp.stateAction('setTenant', { name }).then( () => {
                    $perAdminApp.loadContent('/content/admin/pages/welcome.html')
                });
            },

            deleteSite: function(me, target) {
                $perAdminApp.askUser('Delete Site', me.$i18n('Are you sure you want to delete this site, its children, and generated content and components?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteSite', target)
                    }
                })
            },

            onCardContentClick(name) {
                this.selectTenant(this, name)
            },

            onCreateNewSiteClick() {
                $perAdminApp.action(this, 'selectPath', '/content/admin/pages/pages/createsite')
            },

            onShowTemplateTenantsUpdate(val) {
                this.showTemplateTenants = val
            }
        }
    }
</script>

<style scoped>
    fieldset {
        border: none;
    }

    .tenant-link:hover {
        background-color: rgba(255, 255, 255, .05);
        cursor: pointer;
    }

    .card-action {
        display: flex;
        justify-content: space-between;
    }

    .tenant-filters {
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 50px;
        background-color: #eeeeee;
        border-bottom: 2px solid silver;
        padding: 15px;
    }

    .template-tenant {
        opacity: .6;
    }
</style>