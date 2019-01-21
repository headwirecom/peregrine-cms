<template>
  <themeclean-components-block v-bind:model="model">
    <div class="d-flex justify-content-between col-12 col-md-8">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <a class="btn btn-sm percms-btn-pager"
      v-bind:href="$helper.pathToUrl(model.previous)" v-bind:class="{
            'disabled': model.previous === 'unknown',
            'btn-outline-primary': model.previous !== 'unknown'
        }">{{model.prevlabel}}</a>
      <a class="btn btn-sm percms-btn-pager" v-bind:href="$helper.pathToUrl(model.next)"
      v-bind:class="{
            'disabled': model.next === 'unknown',
            'btn-outline-primary': model.next !== 'unknown'
        }">{{model.nextlabel}}</a>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                return this.$helper.areAllEmpty(this.model.prevlabel, this.model.nextlabel)
            }
        }
    }
</script>

