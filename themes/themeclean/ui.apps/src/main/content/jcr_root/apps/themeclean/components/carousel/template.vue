<template>
  <themeclean-components-block v-bind:model="model">
    <div class="carousel slide w-100 text-light" ref="pcmscarousel" v-bind:style="`height:${model.carouselheight}vh;`"
    v-bind:id="name">
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
          <div class="percms-carousel-text p-3" v-if="item.heading || item.text"
          v-bind:class="{'percms-caption-bg': model.captionbg === 'true'}">
            <h3 v-if="item.heading" v-html="item.heading"></h3>
            <p v-if="item.text" v-html="item.text"></p>
          </div>
        </div>
      </div>
      <a role="button" data-slide="prev" class="carousel-control-prev" v-if="model.controls === 'true'"
      v-bind:href="`#${name}`">
        <div v-bind:class="{'percms-caption-bg': model.captionbg === 'true'}">
          <span class="carousel-control-prev-icon"></span>
        </div>
      </a>
      <a role="button" data-slide="next" class="carousel-control-next" v-if="model.controls === 'true'"
      v-bind:href="`#${name}`">
        <div v-bind:class="{'percms-caption-bg': model.captionbg === 'true'}">
          <span class="carousel-control-next-icon"></span>
        </div>
      </a>
    </div>
  </themeclean-components-block>
</template>

<script>
  export default {
    props: ['model'],
    mounted() {
      $(this.$refs.pcmscarousel).carousel({
        ride: this.model.autoplay === 'true' ? 'carousel' : false,
        interval: this.model.autoplay === 'true' ? parseInt(this.model.interval) * 1000: 0,
        pause: this.model.pause === 'true' ? 'hover' : false,
        wrap: this.model.wrap === 'true',
        keyboard: this.model.keyboard === 'true'
      })
    },
    computed: {
      name() {
          return this.model.path.split('/').slice(1).join('-').slice(4)
      }
    }
  }
</script>
