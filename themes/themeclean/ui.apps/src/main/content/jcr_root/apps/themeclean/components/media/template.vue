<template>
  <div class="w-100">

    <div v-if="model.mediatype === 'video'" class="embed-responsive embed-responsive-16by9">
      <iframe :src="$helper.pathToUrl(model.videosrc)" frameborder="0" allowfullscreen></iframe>
      </iframe>
    </div>
    <img v-else class="w-100" :src="$helper.pathToUrl(model.imagesrc)"> 

    

  </div>
</template>

<script>
export default {
  props: ["model"]
}
</script>
