import {LoggerFactory, LogLevel} from './logger'

const log = LoggerFactory.logger('i18n').setLevelDebug()
let lang = 'en'

function keyToLang(original) {
    try {
        const lowOriginal = original.toLowerCase();
        const resources = $perAdminApp.getView().admin.i18n[lang]
        if(resources[original]) {
            return resources[original].text
        } else if (resources[lowOriginal]){
            return resources[lowOriginal].text
        }
        if(lang === 'en') return original
        if (log.level === LogLevel.FINE) {
            return `T{${original}]`
        } else {
            log.warn(`missing translation for: ${original}`)
            return original
        }
    } catch(error) {
        return original
    }
}

function setLang(language) {
    $perAdminApp.getView().admin.i18n[lang] = {}
    lang = language
    $perAdminApp.getView().state.language = language
    $perAdminApp.loadi18n()
}

function getLang() {
    return lang
}

function getLangs() {
    return [ {name: 'en'}, {name: 'de'} ]
}

const i18n = {
    install(vue) {
        $perAdminApp.loadi18n()
        vue.prototype.$i18n = keyToLang
        vue.prototype.$i18nSetLanguage = setLang
        vue.prototype.$i18nGetLanguage = getLang
        vue.prototype.$i18nGetLanguages = getLangs
    }
}

export default i18n