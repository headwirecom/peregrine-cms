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
    <div :class   = "`peregrine-content-view ${viewModeClass}`"
        @mouseout = "leftOverlayArea">
        <div id           = "editviewoverlay"
            @click        = "onClickOverlay"
            @scroll       = "onScrollOverlay"
            @mousemove    = "mouseMove"
            @dragover     = "onDragOver"
            @drop.prevent = "onDrop">
            <div class="editview-container" ref="editviewContainer">
                <div id         = "editable"
                    ref         = "editable"
                    :class      = "editableClass"
                    :draggable  = "enableEditableFeatures"
                    @dragstart  = "onDragStart"
                    @touchstart = "onEditableTouchStart"
                    @touchend   = "onEditableTouchEnd">
                    <div id="inlineEditContainer"
                        v-show="inlineEditVisible"
                        @click.stop.prevent
                        @input.stop.prevent>
                        <div v-show="inline.isRich"
                            class="editor rich">
                            <trumbowyg :config="inline.config"
                                v-model="inline.content"
                                @input="updateInlineText">
                            </trumbowyg>
                        </div>
                        <div v-show="!inline.isRich"
                            class="editor simple">
                            <div ref="simpleInlineEdit"
                                v-html="inline.content"
                                contenteditable="true"
                                @input="onInlineSimpleEditInput">
                            </div>
                        </div>
                    </div>
                    <div v-if="enableEditableFeatures" class="editable-actions">
                        <ul>
                            <li class="waves-effect waves-light">
                              <a href="#" :title="$i18n('copy')"
                                 @click.stop.prevent="onCopy">
                                    <i class="material-icons">content_copy</i>
                                </a>
                            </li>
                            <li v-if="clipboard" class="waves-effect waves-light">
                              <a :title="$i18n('paste')" href="#"
                                 @click.stop.prevent="onPaste">
                                    <i class="material-icons">content_paste</i>
                                </a>
                            </li>
                            <li v-if="isComponentSelected" class="waves-effect waves-light">
                              <a href="#" :title="$i18n('deleteComponent')"
                                 @click.stop.prevent="onDelete">
                                    <i class="material-icons">delete</i>
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <iframe id = "editview"
            ref    = "editview"
            @load  = "onIframeLoaded"
            :src   = "pagePath"
            frameborder = "0"></iframe>
    </div>
</template>

<script>
import { IgnoreContainers } from '../../../../../../js/constants.js';

const KEY_ESC = 27
const KEY_CTRL = 17
const KEY_CMD = 91
const KEY_C = 67
const KEY_V = 86

function hasCtrlKey(e) {
    return e.keyCode == KEY_CTRL || e.keyCode == KEY_CMD
}

function isInputInFocus() {
    const node = document.activeElement;
    const nodeName = node.nodeName.toUpperCase()
    if (nodeName === 'INPUT' || nodeName === 'TEXTAREA') {
        return true
    }

    return node.getAttribute('contenteditable') === 'true'
}

function isClassInFocus(className) {
    return document.activeElement.className.toString().startsWith(className)
}

function isContainer(el) {
    let subEl = el;
    while (subEl && !subEl.getAttribute('data-per-path')) {
        subEl = subEl.firstElementChild;
    }

    if (subEl && subEl.getAttribute('data-per-droptarget')) {
        return true;
    }

    return false;
}

export default {
    mounted() {
        this.$nextTick(function() {
            /* is this a touch device */
            this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
            this.isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream
            if(this.isTouch) {
                /* selected components are not immediatly draggable on touch devices */
                this.selected.draggable = false
            }
            document.addEventListener('keydown', this.onKeyDown)
            document.addEventListener('keyup', this.onKeyUp)

            /* check if page has loaded */
            var unwatch = $perAdminApp.getApp().$watch('pageView', pageView => {
                if(pageView.status === 'loaded') {
                    this.updateOverlay()
                    unwatch() // we dont need to watch the pageView prop anymore
                }
            }, {deep: true })
        })
    },

    props: ['model'],

    data() {
        return {
            editableVisible: false,
            editableClass: null,
            selected: {
                el: null,
                path: null,
                draggable: true
            },
            clipboard: null,
            ctrlDown: false,
            scrollTop: 0,
            isTouch: false,
            isIOS: false,
            editableTimer: null,
            inline: {
                el: null,
                pathSegments: null,
                propertyName: null,
                isRich: false,
                content: null,
                config: {
                    svgPath: '/etc/felibs/admin/images/trumbowyg-icons.svg',
                    resetCss: false,
                    btnsDef: {
                        formattingWithCode: {
                            dropdown: ['p', 'quote', 'preformatted', 'h1', 'h2', 'h3', 'h4'],
                            ico: 'p',
                            hasIcon: true
                        }
                    },
                    btns: [
                        'viewHTML',
                        'undo',
                        'redo',
                        'formattingWithCode',
                        'strong',
                        'em',
                        'superscript',
                        'subscript',
                        'link',
                        'insertImage',
                        'justifyLeft',
                        'justifyCenter',
                        'justifyRight',
                        'justifyFull',
                        'unorderedList',
                        'orderedList',
                        'removeformat'
                    ]
                }
            }
        }
    },

    watch: {
        viewMode(newViewMode) {
            this.setIframeScrollState(newViewMode)
        }
    },

    computed: {
        isComponentSelected() {
            return this.selected.path && this.selected.path !== '/jcr:content'
        },

        pagePath() {
            return $perAdminApp.getNodeFromView('/pageView/path') + '.html'
        },

        viewMode() {
            const viewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/view')
            const previewMode = $perAdminApp.getNodeFromViewOrNull('/state/tools/workspace/preview')
            let ret = ''
            if(viewMode) {
                ret = viewMode + (previewMode ? ' ' + previewMode : '' )
            } else {
                ret = 'desktop' + (previewMode ? ' ' + previewMode : '' )
            }
            return ret
        },

        viewModeClass() {
            return this.viewMode
        },

        enableEditableFeatures() {
            const node = this.selectedNode
            return node && !node.fromTemplate
        },

        isIgnoreContainersEnabled() {
            const tools = $perAdminApp.getView().state.tools
            return tools
                && tools.workspace
                && tools.workspace.ignoreContainers === IgnoreContainers.ENABLED
        },

        editorVisible() {
            return $perAdminApp.getNodeFromViewOrNull('/state/editorVisible')
        },

        selectedNode() {
            if (this.selected.path) {
                return $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, this.selected.path)
            }
        },

        inlineNode() {
            const segments = this.inline.pathSegments
            if (!segments) {
                return
            }

            let node = this.selectedNode
            for (let i = 0; node && i < segments.length; i++) {
                node = node[segments[i]]
            }

            return node
        },

        inlineEditVisible() {
            return this.editorVisible && this.inlineNode && this.enableEditableFeatures
        }
    },

    methods: {
        updateInlineText(text) {
            const node = this.inlineNode
            if (node) {
                node[this.inline.propertyName] = text
                this.updateInlineStyle()
            }
        },

        onInlineSimpleEditInput(text) {
            this.updateInlineText(event.target.innerHTML)
        },

        /* Window/Document methods =================
        ============================================ */
        onKeyDown(ev) {
            if (isInputInFocus() || isClassInFocus('trumbowyg')) {
                return false
            }

            if (hasCtrlKey(ev)) {
                this.ctrlDown = true
            }
        },

        onKeyUp(ev) {
            if (isInputInFocus()) {
                if (ev.keyCode == KEY_ESC && this.inlineNode) {
                    this.clearInline()
                    this.editSelectedComponent()
                }

                return false
            }

            if (isClassInFocus('ql-editor')) {
                return false
            }

            if (hasCtrlKey(ev)) {
                this.ctrlDown = false
            } else if (this.selectedNode) {
                if (ev.keyCode == KEY_C) {
                    this.onCopy()
                } else if (ev.keyCode == KEY_V) {
                    this.onPaste()
                }
            }
        },

        /* Iframe (editview) methods ===============
        ============================================ */
        onIframeLoaded(ev) {
            const iframeDoc = ev.target.contentWindow.document
            this.setIframeScrollState(this.viewMode)
            iframeDoc.body.style.position = 'relative'

            const heightChangeObserver = new ResizeObserver(this.updateOverlay);
            heightChangeObserver.observe(iframeDoc.body);

        },

        setIframeScrollState(viewMode) {
            const iframeDoc = this.$refs.editview.contentWindow.document
            iframeDoc.body.style.overflowX = 'hidden'
            iframeDoc.body.style.overflowY = 'auto'
        },

        updateOverlay() {
            this.$nextTick(() => {
                /* ensure edit container height === iframe body height */
                this.setEditContainerHeight()
                /* update editable position if selected */
                this.setSelectedEditableStyle()
            })
        },

        /*  Overlay (editviewoverlay) methods ======
        ============================================ */
        onScrollOverlay(ev) {
            this.scrollTop = ev.target.scrollTop
            var editview = this.$refs.editview
            if(this.isIOS) {
                /* ios device, use scroll alternative */
                this.$nextTick(function() {
                    editview.contentWindow.document.body.style.transform = `translateY(-${this.scrollTop}px)`
                })
            } else {
                /* is not IOS device, scroll normally */
                this.$nextTick(function() {
                    editview.contentWindow.scrollTo(0, this.scrollTop)
                })
            }
        },

        setEditContainerHeight() {
            // make sure the iframe exists before we actually try to read it
            if(this.$refs.editview) {
                var iframeHeight = this.$refs.editview.contentWindow.document.body.offsetHeight
                this.$refs.editviewContainer.style.height = `${iframeHeight}px`
            }
        },

        getPosFromMouse(e) {
            var elRect = this.getBoundingClientRect(this.$refs.editview)
            if(e) {
                var posX = e.clientX - elRect.left
                var posY = e.clientY - elRect.top
                return {x: posX, y: posY }
            }
            else {
                // fix for case where mouse event is not defined
                return {x: -elRect.left, y: -elRect.top }
            }
        },

        getElementStyle(e, styleName) {
            if (document.defaultView && document.defaultView.getComputedStyle) {
                return document.defaultView.getComputedStyle(e, "").getPropertyValue(styleName);
            } 
            
            if (e.currentStyle) {
                const name = styleName.replace(/\-(\w)/g, function (strMatch, p1) {
                    return p1.toUpperCase();
                });
                return e.currentStyle[name];
            }

            return ''
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
            if(!el) return null
            var rect = this.getBoundingClientRect(el)
            var ret = null
            if(pos.x > rect.left && pos.x < rect.right && pos.y > rect.top && pos.y < rect.bottom) {
                ret = el
                for(var i = 0; i < el.children.length; i++) {
                    var child = this.findIn(el.children[i], pos)
                    if(child != null) {
                        ret = child
                        break
                    }
                }
            }
            return ret
        },

        getTarget(e) {
            const pos = this.getPosFromMouse(e)
            const editview = this.$refs.editview
            let el = this.findIn(editview.contentWindow.document.body, pos)
            if (!el) return

            let path, inlineEl, inlineProp
            while (el && !path) {
                path = el.getAttribute('data-per-path');
                if (!inlineEl) {
                    inlineProp = el.getAttribute('data-per-inline-property')
                    if (inlineProp){
                        inlineEl = el
                    }
                }

                if (!path) {
                    el = el.parentElement
                }
            }

            if (!el || !path) return

            const inline = inlineEl ? {
                el: inlineEl,
                property: inlineProp
            } : undefined
            return {
                el,
                path,
                node: $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, path),
                isDropTarget: el.getAttribute('data-per-droptarget') === 'true',
                inline
            }
        },

        onClickOverlay(e) {
            if (!e) return
            if (e.target && e.target.getAttribute('contenteditable') === 'true') return;
            const target = this.getTarget(e)
            if (!target) {
                return
            }

            const targetEl = target.el
            if (this.isIgnoreContainersEnabled && isContainer(targetEl)) return;

            const node = target.node
            if (!node || node.fromTemplate) {
                $perAdminApp.notifyUser(this.$i18n('templateComponent'), this.$i18n('fromTemplateNotifyMsg'), {
                    complete: this.removeEditOverlay
                })
            } else {
                const path = target.path
                this.selected.el = targetEl
                this.selected.path = path
                const inline = target.inline
                if (inline) {
                    this.inline.el = inline.el
                    this.inline.content = inline.el.innerHTML
                    this.inline.isRich = inline.el.getAttribute('data-per-inline-is-rich') === 'true'

                    const segments = inline.property.split('.')
                    this.inline.propertyName = segments.pop()
                    segments.shift()
                    this.inline.pathSegments = segments
                } else {
                    this.clearInline()
                }

                this.editSelectedComponent()

                if (inline) {
                    setTimeout(this.focusInline, 0);
                }
            }
        },

        focusInline() {
            if (this.inline.isRich) {
                $('#inlineEditContainer textarea').focus()
                $('#inlineEditContainer .trumbowyg-editor').focus()
            } else {
                this.$refs.simpleInlineEdit.focus()
            }
        },

        editSelectedComponent() {
            this.setSelectedEditableStyle()
            $perAdminApp.action(this, 'showComponentEdit', this.selected.path)
        },

        updateInlineStyle() {
            // copy styles from original element into this one
            const style = window.getComputedStyle(this.inline.el)
            let value = style.cssText.replace('-webkit-user-modify: read-only', '-webkit-user-modify: read-write')
            this.$refs.simpleInlineEdit.setAttribute('style', value)
            value = value.replace(/display.+?;/, '')
            $('#inlineEditContainer .trumbowyg-editor').attr('style', value)
            $('#inlineEditContainer textarea').attr('style', value)
        },

        leftOverlayArea(e) {
            if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return

            // check if we only left the area into the overlay for the actions
            if (!this.getTarget(e)) {
                this.removeEditOverlay()
            }
        },

        removeEditOverlay() {
            this.clearSelected()
            if (this.isTouch) this.selected.draggable = false
        },

        clearSelected() {
            this.selected.el = null
            this.selected.path = null
            
            this.editableClass = null
            
            this.clearInline()
        },

        clearInline() {
            this.inline.el = null
            this.inline.pathSegments = null
            this.inline.propertyName = null
            this.inline.isRich = false
        },

        mouseMove(e) {
            if (!e || this.isTouch) return
            if ($perAdminApp.getNodeFromViewOrNull('/state/editorVisible')) return
            const target = this.getTarget(e)
            if (!target) {
                return
            }

            let targetEl = target.el
            if (this.isIgnoreContainersEnabled && isContainer(targetEl)) return;

            if (targetEl.getAttribute('data-per-droptarget')) {
                this.selected.el = targetEl.parentElement
                this.selected.path = this.selected.el.getAttribute('data-per-path')
            } else {
                this.selected.el = target.el
                this.selected.path = target.path
            }

            this.setSelectedEditableStyle()
        },

        /* Drag and Drop ===========================
        ============================================ */
        onDragStart(ev) {
            if (!this.selected.path) return
            this.editableClass = 'dragging'
            ev.dataTransfer.setData('text', this.selected.path)
        },

        onDragOver(ev) {
            ev.preventDefault()
            const target = this.getTarget(ev)
            if (target) {
                const targetEl = target.el
                const pos = this.getPosFromMouse(ev)
                const targetBox = this.getBoundingClientRect(targetEl)
                const isRoot = target.node.fromTemplate === true
                if (target.isDropTarget) {
                    const dropLocation = targetEl.getAttribute('data-per-location')
                    if (targetBox.bottom - pos.y < 10 && dropLocation === 'after' && !isRoot) {
                        this.dropPosition = 'after'
                        this.setEditableStyle(targetBox, 'drop-bottom')
                    } else if (pos.y - targetBox.top < 10 && dropLocation === 'before' && !isRoot) {
                        this.dropPosition = 'before'
                        this.setEditableStyle(targetBox, 'drop-top')
                    } else if (dropLocation) {
                        this.dropPosition = `into-${dropLocation}`
                        this.setSelectedEditableStyle()
                    } else {
                        this.dropPosition = 'none'
                        this.leftOverlayArea()
                    }
                } else if (!isRoot) {                    
                    const y = pos.y - targetBox.top
                    if (2 * y < targetBox.height) {
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
            if (this.isTouch) this.selected.draggable = false
            const target = this.getTarget(ev)
            if (!target) {
                return false
            }

            const targetPath = target.path
            const componentPath = ev.dataTransfer.getData('text')

            if (targetPath === componentPath) {
                ev.dataTransfer.clearData('text')
                return false
            }

            let addOrMove
            if (componentPath.includes('/components/')) {
                addOrMove = 'addComponentToPath';
            } else {
                addOrMove = 'moveComponentToPath';
                const targetNode = target.node
                if (!targetNode || targetNode.fromTemplate) {
                    $perAdminApp.notifyUser('template component', 'You cannot drag a component into a template section', {
                        complete: this.removeEditOverlay
                    })
                    return false;
                }
            }

            const payload = {
                pagePath : $perAdminApp.getView().pageView.path,
                path: targetPath,
                component: componentPath,
                drop: this.dropPosition
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
            if (!this.selected.path) return
            this.selected.draggable = true
            this.editableClass = 'draggable'
        },

        setEditableStyle(targetBox, editableClass) {
            const editable = this.$refs.editable
            if (editable) {
                const editview = this.$refs.editview
                const scrollY  = editview ? editview.contentWindow.scrollY : 0
                const scrollX  = editview ? editview.contentWindow.scrollX : 0

                const style  = editable.style;
                style.top    = `${targetBox.top + scrollY + (this.isIOS ? this.scrollTop : 0)}px`
                style.left   = `${targetBox.left + scrollX}px`
                style.width  = `${targetBox.width}px`
                style.height = `${targetBox.height}px`

                let color = ''
                const node = this.selectedNode
                if (node && node.fromTemplate) {
                    color = 'orange'
                }

                style['border-color'] = color
            }
            
            this.editableClass = editableClass
        },

        setSelectedEditableStyle() {
            if (this.selected.path) {
                let target = this.selected.el;
                let editableClass = 'selected';
                if (this.inlineNode) {
                    this.updateInlineStyle()
                    target = this.inline.el
                    editableClass += ' no-border'
                }

                const targetBox = this.getBoundingClientRect(target)
                this.setEditableStyle(targetBox, editableClass)
            }
        },

        updateEditablePos(top) {
            this.$refs.editable.style.top = `${top}px`
        },

        onDelete(e) {
            if (this.isComponentSelected) {
                $perAdminApp.stateAction('deletePageNode',  {
                    pagePath: $perAdminApp.getView().pageView.path,
                    path: this.selected.path
                })
            }

            this.clearSelected()
        },

        onCopy(e) {
            this.clipboard = this.selectedNode
        },

        onPaste(e) {
            const payload = {
                pagePath: $perAdminApp.getView().pageView.path,
                data: this.clipboard,
                path: this.selected.path,
                drop: this.selected.isDropTarget ? 'into' : 'after'
            }
            $perAdminApp.stateAction('addComponentToPath', payload)
        },

        refreshEditor(me, target) {
            me.$refs['editview'].contentWindow.location.reload();
        }
    }
}
</script>

<style lang="scss">
    #inlineEditContainer {
        background-color: white;
        margin-top: -39px;

        .simple {
            margin-top: 39px;
        }

        .trumbowyg-box {
            margin: 0;
            overflow: hidden;
            min-height: unset;

            .trumbowyg-button-pane {
                z-index: 1;
            }

            .trumbowyg-editor {
                min-height: unset;

                p {
                    margin: unset;
                    padding: unset;
                }
            }
        }
    }
</style>