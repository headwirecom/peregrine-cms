<template>
  <themeclean-components-block v-bind:model="model" @mounted="onBlockMounted">
    <nav class="navbar align-items-center navbar-light w-100"
         ref="nav"
         v-bind:class="{'navbar-expand-lg': model.collapsed === 'false','navbar-light': model.colorscheme === 'light','navbar-dark': model.colorscheme === 'dark'}">
      <h1 v-if="editAndEmpty">Configure Header</h1>
      <!-- Logo -->
      <span class="navbar-logo">
        <a v-if="model.logo" v-bind:href="$helper.pathToUrl(model.logourl)">
          <img class="menu-logo" v-bind:src="$helper.pathToUrl(model.logo)"
               v-bind:alt="model.logoalttext"
               v-bind:style="`height:${parseInt(model.logosize)}px;`">
        </a>
      </span>
      <!-- Hamburger toggle button -->
      <button class="navbar-toggler navbar-toggler-right" data-toggle="collapse"
              data-target="#navbarSupportedContent" aria-expanded="false"
              aria-controls="navbarSupportedContent"
              aria-label="navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <!-- Collapsible Menu -->
      <div class="collapse navbar-collapse justify-content-end" id="navbarSupportedContent">
        <themeclean-components-textlinks v-bind:model="model"></themeclean-components-textlinks>
        <themeclean-components-menubuttons v-bind:model="model"></themeclean-components-menubuttons>
      </div>
      <div v-if="isEditAndEmpty">no content defined for component</div>
    </nav>
  </themeclean-components-block>
</template>

<script>
  export default {
    props: ['model'],
    mounted() {
      $('body').scrollspy({target: '.navbar'})
      $('.nav-link').click(function (e) {
        $('.navbar-collapse').collapse('hide')
      })
    },
    computed: {
      isEditAndEmpty() {
        if (!$peregrineApp.isAuthorMode()) {
          return false
        }
        return this.$helper.areAllEmpty(this.model.logo, this.model.links, this.model.buttons)
      }
    },
    created() {
      this.$root.$on('block.mounted', this.onBlockMountede)
    },
    methods: {
      onBlockMounted() {
        // Add top margin to perApp to account for fixed header when sticky is true
        if( this.model.sticky === 'true' && !$peregrineApp.isAuthorMode()) {
          if( this.$refs.section.style.position === 'fixed' ){
            const height = this.$refs.section.clientHeight
            this.$refs.section.parentElement.style.marginTop = height + 'px';
          }
        }
        const navSection = this.$refs.nav.parentElement.parentElement.parentElement
        const navPosition = navSection.style.position
        const navSticky = navPosition === "sticky" || navPosition === "fixed"
        const navOffset = navSticky ? navSection.clientHeight : 0

        navSection.style.top = `0px`
        navSection.style.marginTop = `-${navOffset}px`
        navSection.style.paddingTop = `${navOffset}px`
      }
    }
  }
</script>