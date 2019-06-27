<template>
  <div class="drpdwn" v-bind:data-per-path="model.path">
    <button class="drpbtn">
      <i class="material-icons">{{icon}}</i>
    </button>
    <div class="drpdwn-content" >
      <template v-for="child in model.children">
        <template v-if="child.className==drpdwn-content">
          <template v-for="child in child.children">
            <div v-bind:is="child.component" v-bind:model="child" v-on:click="icon=child.icon"></div>
          </template>
        </template>
      </template>
    </div>
  </div>
</template>
<script>
  export default {
    props: ['model'],
    data() {
      return {
        icon: this.model.icon
      }
    }
  }

</script>

<style scoped>
  /* The dropdown container */
  .drpdwn {
    overflow: hidden;
    display: inline-block;
    vertical-align: middle;
    height: 40px;
  }

  /* Dropdown button */
  .drpdwn .drpbtn {
    font-size: 16px;
    border: none;
    outline: none;
    color: white;
    background-color: inherit;
    font-family: inherit; /* Important for vertical align on mobile phones */
    margin: 0; /* Important for vertical align on mobile phones */
    height: inherit;
  }

  /* Add a red background color to navbar links on hover */
  .navbar a:hover, .dropdown:hover .dropbtn {
    background-color:white;
  }

  /* Dropdown content (hidden by default) */
  .drpdwn-content {
    display: none;
    position: absolute;
    background-color: #f9f9f9;
    min-width: 160px;
    box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
    z-index: 1;
  }

  /* Links inside the dropdown */
  .drpdwn-content a {
    float: none;
    color: black;
    padding: 12px 16px;
    text-decoration: none;
    display: block;
    text-align: left;
  }

  /* Add a grey background color to dropdown links on hover */
  .drpdwn-content a:hover {
    background-color: #ddd;
  }

  /* Show the dropdown menu on hover */
  .drpdwn:hover .drpdwn-content {
    display: block;
  }

  nav i.material-icons {
    line-height: 40px;
  }
</style>
