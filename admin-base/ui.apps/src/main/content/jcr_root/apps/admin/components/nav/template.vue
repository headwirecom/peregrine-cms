<template>
<nav v-bind:data-per-path="model.path">
    <div class="nav-wrapper indigo">
      <div class="brand-logo">
      <admin-components-action v-bind:model="{ command: 'selectPath', title: 'home', target: '/content/admin' }"></admin-components-action>
      &nbsp; &gt; {{ vueRoot.adminPage.title}}
      </div>
      <ul id="nav-mobile" class="right hide-on-med-and-down">
        <li>{{this.$root.$data.state.user}}</li>
      </ul>
    </div>
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
