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
  <div :class="`peregrine-content-view ${viewModeClass}`" @mouseout="leftOverlayArea">
    <div id="editable"
         ref="editable"
         :class="editable.class"
         :style="editable.styles"
         :draggable="enableEditableFeatures"
         @dragstart="onDragStart"
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
          component: null, path: null,
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
      view() {
        return $perAdminApp.getView()
      },
      isSelected() {
        return this.selected.component && this.selected.path && this.selected.path
            !== '/jcr:content'
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
        const path = this.selected.path
        if (path === undefined || path === null) {
          return false
        }
        const node = $perAdminApp.findNodeFromPath(this.view.pageView.page, path)
        if (!node) {
          return false
        }
        return !node.fromTemplate
      },
      selectedModel() {
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
          this.selectComponent(this, this.findComponentEl(val))
        } else {
          this.unselect(this)
        }
      },
      'selected.component'(val) {
        if (!val) {
          this.removeEditable()
        } else {
          this.wrapEditableAroundSelected()
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
      selectedModel: {
        deep: true,
        handler(val) {
          if (!this.selected.component) return
          this.wrapEditableAroundSelected()
        }
      },
      previewMode(val) {
        if (val === 'preview') {
          this.iframePreviewMode()
          this.editable.class = null
        } else {
          this.iframeEditMode()
          if (this.selected.component) {
            clearTimeout(this.editable.timer)
            this.editable.timer = setTimeout(() => {
              this.editable.class = 'selected'
              this.wrapEditableAround(this.selected.component)
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
      selectComponent(vm, componentEl) {
        vm.selected.component = componentEl
        vm.selected.path = componentEl.getAttribute(Attribute.PATH) || null

        if ($perAdminApp.findNodeFromPath(vm.view.pageView.page, vm.selected.path).fromTemplate) {
          vm.notifyComponentFromTemplate()
          this.unselect(vm)
        } else {
          vm.editable.class = 'selected'
          $perAdminApp.action(vm, 'showComponentEdit', vm.selected.path).then(() => {
            if (this.inline) {
              set(this.view, '/state/editor/inline', this.inline)
              this.inline = null
            }
          })
        }
      },
      unselect(vm) {
        vm.selected.component = null
        vm.selected.path = null
        vm.editable.class = null
      },
      setSelectedEl(el, inline = null) {
        if (el) {
          this.selected.component = el
          this.selected.path = el.getAttribute(Attribute.PATH) || null
          this.editable.class = 'selected'
          this.wrapEditableAround(el)
          $perAdminApp.action(this, 'showComponentEdit', this.selected.path).then(() => {
            if (inline) {
              set(this.view, '/state/editor/inline', inline)
            }
          })
        } else {
          this.selected.component = null
          this.selected.path = null
        }
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
        this.iframe.doc.querySelector('#peregrine-app').setAttribute('contenteditable', 'false')
        this.addInlineEditClones()
      },

      addInlineEditClones() {
        const elements = this.iframe.body.querySelectorAll(`[${Attribute.INLINE}]`)
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
        this.iframe.doc.addEventListener('dragover', this.onDragOver)
        this.iframe.doc.addEventListener('drop', this.onDrop)
        this.iframe.body.setAttribute('style', 'cursor: default !important')
        this.iframe.body.setAttribute('contenteditable', 'true')
        const elements = this.iframe.body.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          el.setAttribute('contenteditable', 'true')
          if (el.classList.contains('inline-edit-clone')) {
            el.style.display = ''
            el.innerHTML = elements[index + 1].innerHTML
          } else {
            el.style.display = 'none'
            if (this.selected.component === el) {
              this.selected.component = elements[index - 1]
            }
          }
        })
      },

      iframePreviewMode(editable = false) {
        this.iframe.doc.removeEventListener('click', this.onIframeClick)
        this.iframe.doc.removeEventListener('scroll', this.onIframeScroll)
        this.iframe.body.style.cursor = ''
        this.iframe.body.setAttribute('contenteditable', 'false')
        const elements = this.iframe.body.querySelectorAll(`[${Attribute.INLINE}]`)
        elements.forEach((el, index) => {
          el.setAttribute('contenteditable', editable)
          if (el.classList.contains('inline-edit-clone')) {
            el.style.display = 'none'
            if (this.selected.component === el) {
              this.selected.component = elements[index + 1]
            }
          } else {
            el.style.display = ''
          }
        })
      },

      onInlineEdit(event) {
        const el = event.target
        const dataInline = el.getAttribute(Attribute.INLINE).split('.').slice(1)
        dataInline.reverse()
        let parentProp = this.selectedModel
        while (dataInline.length > 1) {
          parentProp = parentProp[dataInline.pop()]
        }
        parentProp[dataInline.pop()] = el.innerHTML
      },

      onInlineFocus(event) {
        const dataInline = event.target.getAttribute(Attribute.INLINE).split('.').slice(1)
        this.inline = dataInline.join('.')
        this.target = event.target
      },

      onIframeClick(ev) {
        this.target = ev.target
      },

      onIframeScroll() {
        this.scrollTop = this.iframe.html.scrollTop
      },

      wrapEditableAroundSelected() {
        if (!this.selected.component) return

        const {top, left, width, height} = this.getBoundingClientRect(this.selected.component)
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

      leftOverlayArea(e) {
        if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

        // check if we only left the area into the overlay for the actions
        this.removeEditable()
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
      onDragStart(ev) {
        if (this.selected.component === null) return

        this.editable.class = 'dragging'
        ev.dataTransfer.setData('text', this.selected.path)
      },

      onDragOver(event) {
        event.preventDefault()
        const targetEl = this.findComponentEl(event.target)
        if (targetEl) {
          this.wrapEditableAround(targetEl)
          const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
          const isRoot = $perAdminApp.findNodeFromPath(this.view.pageView.page,
              targetEl.getAttribute(Attribute.PATH)).fromTemplate === true
          if (isDropTarget) {
            const dropLocation = targetEl.getAttribute('data-per-location')
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
              this.leftOverlayArea()
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
            this.leftOverlayArea()
          }
        } else {
          this.dropPosition = 'none'
          this.leftOverlayArea()
        }
      },

      onDrop(event) {
        this.editable.class = null
        if (this.isTouch) {
          this.selected.draggable = false
        }
        const targetEl = this.findComponentEl(event.target)
        if (typeof targetEl === 'undefined' || targetEl === null) {
          return false
        }
        const targetPath = targetEl.getAttribute(Attribute.PATH)
        const componentPath = event.dataTransfer.getData('text')
        if (targetPath === componentPath) {
          event.dataTransfer.clearData('text')
          return false
        }
        const view = this.view
        const payload = {
          pagePath: view.pageView.path,
          path: targetPath,
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
          /*
          const clone = el.cloneNode(true)
          clone.style.cursor = 'text'
          clone.classList.add('inline-edit-clone')
          clone.addEventListener('input', this.onInlineEdit)
          clone.addEventListener('focus', this.onInlineFocus)
          el.parentNode.insertBefore(clone, el)
           */
        })
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
        if (this.selected.component === null) return

        this.selected.draggable = true
        this.editable.class = 'draggable'
      },

      updateEditablePos(top) {
        this.$refs.editable.style.top = top + 'px'
      },

      onDelete(e) {
        const targetEl = this.selected.component
        const view = this.view
        const pagePath = view.pageView.path
        const payload = {
          pagePath: view.pageView.path,
          path: targetEl.getAttribute(Attribute.PATH)
        }
        if (payload.path !== '/jcr:content') {
          $perAdminApp.stateAction('deletePageNode', payload)
        }
        this.editable.class = null
        this.setSelectedEl(null)
      },

      onCopy(e) {
        const targetEl = this.selected.component
        this.clipboard = $perAdminApp.findNodeFromPath(
            this.view.pageView.page,
            targetEl.getAttribute(Attribute.PATH)
        )
      },

      onPaste(e) {
        const targetEl = this.selected.component
        const nodeFromClipboard = this.clipboard
        const view = this.view
        const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
        let dropPosition
        isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
        const payload = {
          pagePath: view.pageView.path,
          data: nodeFromClipboard,
          path: targetEl.getAttribute(Attribute.PATH),
          drop: dropPosition
        }
        $perAdminApp.stateAction('addComponentToPath', payload)
      },

      refreshEditor(me, target) {
        me.$refs['editview'].contentWindow.location.reload();
      },

      notifyComponentFromTemplate() {
        const title = this.$i18n('templateComponent')
        const message = this.$i18n('fromTemplateNotifyMsg')
        $perAdminApp.notifyUser(title, message, {
          complete: this.unselect
        })
      }
    }
  }
</script>