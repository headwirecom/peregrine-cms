let lang = 'en'

function keyToLang(original) {
    const key = original.toLowerCase()
    try {
        const resources = $perAdminApp.getView().admin.i18n[lang]
        const translation = resources[key]
        if(translation) {
            return translation.text
        }
        if(lang === 'en') return original
        return 'T['+original+']'
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