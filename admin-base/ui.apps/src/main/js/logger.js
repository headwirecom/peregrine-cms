
/**
 * @enum {number}
 */
export const LogLevel = {
  OFF: 0,
  ERROR: 1,
  WARN: 2,
  INFO: 3,
  DEBUG: 4,
  FINE: 5,
};

let loggerFactoryInstance = null

export class Logger {

    constructor(name) {
        this.level = LogLevel.INFO
        this.name = name

        var displayName = `[${name}]`
        if(displayName.length > loggerFactoryInstance.getMaxNameLength()) {
            loggerFactoryInstance.setMaxNameLength(displayName.length)
        } else {
            displayName = displayName + repeatStr(' ',loggerFactoryInstance.getMaxNameLength() - displayName.length)
        }
        this.displayName = displayName
    }

    setLevel(level) {
        this.level = level
        return this
    }

    getDisplayName() {
        return this.displayName
    }

    setDisplayName(displayName) {
        this.displayName = displayName
    }

    setLevelOff() {
        return this.setLevel(LogLevel.OFF)
    }

    setLevelError() {
        return this.setLevel(LogLevel.ERROR)
    }

    setLevelWarn() {
        return this.setLevel(LogLevel.WARN)
    }

    setLevelInfo() {
        return this.setLevel(LogLevel.INFO)
    }

    setLevelDebug() {
        return this.setLevel(LogLevel.DEBUG)
    }

    setLevelFine() {
        return this.setLevel(LogLevel.FINE)
    }

    applyTo(method, level, args) {
        if(typeof args[0] === 'string') {
            args[0] = level + ' ' + this.displayName + ' ' + args[0]
            method.apply(this, args)
        } else {
            args.unshift(this.displayName);
            args.unshift(level)
            method.apply(this, args)
        }
    }

    fine() {
        if(this.level < LogLevel.FINE) return
        var args = Array.prototype.slice.call(arguments);
        this.applyTo(console.log, '[fine ]', args)
    }

    debug() {
        if(this.level < LogLevel.DEBUG) return
        var args = Array.prototype.slice.call(arguments);
        this.applyTo(console.log, '[debug]', args)
    }

    info() {
        if(this.level < LogLevel.INFO) return
        var args = Array.prototype.slice.call(arguments);
        this.applyTo(console.log, '[info ]', args)
    }

    warn() {
        if(this.level < LogLevel.WARN) return
        var args = Array.prototype.slice.call(arguments);
        this.applyTo(console.warn, '[warn ]', args)
    }

    error() {
        if(this.level < LogLevel.ERROR) return
        var args = Array.prototype.slice.call(arguments);
        this.applyTo(console.error, '[error]', args)
    }

}

function repeatStr(str, times) {
    let ret = ""
    for(let i = 0; i < times; i++) {
        ret += str
    }
    return ret
}

export class LoggerFactory {

    constructor() {
        if(!loggerFactoryInstance) {
            loggerFactoryInstance = this
            this.loggers = []
        }
        return loggerFactoryInstance
    }

    getMaxNameLength() {
        return this.maxNameLength ? this.maxNameLength : 0
    }

    setMaxNameLength(length) {
        this.maxNameLength = length
        for(let key in this.loggers) {
            var logger = this.loggers[key]
            var displayName = logger.getDisplayName()
            if(displayName.length < length) {
                logger.setDisplayName(displayName + repeatStr(' ',length - displayName.length))
            }
        }
    }

    static logger(name) {
        return new LoggerFactory().getLogger(name)
    }

    static getLoggers() {
        return new LoggerFactory().loggers
    }

    getLogger(name) {
        var logger = this.loggers[name]
        if(!logger) {
            logger = new Logger(name)
            this.loggers[name] = logger
        }
        return logger
    }

}