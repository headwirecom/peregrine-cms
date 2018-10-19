<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12">
      <span class="breadcrumb-item" v-for="(item,i) in model.links" :key="i"
      v-bind:class="{active: i === model.links.length - 1}">
        <a v-if="i + 1 &lt; model.links.length" v-bind:href="item.link">{{item.text}}</a>
        <span v-if="i+1 === model.links.length">{{item.text}}</span>
      </span>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
           beforeSave(data) {
               delete data.links
               return data
           }
        }
    }
</script>

