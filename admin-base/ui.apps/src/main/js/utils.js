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