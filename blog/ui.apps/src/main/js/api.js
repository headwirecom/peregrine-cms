/** **/


const api = {

    fetchBlogPosts(node) {
        return new Promise( (resolve, reject) => {

            axios.get('/content/objects/blog/posts.harray.infinity.json').then( (response) => {
                Vue.set($peregrineApp.getView().app, 'posts', response.data)
                resolve()
            })

        })
    }
}

window.api = api

export default api

