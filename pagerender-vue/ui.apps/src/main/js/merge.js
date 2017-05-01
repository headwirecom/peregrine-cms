import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('merge').setDebugLevel()

export default function (obj1, obj2) {

    for (var p in obj2) {
        try {
            // Property in destination object set; update its value.
            if ( typeof obj2[p] === 'object' ) {
                if(obj2[p] instanceof Array) {
                    for(var pos = 0; pos < obj2[p].length; pos++) {
                        if(typeof obj2[p][pos] === 'object') {
                            var found = false
                            var path = obj2[p][pos].path
                            for(var i = 0; i < obj1[p].length; i++) {
                                if(obj1[p][i].path === path) {
                                    obj1[p][i] = mergeRecursive(obj1[p][i], obj2[p][pos])
                                    break
                                }
                            }
                            if(!found) {
                                obj1[p].push(obj2[p][pos])
                            }
                        }
                    }
                } else {
                    obj1[p] = mergeRecursive(obj1[p], obj2[p]);
                }
            } else {
                obj1[p] = obj2[p];

            }

        } catch(e) {
            // Property in destination object not set; create it and set its value.
            obj1[p] = obj2[p];

        }
    }

    return obj1

}
