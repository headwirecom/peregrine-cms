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
import {LoggerFactory} from './logger'
import {DATA_EXTENSION, SUFFIX_PARAM_SEPARATOR} from './constants.js'

let logger = LoggerFactory.logger('utils').setLevelDebug()

export function makePathInfo(path) {

    logger.fine('makePathInfo for path', path)
    var htmlPos = path.indexOf('.html')
    var pathPart = path
    var suffixPath = ''
    if(htmlPos >= 0) {
        suffixPath = path.slice(htmlPos)
        pathPart = path.slice(0, htmlPos+5)
    }

    var suffixParams = {}
    if(suffixPath.length > 0) {
        suffixPath = suffixPath.slice(6)
        var suffixParamList = suffixPath.split(SUFFIX_PARAM_SEPARATOR)
        for(var i = 0; i < suffixParamList.length; i+= 2) {
            suffixParams[suffixParamList[i]] = suffixParamList[i+1]
        }
    }

    var ret = { path: pathPart, suffix: suffixPath , suffixParams: suffixParams }
    logger.fine('makePathInfo res:',ret)
    return ret
}

export function pagePathToDataPath(path) {

    logger.fine('converting',path,'to dataPath')
    var firstHtmlExt = path.indexOf('.html')
    var res = null
    if(firstHtmlExt >= 0) {
        var pathNoExt = path.substring(0,firstHtmlExt)
        res = pathNoExt + DATA_EXTENSION
    }
    else {
        res = path + DATA_EXTENSION
    }
    logger.fine('result',res)
    return res

}

export function set(node, path, value) {

    var vue = $perAdminApp.getApp()
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(vue) {
                Vue.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(vue) {
        Vue.set(node, path[0], value)
    }
    else {
        node[path[0]] = value
    }
}

export function get(node, path, value) {

    var vue = $perAdminApp.getApp()
    path = path.slice(1).split('/').reverse()
    while(path.length > 1) {
        var segment = path.pop()
        if(!node[segment]) {
            if(vue) {
                Vue.set(node, segment, {})
            } else {
                node[segment] = {}
            }
        }
        node = node[segment]
    }
    if(value !== undefined && !node[path[0]]) {
        if(vue) {
            Vue.set(node, path[0], value)
        } else {
            node[path[0]] = value
        }
    }
    return node[path[0]]
}

export function parentPath(path) {
    logger.fine('parentPath()',path)
    let segments = path.split('/')
    let name = segments.pop()
    let parentPath = segments.join('/')
    let ret ={ parentPath: parentPath, name: name }
    logger.fine('parentPath() res:',ret)
    return ret
}

export function stripNulls(data) {
    for(var key in data) {
        if(data[key] === null) delete data[key]
        if(typeof data[key] === 'object') {
            stripNulls(data[key])
        }
    }
}

export function jsonEqualizer(name, value) {
    if(value === null || value === undefined || value.length === 0) {
        return undefined
    }
    return value
}

export const deepClone = (obj) => {
    return JSON.parse(JSON.stringify(obj))
}

export const capitalizeFirstLetter = (str) => {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

export const getCaretCharacterOffsetWithin = (element)  => {
    let caretOffset = 0
    const doc = element.ownerDocument || element.document
    const win = doc.defaultView || doc.parentWindow
    let sel
    if (typeof win.getSelection != 'undefined') {
        sel = win.getSelection()
        if (sel.rangeCount > 0) {
            const range = win.getSelection().getRangeAt(0)
            const preCaretRange = range.cloneRange()
            preCaretRange.selectNodeContents(element)
            preCaretRange.setEnd(range.endContainer, range.endOffset)
            caretOffset = preCaretRange.toString().length
        }
    } else if ( (sel = doc.selection) && sel.type !== 'Control') {
        const textRange = sel.createRange()
        const preCaretTextRange = doc.body.createTextRange()
        preCaretTextRange.moveToElementText(element)
        preCaretTextRange.setEndPoint('EndToEnd', textRange)
        caretOffset = preCaretTextRange.text.length
    }
    return caretOffset
}

export const saveSelection = (containerEl, document=document) => {
  const window = document.defaultView
  if (window.getSelection && document.createRange) {
    const range = window.getSelection().getRangeAt(0)
    const preSelectionRange = range.cloneRange()
    preSelectionRange.selectNodeContents(containerEl)
    preSelectionRange.setEnd(range.startContainer, range.startOffset)
    const start = preSelectionRange.toString().length

    return {
      start: start,
      end: start + range.toString().length
    }
  } else if (document.selection) {
    const selectedTextRange = document.selection.createRange()
    const preSelectionTextRange = document.body.createTextRange()
    preSelectionTextRange.moveToElementText(containerEl)
    preSelectionTextRange.setEndPoint('EndToStart', selectedTextRange)
    const start = preSelectionTextRange.text.length

    return {
      start: start,
      end: start + selectedTextRange.text.length
    }
  }
}

export const restoreSelection = (containerEl, savedSel, doc = document) => {
  const win = doc.defaultView

  if (win.getSelection && doc.createRange) {
    let charIndex = 0, range = doc.createRange()
    range.setStart(containerEl, 0)
    range.collapse(true)
    let nodeStack = [containerEl], node, foundStart = false, stop = false

    while (!stop && (node = nodeStack.pop())) {
      if (node.nodeType === 3) {
        const nextCharIndex = charIndex + node.length
        if (!foundStart && savedSel.start >= charIndex && savedSel.start
            <= nextCharIndex) {
          range.setStart(node, savedSel.start - charIndex)
          foundStart = true
        }
        if (foundStart && savedSel.end >= charIndex && savedSel.end
            <= nextCharIndex) {
          range.setEnd(node, savedSel.end - charIndex)
          stop = true
        }
        charIndex = nextCharIndex
      } else {
        let i = node.childNodes.length
        while (i--) {
          nodeStack.push(node.childNodes[i])
        }
      }
    }

    const sel = win.getSelection()
    sel.removeAllRanges()
    sel.addRange(range)
  } else if (doc.selection) {
    const textRange = doc.body.createTextRange()
    textRange.moveToElementText(containerEl)
    textRange.collapse(true)
    textRange.moveEnd('character', savedSel.end)
    textRange.moveStart('character', savedSel.start)
    textRange.select()
  }
}

export const getCurrentDateTime= () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const hour = now.getHours()
  const minute = now.getMinutes()
  const second = now.getSeconds()
  const milli = now.getMilliseconds()

  return `${year}-${month}-${day}_${hour}-${minute}-${second}-${milli}`
}

export function isChromeBrowser() {
  return /Chrome/.test(navigator.userAgent)
      && /Google Inc/.test(navigator.vendor)
}