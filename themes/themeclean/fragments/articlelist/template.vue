<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8">
      <component class="col-12" :is="model.numberedlist === 'true' ? 'ol':'ul'">
        <li v-for="(item,i) in model.listitems" :key="i" v-html="item.text"></li>
      </component>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return (!this.model.listitems || Object.keys(this.model.listitems).length === 0)
            }
        }
    }
</script>