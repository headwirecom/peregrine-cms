import { LoggerFactory } from './logger'
import PeregrineApi from './api'
let filename = 'perAdminApp'
let logger = LoggerFactory.logger(filename).setLevelInfo()

let view = null
let api = null

class PerAdminApp {

    constructor(perAdminAppView) {
        view = perAdminAppView
    }

    getLogger(name) {
        if(!name) return logger
        logger.debug('getting logger for',name)
        return LoggerFactory.logger(name)
    }

    setApi(backend) {
        if(!api) {
            api = new PeregrineApi(backend)
        }
        return api
    }

    getApi() {
        return api
    }

    getView() {
        return view
    }

}

export default PerAdminApp
