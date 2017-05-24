import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('util').setDebugLevel()

import { DATA_EXTENSION, COMPONENT_PREFIX } from './constants.js'

export function pathToPathInfo(path) {

}

/**
 *
 * this method will try to add .data.json to the end of the path (if it was .html, otherwise
 * we just try to add .data.json
 *
 * @param path
 * @returns {*}
 */
export function pagePathToDataPath(path) {

    log.fine('converting',path,'to dataPath')
    var firstHtmlExt = path.indexOf('.html')
    var res = null
    if(firstHtmlExt >= 0) {
        var pathNoExt = path.substring(0,firstHtmlExt)
        res = pathNoExt + DATA_EXTENSION
    }
    else {
        res = path + DATA_EXTENSION
    }
    log.fine('result',res)
    return res

}

/**
 *
 * converts a dash name (admin-component-base) to a camel case style name with a prefix 'cmp' (cmpAdminComponentBase)
 *
 * @param name
 */
export function componentNameToVarName(name) {

    var segments = name.split('-')
    for(var i = 0; i < segments.length; i++) {
        segments[i] = segments[i].charAt(0).toUpperCase() + segments[i].slice(1)
    }
    return COMPONENT_PREFIX + segments.join('')

}