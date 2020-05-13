<!--
  #%L
  example site - UI Apps
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
    <div v-bind:data-per-path="model.path" v-bind:class="[editorType]">
        <pagerendervue-components-placeholder
            v-bind:model="{ path: model.path, component: model.component, location: 'before' }"
            v-bind:class="{'from-template': model.fromTemplate}">
        </pagerendervue-components-placeholder>
        <template v-for="child in model.children">
            <component
                v-bind:is="child.component"
                v-bind:model="child"
                v-bind:class="{'from-template': isFromTemplate(child)}">
            </component>
        </template>
        <pagerendervue-components-placeholder
            v-bind:model="{ path: model.path, component: model.component, location: 'after' }"
            v-bind:class="{'from-template': model.fromTemplate}">
        </pagerendervue-components-placeholder>
    </div>
</template>

<script>
    export default {
        props: [ 'model' ],
        computed: {
            editorType() {
                return $peregrineApp.getAdminAppNode('/state/contentview/editor/type');
            }
        },
        methods: {
            isFromTemplate(m) {
                return (!m.children || m.children.length <= 0) && m.fromTemplate;
            }
        }
    }
</script>
<style>
    div.page-editor .from-template {
        background-image: repeating-linear-gradient(135deg, #ffffff 0px, #ffffff 0px, #80808015 10px, #80808015 20px, #ffffff 20px);
    }
</style>
