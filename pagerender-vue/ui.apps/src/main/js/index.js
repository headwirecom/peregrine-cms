import logger from './logger.js'
var log = logger.logger('peregrineApp').setDebugLevel()

import $peregrineApp from './peregrineApp.js'

var $perView = {};
$peregrineApp.registerView($perView)
$peregrineApp.loadContent('/content/sites/example.html')

log.debug('peregrine rendered loaded')
$perView.loaded = true

