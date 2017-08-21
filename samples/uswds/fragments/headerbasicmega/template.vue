<template>
  <div v-bind:data-per-path="model.path">
    <header class="usa-header usa-header-basic usa-header-basic-megamenu"
    role="banner">
      <div class="usa-nav-container">{% render '@site-title', {header: header}, true %}
        <nav role="navigation"
        class="usa-nav">{% render '@nav-primary', {nav: header.primary}, true %}</nav>
      </div>
    </header>
    <div class="usa-overlay"></div>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

