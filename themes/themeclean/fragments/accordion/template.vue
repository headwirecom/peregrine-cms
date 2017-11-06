<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8" v-bind:id="`accordion${_uid}`">
      <h2 class="text-center" v-if="model.showtitle == 'true'">{{model.title}}</h2>
      <div class="item card border-0" v-for="(item,i) in model.tabs"
      :key="i">
        <a class="card-header border rounded-0" data-toggle="collapse" aria-expanded="true"
        v-bind:data-parent="`#accordion${_uid}`" v-bind:href="`#accordion${_uid}${i}`"
        v-bind:aria-controls="`accordion${_uid}${i}`">{{item.title}}</a>
        <div class="collapse" role="tabpanel" v-bind:id="`accordion${_uid}${i}`">
          <div class="card-body" v-html="item.text"></div>
        </div>
      </div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>