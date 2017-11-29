<template>
  <div class="col-md-6 d-flex justify-content-center justify-content-md-end">
    <a class="m-1" v-for="(item,i) in model.icons" :key="i" v-bind:href="$helper.pathToUrl(item.url)">
      <i v-bind:class="[
            {'text-light': model.colorscheme === 'dark' &amp;&amp; model.iconcustomcolor != 'true'},
            {'text-dark': model.colorscheme === 'light' &amp;&amp; model.iconcustomcolor != 'true'},
            item.icon.split(':')[1]
        ]" v-bind:style="`font-size:${model.iconsize}px;color:${model.iconcustomcolor === 'true' ? model.iconcolor : 'inherit'};`">{{item.icon.split(':')[2]}}</i>
    </a>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

