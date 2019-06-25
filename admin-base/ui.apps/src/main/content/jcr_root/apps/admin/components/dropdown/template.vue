<template>
  <div class="dropdown">
    <button class="dropbtn">{{model.title}}
      <i class="fa fa-caret-down"></i>
    </button>
    <div class="dropdown-content">
      <template v-for="child in model.children">
        <div v-bind:is="child.component" v-bind:model="child"></div>
      </template>
    </div>
  </div>
</template>
<script>
  export default {
    props: ['model']
  }
</script>

<style scoped>
  /* The dropdown container */
  .dropdown {
    float: left;
    overflow: hidden;
  }

  /* Dropdown button */
  .dropdown .dropbtn {
    font-size: 16px;
    border: none;
    outline: none;
    color: white;
    padding: 14px 16px;
    background-color: inherit;
    font-family: inherit; /* Important for vertical align on mobile phones */
    margin: 0; /* Important for vertical align on mobile phones */
  }

  /* Add a red background color to navbar links on hover */
  .navbar a:hover, .dropdown:hover .dropbtn {
    background-color: red;
  }

  /* Dropdown content (hidden by default) */
  .dropdown-content {
    display: none;
    position: absolute;
    background-color: #f9f9f9;
    min-width: 160px;
    box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
    z-index: 1;
  }

  /* Links inside the dropdown */
  .dropdown-content a {
    float: none;
    color: black;
    padding: 12px 16px;
    text-decoration: none;
    display: block;
    text-align: left;
  }

  /* Add a grey background color to dropdown links on hover */
  .dropdown-content a:hover {
    background-color: #ddd;
  }

  /* Show the dropdown menu on hover */
  .dropdown:hover .dropdown-content {
    display: block;
  }
</style>
