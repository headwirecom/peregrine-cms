<template>
  <div v-bind:data-per-path="model.path">
    <div v-if="filteredProducts.length &gt; 0" v-for="product in filteredProducts">
      <hr>
      <h3>{{product['jcr:title']}}</h3>
      <p>{{product.description}}</p>
      <button v-on:click="onInstall(product)">install</button>
    </div>
    <div v-if="filteredProducts.length === 0">no results for {{filter}}</div>
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
                alert(product.link)
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

