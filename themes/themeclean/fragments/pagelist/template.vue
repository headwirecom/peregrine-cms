<template>
  <themeclean-components-block v-bind:model="model">
    <div>
      <ul>
        <li class="root" v-if="model.includeroot">
          <a v-bind:href="model.rootpage">{{model.rootpageTitle}}</a>
        </li>
        <li class="children">
          <a href="">Page 1</a>
          <ul>
            <li>
              <a href="">Sub Page 1</a>
            </li>
            <li>
              <a href="">Sub Page 1</a>
            </li>
          </ul>
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

