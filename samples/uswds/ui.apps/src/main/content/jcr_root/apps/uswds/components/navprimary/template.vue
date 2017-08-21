<template>
  <span>
    <button class="usa-nav-close">
      <img src="/etc/felibs/uswds/img/close.svg" alt="close">
    </button>
    <ul class="usa-nav-primary usa-accordion">
      <li>
        <button class="usa-accordion-button usa-nav-link" aria-expanded="false"
        aria-controls="test">
          <span>text1</span>
        </button>
        <ul id="test" class="usa-nav-submenu" aria-hidden="true">
          <li>
            <a href="/">text2</a>
          </li>
        </ul>
      </li>
      <li>
        <a class="usa-nav-link" href="/">
          <span>text3</span>
        </a>
      </li>
    </ul>
  </span>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

