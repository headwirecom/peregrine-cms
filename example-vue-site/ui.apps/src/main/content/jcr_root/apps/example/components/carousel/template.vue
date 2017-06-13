<template>
  <div 
    class="carousel slide"
    v-bind:id              ="name"
    v-bind:data-interval   ="model.interval"
    v-bind:data-pause      ="model.pause"
    v-bind:data-ride       ="model.ride"
    v-bind:data-indicators ="model.indicators"
    v-bind:data-controls   ="model.controls"
    v-bind:data-wrap       ="model.wrap"
    v-bind:data-keyboard   ="model.keyboard"
    v-bind:data-per-path   ="model.path"
    style="min-height: 300px;background-color:#eceff1;">
    <ol v-if="model.indicators" class="carousel-indicators">
      <li 
        v-for="(slide, index) in model.children"
        :data-target="`#${name}`"
        :data-slide-to="index"></li>
    </ol>
    <div class="carousel-inner" role="listbox">
      <div v-for="(slide, index) in model.children" :class="`carousel-item ${index === 0 ? 'active' : ''}`">
        <img v-if="slide.imagePath" :src="slide.imagePath" :alt="slide.alt" />
        <div v-if="slide.heading || slide.text" class="carousel-caption">
          <h3 v-if="slide.heading">{{slide.heading}}</h3>
          <p v-if="slide.text" v-html="slide.text"></p>
        </div>
      </div>
    </div>
    <template v-if="model.controls">
      <a class="carousel-control-prev" :href="`#${name}`" role="button" data-slide="prev">
        <span class="carousel-control-prev-icon"></span>
      </a>
      <a class="carousel-control-next" :href="`#${name}`" role="button" data-slide="next">
        <span class="carousel-control-next-icon"></span>
      </a>
    </template>
  </div>
</template>

<script>
  export default {
    props: ['model'],
    computed: {
      name() {
          return this.model.path.split('/').slice(1).join('-').slice(4)
      }
    }
  }
</script>
