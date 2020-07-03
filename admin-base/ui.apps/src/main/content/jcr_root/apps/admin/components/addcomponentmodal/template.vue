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
    <div v-if="show" class="add-component-modal-wrapper">
      <div class="add-component-modal">
        <input
            ref="filter"
            v-model="filter"
            type="text"
            class="filter"
            placeholder="filter components"/>
        <div class="component-list">
          <button
              v-for="component in allowedComponents"
              :key="component.path + '|' + component.variation"
              class="component-btn"
              @click="addComponentFromModal(componentKey(component))">
            {{componentDisplayName(component)}}
          </button>
        </div>
      </div>
    </div>
</template>

<script>
  import {get} from '../../../../../../js/utils';

  export default {
    name: 'AddComponentModal',
    props: [],
    data() {
      return {
        show: false,
        filter: ''
      }
    },
    computed: {
      view() {
        return $perAdminApp.getView()
      },
      allowedComponents() {
        return get(this.view, '/admin/components/data', [])
            .filter(el => {
              if (!this.view.state.tenant || !this.view.state.tenant.name) return false
              if (el.group === '.hidden') return false
              if (!this.componentDisplayName(el).toLowerCase().startsWith(
                  this.filter.toLowerCase())) {
                return false
              }
              return el.path.startsWith('/apps/' + this.view.state.tenant.name + '/')
            })
      },
    },
    methods: {
      componentKey(component) {
        if (component.variation) {
          return component.path + ':' + component.variation
        } else {
          return component.path
        }
      },
      componentDisplayName(component) {
        if (component.title) {
          return component.title
        } else {
          return component.path.split('/')[2] + ' ' + component.name
        }
      },

      addComponent(below = true) {
        this.show = true
        this.$nextTick(() => {
          this.$refs.filter.focus()
        })
      },

      addComponentFromModal(component) {
        this.addComponentModal.visible = true
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: this.path,
          component: component,
          drop: 'after'
        }
        $perAdminApp.stateAction('addComponentToPath', payload).then((data) => {
          this.refreshInlineEditElems()
          this.addComponentModal.visible = false
          // TODO: would be nice to select the newly inserted component and focus
          //       into the first contenteditable if there is indeed a contenteditable
        })
      }
    }
  }
</script>