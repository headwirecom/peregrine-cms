
var loggers = [];

const DEBUG_LEVELS = {
    ALL: 0,
    DEBUG: 4
};

class Logger {

    constructor(name) {
        this.name = name
    }

    setDebugLevel() {
        this.level = DEBUG_LEVELS.DEBUG
        return this
    }

    debug() {
        console.log(arguments)
    }

    error() {
        console.log(arguments)
    }

}

function getLogger(name) {
    return new Logger(name)
}

export default {

    logger: function(name) {
        return getLogger(name)
    }

}