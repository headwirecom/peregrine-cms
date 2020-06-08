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
    <div class="tooling-page">
        <template v-for="child in model.children">
            <component v-bind:is="child.component" v-bind:model="child" v-bind:key="child.path"></component>
        </template>
        <admin-components-about></admin-components-about>
        <admin-components-notifyuser></admin-components-notifyuser>
        <admin-components-askuser></admin-components-askuser>
        <admin-components-promptuser></admin-components-promptuser>
    </div>
</template>

<script>
    import {set} from '../../../../../../js/utils'

    export default {
    props: ['model'],
    mounted(){
        // init materialize plugins
        $('.modal').modal()
    },
    methods: {
        selectPath: function(me, target) {
            // const view = $perAdminApp.getView()
            // const tenant = view.state.tenant
            // const action = target.action || target
            // const section = action.split('/').slice(-1).pop()
            // set(view, '/state/current/section/name', section)
            // const payload = {
            //     path: `/state/tools/${section}`
            //     // ,
            //     // selected: `/content/${tenant.name}/${section}`
            // }
            // $perAdminApp.stateAction('selectToolsNodesPath', payload).then(() => {
            //     $perAdminApp.loadContent(action + '.html')
            // })
            $perAdminApp.loadContent(target + '.html')
        },
        editPreview: function(me, target) {
            $perAdminApp.stateAction('editPreview', target)
        },
        editPage: function(me, target) {
        },
        addSite: function(me, target) {
            $perAdminApp.stateAction('createTenantWizard', '/content')
        },
        configureTenant: function(me, target) {
            $perAdminApp.stateAction('configureTenant', target)
        }
    }
}
</script>
