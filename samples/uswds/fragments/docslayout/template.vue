<template>
  <main class="usa-grid usa-section usa-content usa-layout-docs" id="main-content"
  v-bind:data-per-path="model.path">
    <aside class="usa-width-one-fourth usa-layout-docs-sidenav">
      <ul class="usa-sidenav-list">
        <li>
          <a href="javascript:void(0);">Page title</a>
        </li>
        <li>
          <a class="usa-current" href="javascript:void(0);">Page heading (h1)</a>
          <ul class="usa-sidenav-sub_list">
            <li>
              <a href="#section-heading-h2">Section heading (h2)</a>
            </li>
            <li>
              <a href="#section-heading-h3">Subsection heading (h3)</a>
            </li>
            <li>
              <a href="#section-heading-h4">Subsection heading (h4)</a>
            </li>
          </ul>
        </li>
        <li>
          <a href="javascript:void(0);">Page title</a>
        </li>
      </ul>
    </aside>
    <div class="usa-width-three-fourths usa-layout-docs-main_content">
      <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'before' }"></pagerendervue-components-placeholder>
      <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
      </template>
      <pagerendervue-components-placeholder v-bind:model="{ path: model.path, component: model.component, location: 'after' }"></pagerendervue-components-placeholder>
    </div>
  </main>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

