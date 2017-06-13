<template>
	<div class="edit-collection" style="flex:1;">
		<h5>{{schema.title}} <button type="button" class="btn btn-primary" v-on:click="onAddItem">add</button></h5>
		<ul class="collapsible">
	    <li v-for="(item, index) in schema.items" v-bind:class="activeItem === index ? 'active' : ''">
	      <div class="collapsible-header" v-on:click="onSetActiveItem(index)">
	      	<i class="material-icons" v-on:click="onRemoveItem(index)">clear</i>Item #{{index}}
	      </div>
	      <div class="collapsible-body">
	      	<vue-form-generator 
		        :schema="item" 
		        :model="model.children[index]"></vue-form-generator>
	      </div>
	    </li>
	</div>
</template>

<script>
  export default {
    mixins: [ VueFormGenerator.abstractField ],
    beforeMount(){
    	/* if model already has child items, create a schema for each */
  		if(this.model.children.length > 0){
  			var len = this.model.children.length
  			for(var i=0; i<len; i++){
  				this.schema.items.push({ fields: this.schema.fields.slice(0)})
  			}
  		}
  	},
  	data(){
  		return{
  			activeItem: 0
  		}
  	},
    computed: {
    	itemModel(){
    		var model = {}
    		this.schema.fields.forEach( (item, index) => {
    			model[item.model] = ''
    		})
    		return model
    	}
    },
    methods: {
      onAddItem(e){
        this.schema.items.push({ fields: this.schema.fields.slice(0)})
        this.model.children.push(Object.assign({}, this.itemModel)) 
      },
      onRemoveItem(index){
        this.schema.items.splice(index, 1)
        this.model.children.splice(index, 1)
      },
      onSetActiveItem(index){
      	this.activeItem = index
      }
    }
  }
</script>
