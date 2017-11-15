<template>
  <div class="col-12 percms-image-wrap p-0" :style="{flexBasis: `${model.mediawidth}%`}">

    <div v-if="model.mediatype === 'video'" class="embed-responsive embed-responsive-16by9">
      <iframe :src="model.videosrc" frameborder="0" allowfullscreen></iframe>
      </iframe>
    </div>
    <img v-else :src="model.imagesrc"> 

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
