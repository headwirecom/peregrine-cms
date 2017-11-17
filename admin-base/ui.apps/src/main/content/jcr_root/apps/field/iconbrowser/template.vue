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
      <template v-if="!schema.preview">
        <input
          :id="getFieldID(schema)"
          type="text"
          :value="value"
          :disabled="disabled"
          :maxlength="schema.max"
          :placeholder="schema.placeholder"
          :readonly="schema.readonly"
          @input="value = $event.target.value" />
        <button v-on:click.stop.prevent="browse" class="btn-flat">
          <i class="material-icons">insert_drive_file</i>
        </button>
        <div class="icon">
            <i v-bind:class="selectedIcon.class">{{selectedIcon.text}}</i>
        </div>
        <admin-components-iconbrowser 
            v-if="isOpen"
            :families="families"
            :selectedIcon="selectedIcon"
            :onCancel="onCancel"
            :onSelect="onSelect">
        </admin-components-iconbrowser>
      </template>
      <p v-else><i v-bind:class="selectedIcon.class">{{selectedIcon.text}}</i>{{value}}</p>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        mixins: [ VueFormGenerator.abstractField ],
        data () {
            return {
                isOpen: false,
                families: null,
                selectedIcon: {
                    family: null,
                    class: null,
                    text: null
                }
            }
        },
        created () {
            // set initial state from value
            this.selectedIcon = this.getIconFromValue(this.value)
            // create families array from schema
            this.families = this.schema.families.map(family => {
                return {
                    name: family,
                    value: this.camelize(family)
                }
            })
        },
        watch: {
            value (newValue) {
                // keep selectedIcon synced with value (if valid)
                this.selectedIcon = this.getIconFromValue(newValue)
            }
        },
        methods: {
            onCancel () {
                this.isOpen = false
            },
            onSelect (icon) {
                this.value = `${icon.family}:${icon.class}:${icon.text}`
                this.isOpen = false
            },
            browse () {
                this.isOpen = true
            },
            getIconFromValue (value) {
                let iconParts = value.split(':') //[iconFamily, iconClass, iconText]
                if(iconParts && iconParts.length > 2){
                    return {
                        family: iconParts[0],
                        class: iconParts[1],
                        text: iconParts[2]
                    }
                }
                return this.selectedIcon
            },
            camelize (str) {
                return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {
                    return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
                }).replace(/\s+/g, '');
            }
        }
    }
</script>
