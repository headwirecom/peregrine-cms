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
              target: '/content/admin/pages/welcome',
              classes: 'peregrine-logo',
              tooltipTitle: $i18n('home')
            }">
            <admin-components-logo/>
          </admin-components-action>

          <!-- <template v-if="vueRoot.adminPage">
            <template v-for="item in vueRoot.adminPage.breadcrumbs">
              <admin-components-action
                  v-bind:key="item.path"
                  v-bind:model="{
                command: 'selectPath',
                title: $i18n(item.title),
                target: item.path
              }"></admin-components-action>
            </template>
          </template> -->
        </div>
        <ul id="nav-mobile" class="right hide-on-small-and-down">

          <li>
          <admin-components-action v-if="!model.hideTenants"
            v-bind:model="{
            command: 'selectPath',
            title: state.tenant ? state.tenant.name : 'unknown',
            target: '/content/admin/pages/index'
          }"></admin-components-action>

          <admin-components-action v-else
              v-bind:model="{
            command: 'selectPath',
            title: $i18n('Tenants'),
            target: '/content/admin/pages/index'
          }"></admin-components-action>

          <admin-components-separator />

          <admin-components-action
              v-bind:model="{
            command: 'selectPath',
            title: $i18n('Pages'),
            target: '/content/admin/pages/pages'
          }"></admin-components-action>

          <admin-components-action
              v-bind:model="{
            command: 'selectPath',
            title: $i18n('Assets'),
            target: '/content/admin/pages/assets'
          }"></admin-components-action>

          <admin-components-action
              v-bind:model="{
            command: 'selectPath',
            title: $i18n('Objects'),
            target: '/content/admin/pages/objects'
          }"></admin-components-action>

          <admin-components-action
              v-bind:model="{
            command: 'selectPath',
            title: $i18n('Templates'),
            target: '/content/admin/pages/templates'
          }"></admin-components-action>

          <admin-components-separator />
          </li>

          

          <!-- <li v-if="!model.hideTenants" class="tenant-select">
            <vue-multiselect
                v-model="state.tenant"
                deselect-label=""
                track-by="name"
                label="name"
                placeholder="Site"
                :title="$i18n('tenantsSelect')"
                :options="tenants"
                :searchable="false"
                :allow-empty="false"
                @select="onSelectTenant"/>
          </li> -->
          <li v-if="this.$root.$data.state">
            <a v-bind:title="$i18n('logout')" href="/system/sling/logout?resource=/index.html">
              {{this.$root.$data.state.user}}
            </a>
          </li>
          <li v-if="help">
            <a v-bind:title="$i18n('help')" href="#" v-on:click="onShowHelp">{{$i18n('help')}}</a>
          </li>
          <li>
            <a v-bind:title="$i18n('aboutNavBtn')" href="#" v-on:click="onShowAbout">{{$i18n('aboutNavBtn')}}</a>
          </li>
          <li>
            <vue-multiselect
                :value="language"
                deselect-label=""
                track-by="name"
                label="name"
                placeholder="Language"
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
      <component v-bind:is="child.component" v-bind:model="child"
                 v-bind:key="child.path"></component>
    </template>

  </nav>
</template>

<script>
  export default {
    props: ['model'],
    data() {
      return {
        state: $perAdminApp.getView().state,
        tenants: $perAdminApp.getView().admin.tenants || []
      }
    },
    computed: {
      language() {
        return {name: $perAdminApp.getView().state.language}
      },
      languages() {
        return this.$i18nGetLanguages()
      },
      vueRoot() {
        return this.$root
      },
      isExtended() {
        return this.model.children && this.model.children.length > 0
      },
      help() {
        if ($perAdminApp.getView()) {
          return $perAdminApp.findNodeFromPath($perAdminApp.getView().adminPage,
              '/jcr:content/tour')
        }
      }
    },
    beforeCreate() {
      $perAdminApp.getApi().populateTenants().then(() => {
        this.refreshTenants()
      })
    },
    methods: {
      onSelectLang({name}) {
        this.$i18nSetLanguage(name)
        $perAdminApp.forceFullRedraw()
      },
      onSelectTenant(tenant) {
        $perAdminApp.stateAction('setTenant', tenant)
      },
      onShowHelp() {
        $perAdminApp.action(this, 'showTour', '')
      },
      onShowAbout() {
        $('#aboutPeregrine').modal('open');
      },
      refreshTenants() {
        this.tenants = $perAdminApp.getView().admin.tenants || []
        this.state = $perAdminApp.getView().state
      }
    }
  }
</script>
