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
    <div class="pathfield">
        <template v-for="(item, index) in pathSegments">
            /&nbsp;<admin-components-action
                v-bind:model="{
                    target: { path: item.path },
                    title: (index == 0)? $i18n(item.name) : item.name,
                    command: 'selectPathInNav'
                }"></admin-components-action>&nbsp;
        </template>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            path: function() {
                var dataFrom = this.model.dataFrom
                var node = $perAdminApp.getNodeFrom(this.$root.$data, dataFrom)
                return node
            },
            pathSegments: function() {
                var segments = this.path.toString().split('/')
                var ret = []
                for(var i = 3; i < segments.length; i++) {
                    ret.push( { name: segments[i], path: segments.slice(0, i+1).join('/') } )
                }
                return ret;
            }
        }
    }
</script>
