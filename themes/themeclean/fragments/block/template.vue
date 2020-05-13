<template>
  <section class="d-flex align-items-center" ref="section" v-bind:class="[classes, colors]"
  v-bind:style="[styles, sticky]" v-bind:data-per-path="model.path">
    <a class="percms-anchor" ref="anchor" v-bind:id="model.anchorname"></a>
    <div class="embed-responsive embed-responsive-16by9" v-if="model.custombackground === 'true' &amp;&amp; model.backgroundtype == 'video' &amp;&amp; model.bgvideo"
    v-bind:style="`position:${'absolute'};pointer-events:${'none'};`">
      <iframe class="embed-responsive-item" v-bind:src="model.bgvideo + '?autoplay=1&amp;loop=1&amp;controls=0&amp;mute=1'"></iframe>
    </div>
    <div v-bind:class="model.fullwidth === 'true' ? 'container-fluid' : 'container'">
      <div class="row justify-content-center">
        <slot></slot>
      </div>
    </div>
  </section>
</template>

<script>
    export default {
        props: {
          model: {
            type: Object
          }
        },
        mounted() {
          // Add top margin to perApp to account for fixed header when sticky is true
          if( this.model.sticky === 'true' && !$peregrineApp.isAuthorMode()) {
            if( this.$refs.section.style.position === 'fixed' ){
              const height = this.$refs.section.clientHeight
              this.$refs.section.parentElement.style.marginTop = height + 'px';
            }
          }
          //Offset height of anchor by height of the navbar and top padding
          let navSection = document.querySelector('nav').parentElement.parentElement.parentElement
          let navPosition = navSection.style.position
          let navSticky = navPosition === "sticky" || navPosition === "fixed" 
          let navOffset = navSticky ? navSection.clientHeight : 0

          this.$refs.anchor.style.top = `0px`
          this.$refs.anchor.style.marginTop = `-${navOffset}px`
          this.$refs.anchor.style.paddingTop = `${navOffset}px`

        },
        computed: {          
          classes: function() {
            let classObject = {}
            classObject['percms-view-height'] = this.model.fullheight == 'true'
            classObject[`elevation-${this.model.elevation}`] = this.model.elevation > 0
            return classObject      
          },
          colors: function() {
            let classes = {}
            if( this.model.colorscheme === '' ) return classes
            if( this.model.custombackground === 'false' ) {
              classes['bg-dark'] = this.model.colorscheme === 'dark'
              classes['bg-light'] = this.model.colorscheme === 'light'
            }
            classes['text-dark'] = this.model.colorscheme === 'light'
            classes['text-light'] = this.model.colorscheme === 'dark'
            return classes
          },
          sticky: function() {
            const sticky = this.model.sticky === 'true'
            return sticky && !$peregrineApp.isAuthorMode() ?
            {
              position: 'fixed',
              position: 'sticky',
              top: '0',
              width: '100%',
              zIndex: '1000'
            } : {}
          },
          styles: function() {
            return {
              paddingTop: `${this.model.toppadding}px`,
              paddingBottom: `${this.model.bottompadding}px`,
              background: this.backgroundStyles(),
              backgroundSize: 'cover',
              position: 'relative',
              overflow: 'hidden'
            }
          }
        },
        methods: {
          backgroundStyles() {
              if( this.model.custombackground === 'false') return ''
              switch (this.model.backgroundtype) {

                case 'image':
                  const overlay = this.model.overlay === 'true' ? `${this.overlayStyle()},` : '' 
                  if(this.model.bgimage) {
                    return overlay + `url("${this.model.bgimage}") center center / cover no-repeat`
                  } 
                  return overlay;

                case 'gradient':
                  return `linear-gradient(to right,${this.model.bgcolor},${this.model.color2})`

                case 'color':
                  return `${this.model.bgcolor}`
                
                default:
                  return '' 

            }
          },
          colorOpacity() {
            const hex = this.model.overlaycolor;
            return `rgba(${parseInt(hex.slice(1,3),16)}, 
                         ${parseInt(hex.slice(3,5),16)}, 
                         ${parseInt(hex.slice(5,7),16)}, 
                         ${this.model.overlayopacity / 100} )`
          },
          overlayStyle() {
            return `linear-gradient(to right, ${this.colorOpacity()}, ${this.colorOpacity()})`
          }
        }

    }
</script>

