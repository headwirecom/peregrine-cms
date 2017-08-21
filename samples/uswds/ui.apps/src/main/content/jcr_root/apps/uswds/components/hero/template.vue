<template>
  <section class="usa-hero" v-bind:data-per-path="model.path">
    <div class="usa-grid">
      <div class="usa-hero-callout usa-section-dark">
        <h2>
          <span class="usa-hero-callout-alt">callout</span>
        </h2>
        <a class="usa-hero-link" href="/">link</a>
        <a class="usa-button usa-button-big usa-button-secondary" href="/">button</a>
      </div>
    </div>
  </section>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

