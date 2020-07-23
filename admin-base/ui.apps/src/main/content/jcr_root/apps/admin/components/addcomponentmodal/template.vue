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
  <div v-show="visible" class="add-component-modal-wrapper">
    <div class="add-component-modal">
      <div ref="header" class="header">Add Component: <b>{{ computedDrop }}</b></div>
      <div class="content">
        <input
            ref="filter"
            v-model="filter"
            type="text"
            class="filter"
            placeholder="filter components"
            @keydown="onFilterKeyDown"/>
        <div class="component-list">
          <button
              v-for="(component, index) in filteredComponents"
              :key="`${component.path}-${component.variation}-${index}`"
              ref="componentBtn"
              class="component-btn"
              @click="onComponentBtnClick(component)"
              @keydown="omComponentBtnKeyDown($event, index)">
            {{componentDisplayName(component)}}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {get} from '../../../../../../js/utils'
  import {Attribute, Key} from '../../../../../../js/constants'

  export default {
    name: 'AddComponentModal',
    props: {
      selectedComponent: null,
      isDropTarget: Boolean,
      windows: Array
    },
    data() {
      return {
        visible: false,
        filter: '',
        component: null,
        windowEventListeners: [],
        drop: ''
      }
    },
    computed: {
      view() {
        return $perAdminApp.getView()
      },
      node() {
        const path = get(this.view, '/state/editor/path', null)
        if (path) {
          return $perAdminApp.findNodeFromPath(this.view.pageView.page, path)
        } else {
          return null
        }
      },
      filteredComponents() {
        return get(this.view, '/admin/components/data', []).filter(el => {
          const tenant = this.view.state.tenant
          const lCmpName = this.componentDisplayName(el).toLowerCase()
          const filter = this.filter.toLowerCase()

          if (!tenant || !tenant.name || el.group === '.hidden' || !lCmpName.startsWith(filter)) {
            return false
          } else {
            return el.path.startsWith('/apps/' + tenant.name + '/')
          }
        })
      },
      componentKey() {
        let key = this.component.path

        if (this.component.variation) {
          key += ':' + this.component.variation
        }

        return key
      },
      componentCount() {
        return this.filteredComponents.length
      },
      computedDrop() {
        return (this.isDropTarget ? 'into-' : '') + this.drop
      }
    },
    watch: {
      windows(val) {
        this.clearWindowEventListeners()
        this.bindWindowEventListeners()
      }
    },
    mounted() {
      this.bindWindowEventListeners()
    },
    methods: {
      open(drop) {
        this.visible = true
        this.drop = drop
        this.$nextTick(() => {
          this.$refs.header.scrollIntoView(true)
          this.$refs.filter.focus()
        })
      },
      close() {
        this.visible = false
        this.filter = ''
        this.dropBefore = false
      },
      onComponentBtnClick(component) {
        this.component = component
        this.addComponentFromModal()
      },
      componentDisplayName(component) {
        if (component.title) {
          return component.title
        } else {
          return component.path.split('/')[2] + ' ' + component.name
        }
      },
      addComponentFromModal() {
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: this.selectedComponent.getAttribute(Attribute.PATH),
          component: this.componentKey,
          drop: this.computedDrop
        }

        $perAdminApp.stateAction('savePageEdit', {
          data: this.node,
          path: this.view.state.editor.path
        }).then(() => {
          return $perAdminApp.stateAction('addComponentToPath', payload)
        }).then((data) => {
          this.close()
          this.$emit('component-added', this.findNewNode(payload))
        })
      },
      findNewNode(payload) {
        if (['after', 'before'].includes(payload.drop)) {
          const split = payload.path.split('/')
          split.pop()
          const parentPath = split.join('/')
          const parentNode = $perAdminApp.findNodeFromPath(this.view.pageView.page, parentPath)
          let index = null
          parentNode.children.some((child, i) => {
            if (child.path === payload.path) {
              index = i
            }
          })

          if (payload.drop === 'after') {
            return parentNode.children[index + 1]
          } else if (payload.drop === 'before') {
            return parentNode.children[index - 1]
          }
        } else if (payload.drop.startsWith('into')) {
          const targetNode = $perAdminApp.findNodeFromPath(this.view.pageView.page, payload.path)

          if (payload.drop === 'into-before') {
            return targetNode.children[0]
          } else if (payload.drop === 'into-after') {
            return targetNode.children[targetNode.children.length - 1]
          }
        }

        throw `unexpected drop: ${payload.drop}`
      },
      onKeyDown(event) {
        if (!this.selectedComponent) return

        const key = event.which
        const shift = event.shiftKey
        const ctrlOrCmd = event.ctrlKey || event.metaKey

        if (!this.visible) {
          if (ctrlOrCmd) {
            if (key === Key.DOT) {
              this.open('after')
            } else if (key === Key.COMMA) {
              this.open('before')
            }
          }
        } else if (this.visible) {
          if (key === Key.ESC) {
            this.close()
          }
        }
      },
      onFilterKeyDown(event) {
        if (this.componentCount <= 0) return

        const key = event.which

        if (key === Key.ARROW_DOWN || key === Key.ARROW_RIGHT) {
          event.preventDefault()
          this.$refs.componentBtn[0].focus()
        }
      },
      omComponentBtnKeyDown(event, index) {
        if (this.componentCount <= 0) return

        const key = event.which

        if (key === Key.ARROW_DOWN || key === Key.ARROW_RIGHT) {
          if (index + 1 < this.componentCount) {
            event.preventDefault()
            this.$refs.componentBtn[index + 1].focus()
          }
        } else if (key === Key.ARROW_UP || key === Key.ARROW_LEFT) {
          if (index - 1 >= 0) {
            event.preventDefault()
            this.$refs.componentBtn[index - 1].focus()
          } else {
            this.$refs.header.scrollIntoView(true)
            this.$refs.filter.focus()
          }
        }
      },
      bindWindowEventListeners() {
        this.windows.forEach((win) => {
          this.windowEventListeners.push({
            window: win,
            type: 'keydown',
            id: win.addEventListener('keydown', this.onKeyDown)
          })
        })
      },
      clearWindowEventListeners() {
        this.windowEventListeners.forEach((listener) => {
          listener.window.removeEventListener(listener.type, listener.id)
        })
      }
    }
  }
</script>