<template>
  <div v-bind:data-per-path="model.path">
    <ol class="breadcrumb">
      <li v-bind:class="i+1 === model.links.length ? 'breadcrumb-item active' : 'breadcrumb-item'" v-for="(item,i) in model.links" :key="i">
        <a v-if="i+1 < model.links.length" v-bind:href="$helper.pathToUrl(item.link)">{{item.text}}</a>
        <span v-if="i+1 === model.links.length" v-html="item.text"/>
      </li>
    </ol>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

