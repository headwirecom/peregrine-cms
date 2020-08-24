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
<div class="row">
    <admin-components-iconaction v-for="(action) in fromSource" v-bind:key="action.path" v-bind:model="toTenant(action)"></admin-components-iconaction>
</div>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
            toTenant: function(action) {
                // need to switch /content/admin in action to actual tenant path
                const res = JSON.parse(JSON.stringify(action, true, 2))
                const segments = res.action.split('/')
                const tenant = $perAdminApp.getNodeFromViewOrNull('/state/tenant')
                if(res.action.startsWith('/content/admin/pages/pages')) {
                    res.action += `.html/path:/content/${tenant.name}/pages`
                } else if(res.action.startsWith('/content/admin/pages/assets')) {
                    res.action += `.html/path:/content/${tenant.name}/assets`
                } else if(res.action.startsWith('/content/admin/pages/objects')) {
                    res.action += `.html/path:/content/${tenant.name}/objects`
                } else if(res.action.startsWith('/content/admin/pages/templates')) {
                    res.action += `.html/path:/content/${tenant.name}/templates`
                } else if(res.action.startsWith('/content/admin/pages/object-definitions')) {
                    res.action += `.html/path:/content/${tenant.name}/object-definitions`
                } else if(res.action.startsWith('/content/admin/pages/recyclebin')) {
                    res.action += `.html/path:/content/${tenant.name}`
                }
                return res
            }
        },
        computed: {
            fromSource: function() {
                var segments = this.model.source.split('/').slice(1)
                var ret = this.$root.$data
                for(var i = 0; i < segments.length; i++) {
                    ret = ret[segments[i]]
                }
                return ret
            }
        }
    }
</script>
