<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12">
      <div class="perIsEditAndEmpty" v-if="isEditAndEmpty">no content defined for component</div>
      <ul class="root" v-if="model.includeroot === 'true'">
        <li class="root">
          <a v-bind:href="$helper.pathToUrl(model.rootPageLink)">{{model.rootPageTitle}}</a>
          <ul>
            <li class="children" v-for="(child,i) in model.childrenPages" :key="i">
              <a v-bind:href="$helper.pathToUrl(child.path)">{{child.title}}</a>
              <themeclean-components-pagelistnested v-bind:model="child"
              v-if="child.hasChildren"></themeclean-components-pagelistnested>
            </li>
          </ul>
        </li>
      </ul>
      <ul class="noroot" v-if="model.includeroot !== 'true'">
        <li class="childrennoroot" v-for="(child,i) in model.childrenPages" :key="i">
          <a v-bind:href="$helper.pathToUrl(child.path)">{{child.title}}</a>
          <themeclean-components-pagelistnested v-bind:model="child"
          v-if="child.hasChildren"></themeclean-components-pagelistnested>
        </li>
      </ul>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        methods: {
            beforeSave(data) {
                delete data.childrenPages
                return data
            }
        },
        computed: {
        	isEditAndEmpty() {
            if(!$peregrineApp.isAuthorMode()) return false
            return this.$helper.areAllEmpty(this.model.rootpage && this.model.levels);
          }
        }
    }
</script>

