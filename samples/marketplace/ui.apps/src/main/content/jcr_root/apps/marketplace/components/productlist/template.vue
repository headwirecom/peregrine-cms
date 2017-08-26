<template>
  <div v-bind:data-per-path="model.path">
    <div v-if="filteredProducts.length &gt; 0" v-for="product in filteredProducts">
      <div class="row">
        <div class="col s2">
          <img width="100%" src="data:image/png;base64,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">
        </div>
        <div class="col s10">
          <h4>{{product['jcr:title']}} - 1.0.0-SNAPSHOT</h4>
          <p>{{product.description}}</p>
          <button v-if="status(product, 'NOT_PRESENT')" class="btn" v-on:click="onInstall(product)">install</button>
          <button v-if="status(product, 'PRESENT_NEW_VERSION')" class="btn" v-on:click="onInstall(product)">update</button>
          <button v-if="status(product, 'PRESENT_CURRENT_VERSION')" class="btn">up to date</button>
        </div>
      </div>
      <div v-if="filteredProducts.length === 0">
        <hr>no results for {{filter}}
      </div>
    </div>
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
            status(product, status) {
                return status === 'NOT_PRESENT'
            },
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
                                alert(JSON.stringify(response.data, true, 2))
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

