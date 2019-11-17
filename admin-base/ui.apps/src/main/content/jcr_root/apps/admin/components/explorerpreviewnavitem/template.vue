<template>
  <li>
    <a :title="title"
       class="waves-effect waves-light"
       v-on:click.stop.prevent="onClick">
      <i class="editor-icon material-icons">{{ icon }}</i>
    </a>
  </li>
</template>

<script>
  export default {
    props: {
      title: {
        type: String,
        required: false
      },
      icon: {
        type: String,
        required: true
      }
    },
    data: function () {
      return {};
    },
    computed: {},
    methods: {
      onClick() {
        this.$emit('click');
      }
    }
  }
</script>

<style scoped>

  li.active a {
    background-color: #37474f;
    color: #cfd8dc;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li {
    border-left: 1px solid #eceff1;
    border-right: 1px solid #b0bec5;
    height: 100%;
    line-height: 45px;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li:first-child {
    border-left: none;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li:first-child {
    border-left: 1px solid #b0bec5
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:hover,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:focus,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a:active,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:hover,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:focus,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a:active {
    background-color: #b0bec5;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a {
    padding: 0 0.75rem;
    height: 100%;
    display: inline-block;
  }

  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a .material-icons,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a .material-icons,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-right > li > a .svg-icon,
  .explorer .explorer-layout .row .explorer-preview .explorer-preview-nav .nav-left > li > a .svg-icon {
    line-height: 45px;
  }
</style>
