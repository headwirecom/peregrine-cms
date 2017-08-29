
function experience(model, name, defaultValue) {
    const experience = 'lang:'+$perAdminApp.getView().state.language
    if(model.experiences) {
        for (let i = 0; i < model.experiences.length; i++) {
            const exp = model.experiences[i]
            if(exp.experiences.indexOf(experience) >= 0) {
                const ret = exp[name]
                return ret ? ret : defaultValue
            }
        }
    }
    if(experience !== 'lang:en' && experience.indexOf('lang:') === 0) {
        return 'T[' + (model[name] ? model[name] : defaultValue) +']'
    }
    return model[name] ? model[name] : defaultValue

}

const experiences = {
    install(vue) {
        vue.prototype.$exp = experience
    }
}

export default experiences