<template>
  <ul class="navbar-nav nav-dropdown text-right">
    <li class="nav-item" v-for="(item,i) in model.links" :key="i">
      <a class="nav-link" v-bind:href="$helper.pathToUrl(item.link)" v-html="item.text"
      v-bind:class="model.colorscheme === 'light' ? 'text-dark':'text-light'"></a>
    </li>
  </ul>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

