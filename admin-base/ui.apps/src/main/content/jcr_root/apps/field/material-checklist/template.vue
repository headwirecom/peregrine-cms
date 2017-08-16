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

    <ul v-if="schema.listBox && !schema.preview" class="check-list" :disabled="disabled">
      <li v-for="item in items">
        <span>
          <input :id="`material-checklist-${getItemName(item)}`" type="checkbox" :checked="isItemChecked(item)" :disabled="disabled" @change="onChanged($event, item)"/>
          <label :for="`material-checklist-${getItemName(item)}`">{{ getItemName(item) }}</label>
        </span>
      </li>
    </ul>

    <div v-if="!schema.listBox && !schema.preview" class="select-wrapper checklist-dropdown" :class="{ expanded: isExpanded }">
      <span class="caret"></span>
      <input 
        ref="selectInput"
        type="text" 
        class="form-control select-dropdown" 
        :value="dropdownValue"
        @click="onExpandCombo" />
      <template v-if="isExpanded">  
        <ul 
          class="dropdown-content select-dropdown">
          <li class="disabled">
            <span>{{ dropdownValue }}</span>
          </li>
          <li  
            v-for="item in items" 
            :class="{'is-checked': isItemChecked(item)}">
            <span>
              <input :id="`material-checklist-${getItemName(item)}`" type="checkbox" :checked="isItemChecked(item)" :disabled="disabled" @change="onChanged($event, item)"/>
              <label :for="`material-checklist-${getItemName(item)}`">{{ getItemName(item) }}</label>
            </span>
          </li>
        </ul>
        <div class="dropdown-bg" @click="onExpandCombo"></div>
      </template>
    </div>

    <ul v-if="schema.preview" class="preview-list">
      <li v-for="item in items" v-if="isItemChecked(item)" class="preview-item">{{ getItemName(item) }}</li>
    </ul>
</template>

<script>
  export default {
    mixins: [ VueFormGenerator.abstractField ], 
    data() {
      return {
        isExpanded: false
      };
    },
    computed: {
      items() {
        let values = this.schema.values;
        if (typeof(values) == "function") {
          return values.apply(this, [this.model, this.schema]);
        } else
          return values;
      },
      selectedCount() {
        if (this.value)
          return this.value.length;

        return 0;
      }, 
      dropdownValue(){
        return this.selectedCount + ' selected'
      } 
    },
    methods: {
      getItemValue(item) {
        if (_.isObject(item)){
          if (typeof this.schema["checklistOptions"] !== "undefined" && typeof this.schema["checklistOptions"]["value"] !== "undefined") {
            return item[this.schema.checklistOptions.value];
          } else {
            if (typeof item["value"] !== "undefined") {
              return item.value;
            } else {
              throw "`value` is not defined. If you want to use another key name, add a `value` property under `checklistOptions` in the schema. https://icebob.gitbooks.io/vueformgenerator/content/fields/checklist.html#checklist-field-with-object-values";
            }
          }
        } else {
          return item;
        }
      },
      getItemName(item) {
        if (_.isObject(item)){
          if (typeof this.schema["checklistOptions"] !== "undefined" && typeof this.schema["checklistOptions"]["name"] !== "undefined") {
            return item[this.schema.checklistOptions.name];
          } else {
            if (typeof item["name"] !== "undefined") {
              return item.name;
            } else {
              throw "`name` is not defined. If you want to use another key name, add a `name` property under `checklistOptions` in the schema. https://icebob.gitbooks.io/vueformgenerator/content/fields/checklist.html#checklist-field-with-object-values";
            }
          }
        } else {
          return item;
        }
      },
      isItemChecked(item) {
        return (this.value && this.value.indexOf(this.getItemValue(item)) != -1);
      },
      onChanged(event, item) {
        if (_.isNil(this.value) || !Array.isArray(this.value)){
          this.value = [];
        }

        if (event.target.checked) {
          // Note: If you modify this.value array, it won't trigger the `set` in computed field
          const arr = _.clone(this.value);
          arr.push(this.getItemValue(item));
          this.value = arr;
        } else {
          // Note: If you modify this.value array, it won't trigger the `set` in computed field
          const arr = _.clone(this.value);
          arr.splice(this.value.indexOf(this.getItemValue(item)), 1);
          this.value = arr;
        }
      },
      onExpandCombo() {
        this.isExpanded = !this.isExpanded;       
      }
    }
  };
</script>
