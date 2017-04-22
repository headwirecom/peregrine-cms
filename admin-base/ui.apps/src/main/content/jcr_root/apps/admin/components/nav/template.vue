<template>
<nav>
    <div class="nav-wrapper indigo">
      <div class="brand-logo">
      <admin-components-action v-bind:command="'selectPath'" v-bind:title="'home'" v-bind:target="'/content/admin'"></admin-components-action>
      &nbsp; &gt; {{ vueRoot.adminPage.title}}
      </div>
      <ul id="nav-mobile" class="right hide-on-med-and-down">
        <!-- other tools go here -->
      </ul>
    </div>
  </nav>
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
