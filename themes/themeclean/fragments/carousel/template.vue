<template>
  <div class="carousel slide" style="min-height: 300px; background-color: rgb(236, 239, 241);"
  v-bind:data-per-path="model.path" v-bind:id="name" v-bind:data-interval="model.interval"
  v-bind:data-pause="model.pause" v-bind:data-ride="model.ride" v-bind:data-indicators="model.indicators"
  v-bind:data-controls="model.controls" v-bind:data-wrap="model.wrap" v-bind:data-keyboard="model.keyboard">
    <ol class="carousel-indicators" v-if="model.indicators">
      <li v-for="(item,i) in model.slides" :key="i" v-bind:data-target="`#${name}`"
      v-bind:data-slide-to="i"></li>
    </ol>
    <div role="listbox" class="carousel-inner">
      <div v-for="(item,i) in model.slides" :key="i" v-bind:class="`carousel-item ${i === 0 ? 'active' : ''}`">
        <img v-if="item.imagepath" v-bind:src="item.imagepath" v-bind:alt="item.alt">
        <div class="carousel-caption" v-if="item.heading || item.text">
          <h3 v-if="item.heading" v-html="item.heading"></h3>
          <p v-if="item.text" v-html="item.text"></p>
        </div>
      </div>
    </div>
    <a role="button" data-slide="prev" class="carousel-control-prev" v-if="model.controls"
    v-bind:href="`#${name}`">
      <span class="carousel-control-prev-icon"></span>
    </a>
    <a role="button" data-slide="next" class="carousel-control-next" v-if="model.controls"
    v-bind:href="`#${name}`">
      <span class="carousel-control-next-icon"></span>
    </a>
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
