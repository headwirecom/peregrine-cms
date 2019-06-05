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
<transition name="modal">
    <div class="modal-mask" v-on:click.stop.prevent="onCancel">
        <div class="modal-wrapper">
        <div class="pathbrowser iconbrowser modal-container" v-on:click.stop.prevent="onPrevent">
            <header class="iconbrowser-header">
                <div class="iconbrowser-options">  
                    <vue-multiselect 
                        v-model="selectedFamily"
                        deselect-label=""
                        track-by="name"
                        label="name"
                        placeholder="Select one"
                        :options="iconFamilies"
                        :searchable="true"
                        :allow-empty="false">
                    </vue-multiselect>                              
                </div>
                <div class="iconbrowser-filter">
                    <input placeholder="search by icon name"  type="search" v-model="search">
                </div>
                <p class="range-field">
                    <input 
                        type="range" 
                        min="120" 
                        max="400" 
                        v-model="cardSize"/>
                </p>
            </header>
                
            <div class="modal-content"> 
                <div class="col-browse">                    
                    <template v-if="icons && icons.length > 0" v-for="(icon, index) in icons">
                        <div  
                            :key="index"
                            v-if="searchFilter(icon)"
                            :class="isSelected(icon) ? 'item-icon selected' : 'item-icon'"
                            v-bind:style="`width: ${cardSize}px; height: ${cardSize}px`"
                            v-on:click.stop.prevent="selectIcon(icon)"
                            v-bind:title="icon.class">
                            <div class="item-content">
                                <i
                                    v-bind:class="icon.class"
                                    v-bind:style="`font-size: ${cardIconSize(cardSize)}px`">{{icon.text}}</i>
                                <br/>
                                <span class="truncate">{{icon.family === 'material' ? icon.text : icon.class}}</span>
                            </div>
                        </div>
                    </template>
                    <p v-else class="flow-text">No icons.</p>
                </div>
            </div>
            <div class="modal-footer">
                <span class="selected-path">
                    <i v-if="currentIcon" v-bind:class="currentIcon.class">{{currentIcon.text}}</i> {{currentIcon.family === 'material' ? currentIcon.text : currentIcon.class}}
                </span>
                <button type="button" class="modal-action modal-close waves-effect waves-green btn-flat" v-on:click.prevent=
                "onCancel">Cancel</button>
                <button type="button" class="modal-action modal-close waves-effect waves-green btn-flat" v-on:click.prevent=
                "onSelect(currentIcon)">Select</button>
            </div>
        </div>
        </div>
</div>
</transition>
</template>

<script>
    export default {
        props: ['families', 'selectedIcon', 'onSetIcon', 'onCancel', 'onSelect'],
        mounted (){
            if(this.selectedIcon){
                this.currentIcon = this.selectedIcon
                if(this.selectedIcon.family){
                    this.selectedFamily.name = this.selectedIcon.family
                    this.selectedFamily.value = this.camelize(this.selectedIcon.family)
                }
            }
        },
        data () {
            return {
                cardSize: 120,
                search: '',
                selectedFamily: {
                    name: 'all',
                    value: 'all'
                },
                currentIcon: {
                    family: null,
                    class: null,
                    text: null
                }
            }
        },
        computed: {
            icons () {
                if(this.selectedFamily.name === 'all'){
                    return peregrineFontFamilies
                }
                return peregrineFontFamilies
                    .map(icon => icon)
                    .filter(icon => icon.family === this.selectedFamily.value)
            },
            iconFamilies () {
                return [{name: 'all', value: '*'}].concat(this.families)
            }
        },
        methods: {
            onPrevent () {
                return false
            },
            searchFilter (icon) {
                if(this.search.length === 0) return true
                if(icon.family === 'material'){
                     return (icon.text.indexOf(this.search) >= 0)
                }
                return (icon.class.indexOf(this.search) >= 0)
            },
            isSelected (icon) {
                if(icon.family === 'material'){
                    return this.currentIcon.text === icon.text
                }
                return this.currentIcon.class === icon.class
            },
            selectIcon (icon) {
                this.currentIcon = icon
            },
            cardIconSize (cardSize) {
                return Math.floor(cardSize/3)
            },
            camelize (str) {
                return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {
                    return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
                }).replace(/\s+/g, '');
            }
        }
    }
</script>
