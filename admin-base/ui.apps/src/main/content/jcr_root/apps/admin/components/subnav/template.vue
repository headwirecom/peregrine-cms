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
    <div class="nav-content sub-nav">
        <template v-for="child in model.children">
            <div v-bind:is="child.component" v-bind:model="child"></div>
        </template>
        <!-- <template v-if="isEditor()">
            <admin-components-separator></admin-components-separator>
            <admin-components-action
                v-bind:model="{
                  command: 'selectPath',
                  download: getDownloadPath(),
                  target: getPath() + '/jcr:content.xml',
                  tooltipTitle: $i18n('exportModule'),
                  title: 'Export',
                  type: 'download'
                }"
            ></admin-components-action>
        </template> -->
    </div>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        }
    },
    methods: {
        isEditor: function() {
            return this.$root.$data.adminPage.title === "editor"
        },
        getPath: function(){
            if( this.$root.$data.pageView){
                if( this.$root.$data.pageView.path ){
                    return this.$root.$data.pageView.path;
                } else {
                    return "";
                }
            } else {
                return "";
            }
        },
        getDownloadPath(){
            return this.getPath().split('/').reverse()[0];
        }
    }
}
</script>

