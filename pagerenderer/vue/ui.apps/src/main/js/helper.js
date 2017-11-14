
const helpers = {
    isEmpty: function(field) {
        if(field === undefined || field === null || field === '' || field === '<p><br></p>') {
            return true
        }
        return false
    },
    areAllEmpty() {
        for(let i = 0; i < arguments.length; i++) {
            const ret = helpers.isEmpty(arguments[i])
            if(ret === false) return false
        }
        return true
    }
}

const helper = {
    install(vue) {
        vue.prototype.$helper = helpers
    }
}

export default helper