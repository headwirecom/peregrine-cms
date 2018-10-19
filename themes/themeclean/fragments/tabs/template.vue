<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-10">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <div class="row">
        <h2 class="text-center col-12 pb-4" v-if="model.showtitle == 'true' &amp;&amp; model.title"
        v-html="model.title"></h2>
        <h3 class="text-center col-12 pb-4" v-if="model.showsubtitle == 'true' &amp;&amp; model.subtitle"
        v-html="model.subtitle"></h3>
      </div>
      <div class="row justify-content-center" v-bind:class="{
            'flex-row': model.mediaposition === 'before',
            'flex-row-reverse': model.mediaposition === 'after'
        }">
        <div class="col col-md-auto pb-3" v-if="model.showmedia == 'true'"
        v-bind:style="{width:`${model.mediawidth}%`}">
          <themeclean-components-media :model="model"></themeclean-components-media>
        </div>
        <div class="col-12 col-md py-3 py-md-0 d-flex flex-column">
          <!-- Tab Nav -->
          <ul class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist">
            <li class="nav-item" v-for="(item,i) in model.tabs" :key="i">
              <a data-toggle="pill" role="tab" aria-expanded="true" v-bind:href="`#tab${_uid}${parseInt(i)+1}`"
              v-bind:class="[
            {'nav-link': true},
            {'active': i == 0},
            {'bg-primary': model.tabcolor === 'primary'},
            {'bg-secondary': model.tabcolor === 'secondary'},
            {'bg-success': model.tabcolor === 'success'},
            {'bg-danger': model.tabcolor === 'danger'},
            {'bg-warning': model.tabcolor === 'warning'},
            {'bg-info': model.tabcolor === 'info'},
            {'bg-light': model.tabcolor === 'light'},
            {'bg-dark': model.tabcolor === 'dark'},
            textClasses
        ]" v-bind:id="`tab-control-${_uid}${parseInt(i)+1}`" v-bind:aria-controls="`tab${_uid}${parseInt(i)+1}`"
              v-html="item.title"></a>
            </li>
          </ul>
          <!-- Tab Content -->
          <div class="tab-content" id="myTabContent">
            <div role="tabpanel" v-for="(item,i) in model.tabs" :key="i" v-bind:id="`tab${_uid}${parseInt(i)+1}`"
            v-bind:aria-labelledby="`tablabel${_uid}${parseInt(i)+1}`" v-bind:class="i == 0 ? 'tab-pane fade show active' : 'tab-pane fade'">
              <div class="text-center py-3" v-html="item.text"></div>
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
                //return !(this.model.tabs.length > 0)
                return this.$helper.areAllEmpty(this.model.showtitle === 'true' && this.model.title, this.model.showsubtitle === 'true' && this.model.subtitle , this.model.tabs , this.model.showmedia === 'true')
          },
          textClasses() {
            return `text-${this.model.tabcolor}`
          }
        }
    }
</script>