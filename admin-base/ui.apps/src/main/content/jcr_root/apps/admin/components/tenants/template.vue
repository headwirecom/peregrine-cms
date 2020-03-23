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

    <div>
        <admin-components-action
            v-bind:model="{
                target: '/content/admin/pages/pages/createsite',
                command: 'selectPath',
                tooltipTitle: $i18n('create tenant'),
            }">{{$i18n('create tenant')}}
        </admin-components-action>
        <fieldset class="vue-form-generator">
            <div class="form-group required">
                <div class="row">
                    <div class="col s6">
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
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </fieldset>
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
                return $perAdminApp.getNodeFrom($perAdminApp.getView(), '/admin/tenants')
            }
        },
        created() {
        },
        methods: {
            selectTenant(me, target) {
                $perAdminApp.stateAction('setTenant', { name: target}).then( () => {
                    $perAdminApp.loadContent('/content/admin/pages/index.html');
                });
            }
        }
    }
</script>

<style>
</style>