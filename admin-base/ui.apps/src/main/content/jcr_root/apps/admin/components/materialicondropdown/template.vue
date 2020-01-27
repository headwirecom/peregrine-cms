<template>
  <div class="drpdwn" v-bind:data-per-path="model.path">
    <button class="drpbtn">
      <a v-bind:title="$i18n('screenDropdown')"  class="btn-floating waves-effect waves-light">
        <i class="material-icons">{{icon}}</i>
      </a>
    </button>
    <div class="drpdwn-content" >
      <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
      </template>
    </div>
  </div>
</template>
<script>
  export default {
    props: ['model'],
    computed:{
      icon: function(){
        let currentState = $perAdminApp.getNodeFromViewOrNull("/state/tools/workspace/view")
        let foundicon = this.model.icon;

        this.model.children.forEach( function(child){
          if (child.target === currentState){
            foundicon = child.icon;
          }
        });
        return foundicon;
      }
    }
  }

</script>

<style scoped>
  /* The dropdown container */
  .drpdwn {
    display: inline-block;
    vertical-align: middle;
    height: 40px;
  }

  /* Dropdown button */
  .drpbtn {
    border: none;
    outline: none;
    background-color: inherit;
    margin: 0; /* Important for vertical align on mobile phones */
    height: inherit;
  }

  /* Dropdown content (hidden by default) */
  .drpdwn-content {
    display: none;
    position: absolute;
    max-width: 54px;
    padding-top: 1px;
  }

  /* Add shadow to dropdown content items*/
  .drpdwn-content span {
    box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
  }

  /* Set line-height for icons inside the navigation bar */
  .material-icons {
    line-height: 40px;
  }

  /* Show the dropdown menu on hover */
  .drpdwn:hover .drpdwn-content {
    display: block;
  }
</style>
