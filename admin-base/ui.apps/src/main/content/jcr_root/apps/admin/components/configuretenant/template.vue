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
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'editRootTemplate',
                title: 'configure website root template',
                target: '/content/admin/pages/templates/edit',
                classes: 'btn'
            }">
        </admin-components-action>
    </div>
    <div>
        <admin-components-action
            v-bind:model="{
                command: 'tenantSetupReplication',
                title: 'site setup replication',
                target: '/content/admin/pages/templates/edit',
                classes: 'btn'
            }">
        </admin-components-action>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
            editRootTemplate(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('editTemplate', `/content/${tenant}/templates` );
            },
            tenantSetupReplication(me, target) {
                const tenant = $perAdminApp.getView().state.tenant.name;
                $perAdminApp.stateAction('tenantSetupReplication', `/content/${tenant}`, 'true');
            }
        }
    }
</script>
