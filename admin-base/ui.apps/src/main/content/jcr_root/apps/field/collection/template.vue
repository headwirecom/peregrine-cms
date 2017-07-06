<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
	<div class="edit-collection">
		<h5>
      {{schema.title}} 
      <button type="button" class="waves-effect waves-light btn-floating" v-on:click="onAddItem">
        <i class="material-icons">add</i>
      </button>
    </h5>
    <ul v-if="schema.multifield" class="collapsible">
      <li v-for="(item, index) in schema.items" v-bind:class="activeItem === index ? 'active' : ''">
        <div class="collapsible-header" v-on:click="onSetActiveItem(index)">
          <i class="material-icons" v-on:click="onRemoveItem(index)">clear</i>Item #{{index}}
        </div>
        <div class="collapsible-body">
          <vue-form-generator
            :schema="item"
            :model="value[index]"></vue-form-generator>
        </div>
      </li>
    </ul>
    <ul v-else class="collection-fields">
      <li v-for="(item, index) in schema.items" class="collection-field">
        <vue-form-generator
            :schema="item"
            :model="{[schema.model]: value}"></vue-form-generator>
        <button v-on:click.stop.prevent="onRemoveItem(index)" class="waves-effect waves-light btn-flat">
          <i class="material-icons">delete</i>
        </button>
      </li>
    </ul>
	</div>
</template>

<script>
  export default {
    mixins: [ VueFormGenerator.abstractField ],
    beforeMount(){
      console.log("value: ", this.value)
      console.log("schema.items: ", this.schema.items)
      // this.model[this.schema.model] = this.value
      var model = this.value
      /* if model already has child items, create a schema for each */
      if(model){
        var len = model.length
    		if(len > 0){
    			for(var i=0; i<len; i++){
    				this.schema.items.push({ fields: JSON.parse(JSON.stringify(this.schema.fields.slice(0)))})
            if(!this.schema.multifield){
              this.schema.items[i].fields[0].model = this.schema.model + '['+i+']'
            }
    			}
    		}
      } else {
        this.value = []
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
    		model.name = 'n' + Date.now()
    		return model
    	}
    },
    methods: {
      onAddItem(e){
        console.log('itemModel: ', this.itemModel)
        this.schema.items.push({ fields: JSON.parse(JSON.stringify(this.schema.fields.slice(0)))})
        if(!this.schema.multifield){
          this.schema.items[this.schema.items.length - 1].fields[0].model = this.schema.model + '['+(this.schema.items.length - 1)+']'
          this.value.push('')
        }
      },
      onRemoveItem(index){
        this.schema.items.splice(index, 1)
        this.value.splice(index, 1)
      },
      onSetActiveItem(index){
      	this.activeItem = index
      }

    }
  }
</script>
