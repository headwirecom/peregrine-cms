<template>
  <div class="d-flex justify-content-end flex-column flex-sm-row">
    <a class="btn mb-2 mb-sm-0 ml-2 mr-0" v-for="(item,i) in model.buttons"
    :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)" v-bind:class="{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': model.buttoncolor === 'primary',
            'btn-secondary': model.buttoncolor === 'secondary'
        }" v-html="item.buttontext"></a>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

