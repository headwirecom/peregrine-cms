<template>
  <div class="d-flex justify-content-end flex-column flex-sm-row">
    <a class="nav-link btn mb-2 mb-sm-0 ml-sm-2 mr-0" v-for="(item,i) in model.buttons"
    :key="i" v-bind:href="$helper.pathToUrl(item.buttonlink)" v-bind:class="{
            'btn-lg': model.buttonsize === 'large',
            'btn-sm': model.buttonsize === 'small',
            'btn-primary': item.buttoncolor === 'primary',
            'btn-secondary': item.buttoncolor === 'secondary',
            'btn-success': item.buttoncolor === 'success',
            'btn-danger': item.buttoncolor === 'danger',
            'btn-warning': item.buttoncolor === 'warning',
            'btn-info': item.buttoncolor === 'info',
            'btn-light': item.buttoncolor === 'light',
            'btn-dark': item.buttoncolor === 'dark'
        }" v-html="item.buttontext"></a>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

