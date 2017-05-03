import { LoggerFactory } from './logger'
let logger = LoggerFactory.logger('utils').setLevelDebug()

import { DATA_EXTENSION, COMPONENT_PREFIX } from './constants.js'

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
        var suffixParamList = suffixPath.split('//')
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
    if(value && !node[path[0]]) {
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