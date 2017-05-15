<template>
<nav v-bind:data-per-path="model.path" class="nav-extended">
    <div class="nav-wrapper blue-grey darken-3">
      <div class="brand-logo">
        <admin-components-action 
          v-bind:model="{ 
            command: 'selectPath', 
            title: 'home', 
            target: '/content/admin' 
          }">
        </admin-components-action>
        &raquo; {{ vueRoot.adminPage.title}}
      </div>
      <ul id="nav-mobile" class="right hide-on-med-and-down">
        <li>{{this.$root.$data.state.user}}</li>
      </ul>
    </div>
    <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>

</nav>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        vueRoot: function() {
            return this.$root
        }
    }
}
</script>
