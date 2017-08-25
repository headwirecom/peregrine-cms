<template>
  <div v-bind:data-per-path="model.path">
    <div v-if="filteredProducts.length &gt; 0" v-for="product in filteredProducts">
      <hr>
      <h3>{{product['jcr:title']}}</h3>
      <p>{{product.description}}</p>
      <button v-on:click="onInstall(product)">install</button>
    </div>
    <div v-if="filteredProducts.length === 0">
      <hr>no results for {{filter}}</div>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            products() {
                return $peregrineApp.getView().app.products['__children__']
            },
            filteredProducts() {
                let ret = []
                const filter = this.filter
                for(let i = 0; i < this.products.length; i++) {
                    if(this.applyFilter(this.products[i], filter)) {
                        ret.push(this.products[i])
                    }
                }
                return ret
            },
            filter() {
                return $peregrineApp.getView().app.filter
            }
        },
        methods: {
            onInstall(product) {
                axios.get(product.link, {responseType: "blob"}).then( (response) => {
                    //alert(response.data)
                    var data = new FormData()
                    let name = product['jcr:title']
                    data.append('file', response.data, name)
                    data.append('force','')
                    axios.post('/bin/cpm/package.upload.json', data, {})
                        .then( (response) => {
                            axios.post('/bin/cpm/package.install.json'+response.data.path, null, {}).then( (response) => {
                                alert('installation complete')
                            })
                        } )
                        .catch( err => alert(err) )
                })
            },
            applyFilter(product, filter) {
                if(filter) {
                    return product['jcr:title'].indexOf(filter) >= 0
                }
                return true
            }
        }
    }
</script>

