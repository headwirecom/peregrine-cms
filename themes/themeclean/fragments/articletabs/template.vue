<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8">
      <!-- Tab List -->
      <ul class="nav nav-pills d-flex justify-content-center" id="myTab" role="tablist">
        <li class="nav-item" v-for="(item,i) in model.tabs" :key="i">
          <a data-toggle="pill" role="tab" aria-expanded="true" v-bind:href="`#tab${_uid}${parseInt(i)+1}`"
          v-bind:class="[
			{'nav-link': true},
			{'btn': true},
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
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <!-- Tab Content -->
      <div class="tab-content" id="myTabContent">
        <div role="tabpanel" v-for="(item,i) in model.tabs" :key="i" v-bind:id="`tab${_uid}${parseInt(i)+1}`"
        v-bind:aria-labelledby="`tablabel${_uid}${parseInt(i)+1}`" v-bind:class="i == 0 ? 'tab-pane fade show active' : 'tab-pane fade'">
          <div class="d-flex justify-content-center">
            <div class="article py-3" v-html="item.text"></div>
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
                return this.$helper.areAllEmpty(this.model.tabs)
          },
          textClasses() {
            return `text-${this.model.tabcolor}`
          }
        }
    }
</script>

