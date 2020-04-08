<template>
  <li>
    <a :title="title"
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
