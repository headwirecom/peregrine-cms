<template>
  <component :is="tag">{{ text }}</component>
</template>

<script>
  export default {
    mixins: [ VueFormGenerator.abstractField ],
    data() {
      return {
        default: {
          weight: 5,
          text: 'No [text] set!!'
        }
      }
    },
    computed: {
      tag() {
        let iWeight = parseInt(this.schema.weight);
        if (iWeight && iWeight > 0 && iWeight <= 6) {
          return 'h' + iWeight;
        }
        return 'h' + this.default.weight;
      },
      text() {
        let text = this.schema.text;
        return text? text : this.default.text;
      }
    }
  }
</script>

<style scoped>
  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    color: #455a64;
  }
</style>