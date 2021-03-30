<template>
  <div class="progress">
    <div class="indeterminate"></div>
  </div>
</template>

<script>
export default {
  name: 'LinearPreloader'
}
</script>

<style scoped>
.progress {
  background-color: var(--pcms-gray);
}

.progress .indeterminate {
  background-color: var(--pcms-blue-grey-darken-3);
}
</style>