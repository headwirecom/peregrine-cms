<!--
  #%L
  peregrine vuejs page renderer - UI Apps
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
<div  class="container" v-bind:data-per-path="model.path">
    <pagerendervue-components-placeholder v-if="renderBefore"
        v-bind:model="{ path: model.path, component: model.component, location: 'before' }">
    </pagerendervue-components-placeholder>
    <pagerendervue-components-placeholder v-if="renderSingle"
        v-bind:model="{ path: model.path, component: 'page: drop components here', location: 'into' }">
    </pagerendervue-components-placeholder>
    <div v-for="child in model.children" v-bind:key="child.path">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </div>
    <pagerendervue-components-placeholder v-if="renderAfter"
        v-bind:model="{ path: model.path, component: model.component, location: 'after' }">
    </pagerendervue-components-placeholder>
</div>
</template>

<script>
export default {
    props: [ 'model' ],
    computed: {
        renderAfter() {
            return(
                this.model.children.length > 0
                && this.model.children.children
                && this.model.children.children.length === 0
                ) 
        },
        renderBefore() {
            return(
                this.model.children.length > 0
                && this.model.children.children
                && this.model.children.children.length === 0
                ) 
        },
        renderSingle() {
            return (!this.model.children || this.model.children.length === 0)
        }
    }
}
</script>
