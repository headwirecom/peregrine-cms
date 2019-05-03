/*-
 * #%L
 * peregrine sveltejs page renderer - UI Apps
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

    setOffLevel() {
        return this.setLevel(LogLevel.OFF)
    }

    setErrorLevel() {
        return this.setLevel(LogLevel.ERROR)
    }

    setWarnLevel() {
        return this.setLevel(LogLevel.WARN)
    }

    setInfoLevel() {
        return this.setLevel(LogLevel.INFO)
    }

    setDebugLevel() {
        return this.setLevel(LogLevel.DEBUG)
    }

    setFineLevel() {
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

    getLogger(name) {
        var logger = this.loggers[name]
        if(!logger) {
            logger = new Logger(name)
            this.loggers[name] = logger
        }
        return logger
    }

}
