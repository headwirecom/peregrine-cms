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
    <label>{{schema.title}} </label>
    <ul v-if="!schema.preview" class="collapsible" v-bind:class="schema.multifield ? 'multifield' : 'singlefield'" ref="collapsible">
        <li v-for="(item, index) in value" v-bind:class="getItemClass(item, index)" v-bind:key="item.path"> {{item._opDelete}}
            <div 
              class="collapsible-header" 
              draggable="true" 
              v-on:dragstart = "onDragStart(item, index, $event)"
              v-on:dragover.prevent  ="onDragOver"
              v-on:dragenter.prevent ="onDragEnter"
              v-on:dragleave.prevent ="onDragLeave"
              v-on:drop.prevent      ="onDrop(item, index, $event)"
              v-on:click.stop.prevent="onSetActiveItem(index)">
                <i class="material-icons">drag_handle</i>
                <span v-if="schema.multifield">{{itemName(item, index)}}</span> 
                <input
                  v-else
                  v-model="value[index]">
                <i class="material-icons delete-icon" v-on:click="onRemoveItem(item, index)">delete</i>
            </div>
            <transition
              v-on:enter="enter"
              v-on:leave="leave"
              v-bind:css="false">
              <div v-if="schema.multifield && activeItem === index" class="collapsible-body">
                  <vue-form-generator
                    :schema="schema"
                    :model="prepModel(item, schema)"></vue-form-generator>
              </div>
            </transition>
        </li>
        <button type="button" class="btn-flat btn-add-item" v-on:click="onAddItem">
          <i class="material-icons">add</i>
        </button>
    </ul>
    <ul v-else class="collection">
      <template v-for="(item,i) in value">

        <ul v-if="typeof item === 'object'" class="collection z-depth-1" :key="item.name">
          <vue-form-generator
            v-if="schema.multifield" 
            class="collection-item"
            :schema="schema"
            :model="prepModel(item, schema)"></vue-form-generator>
        </ul>

        <li v-else class="collection-item" :key="item+i">{{item}}</li>

      </template>
    </ul>
	</div>
</template>

<script>
  export default {
    mixins: [ VueFormGenerator.abstractField ],
    beforeMount(){
      if(!this.value) this.value = []
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
        if(!this.schema.multifield) return
        if(this.activeItem === index){
          return 'active'
        }
        if(item._opDelete){
          return 'deleted'
        }
        return false
      },
      itemName(item, index) {
          if(this.schema.fieldLabel) {
              const len = this.schema.fieldLabel.length
              for(let i=0; i<len; i++){
                  let label = this.schema.fieldLabel[i]
                  let childItem = this.value[index]
                  if(childItem[label]){
                      return childItem[label]
                  }
              }
          }
          return parseInt(index) + 1
      },
      prepModel(model, schema) {
            for(let i = 0; i < schema.fields.length; i++) {
                const field = schema.fields[i].model
                if(!model[field]) {
                    Vue.set(model, field, '')
                }
            }
            return model
        },

      onAddItem(e){
        if(!this.schema.multifield){
          var newChild = '';
            if(!this.value || this.value === '')
            {
                Vue.set(this, 'value', new Array())
            }
        } else {
            var newChild = { name: 'n' + Date.now()}
            this.prepModel(newChild, this.schema)
            newChild['sling:resourceType'] = this.schema.resourceType
        }
        this.value.push(newChild)
        // Vue.set(this.value, this.value.length -1, newChild)
        this.onSetActiveItem(this.value.length - 1)
        this.$forceUpdate()
      },
      onRemoveItem(item, index){
        this.value.splice(index, 1)
        if( this.schema.multifield ) {
          if( item.hasOwnProperty('path') ) {
            let _deleted = $perAdminApp.getNodeFromViewWithDefault("/state/tools/_deleted", {});
            let copy = JSON.parse(JSON.stringify(item));
            copy._opDelete = true;
            if(!_deleted[this.schema.model]) _deleted[this.schema.model] = [];
            _deleted[this.schema.model].push(copy)
          }
          this.activeItem = null
        }
      },
      onSetActiveItem(index){
        if(!this.schema.multifield) return
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
            if(firstField) firstField.focus()
          })
        }
      },
      onDragStart(item, index, ev){
        ev.dataTransfer.setData('text', index)
      },
      onDragOver(ev){
        const center = ev.target.offsetHeight / 2 
        ev.target.classList.toggle('drop-after', ev.offsetY > center )
        ev.target.classList.toggle('drop-before', ev.offsetY < center )
      },
      onDragEnter(ev){
      },
      onDragLeave(ev){
        ev.target.classList.remove('drop-after','drop-before')
      },
      onDrop(item, index, ev) {
          ev.target.classList.remove('drop-after','drop-before')
          const oldIndex = parseInt(ev.dataTransfer.getData("text"))
          this.onReorder(oldIndex, index)
      },
      onReorder(old_index, new_index) {
        if (new_index >= this.length) {
            var k = new_index - this.length;
            while ((k--) + 1) {
                this.value.push(undefined);
            }
        }
        this.value.splice(new_index, 0, this.value.splice(old_index, 1)[0]);
        this.$forceUpdate();
      },
      // animations with Velocity.js
      enter: function (el, done) {
        window.Materialize.Vel(el, "slideDown", { duration: 250 })
      },
      leave: function (el, done) {
        window.Materialize.Vel(el, "slideUp", { duration: 250 }, { complete: done })
      }
    }
  }
</script>
