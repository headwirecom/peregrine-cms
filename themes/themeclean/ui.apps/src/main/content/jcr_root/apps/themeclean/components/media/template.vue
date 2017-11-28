<template>
  <div class="w-100">

    <div v-if="model.mediatype === 'video'" class="embed-responsive embed-responsive-16by9">
      <iframe :src="$helper.pathToUrl(model.videosrc)" frameborder="0" allowfullscreen></iframe>
      </iframe>
    </div>
    <i v-if="model.mediatype === 'icon'" v-bind:class="item.icon.split(':')[1]" v-bind:style="`font-size:${model.iconsize}px;color:${model.iconcolor};`">
      {{mediaicon.split(':')[2]}}
    </i>
    <img v-else class="w-100" :src="$helper.pathToUrl(model.imagesrc)"> 

    <div v-if="model.mediacaption" class="percms-caption">
      <div>
        <h1 class="p-5" v-html="model.mediacaption"></h1>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  props: ["model"]
}
</script>
