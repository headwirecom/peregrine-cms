<template>
<div :class="`spinner ${position}`" :style="`width: ${width}px`">
  <svg class="circular" viewBox="25 25 50 50">
    <circle class="path" cx="50" cy="50" r="20" fill="none" stroke-width="2" stroke-miterlimit="10"/>
  </svg>
</div>

</template>

<script>
export default {
  props: ['width', 'position']
}
</script>