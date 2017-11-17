<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8 article" v-bind:class="model.textalign">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <h2 class="col-12 p-0 pb-3" v-if="model.showtitle == 'true'">{{model.title}}</h2>
      <h3 class="col-12 p-0 pb-3" v-if="model.showsubtitle == 'true'">{{model.subtitle}}</h3>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                //return !(this.model.showtitle === 'true' || this.model.showsubtitle === 'true')
                return this.$helper.areAllEmpty(this.model.showtitle === 'true', this.model.showsubtitle === 'true')
            }
        }
    }
</script>