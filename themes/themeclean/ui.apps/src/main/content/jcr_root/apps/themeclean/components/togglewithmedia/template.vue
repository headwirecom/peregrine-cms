<template>
  <themeclean-components-block v-bind:model="model">
    <div class="container">
      <h2 class="text-center pb-4" v-if="model.showtitle == 'true'"
      v-html="model.title"></h2>
      <div class="row justify-content-center">
        <themeclean-components-media :model="model"></themeclean-components-media>
        <div class="col-12 col-md">
          <div class="item card bg-transparent border-0 rounded-0" v-for="(item,i) in model.toggles"
          :key="i">
            <a aria-expanded="false" class="card-header border rounded-0 text-dark"
            data-toggle="collapse" v-bind:data-parent="`#accordion${_uid}`" v-bind:href="`#accordion${_uid}${i}`"
            v-bind:aria-controls="`accordion${_uid}${i}`" v-html="item.title"></a>
            <div class="collapse rounded-0" role="tabpanel" v-bind:id="`accordion${_uid}${i}`">
              <div class="card-body rounded-0" v-html="item.text"></div>
            </div>
          </div>
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