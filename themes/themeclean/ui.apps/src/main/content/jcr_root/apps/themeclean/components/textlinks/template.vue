<template>
  <ul class="navbar-nav nav-dropdown text-right">
    <li class="nav-item" v-for="(item,i) in model.links" :key="i">
      <a class="nav-link" v-bind:href="$helper.pathToUrl(item.link)" v-html="item.text"
      v-bind:class="{'text-dark' : model.colorscheme === 'light','text-light' : model.colorscheme === 'dark'}"></a>
    </li>
  </ul>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
          activeClass (url) {
            return {'per-active': this.$data.path === url}
          }
        }
    }
</script>

