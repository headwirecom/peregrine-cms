/*-
 * #%L
 * admin base - UI Apps
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */
import { LoggerFactory } from './logger'

let log = LoggerFactory.logger('rendererBridge').setLevelDebug()

/**
 * Bridge between the author interface and the rendered site (edit view iframe).
 *
 * The editor always talks to the renderer through this bridge, never directly.
 * Two transports implement the same interface:
 *
 * - LegacyTransport: the historical protocol. Same-origin direct window access
 *   ($peregrineApp on the iframe window, shared reactive pageView object).
 *   Live preview updates happen implicitly through Vue 2 reactivity, so the
 *   model notification hooks are no-ops.
 *
 * - PostMessageTransport: the versioned renderer protocol (see
 *   renderer-protocol/spec/PROTOCOL.md). JSON messages over postMessage with
 *   capability negotiation. Model changes are sent explicitly.
 *
 * Transport selection is handshake based: a renderer that speaks the new
 * protocol announces itself with 'renderer:ready' after it boots. Until (and
 * unless) that message arrives, the bridge stays on the legacy transport, so
 * old renderers keep working unchanged.
 */

const PROTOCOL_VERSION = '1.0'

// message types the renderer may send us (the protocol has no envelope
// marker, so we filter inbound traffic by source window + known type)
const RENDERER_MESSAGE_TYPES = [
    'renderer:ready',
    'inline:edit',
    'component:click',
    'component:hover',
    'component:refreshed',
    'component:refresh-failed',
    'component:dragover',
    'component:drop',
    'scroll'
]

const EDITOR_CAPABILITIES = {
    dialogEditing: true,
    dragDrop: true,
    addComponent: true,
    deleteComponent: true,
    inlineEdit: true
}

class LegacyTransport {

    constructor(iframe) {
        this.name = 'legacy'
        this.iframe = iframe
        this.capabilities = {}
    }

    get win() {
        return this.iframe ? this.iframe.contentWindow : null
    }

    get app() {
        try {
            return this.win ? this.win.$peregrineApp : null
        } catch (error) {
            log.warn('edit view is not same-origin, legacy transport unavailable')
            return null
        }
    }

    loadContent(path) {
        if (this.app) this.app.loadContent(path)
    }

    loadComponent(name) {
        if (this.app) this.app.loadComponent(name)
    }

    reload() {
        if (this.win) this.win.location.reload()
    }

    // the legacy protocol shares the reactive pageView object with the
    // renderer, so the preview updates without any explicit notification
    pageLoaded(path, page) {}
    modelChanged(path, node) {}
    inlineEditStart(path, field) {}
    inlineEditEnd(path, field) {}
    setMode(mode) {}
}

class PostMessageTransport {

    constructor(iframe, rendererInfo) {
        this.name = 'postMessage'
        this.iframe = iframe
        this.rendererInfo = rendererInfo || {}
        this.capabilities = this.rendererInfo.capabilities || {}
    }

    get win() {
        return this.iframe ? this.iframe.contentWindow : null
    }

    send(type, payload) {
        if (!this.win) return
        // flat message envelope per the protocol spec: { type, ...payload }
        const message = { type: type }
        if (payload) {
            Object.keys(payload).forEach(function(key) {
                message[key] = payload[key]
            })
        }
        this.win.postMessage(message, window.location.origin)
    }

    loadContent(path) {
        this.send('page:load', { path: path })
    }

    loadComponent(name) {
        // the renderer owns its component registry in the new protocol
    }

    reload() {
        this.send('page:reload')
    }

    pageLoaded(path, page) {
        // include the full page model so protocol renderers are fully
        // message driven and never need to reach into the parent window
        let model = page
        if (!model && window.$perAdminApp) {
            model = window.$perAdminApp.getNodeFromViewOrNull('/pageView/page')
        }
        this.send('page:update', {
            path: path,
            page: model ? JSON.parse(JSON.stringify(model)) : null
        })
    }

    modelChanged(path, node) {
        if (path && node) {
            // strip Vue reactivity wrappers so postMessage can clone the node
            this.send('component:update', {
                path: path,
                data: JSON.parse(JSON.stringify(node))
            })
        } else {
            this.send('page:update', { path: path })
        }
    }

    inlineEditStart(path, field) {
        this.send('inline:edit-start', { path: path, field: field })
    }

    inlineEditEnd(path, field) {
        this.send('inline:edit-end', { path: path, field: field })
    }

    setMode(mode) {
        this.send('mode:change', { mode: mode })
    }
}

const bridge = {

    iframe: null,
    transport: null,

    /**
     * Attach the bridge to the edit view iframe. Called every time the iframe
     * (re)loads. Resets to the legacy transport; a new-protocol renderer will
     * upgrade the transport by sending 'renderer:ready'.
     */
    attach(iframe) {
        this.iframe = iframe
        this.transport = new LegacyTransport(iframe)
        log.fine('bridge attached, transport:', this.transport.name)
        // probe: a protocol renderer may have sent renderer:ready before we
        // attached (its scripts run before the iframe load event). Sending
        // admin:ready prompts it to announce itself again; legacy renderers
        // have no message listener and simply ignore this.
        try {
            iframe.contentWindow.postMessage(
                { type: 'admin:ready', capabilities: EDITOR_CAPABILITIES },
                window.location.origin)
        } catch (error) {
            // iframe not ready or cross-origin; the renderer-initiated
            // handshake still applies
        }
    },

    detach() {
        this.iframe = null
        this.transport = null
    },

    isPostMessage() {
        return this.transport && this.transport.name === 'postMessage'
    },

    rendererCapability(name) {
        return this.transport && this.transport.capabilities
            ? this.transport.capabilities[name] === true
            : false
    },

    loadContent(path) {
        if (this.transport) this.transport.loadContent(path)
    },

    loadComponent(name) {
        if (this.transport) this.transport.loadComponent(name)
    },

    reload() {
        if (this.transport) this.transport.reload()
    },

    pageLoaded(path, page) {
        if (this.transport) this.transport.pageLoaded(path, page)
    },

    /**
     * Single funnel for "the page model was mutated by the editor".
     * Legacy: no-op (shared object reactivity re-renders the preview).
     * PostMessage: emits component:update / page:update.
     */
    modelChanged(path, node) {
        if (this.transport) this.transport.modelChanged(path, node)
    },

    inlineEditStart(path, field) {
        if (this.transport) this.transport.inlineEditStart(path, field)
    },

    inlineEditEnd(path, field) {
        if (this.transport) this.transport.inlineEditEnd(path, field)
    },

    setMode(mode) {
        if (this.transport) this.transport.setMode(mode)
    },

    /**
     * Renderer -> editor messages (new protocol only).
     */
    handleRendererMessage(msg, event) {
        switch (msg.type) {
            case 'component:click':
                if (msg.path && window.$perAdminApp) {
                    window.$perAdminApp.stateAction('editComponent', msg.path)
                }
                break
            case 'inline:edit':
                // inline editing mechanics stay in the editor for now (the
                // editor manipulates the contenteditable elements directly);
                // renderers that push inline:edit are acknowledged but the
                // authoritative flow remains editor driven
                log.fine('inline:edit from renderer', msg)
                break
            case 'component:refreshed':
            case 'component:refresh-failed':
            case 'component:hover':
            case 'scroll':
                log.fine('renderer message', msg.type, msg)
                break
            default:
                // per spec: unknown message types are ignored
                log.fine('ignoring unknown renderer message type', msg.type)
        }
    }
}

// handshake listener: a new-protocol renderer sends 'renderer:ready' once its
// edit runtime has booted; that upgrades the transport for the edit view
window.addEventListener('message', function(event) {
    if (event.origin !== window.location.origin) return
    const msg = event.data
    if (!msg || typeof msg.type !== 'string') return
    if (RENDERER_MESSAGE_TYPES.indexOf(msg.type) < 0) return
    if (!bridge.iframe || event.source !== bridge.iframe.contentWindow) return

    if (msg.type === 'renderer:ready') {
        const major = String(msg.protocolVersion || '').split('.')[0]
        if (major !== PROTOCOL_VERSION.split('.')[0]) {
            log.warn('renderer protocol major version mismatch, staying on legacy transport:',
                msg.protocolVersion, 'vs', PROTOCOL_VERSION)
            return
        }
        // renderers answer EVERY admin:ready probe with renderer:ready (their
        // boot announce can be consumed before attach() resets the transport,
        // so a single answer could leave us stranded on legacy). When we are
        // already on postMessage just refresh the capabilities and do NOT
        // reply with another admin:ready - that reply is what would loop.
        const alreadyUpgraded = bridge.transport && bridge.transport.name === 'postMessage'
        bridge.transport = new PostMessageTransport(bridge.iframe, msg)
        if (!alreadyUpgraded) {
            bridge.transport.send('admin:ready', { capabilities: EDITOR_CAPABILITIES })
            log.info('renderer speaks protocol', msg.protocolVersion,
                '(' + (msg.framework || 'unknown framework') + ') - upgraded to postMessage transport')
        }
        return
    }

    bridge.handleRendererMessage(msg, event)
})

window.$rendererBridge = bridge

export default bridge
