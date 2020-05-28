<template>
  <div class="switch">
    <label>
      {{ offLabel }}
      <input :disabled="disabled" type="checkbox" v-model="value">
      <span class="lever"></span>
      {{ onLabel }}
    </label>
  </div>
</template>

<script>
  export default {
    name: 'MaterializeSwitch',
    props: {
      disabled: {
        type: Boolean,
        default: false
      },
      offLabel: {
        type: String,
        default: 'Off'
      },
      onLabel: {
        type: String,
        default: 'On'
      }
    },
    data() {
      return {
        value: false
      }
    },
    watch: {
      value(val) {
        this.$emit('update', val)
      }
    }
  }
</script>