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
	<div class="wrap">
		<h5>
      {{schema.title}} 
      <button type="button" class="waves-effect waves-light btn-floating" v-on:click="onAddItem">
        <i class="material-icons">add</i>
      </button>
    </h5>
    <ul v-if="schema.multifield" class="collapsible" ref="collapsible">
        <li v-for="(item, index) in value"
            v-bind:class="getItemClass(item, index)">
            <div class="collapsible-header" v-on:click.stop.prevent="onSetActiveItem(index)">
                {{itemName(item, index)}} <i class="material-icons" v-on:click.stop.prevent="onRemoveItem(item, index)">delete</i>
            </div>
            <div class="collapsible-body">
                <vue-form-generator
                  :schema="schema"
                  :model="item"></vue-form-generator>
            </div>
        </li>
        <!--
      <li 
        v-for="(item, index) in [schema.model]"
        v-bind:class="getItemClass(item, index)">
        <div class="collapsible-header" v-on:click.stop.prevent="onSetActiveItem(index)">
          {{itemName(item, index)}} <i class="material-icons" v-on:click.stop.prevent="onRemoveItem(item, index)">delete</i>
        </div>
        <div class="collapsible-body">
          <vue-form-generator
            :schema="schema.fields"
            :model="value[index]"></vue-form-generator>
        </div>
      </li>
      -->
    </ul>
    <ul v-else class="collection-fields">
      <li v-for="(item, index) in value" class="collection-field">
        <vue-form-generator
            :schema="getSchemaForIndex(schema, index)"
            :model="value"></vue-form-generator>
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
//      console.log("value: ", this.value)
//      console.log("schema.items: ", this.schema.items)
      // this.model[this.schema.model] = this.value
//      var model = this.value
      /* if model already has child items, create a schema for each */
      if(this.value){
//        var len = model.length
//    		if(len > 0){
//    			for(var i=0; i<len; i++){
//    				this.schema.items.push({ fields: JSON.parse(JSON.stringify(this.schema.fields.slice(0)))})
//            if(!this.schema.multifield){
//              this.schema.items[i].fields[0].model = this.schema.model + '['+i+']'
//            }
//    			}
//    		}
      } else {
        this.value = []
      }
  	},
    mounted(){
//      console.log('this.model: ', this.model)
      if(this.schema.multifield){
        $(this.$refs.collapsible).collapsible({accordion: false})
      }
    },
    beforeDestroy(){
      $(this.$refs.collapsible).collapsible('destroy')
    },
  	data(){
  		return{
  			activeItem: null
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
  	    getSchemaForIndex(schema, index) {
            const newSchema = JSON.parse(JSON.stringify(schema));
            newSchema.fields[0].model = ''+index
            return newSchema
        },
      getItemClass(item, index){
        if(this.activeItem === index){
          return 'active'
        }
        if(item._opDelete){
          return'deleted'
        }
        return false
      },
      itemName(item, index) {
          if(this.schema.fieldLabel) {
              const len = this.schema.fieldLabel.length
              for(let i=0; i<len; i++){
                  let label = this.schema.fieldLabel[i]
                  let childItem = this.value[index]
                  // console.log('child item: ', childItem)
                  if(childItem[label]){
                      return childItem[label]
                  }
              }
          }
          return parseInt(index) + 1
      },
      onAddItem(e){
//        this.schema.items.push({ fields: JSON.parse(JSON.stringify(this.schema.fields.slice(0)))})
        if(!this.schema.multifield){
//          this.schema.items[this.schema.items.length - 1].fields[0].model = this.schema.model + '['+(this.schema.items.length - 1)+']'
          this.value.push('')
        } else {
            this.value.push({ name: 'n' +Date.now()})
            this.onSetActiveItem(this.value.length - 1)
        }
      },
      onRemoveItem(item, index){
        item._opDelete = true
        let modelItem = this.value[index]
        modelItem._opDelete = true
//        this.$set(this.schema.items, index, item)
        this.$set(this.value, index, modelItem)
      },
      onSetActiveItem(index){
        if(index === this.activeItem){
          $(this.$refs.collapsible).collapsible('close', this.activeItem)
          this.activeItem = null
        } else {
          this.$nextTick(function () {
            if(this.activeItem !== null){
              $(this.$refs.collapsible).collapsible('close', this.activeItem)
            }
            $(this.$refs.collapsible).collapsible('open', index)
            this.activeItem = index
            // focus first field of expanded item
            let firstField = this.$refs.collapsible.querySelector('li.active input')
            firstField.focus()
          })
        }
      }

    }
  }
</script>
