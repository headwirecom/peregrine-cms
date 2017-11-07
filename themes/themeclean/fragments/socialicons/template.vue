<template>
  <div class="col-md-6 d-flex justify-content-md-end">
    <div v-for="(item,i) in model.icons" :key="i">
      <a v-bind:href="item.url">
        <i v-bind:class="item.icon.split(':')[1]">{{item.icon.split(':')[2]}}</i>
      </a>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

