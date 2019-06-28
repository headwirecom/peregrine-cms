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
<nav v-bind:data-per-path="model.path" v-bind:class="isExtended ? 'nav-extended' : ''">
    <div class="nav-wrapper blue-grey darken-3">
      <div class="col s12">
        <div class="brand-logo">
          <admin-components-action 
            v-bind:model="{ 
              command: 'selectPath', 
              target: '/content/admin',
              classes: 'peregrine-logo',
              tooltipTitle: 'home'
            }">
            <admin-components-logo></admin-components-logo>
          </admin-components-action>
            <template v-if="vueRoot.adminPage">
                <template v-for="item in vueRoot.adminPage.breadcrumbs">
                    <admin-components-action
                            v-bind:model="{
                command: 'selectPath',
                title: item.title,
                target: item.path
              }"></admin-components-action>
                </template>
            </template>
        </div>
        <ul id="nav-mobile" class="right hide-on-small-and-down">
            <li v-if="this.$root.$data.state">
                <a title="logout" href="/system/sling/logout?resource=/index.html">
                    {{this.$root.$data.state.user}}
                </a>
            </li>
            <li v-if="help">
                <a title="$i18n('help')" href="#" v-on:click="onShowHelp">{{$i18n('help')}}</a>
            </li>
            <li>
                 <vue-multiselect
                    :value="language"
                    deselect-label=""
                    track-by="name"
                    label="name"
                    placeholder="Select one"
                    :options="languages"
                    :searchable="false"
                    :allow-empty="false"
                    @select="onSelectLang"
                 ></vue-multiselect>
            </li>
        </ul>
      </div>
    </div>
    <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>

</nav>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        language () {
            return { name: $perAdminApp.getView().state.language }
        },
        languages () {
            return this.$i18nGetLanguages()
        },
        vueRoot () {
            return this.$root
        },
        isExtended () {
            return this.model.children && this.model.children.length > 0
        },
        help () {
            if($perAdminApp.getView()) {
                return $perAdminApp.findNodeFromPath($perAdminApp.getView().adminPage, '/jcr:content/tour')
            }
        }
    },
    methods: {
        onSelectLang ({name}) {
            this.$i18nSetLanguage(name)
            $perAdminApp.forceFullRedraw()
        },
        onShowHelp () {
            $perAdminApp.action(this, 'showTour', '')
        }
    }
}
</script>
