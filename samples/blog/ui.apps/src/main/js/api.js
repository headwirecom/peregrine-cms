/** **/


const api = {

    fetchBlogPosts(node) {
        return new Promise( (resolve, reject) => {
            axios.get('/api/blog/blogPosts.json/content/objects/blog')
                .then( (response) => {
                    Vue.set($peregrineApp.getView().app, 'posts', response.data)
                    resolve()
                })
        })
    }
}

window.api = api

export default api

