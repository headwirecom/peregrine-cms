/*-
 * #%L
 * peregrine vuejs page renderer - UI Apps
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
import {LoggerFactory} from './logger.js'

let log = LoggerFactory.logger('state').setDebugLevel()

function getClickable(node) {
    while(node.nodeName.toString() !== 'A') {
        node = node.parentNode
        if(!node) return null
    }
    return node
}

function getContentviewEditorActive() {
    if (window.parent && window.parent.$perAdminApp) {
        const view = window.parent.$perAdminApp.getView()
        if (view && view.state && view.state.contentview
            && view.state.contentview.editor) {
            return view.state.contentview.editor.active
        } else {
            return false
        }
    }
}

window.onclick = function(ev) {

    var currentServer = window.location.protocol+'//'+window.location.host+'/'
    log.fine("LOCATION: " , window.location.host);
    log.fine("NODENAME: " , ev.target.nodeName.toString());
    var node = getClickable(ev.target)

    if(node) {
        var toUrl = node.href
        log.fine("onClick() - "+ toUrl);
        log.fine(toUrl, currentServer)

        if(!(
            toUrl.startsWith('http') ||
            toUrl.startsWith('/') ||
            toUrl.startsWith('#')
        )) {
            return true
        }

        if(toUrl.startsWith("#")) {
            // do nothing, it's an internal page reference
        } else if (getContentviewEditorActive()) {
            // do nothing, editor is open/active
            return false
        } else {
            //Dont' load new content for an href on the same page
            let currentUrl = window.location.href.replace(/\#\w+$/, '')
            if(toUrl.startsWith( currentUrl ) && toUrl.match(/\#\w+$/)) return true;
            if(toUrl.startsWith(currentServer)) {
                ev.preventDefault()
                var gotoPath = '/'+toUrl.slice(currentServer.length)
                log.fine("gotoPath : " + gotoPath);
                $peregrineApp.loadContent(gotoPath, false)
                return false
            }
        }
    }
}

window.onpopstate = function(e){
    if(e.state) {
        log.fine("ONPOPSTATE : " + e.state.path);
        $peregrineApp.loadContent(e.state.path, false, true)
    } else {
        log.warn(e);
        $peregrineApp.loadContent((document.location? document.location.href : null), false, true)
    }
}



export default {

    initBrowserStateHandler: function() {
        log.fine('this should init our state handler')
    }

}
