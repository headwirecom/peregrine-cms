<template>
  <div v-bind:data-per-path="model.path">
    <ol class="breadcrumb">
      <li class="breadcrumb-item">
        <a href="#">Home</a>
      </li>
      <li class="breadcrumb-item">
        <a href="#">Library</a>
      </li>
      <li class="breadcrumb-item active">Data</li>
    </ol>
  </div>
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

