<template>
  <themeclean-components-block v-bind:model="model">
    <div class="container">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <h2 class="text-center pb-4" v-if="model.showtitle == 'true'"
      v-html="model.title"></h2>
      <div class="row col-12 p-0 d-md-flex flex-wrap justify-content-center"
      v-bind:class="model.mediaposition === 'after' ? 'flex-row-reverse': 'flex-row'">
        <div class="col-auto" v-bind:style="{width:`${model.mediawidth}%`}">
          <themeclean-components-media :model="model"></themeclean-components-media>
        </div>
        <div class="col-12 col-md" v-bind:ref="collapsible" v-bind:id="model.toggletype === 'accordion' ? `accordion${_uid}` : ''">
          <div class="item card bg-transparent border-0 rounded-0" v-for="(item,i) in model.accordiontoggle"
          :key="i">
            <a aria-expanded="false" class="card-header border rounded-0" data-toggle="collapse"
            v-bind:data-parent="`#accordion${_uid}`" v-bind:href="`#accordion${_uid}${i}`"
            v-bind:aria-controls="`accordion${_uid}${i}`" v-html="item.title"></a>
            <div class="collapse rounded-0" role="tabpanel" v-bind:id="`accordion${_uid}${i}`">
              <div class="card-body rounded-0" v-html="item.text"></div>
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
        updated() {
          $(this.$refs.collapsible).collapse()
        },
        computed: {
        	isEditAndEmpty() {
            if(!$peregrineApp.isAuthorMode()) return false
            return this.$helper.areAllEmpty( this.model.showtitle === 'true' , this.model.tabs );
          }
        }
    }
</script>