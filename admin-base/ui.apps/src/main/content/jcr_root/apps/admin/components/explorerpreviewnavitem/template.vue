<template>
  <li>
    <a :title="title"
       v-on:click.stop.prevent="onClick">
      <i class="editor-icon material-icons">{{ icon }}</i>
    </a>
  </li>
</template>

<script>
import iconLibValidator from '../../../../../../js/validators/iconLib'

export default {
    name: 'ExplorerPreviewNavItem',
    props: {
      title: {
        type: String,
        required: false
      },
      icon: {
        type: String,
        required: true
      },
      iconLib: {
        type: String,
        required: false,
        validator: iconLibValidator
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
