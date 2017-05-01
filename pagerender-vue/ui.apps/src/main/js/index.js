import { LoggerFactory } from './logger.js'
let log = LoggerFactory.logger('peregrineApp').setDebugLevel()

import $peregrineApp from './peregrineApp.js'

var $perView = {};
$peregrineApp.registerView($perView)
$peregrineApp.loadContent('/content/sites/example.html')

log.debug('peregrine rendered loaded')
$perView.loaded = true

