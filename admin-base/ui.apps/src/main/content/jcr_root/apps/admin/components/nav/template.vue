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
        <div class="brand-logo" @click="$refs.notificationsModal.open()">
          <admin-components-logo/>
        </div>
        <admin-components-materializemodal ref="notificationsModal">
          <p>Notifications: Coming soon...</p>
          <h5>-------------------------------------------------------------------------------</h5>
          <h3>
            Change Website has been moved:
          </h3>
          <img src="data:image/png;base64,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"/>
        </admin-components-materializemodal>
        <admin-components-materializedropdown
            v-if="!model.hideTenants && state.tenant"
            class="current-tenant-name nav-link"
            :class="{active: getActiveSection() === 'tenants'}"
            :below-origin="true"
            :items="tenantDdItems">
          {{ state.tenant.name }}
        </admin-components-materializedropdown>
      </div>
      <div class="nav-center">
        <ul v-if="!model.hideTenants" class="hide-on-small-and-down nav-mobile">
          <admin-components-action
              v-for="section in sections"
              :key="`section-${section.name}`"
              tag="li"
              :model="getSectionModel(section)"
              :class="{active: getActiveSection() === section.name, 'no-mobile': !section.mobile}"
              class="nav-link"/>
        </ul>
        <ul v-else class="hide-on-small-and-down nav-mobile">
          <admin-components-action
              tag="li"
              :model="getSectionModel({title: 'Home', name: 'index'})"
              :class="{active: getActiveSection() === 'index'}"
              class="nav-link"/>
        </ul>
      </div>
      <ul class="nav-right hide-on-small-and-down nav-mobile">
        <admin-components-materializemodal ref="languageModal">
          <template>
            <vue-multiselect
                :value="language"
                deselect-label=""
                track-by="name"
                label="name"
                placeholder="Language"
                :options="languages"
                :searchable="false"
                :allow-empty="false"
                @select="onSelectLang"/>
            <div class="modal-spacer"></div>
          </template>
          <template slot="footer">
            <button class="btn btn-primary" @click="$refs.languageModal.close()">
              {{ $i18n('Close') }}
            </button>
          </template>
        </admin-components-materializemodal>
        <admin-components-materializedropdown
            tag="li"
            class="nav-link user-link"
            :below-origin="true"
            :items="userDdItems">
          <template>
            <div class="user-circle">
              <i class="material-icons large">face</i>
            </div>
          </template>
          <template slot="header">
            <div class="user-circle big" :title="$i18n('profile picture')">
              <i class="material-icons">face</i>
            </div>
            <table>
              <tr>
                <td>Logged in as</td>
                <td>{{ username }}</td>
              </tr>
              <tr>
                <td>Language</td>
                <td>{{ language.name }}</td>
              </tr>
              <tr>
                <td>Timezone</td>
                <td>{{ gmtOffset }}</td>
              </tr>
            </table>
          </template>
        </admin-components-materializedropdown>
        <admin-components-materializedropdown
            tag="li"
            class="nav-link more-link"
            :below-origin="true"
            :gutter="2"
            :items="moreDdItems">
          <i class="material-icons">more_vert</i>
        </admin-components-materializedropdown>
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
      username() {
        return this.$root.$data.state.user
      },
      help() {
        if ($perAdminApp.getView()) {
          return $perAdminApp.findNodeFromPath($perAdminApp.getView().adminPage,
              '/jcr:content/tour')
        }
      },
      moreDdItems() {
        return [
          {label: 'help', icon: 'help', disabled: !this.help, click: this.onHelpClick},
          {label: 'tutorials', icon: 'import_contacts', click: this.onTutorialsClick},
          {label: '--------------------', disabled: true},
          {label: 'aboutNavBtn', icon: 'info', click: this.onAboutClick},
        ]
      },
      tenantDdItems() {
        return [
          {label: 'Settings', icon: 'settings', click: this.onSettingsClick},
          {label: 'Change website', icon: 'swap_horiz', click: this.onChangeWebsiteClick}
        ]
      },
      userDdItems() {
        return [
          {label: 'Profile', icon: 'account_box', disabled: true},
          {label: 'Language', icon: 'language', click: this.onLanguageClick},
          {label: '', disabled: true},
          {label: 'Logout', icon: 'exit_to_app', click: this.onLogoutClick}
        ]
      },
      gmtOffset() {
        const offsetInMinutes = new Date().getTimezoneOffset()
        const offset = offsetInMinutes / 60 * -1
        const algebraicSign = offset >= 0 ? '+' : '-'
        return `GMT${algebraicSign}${offset}`
      }
    },
    beforeCreate() {
      $perAdminApp.getApi().populateTenants().then(() => {
        this.refreshTenants()
      })
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
      onSettingsClick() {
        $perAdminApp.action(this, 'configureTenant', {
          path: '/content', name: this.state.tenant.name
        })
      },
      onChangeWebsiteClick() {
        $perAdminApp.action(this, 'selectPath', '/content/admin/pages/index')
      },
      onLogoutClick() {
        window.location.href = '/system/sling/logout?resource=/index.html';
      },
      onLanguageClick() {
        this.$refs.languageModal.open();
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
      }
    }
  }
</script>
