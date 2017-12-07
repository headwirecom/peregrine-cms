<template>
  <themeclean-components-block v-bind:model="model">
    <div class="carousel slide" v-bind:style="`height:${model.carouselheight}vh;`"
    v-bind:id="name" v-bind:data-ride="model.autoplay === 'true' ? 'carousel' : 'false'"
    v-bind:data-interval="model.autoplay === 'true' ? 1000*model.interval : 'false'"
    v-bind:data-pause="model.autoplay === 'true' &amp;&amp; model.pause === 'true' ? 'hover' : 'false'"
    v-bind:data-wrap="model.wrap === 'true'" v-bind:data-keyboard="model.keyboard === 'true'">
      <ol class="carousel-indicators" v-if="model.indicators === 'true'">
        <li v-for="(item,i) in model.slides" :key="i" v-bind:data-target="`#${name}`"
        v-bind:data-slide-to="i" v-bind:class="{active: i === 0}"></li>
      </ol>
      <div role="listbox" class="carousel-inner">
        <div class="carousel-item" v-for="(item,i) in model.slides" :key="i" v-bind:class="{active: i === 0}">
          <div class="d-flex flex-column justify-content-center">
            <img class="percms-carousel-image" v-if="item.imagepath" v-bind:src="item.imagepath"
            v-bind:alt="item.alt">
          </div>
          <div class="carousel-caption" v-if="item.heading || item.text">
            <h3 v-if="item.heading" v-html="item.heading"></h3>
            <p v-if="item.text" v-html="item.text"></p>
          </div>
        </div>
      </div>
      <a role="button" data-slide="prev" class="carousel-control-prev" v-if="model.controls === 'true'"
      v-bind:href="`#${name}`">
        <span class="carousel-control-prev-icon"></span>
      </a>
      <a role="button" data-slide="next" class="carousel-control-next" v-if="model.controls === 'true'"
      v-bind:href="`#${name}`">
        <span class="carousel-control-next-icon"></span>
      </a>
    </div>
  </themeclean-components-block>
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
