<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="row">
        <h2 class="text-center col-12 pb-4" v-if="model.showtitle == 'true'"
        v-html="model.title"></h2>
        <h3 class="text-center col-12 pb-4" v-if="model.showsubtitle == 'true'"
        v-html="model.subtitle"></h3>
      </div>
      <div class="row">
        <div v-bind:style="{flexBasis:`${model.mediawidth}%`}">
          <themeclean-components-media :model="model"></themeclean-components-media>
        </div>
        <div class="col-12 col-md pt-3 d-flex flex-column justify-content-center">
          <!-- Tab Nav -->
          <div class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist"
          style="margin-left: 2em">
            <ul class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist">
              <li class="nav-item" v-for="(item,i) in model.tabs" :key="i">
                <a data-toggle="pill" role="tab" aria-expanded="true" v-bind:href="`#tab${_uid}${parseInt(i)+1}`"
                v-bind:class="i == 0 ? 'nav-link active' : 'nav-link'"
                v-bind:id="`tab-control-${_uid}${parseInt(i)+1}`" v-bind:aria-controls="`tab${_uid}${parseInt(i)+1}`"
                v-html="item.title"></a>
              </li>
            </ul>
          </div>
          <!-- Tab Content -->
          <div class="tab-content" id="myTabContent">
            <div role="tabpanel" v-for="(item,i) in model.tabs" :key="i" v-bind:id="`tab${_uid}${parseInt(i)+1}`"
            v-bind:aria-labelledby="`tablabel${_uid}${parseInt(i)+1}`" v-bind:class="i == 0 ? 'tab-pane fade show active' : 'tab-pane fade'">
              <div class="text-center py-5" v-html="item.text"></div>
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
                //return !(this.model.showtitle === 'true' || this.model.showsubtitle === 'true'  || this.model.tabs.length > 0 || this.model.imagesrc || this.model.videosrc)
                return this.$helper.areAllEmpty(this.model.showtitle === 'true' , this.model.showsubtitle === 'true'  , this.model.tabs , this.model.imagesrc , this.model.videosrc)
            }
        }
    }
</script>