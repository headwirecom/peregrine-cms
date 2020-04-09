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
      <div class="nav-left">
        <div class="brand-logo">
          <admin-components-action
              v-bind:model="{
                  command: 'selectPath',
                  target: '/content/admin/pages/index',
                  classes: 'peregrine-logo',
                  tooltipTitle: $i18n('home')
              }">
            <admin-components-logo/>
          </admin-components-action>
        </div>
        <template v-if="!model.hideTenants && state.tenant">
          <div class="current-tenant-name">
            {{ state.tenant.name }}
          </div>
        </template>
      </div>
      <div class="nav-center">
        <template v-if="!model.hideTenants">
          <ul class="hide-on-small-and-down nav-mobile">
            <admin-components-action
                v-for="section in sections"
                :key="`section-${section.name}`"
                tag="li"
                :model="getSectionModel(section)"
                :class="{active: getActiveSection() === section.name, 'show-on-medium': !section.mobile}"
                class="nav-link"/>
            <admin-components-action
                v-if="state.tenant"
                tag="li"
                class="nav-link"
                :class="{active: getActiveSection() === 'tenants'}"
                :model="{
                  target: { path: '/content', name: state.tenant.name },
                  command: 'configureSite',
                  title: $i18n('Settings'),
                  tooltipTitle: `${$i18n('configure')} '${state.tenant.title || state.tenant.name}'`
              }"/>
          </ul>
        </template>
      </div>
      <ul class="nav-right hide-on-small-and-down nav-mobile">
        <li class="nav-link">
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
        <li v-if="this.$root.$data.state" class="nav-link user-link">
          <a :title="$i18n('logout') + ' ' + $root.$data.state.user"
             class="user-icon"
             href="/system/sling/logout?resource=/index.html">
            {{$root.$data.state.user[0].toUpperCase()}}
          </a>
        </li>
        <li class="nav-link more-link">
          <a href="#" ref="more" data-activates="more-content">
            <i class="material-icons">more_vert</i>
          </a>
          <ul id="more-content" class="dropdown-content">
            <li class="item" :class="{disabled: !help}" :title="$i18n('help')" @click="onHelpClick">
              {{ $i18n('help') }}
            </li>
            <li class="item" :title="$i18n('tutorials')" @click="onTutorialsClick">
              {{ $i18n('tutorials') }}
            </li>
            <li class="item disabled"></li>
            <li class="item" :title="$i18n('aboutNavBtn')" href="#" @click="onAboutClick">
              {{ $i18n('aboutNavBtn') }}
            </li>
          </ul>
        </li>
      </ul>
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
        tenants: $perAdminApp.getView().admin.tenants || [],
        sections: [
          {name: 'welcome', title: 'Dashboard', mobile: true},
          {name: 'pages', title: 'Pages'},
          {name: 'assets', title: 'Assets'},
          {name: 'objects', title: 'Objects'},
          {name: 'templates', title: 'Templates'}
        ],
        helpSelection: 'Help'
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
    mounted() {
      $(this.$refs.more).dropdown({belowOrigin: true});
    },
    methods: {
      getSectionModel(section) {
        return {
          command: 'selectPath',
          title: this.$i18n(section.title),
          target: `/content/admin/pages/${section.name}`
        }
      },
      onSelectLang({name}) {
        this.$i18nSetLanguage(name)
        $perAdminApp.forceFullRedraw()
      },
      onSelectTenant(tenant) {
        $perAdminApp.stateAction('setTenant', tenant)
      },
      onHelpClick() {
        $perAdminApp.action(this, 'showTour', '')
      },
      onTutorialsClick() {
        document.getElementById('peregrine-main').classList.toggle('tutorial-visible')
      },
      onAboutClick() {
        $('#aboutPeregrine').modal('open');
      },
      refreshTenants() {
        this.tenants = $perAdminApp.getView().admin.tenants || []
        this.state = $perAdminApp.getView().state
      },
      getActiveSection() {
        const breadcrumbs = $perAdminApp.getView().adminPage.breadcrumbs
        if (breadcrumbs) {
          return breadcrumbs[0].path.split('/')[4]
        }
        return 'welcome'
      },
      onHelpSelect() {
        console.log('BLA')
      }
    }
  }
</script>
