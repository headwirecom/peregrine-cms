<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <ul>
        <li class="root" v-if="model.includeroot === 'true'">
          <a v-bind:href="model.rootPageLink">{{model.rootPageTitle}}</a>
        </li>
        <li class="children" v-for="(child,i) in model.childrenPages" :key="i">
          <a v-bind:href="child.path">{{child.title}}</a>
          <themeclean-components-pagelistnested v-bind:model="child"></themeclean-components-pagelistnested>
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

