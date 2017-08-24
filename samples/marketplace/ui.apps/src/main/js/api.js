/** **/


const api = {

    fetchProducts(node) {
        return new Promise( (resolve, reject) => {
            axios.get('/content/objects/marketplace.harray.infinity.json')
                .then( (response) => {
                    Vue.set($peregrineApp.getView().app, 'products', response.data)
                    resolve()
                })
        })
    }
}

window.api = api

export default api

