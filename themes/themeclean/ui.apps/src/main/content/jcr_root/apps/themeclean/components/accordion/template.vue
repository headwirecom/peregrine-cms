<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-10">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <h2 class="text-center pb-4" v-if="model.showtitle == 'true'"
      v-html="model.title"></h2>
      <div class="row" v-bind:class="model.mediaposition === 'after' ? 'flex-row-reverse': 'flex-row'">
        <div class="col-12 col-md-auto p-0 pb-3 p-md-3" v-if="model.showmedia === 'true'"
        v-bind:style="{width:`${model.mediawidth}%`}">
          <themeclean-components-media :model="model"></themeclean-components-media>
        </div>
        <!-- Card Container -->
        <div class="col-12 col-md p-0 border-0" v-bind:id="`accordion${_uid}`">
          <!-- Card Items -->
          <div class="item card bg-transparent border-0" v-for="(item,i) in model.accordiontoggle"
          :key="i">
            <a aria-expanded="false" class="d-flex justify-content-between align-items-center card-header border-0 bg-transparent px-3"
            ref="collapsible" data-toggle="collapse" v-bind:data-parent="model.toggletype === 'accordion' ? `#accordion${_uid}` : ''"
            v-bind:href="`#accordion${_uid}${i}`" v-bind:aria-controls="`accordion${_uid}${i}`">
              <h4 v-html="item.title"></h4>
              <i class="fa fa-chevron-down" aria-hidden="true"></i>
            </a>
            <div class="collapse" role="tabpanel" v-bind:id="`accordion${_uid}${i}`">
              <div class="card-body p-0 px-3 bg-transparent border-0" v-html="item.text"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	isEditAndEmpty() {
            if(!$peregrineApp.isAuthorMode()) return false
            return this.$helper.areAllEmpty( this.model.showtitle === 'true' && this.model.title, this.model.showmedia === 'true', this.model.accordiontoggle );
          }
        }
    }
</script>