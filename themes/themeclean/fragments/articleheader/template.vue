<template>
  <themeclean-components-block v-bind:model="model">
    <div class="col-12 col-md-8" v-bind:class="model.textalign">
      <h1 v-if="editAndEmpty">Configure Here</h1>
      <h2 class="col-12 p-0 pb-3" v-if="model.showtitle == 'true'">{{model.title}}</h2>
      <h3 class="col-12 p-0 pb-3" v-if="model.showsubtitle == 'true'">{{model.subtitle}}</h3>
    </div>
  </themeclean-components-block>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
        	editAndEmpty() {
                if(!$peregrineApp.isAuthorMode()) return false
                var model = this.model
                for (var property in model) {
                    if (model.hasOwnProperty(property)) {
                        // check if model.prop is false/null
                        if(!model.property || model.property == null || model.property.length === 0){
                            return true
                        } 
                    }
                }
                return false
            }
        }
    }
</script>