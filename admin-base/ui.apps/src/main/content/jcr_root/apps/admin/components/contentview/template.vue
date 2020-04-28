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
    <div id="editviewoverlay"
         @click="onClickOverlay"
         @scroll="onScrollOverlay"
         @mousemove="mouseMove"
         @dragover="onDragOver"
         @drop.prevent="onDrop">
      <div class="editview-container" ref="editviewContainer">
        <div :class="editableClass"
             ref="editable"
             id="editable"
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
              <li v-if="isComponentSelected" class="waves-effect waves-light">
                <a href="#" :title="$i18n('deleteComponent')" @click.stop.prevent="onDelete">
                  <i class="material-icons">delete</i>
                </a>
              </li>
            </ul>
          </div>
        </div>
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
          @load="onIframeLoaded"/>
    </template>
  </div>
</template>

<script>
  export default {
    props: ['model'],

    data() {
      return {
        editableVisible: false,
        editableClass: null,
        selectedComponent: null,
        selectedComponentDragable: true,
        clipboard: null,
        ctrlDown: false,
        scrollTop: 0,
        isTouch: false,
        isIOS: false,
        editableTimer: null,
        iframe: {
          loaded: false
        }
      }
    },
    computed: {
      isComponentSelected() {
        return this.selected && this.selected.path && this.selected.path !== '/jcr:content'
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
        const targetEl = this.selectedComponent
        if (!!targetEl) {
          return false
        }
        const path = targetEl.getAttribute('data-per-path')
        if (path === undefined || path === null) {
          return false
        }
        const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path)
        if (!node) {
          return false
        }
        return !node.fromTemplate
      },
    },
    watch: {
      viewMode(newViewMode) {
        this.setIframeScrollState(newViewMode)
      }
    },
    mounted() {
      this.$nextTick(() => {
        /* is this a touch device */
        this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
        this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
        if (this.isTouch) {
          /* selected components are not immediatly draggable on touch devices */
          this.selectedComponentDragable = false
        }
        document.addEventListener('keydown', this.onKeyDown)
        document.addEventListener('keyup', this.onKeyUp)

        /* check if page has loaded */
        const unwatch = $perAdminApp.getApp().$watch('pageView', pageView => {
          if (pageView.status === 'loaded') {
            this.updateOverlay()
            unwatch() // we dont need to watch the pageView prop anymore
          }
        }, {deep: true})
      })
    },
    methods: {
      /* Window/Document methods =================
      ============================================ */
      onKeyDown(ev) {
        const nodeName = document.activeElement.nodeName
        const className = '' + document.activeElement.className
        /* check no field is currently in focus */
        if (nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className.startsWith('trumbowyg')) {
          return false
        } else {
          const ctrlKey = 17
          const cmdKey = 91
          if (ev.keyCode === ctrlKey || ev.keyCode === cmdKey) {
            this.ctrlDown = true
          }
          if (this.selectedComponent !== null) {
            const cKey = 67
            const vKey = 86
            if (this.ctrlDown && (ev.keyCode === cKey)) {
              this.onCopy()
            }
            if (this.ctrlDown && (ev.keyCode === vKey)) {
              this.onPaste()
            }
          }
        }
      },

      onKeyUp(ev) {
        const nodeName = document.activeElement.nodeName
        const className = document.activeElement.className
        /* check no field is currently in focus */
        if (nodeName === 'INPUT' || nodeName === 'TEXTAREA' || className === 'ql-editor') {
          return false
        } else {
          const ctrlKey = 17
          const cmdKey = 91
          if (ev.keyCode === ctrlKey || ev.keyCode === cmdKey) {
            this.ctrlDown = false
          }
        }
      },

      /* Iframe (editview) methods ===============
      ============================================ */
      onIframeLoaded(ev) {
        this.iframe.loaded = true
        const iframeDoc = ev.target.contentWindow.document
        this.setIframeScrollState(this.viewMode)
        iframeDoc.body.style.position = 'relative'
        const heightChangeObserver = new ResizeObserver(this.updateOverlay);
        heightChangeObserver.observe(iframeDoc.body);

      },

      setIframeScrollState(viewMode) {
        const iframeDoc = this.$refs.editview.contentWindow.document
        if (viewMode.endsWith('preview')) {
          iframeDoc.body.style.overflowX = 'hidden'
          iframeDoc.body.style.overflowY = 'auto'
        } else {
          iframeDoc.body.style.overflowX = 'hidden'
          iframeDoc.body.style.overflowY = 'auto'
        }
      },

      updateOverlay() {
        this.$nextTick(() => {
          /* ensure edit container height === iframe body height */
          this.setEditContainerHeight()
          /* update editable position if selected */
          if (this.selectedComponent !== null) {
            const targetBox = this.getBoundingClientRect(this.selectedComponent)
            this.setEditableStyle(targetBox, 'selected')
          }
        })
      },

      /*  Overlay (editviewoverlay) methods ======
      ============================================ */
      onScrollOverlay(ev) {
        this.scrollTop = ev.target.scrollTop
        const editview = this.$refs.editview
        if (this.isIOS) {
          /* ios device, use scroll alternative */
          this.$nextTick(() => {
            editview.contentWindow.document.body.style.transform = `translateY(-${this.scrollTop}px)`
          })
        } else {
          /* is not IOS device, scroll normally */
          this.$nextTick(() => {
            editview.contentWindow.scrollTo(0, this.scrollTop)
          })
        }
      },

      setEditContainerHeight() {
        // make sure the iframe exists before we actually try to read it
        if (this.$refs.editview) {
          const iframeHeight = this.$refs.editview.contentWindow.document.body.offsetHeight
          this.$refs.editviewContainer.style.height = iframeHeight + 'px'
        }
      },

      getPosFromMouse(e) {
        const elRect = this.getBoundingClientRect(this.$refs.editview)
        if (e) {
          const posX = e.clientX - elRect.left
          const posY = e.clientY - elRect.top
          return {x: posX, y: posY}
        } else {
          // fix for case where mouse event is not defined
          return {x: -elRect.left, y: -elRect.top}
        }
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
        let rect = e.getBoundingClientRect()
        let marginTop = parseFloat(this.getElementStyle(e, 'margin-top'))
        let marginLeft = parseFloat(this.getElementStyle(e, 'margin-left'))
        let marginRight = parseFloat(this.getElementStyle(e, 'margin-right'))
        let marginBottom = parseFloat(this.getElementStyle(e, 'margin-bottom'))
        let newRect = {
          left: rect.left - marginLeft,
          right: rect.right + marginRight,
          top: rect.top - marginTop,
          bottom: rect.bottom + marginBottom,
        }
        newRect.width = newRect.right - newRect.left
        newRect.height = newRect.bottom - newRect.top
        return newRect;
      },

      findIn(el, pos) {
        if (!el) return

        const rect = this.getBoundingClientRect(el)
        let ret = null
        if (pos.x > rect.left && pos.x < rect.right && pos.y > rect.top && pos.y < rect.bottom) {
          ret = el
          for (let i = 0; i < el.children.length; i++) {
            const child = this.findIn(el.children[i], pos)
            if (child != null) {
              ret = child
              break
            }
          }
        }
        return ret
      },

      getTargetEl(e) {
        const pos = this.getPosFromMouse(e)
        const editview = this.$refs.editview
        let targetEl = this.findIn(editview.contentWindow.document.body, pos)

        if (!targetEl) return

        while (!targetEl.getAttribute('data-per-path')) {
          targetEl = targetEl.parentElement
          if (!targetEl) {
            break
          }
        }
        return targetEl
      },

      onClickOverlay(e) {
        if (!e) return

        const targetEl = this.getTargetEl(e)
        if (targetEl) {
          const path = targetEl.getAttribute('data-per-path')
          const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path)
          if (node.fromTemplate) {
            $perAdminApp.notifyUser(this.$i18n('templateComponent'),
                this.$i18n('fromTemplateNotifyMsg'), {
                  complete: this.removeEditOverlay
                })
          } else {
            this.selectedComponent = targetEl
            const targetBox = this.getBoundingClientRect(targetEl)
            this.setEditableStyle(targetBox, 'selected')
            $perAdminApp.action(this, 'showComponentEdit', path)
          }
        }
      },

      leftOverlayArea(e) {
        if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

        // check if we only left the area into the overlay for the actions
        const targetEl = this.getTargetEl(e)
        if (targetEl) return
        this.removeEditOverlay()
      },

      removeEditOverlay() {
        this.selectedComponent = null
        this.editableClass = null
        if (this.isTouch) {
          this.selectedComponentDragable = false
        }
      },

      mouseMove(e) {
        if (!e || this.isTouch) return
        if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

        let targetEl = this.getTargetEl(e)
        if (targetEl) {
          if (targetEl.getAttribute('data-per-droptarget')) {
            targetEl = targetEl.parentElement
          }
          this.selectedComponent = targetEl
          const targetBox = this.getBoundingClientRect(targetEl)
          this.setEditableStyle(targetBox, 'selected')
        }
      },

      /* Drag and Drop ===========================
      ============================================ */
      onDragStart(ev) {
        if (this.selectedComponent === null) return

        this.editableClass = 'dragging'
        ev.dataTransfer.setData('text', this.selectedComponent.getAttribute('data-per-path'))
      },

      onDragOver(ev) {
        ev.preventDefault()
        const targetEl = this.getTargetEl(ev)

        if (targetEl) {
          const pos = this.getPosFromMouse(ev)
          const targetBox = this.getBoundingClientRect(targetEl)
          const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'

          const isRoot = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page,
              targetEl.getAttribute('data-per-path')).fromTemplate === true

          if (isDropTarget) {
            const dropLocation = targetEl.getAttribute('data-per-location')
            if (targetBox.bottom - pos.y < 10 && dropLocation === 'after' && !isRoot) {
              this.dropPosition = 'after'
              this.setEditableStyle(targetBox, 'drop-bottom')
            } else if (pos.y - targetBox.top < 10 && dropLocation === 'before' && !isRoot) {
              this.dropPosition = 'before'
              this.setEditableStyle(targetBox, 'drop-top')
            } else if (dropLocation) {
              this.dropPosition = 'into-' + dropLocation
              this.setEditableStyle(targetBox, 'selected')
            } else {
              this.dropPosition = 'none'
              this.leftOverlayArea()
            }
          } else if (!isRoot) {
            const y = pos.y - targetBox.top
            if (y < targetBox.height / 2) {
              this.dropPosition = 'before'
              this.setEditableStyle(targetBox, 'drop-top')
            } else {
              this.dropPosition = 'after'
              this.setEditableStyle(targetBox, 'drop-bottom')
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

      onDrop(ev) {
        this.editableClass = null
        if (this.isTouch) {
          this.selectedComponentDragable = false
        }
        const targetEl = this.getTargetEl(ev)
        if (typeof targetEl === 'undefined' || targetEl === null) {
          return false
        }
        const targetPath = targetEl.getAttribute('data-per-path');
        const componentPath = ev.dataTransfer.getData('text')
        if (targetPath === componentPath) {
          ev.dataTransfer.clearData('text')
          return false
        }
        const view = $perAdminApp.getView()
        const payload = {
          pagePath: view.pageView.path,
          path: targetEl.getAttribute('data-per-path'),
          component: componentPath,
          drop: this.dropPosition
        }
        let addOrMove
        if (componentPath.includes('/components/')) {
          addOrMove = 'addComponentToPath';
        } else {
          addOrMove = 'moveComponentToPath';
          const targetNode = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page,
              targetPath)
          if ((!targetNode) || (targetNode.fromTemplate)) {
            $perAdminApp.notifyUser('template component',
                'You cannot drag a component into a template section', {
                  complete: this.removeEditOverlay
                })
            return false;
          }
        }
        $perAdminApp.stateAction(addOrMove, payload)
        ev.dataTransfer.clearData('text')
      },

      /* Editable methods ========================
      ============================================ */
      onEditableTouchStart(ev) {
        this.editableTimer = setTimeout(this.onLongTouchOverlay, 800)
      },
      onEditableTouchEnd(ev) {
        clearTimeout(this.editableTimer)
      },
      onLongTouchOverlay() {
        if (this.selectedComponent === null) return

        this.selectedComponentDragable = true
        this.editableClass = 'draggable'
      },
      setEditableStyle(targetBox, editableClass) {
        const editable = this.$refs.editable
        const editview = this.$refs.editview
        const scrollY = editview ? editview.contentWindow.scrollY : 0
        const scrollX = editview ? editview.contentWindow.scrollX : 0
        if (editable) {
          editable.style.top = (targetBox.top + scrollY + (this.isIOS ? this.scrollTop : 0)) + 'px'
          editable.style.left = (targetBox.left + scrollX) + 'px'
          editable.style.width = targetBox.width + 'px'
          editable.style.height = targetBox.height + 'px'
          if (this.selectedComponent) {
            const path = this.selectedComponent.getAttribute('data-per-path')
            const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path)
            if (node && node.fromTemplate) {
              editable.style['border-color'] = 'orange'
            } else {
              editable.style['border-color'] = ''
            }
          } else {
            editable.style['border-color'] = ''
          }
        }
        this.editableClass = editableClass
      },

      updateEditablePos(top) {
        this.$refs.editable.style.top = top + 'px'
      },

      onDelete(e) {
        const targetEl = this.selectedComponent
        const view = $perAdminApp.getView()
        const pagePath = view.pageView.path
        const payload = {
          pagePath: view.pageView.path,
          path: targetEl.getAttribute('data-per-path')
        }
        if (payload.path !== '/jcr:content') {
          $perAdminApp.stateAction('deletePageNode', payload)
        }
        this.editableClass = null
        this.selectedComponent = null
      },

      onCopy(e) {
        const targetEl = this.selectedComponent
        this.clipboard = $perAdminApp.findNodeFromPath(
            $perAdminApp.getView().pageView.page,
            targetEl.getAttribute('data-per-path')
        )
      },

      onPaste(e) {
        const targetEl = this.selectedComponent
        const nodeFromClipboard = this.clipboard
        const view = $perAdminApp.getView()
        const isDropTarget = targetEl.getAttribute('data-per-droptarget') === 'true'
        let dropPosition
        isDropTarget ? dropPosition = 'into' : dropPosition = 'after'
        const payload = {
          pagePath: view.pageView.path,
          data: nodeFromClipboard,
          path: targetEl.getAttribute('data-per-path'),
          drop: dropPosition
        }
        $perAdminApp.stateAction('addComponentToPath', payload)
      },
      refreshEditor(me, target) {
        me.$refs['editview'].contentWindow.location.reload();
      },
      isContainer(el) {
        if (el && el.getAttribute('data-per-droptarget')) {
          return true;
        }
        let subEl = el.firstElementChild;
        if (!subEl) {
          return false;
        }
        while (!subEl.getAttribute('data-per-path')) {
          subEl = subEl.firstElementChild;
          if (!subEl) {
            return false;
          }
        }
        return !!subEl.getAttribute('data-per-droptarget');

      }
    }
  }
</script>

