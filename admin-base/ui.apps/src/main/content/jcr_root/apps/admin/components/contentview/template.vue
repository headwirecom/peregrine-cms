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
  <div :class="`peregrine-content-view ${viewModeClass}`">
    <div id="editable"
         ref="editable"
         :class="editable.class"
         :style="editable.styles"
         :draggable="enableEditableFeatures"
         @dragstart="onEditableDragStart"
         @touchstart="onEditableTouchStart"
         @touchend="onEditableTouchEnd">
      <div v-if="enableEditableFeatures" class="editable-actions">
        <ul>
          <li class="waves-effect waves-light">
            <a href="#" :title="$i18n('copy')" @click.stop.prevent="onCopy">
              <i class="material-icons">content_copy</i>
            </a>
          </li>
          <li v-if="clipboard" class="waves-effect waves-light">
            <a :title="$i18n('paste')" href="#" @click.stop.prevent="onPaste">
              <i class="material-icons">content_paste</i>
            </a>
          </li>
          <li v-if="isSelected" class="waves-effect waves-light">
            <a href="#" :title="$i18n('deleteComponent')" @click.stop.prevent="onDelete">
              <i class="material-icons">delete</i>
            </a>
          </li>
        </ul>
      </div>
    </div>
    <template>
      <div v-if="!iframe.loaded" class="spinner-wrapper">
        <admin-components-materializespinner/>
      </div>
      <iframe
          v-show="iframe.loaded"
          id="editview"
          ref="editview"
          :src="pagePath"
          :data-per-mode="previewMode"
          @load="onIframeLoaded"
          @mouseleave="onIframeMouseLeave"
          @mouseenter="onIframeMouseEnter"/>
    </template>
  </div>
</template>

<script>
  import {Attribute} from '../../../../../../js/constants'
  import {set} from '../../../../../../js/utils'

  export default {
    props: ['model'],
    data() {
      return {
        target: null,
        inline: null,
        scrollTop: 0,
        dragging: false,
        editable: {
          visible: false,
          class: null,
          timer: null,
          delay: 200,
          styles: {
            top: 0,
            left: 0,
            width: 0,
            height: 0
          }
        },
        selected: {
          draggable: true
        },
        mouseover: {},
        iframe: {
          loaded: false,
          mouse: false,
          doc: null, html: null, body: null, app: null,
          scrollTop: 0
        },
        clipboard: null,
        ctrlDown: false,
        isTouch: false,
        isIOS: false
      }
    },
    computed: {
      component() {
        if (this.target) {
          return this.findComponentEl(this.target)
        } else {
          return null
        }
      },
      path() {
        if (this.component) {
          return this.component.getAttribute(Attribute.PATH)
        } else {
          return null
        }
      },
      dropTarget() {
        return this.component.getAttribute(Attribute.DROPTARGET)
      },
      dropLocation() {
        return this.component.getAttribute(Attribute.LOCATION)
      },
      targetInline() {
        return this.target.getAttribute(Attribute.INLINE)
      },
      view() {
        return $perAdminApp.getView()
      },
      isSelected() {
        return this.component && this.path && this.path !== '/jcr:content'
      },
      pagePath() {
        return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
      },
      previewMode() {
        const ws = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace')
        return ws ? ws.preview : ''
      },
      viewMode() {
        const viewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
        const previewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/preview')
        let ret
        if (viewMode) {
          ret = viewMode + (previewMode ? ' ' + previewMode : '')
        } else {
          ret = 'desktop' + (previewMode ? ' ' + previewMode : '')
        }
        return ret
      },
      viewModeClass() {
        return this.viewMode
      },
      enableEditableFeatures() {
        if (this.path === undefined || this.path === null) return false

        const node = $perAdminApp.findNodeFromPath(this.view.pageView.page, this.path)
        if (!node) {
          return false
        }
        return !node.fromTemplate
      },
      node() {
        if (this.view.state.editor && this.view.state.editor.path) {
          return $perAdminApp.findNodeFromPath(this.view.pageView.page, this.view.state.editor.path)
        } else {
          return null
        }
      }
    },
    watch: {
      target(val) {
        if (val) {
          this.selectComponent(this)
        } else {
          this.unselect(this)
        }
      },
      scrollTop() {
        this.wrapEditableAroundSelected()
      },
      'iframe.mouse'(val) {
        if (val && this.previewMode !== 'preview') {
          this.iframeEditMode()
        } else {
          this.iframePreviewMode(this.preview !== 'preview')
        }
      },
      node: {
        deep: true,
        handler(val) {
          if (!this.component) return
          this.wrapEditableAroundSelected()
        }
      },
      previewMode(val) {
        if (val === 'preview') {
          this.iframePreviewMode()
          this.editable.class = null
        } else {
          this.iframeEditMode()
          if (this.component) {
            clearTimeout(this.editable.timer)
            this.editable.timer = setTimeout(() => {
              this.editable.class = 'selected'
              this.wrapEditableAroundSelected()
            }, this.editable.delay)
          }
        }
      }
    },
    mounted() {
      this.$nextTick(() => {
        /* is this a touch device */
        this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
        this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
        if (this.isTouch) {
          /* selected components are not immediatly draggable on touch devices */
          this.selected.draggable = false
        }
      })
    },
    methods: {
      selectComponent(vm, el = vm.target) {
        vm.target = el
        if (!vm.target || !vm.component || !vm.path) return

        if ($perAdminApp.findNodeFromPath(vm.view.pageView.page, vm.path).fromTemplate) {
          this.unselect(vm)

          if (!this.dragging) {
            $perAdminApp.toast(vm.$i18n('fromTemplateNotifyMsg'), 'warn')
          }
        } else {
          this.wrapEditableAroundSelected()
          if (!this.dragging) {
            vm.editable.class = 'selected'
            $perAdminApp.action(vm, 'showComponentEdit', vm.path).then(() => {
              if (vm.inline) {
                set(vm.view, '/state/editor/inline', vm.inline)
                vm.inline = null
              }
            })
          }
        }
      },

      unselect(vm) {
        vm.target = null
        vm.editable.class = null
      },

      findComponentEl(targetEl) {
        let componentEl = targetEl
        while (!componentEl.getAttribute(Attribute.PATH)) {
          componentEl = componentEl.parentElement
          if (!componentEl) {
            break
          }
        }
        return componentEl
      },

      onIframeMouseLeave(event) {
        this.iframe.mouse = false
      },

      onIframeMouseEnter(event) {
        this.iframe.mouse = true
      },

      onIframeLoaded(ev) {
        this.iframe.loaded = true
        this.iframe.doc = this.$refs.editview.contentWindow.document
        this.iframe.html = this.iframe.doc.querySelector('html')
        this.iframe.body = this.iframe.doc.querySelector('body')
        this.iframe.app = this.iframe.doc.querySelector('#peregrine-app')
        this.iframe.doc.querySelector('#peregrine-app').setAttribute('contenteditable', 'false')
        this.addInlineEditClones()
      },

      addInlineEditClones() {
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el) => {
          const clsList = el.classList
          if (!clsList.contains('inline-edit-clone') && !clsList.contains('inline-edit-original')) {
            el.classList.add('inline-edit-original')
            const clone = el.cloneNode(true)
            clone.style.cursor = 'text'
            clone.classList.add('inline-edit-clone')
            clone.addEventListener('input', this.onInlineEdit)
            clone.addEventListener('focus', this.onInlineFocus)
            el.parentNode.insertBefore(clone, el)
          }
        })
        this.iframeEditMode()
      },

      iframeEditMode() {
        this.iframe.doc.addEventListener('click', this.onIframeClick)
        this.iframe.doc.addEventListener('scroll', this.onIframeScroll)
        this.iframe.doc.addEventListener('dragover', this.onIframeDragOver)
        this.iframe.doc.addEventListener('drop', this.onIframeDrop)
        this.iframe.body.setAttribute('style', 'cursor: default !important')
        this.iframe.body.setAttribute('contenteditable', 'true')
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          el.setAttribute('contenteditable', 'true')
          if (el.classList.contains('inline-edit-clone')) {
            el.style.display = ''
            el.innerHTML = elements[index + 1].innerHTML
          } else {
            el.style.display = 'none'
            if (this.component === el) {
              this.target = elements[index - 1]
            }
          }
        })
      },

      iframePreviewMode(editable = false) {
        this.iframe.doc.removeEventListener('click', this.onIframeClick)
        this.iframe.doc.removeEventListener('scroll', this.onIframeScroll)
        this.iframe.body.style.cursor = ''
        this.iframe.body.setAttribute('contenteditable', 'false')
        const elements = this.iframe.app.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          el.setAttribute('contenteditable', editable)
          if (el.classList.contains('inline-edit-clone')) {
            el.style.display = 'none'
            if (this.component === el) {
              this.target = elements[index + 1]
            }
          } else {
            el.style.display = ''
          }
        })
      },

      onInlineEdit(event) {
        this.target = event.target
        const dataInline = this.targetInline.split('.').slice(1)
        dataInline.reverse()
        let parentProp = this.node
        while (dataInline.length > 1) {
          parentProp = parentProp[dataInline.pop()]
        }
        parentProp[dataInline.pop()] = this.target.innerHTML
      },

      onInlineFocus(event) {
        this.target = event.target
        const dataInline = this.targetInline.split('.').slice(1)
        this.inline = dataInline.join('.')
      },

      onIframeClick(ev) {
        this.target = ev.target
      },

      onIframeScroll() {
        this.scrollTop = this.iframe.html.scrollTop
      },

      wrapEditableAroundSelected() {
        if (!this.component) return

        const {top, left, width, height} = this.getBoundingClientRect(this.component)
        this.editable.styles.top = `${top}px`
        this.editable.styles.left = `${left}px`
        this.editable.styles.width = `${width}px`
        this.editable.styles.height = `${height}px`
      },

      getElementStyle(e, styleName) {
        let styleValue = '';
        if (document.defaultView && document.defaultView.getComputedStyle) {
          styleValue = document.defaultView.getComputedStyle(e, '').getPropertyValue(styleName);
        } else if (e.currentStyle) {
          styleName = styleName.replace(/-(\w)/g, (strMatch, p1) => {
            return p1.toUpperCase();
          });
          styleValue = e.currentStyle[styleName];
        }
        return styleValue;
      },

      getBoundingClientRect(e) {
        const rect = e.getBoundingClientRect()
        const marginTop = parseFloat(this.getElementStyle(e, 'margin-top'))
        const marginLeft = parseFloat(this.getElementStyle(e, 'margin-left'))
        const marginRight = parseFloat(this.getElementStyle(e, 'margin-right'))
        const marginBottom = parseFloat(this.getElementStyle(e, 'margin-bottom'))
        const newRect = {
          left: rect.left - marginLeft,
          right: rect.right + marginRight,
          top: rect.top - marginTop,
          bottom: rect.bottom + marginBottom,
        }
        newRect.width = newRect.right - newRect.left
        newRect.height = newRect.bottom - newRect.top
        return newRect;
      },

      removeEditable() {
        this.target = null
        this.editable.class = null
        if (this.isTouch) {
          this.selected.draggable = false
        }
      },

      getRelativeMousePosition() {
        if (!this.mouseover) return {x: -1, y: -1}
        const componentEl = this.findComponentEl(event.target)
        const offset = this.getBoundingClientRect(componentEl)
        return {
          width: offset.width,
          x: event.pageX - offset.left,
          xPercentage: (event.pageX - offset.left) / offset.width * 100,
          height: offset.height,
          y: event.pageY - offset.top - this.iframe.scrollTop,
          yPercentage: (event.pageY - offset.top - this.iframe.scrollTop) / offset.height * 100
        }
      },

      /* Drag and Drop ===========================
      ============================================ */
      onEditableDragStart(ev) {
        if (this.selected.component === null) return

        this.editable.class = 'dragging'
        ev.dataTransfer.setData('text', this.selected.path)
      },

      onIframeDragOver(event) {
        event.preventDefault()
        this.dragging = true
        this.target = event.target

        if (this.component) {
          const isDropTarget = this.dropTarget === 'true'
          const isRoot = $perAdminApp.findNodeFromPath(
              this.view.pageView.page, this.path).fromTemplate === true

          if (isDropTarget) {
            const dropLocation = this.dropLocation
            if (dropLocation === 'after' && !isRoot) {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            } else if (dropLocation === 'before' && !isRoot) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else if (dropLocation) {
              this.dropPosition = 'into-' + dropLocation
              this.editable.class = 'selected'
            } else {
              this.dropPosition = 'none'
            }
          } else if (!isRoot) {
            const relativeMousePosition = this.getRelativeMousePosition(event)
            if (relativeMousePosition.yPercentage <= 43.5) {
              this.dropPosition = 'before'
              this.editable.class = 'drop-top'
            } else {
              this.dropPosition = 'after'
              this.editable.class = 'drop-bottom'
            }
          } else {
            this.dropPosition = 'none'
          }
        } else {
          this.dropPosition = 'none'
        }
      },

      onIframeDrop(event) {
        event.preventDefault()
        this.dragging = false
        this.target = event.target
        if (this.isTouch) {
          this.selected.draggable = false
        }
        if (typeof this.component === 'undefined' || this.component === null) return false

        const componentPath = event.dataTransfer.getData('text')
        if (this.path === componentPath) {
          event.dataTransfer.clearData('text')
          return false
        }
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: this.path,
          component: componentPath,
          drop: this.dropPosition
        }
        let addOrMove
        if (componentPath.includes('/components/')) {
          addOrMove = 'addComponentToPath';
        } else {
          addOrMove = 'moveComponentToPath';
          const targetNode = $perAdminApp.findNodeFromPath(this.view.pageView.page, targetPath)
          if (!targetNode || targetNode.fromTemplate) {
            $perAdminApp.notifyUser('template component',
                'You cannot drag a component into a template section', {
                  complete: this.removeEditable
                })
            return false;
          }
        }
        $perAdminApp.stateAction(addOrMove, payload).then((data) => {
          this.addInlineEditClones()
        })
        this.unselect(this)
        event.dataTransfer.clearData('text')
      },

      /* Editable methods ========================
      ============================================ */
      onEditableTouchStart(ev) {
        this.editable.timer = setTimeout(this.onLongTouchOverlay, 800)
      },

      onEditableTouchEnd(ev) {
        clearTimeout(this.editable.timer)
      },

      onLongTouchOverlay() {
        if (this.component === null) return

        this.selected.draggable = true
        this.editable.class = 'draggable'
      },

      updateEditablePos(top) {
        this.$refs.editable.style.top = top + 'px'
      },

      onDelete(e) {
        const view = this.view
        const pagePath = view.pageView.path
        const payload = {
          pagePath: view.pageView.path,
          path: this.path
        }
        if (payload.path !== '/jcr:content') {
          $perAdminApp.stateAction('deletePageNode', payload).then((data) => {
            this.cleanLeftOvers(payload.path)
          })
        }
        this.editable.class = null
      },

      cleanLeftOvers(path) {
        const leftOvers = this.iframe.app.querySelectorAll(`[${Attribute.PATH}="${path}"]`)

        if (leftOvers && leftOvers.length > 0) {
          leftOvers.forEach((el) => {
            el.remove()
          })
        }
      },

      onCopy(e) {
        this.clipboard = $perAdminApp.findNodeFromPath(
            this.view.pageView.page,
            this.path
        )
      },

      onPaste(e) {
        const nodeFromClipboard = this.clipboard
        const view = this.view
        const isDropTarget = this.dropTarget === 'true'
        let dropPosition
        isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
        const payload = {
          pagePath: view.pageView.path,
          data: nodeFromClipboard,
          path: this.path,
          drop: dropPosition
        }
        $perAdminApp.stateAction('addComponentToPath', payload).then((data) => {
          this.addInlineEditClones()
        })
      },

      refreshEditor(me, target) {
        me.$refs['editview'].contentWindow.location.reload();
      }
    }
  }
</script>