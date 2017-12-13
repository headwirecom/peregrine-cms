<template>
  <themeclean-components-block v-bind:model="model">
    <nav class="navbar align-items-center navbar-light w-100" v-bind:class="{'navbar-expand-lg': model.collapsed === 'false','navbar-light': model.colorscheme === 'light','navbar-dark': model.colorscheme === 'dark'}">
      <h1 v-if="editAndEmpty">Configure Header</h1>
      <!-- Logo -->
      <span class="navbar-logo">
        <a v-if="model.logo" v-bind:href="$helper.pathToUrl(model.logourl)">
          <img class="menu-logo" v-bind:src="$helper.pathToUrl(model.logo)" v-bind:alt="model.logoalttext"
          v-bind:style="`height:${parseInt(model.logosize)}px;`">
        </a>
      </span>
      <!-- Hamburger toggle button -->
      <button class="navbar-toggler navbar-toggler-right" data-toggle="collapse"
      data-target="#navbarSupportedContent" aria-expanded="false" aria-controls="collapseExample">
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
          $('body').scrollspy({target:'.navbar'})
          $('.nav-link').click( function(e) {
            $('.navbar-collapse').collapse('hide')
          })
        },
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return this.$helper.areAllEmpty(this.model.logo, this.model.links,  this.model.buttons)
            }
        }
    }
</script>