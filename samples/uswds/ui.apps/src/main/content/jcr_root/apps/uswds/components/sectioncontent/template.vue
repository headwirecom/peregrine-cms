<template>
  <div v-bind:data-per-path="model.path">
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'before' }"></pagerendervue-components-placeholder>
    <section class="usa-section">
      <div class="usa-grid">
        <h2>Section heading</h2>
        <p class="usa-font-lead">Everything up to this point should help people understand your agency
          or project: who you are, your goal or mission, and how you approach it.
          Use this section to encourage them to act. Describe why they should get
          in touch here, and use an active verb on the button below. &#x201C;Get
          in touch,&#x201D; &#x201C;Learn more,&#x201D; and so on.</p>
        <a class="usa-button usa-button-big"
        href="#">Call to action</a>
      </div>
    </section>
    <template v-for="child in model.children">
      <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>
    <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after' }"></pagerendervue-components-placeholder>
  </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

