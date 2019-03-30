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
    <vue-multiselect 
      v-if="!schema.preview"
      v-model="modelFromValue" 
      :multiple="false"
      :track-by="trackBy"
      :label="label"
      :deselectLabel="deselectLabel"
      :options="schema.values"
      :searchable="false"
      :taggable="false"
      :clear-on-select="true"
      :close-on-select="true"
      :placeholder="placeholder"
      :allow-empty="allowEmpty"
      :show-labels="false">
    </vue-multiselect>

    <p v-else class="preview-item">{{value}}</p>
  </div>
</template>

<script>
    export default {
      // v-bind="obj" is same as ...obj (object destructuring)
      props: ['model'], 
      mixins: [ VueFormGenerator.abstractField ],
      computed: {
        placeholder () {
          if(this.schema.selectOptions && this.schema.selectOptions.placeholder){
            return this.schema.selectOptions.placeholder
          }
          return 'Nothing selected'
        },
        allowEmpty () {
          return this.schema.required ? false : true
        },
        trackBy () {
          if(this.schema.selectOptions && this.schema.selectOptions.value){
            return this.schema.selectOptions.value
          }
          return 'value'
        },
        label () {
          if(this.schema.selectOptions && this.schema.selectOptions.name){
            return this.schema.selectOptions.name
          }
          return 'name'
        },
        deselectLabel () {
          if(this.schema.selectOptions && this.schema.selectOptions.deselectLabel){
            return this.schema.selectOptions.deselectLabel
          }
          return ''
        },
        modelFromValue: {
          get () {
            // will catch falsy, null or undefined
            if(this.value && this.value != null){
              // if model is a string, convert to object with name and value
              if(typeof this.value === 'string'){ 
                return this.schema.values.filter(item => item.value === this.value)[0]     
              } else {
                return this.value
              }
            } else {
              return ''
            }
          },
          set (newValue) {
            if(newValue && newValue != null){
              this.value = newValue[this.trackBy]
            } else {
              this.value = ''
            }
          }
        }
      }
    }
</script>
