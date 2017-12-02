<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12">
      <ul class="root" v-if="model.includeroot === 'true'">
        <li class="root">
          <a v-bind:href="model.rootPageLink">{{model.rootPageTitle}}</a>
          <ul>
            <li class="children" v-for="(child,i) in model.childrenPages" :key="i">
              <a v-bind:href="child.path">{{child.title}}</a>
              <themeclean-components-pagelistnested v-bind:model="child"
              v-if="child.hasChildren"></themeclean-components-pagelistnested>
            </li>
          </ul>
        </li>
      </ul>
      <ul class="noroot" v-if="model.includeroot !== 'true'">
        <li class="childrennoroot" v-for="(child,i) in model.childrenPages" :key="i">
          <a v-bind:href="child.path">{{child.title}}</a>
          <themeclean-components-pagelistnested v-bind:model="child"
          v-if="child.hasChildren"></themeclean-components-pagelistnested>
        </li>
      </ul>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

