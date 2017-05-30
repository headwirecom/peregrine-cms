<template>
<nav v-bind:data-per-path="model.path" v-bind:class="isExtended ? 'nav-extended' : ''">
    <div class="nav-wrapper blue-grey darken-3">
      <div class="col s12">
        <div class="brand-logo">
          <admin-components-action 
            v-bind:model="{ 
              command: 'selectPath', 
              title: 'home', 
              target: '/content/admin',
              classes: 'peregrine-logo'
            }">
          </admin-components-action>
          <template v-for="item in vueRoot.adminPage.breadcrumbs">
            <admin-components-action
              v-bind:model="{
                command: 'selectPath',
                title: item.title,
                target: item.path
              }"></admin-components-action>
          </template>
        </div>
        <ul id="nav-mobile" class="right hide-on-med-and-down">
            <li><a href="/system/sling/logout?resource=/index.html">{{this.$root.$data.state.user}}</a></li>
        </ul>
      </div>
    </div>
    <template v-for="child in model.children">
        <component v-bind:is="child.component" v-bind:model="child"></component>
    </template>

</nav>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        vueRoot: function() {
            return this.$root
        },
        isExtended: function() {
            return this.model.children && this.model.children.length > 0
        }

    }
}
</script>
