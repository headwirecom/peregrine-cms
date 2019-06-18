<template>
  <div v-bind:data-per-path="model.path">
    <div class="circle" :style="{ width: radius + 'px', height: radius + 'px' }"></div>
  </div>
</template>

<script>
  export default {
    props: ['model'],
    computed: {
      radius() {
        return (!this.model.radius || this.model.radius <= 0) ? 200 : this.model.radius * 2;
      }
    }
  }
</script>
<style scoped>
  .circle {
    margin: auto;
    border-radius: 50%;
    background-color: red;
    border: 2px solid #000000;
  }
</style>
