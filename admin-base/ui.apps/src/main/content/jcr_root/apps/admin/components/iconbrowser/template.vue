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
<div id="iconBrowserModal" class="pathbrowser iconbrowser modal modal-fixed-footer">
    <header class="iconbrowser-header">
        <ul class="iconbrowser-options">
            <li>                    
                <button  
                    type="button" 
                    ref="familyDropdownBtn" 
                    class="dropdown-button" 
                    data-activates='familyDropdown'>{{selectedFamily}}</button>
                <ul id="familyDropdown" class="dropdown-content">
                    <li>
                        <a href="#!" v-on:click="selectFamily('all')">all</a>
                    </li>
                    <li 
                        v-for="family in families" 
                        :key="family">
                        <a href="#!" v-on:click="selectFamily(family)">{{family}}</a>
                    </li>
                </ul>                
            </li>
            <li>
                <input placeholder="search"  type="text" v-model="search">
            </li>
        </ul>
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
            <template v-if="list.length > 0">
                <isotope 
                    ref="isotope" 
                    class="isotopes" 
                    v-bind:options="getIsotopeOptions()"
                    v-bind:list="list">
                    <div 
                        v-for="(icon, index) in list" 
                        :key="index">
                            <div 
                                v-if="searchFilter(icon)"
                                :class="isSelected(icon) ? 'item-file selected' : 'item-file'"
                                :title="icon"
                                v-bind:style="`width: ${cardSize}px; height: ${cardSize}px`"
                                v-on:click.stop.prevent="selectIcon(icon)"
                                v-bind:title="icon">
                                <div class="item-content">
                                    <i 
                                        class="material-icons"
                                        :style="`font-size: ${cardIconSize(cardSize)}px`">{{icon}}</i>
                                    <br/>
                                    <span class="truncate">{{icon}}</span>
                                </div>
                            </div>
                        
                    </div>
                </isotope>
            </template>
            <p v-else class="flow-text">No icons.</p>
        </div>
    </div>
    <div class="modal-footer">
        <span class="selected-path"><i class="material-icons">{{selectedIcon}}</i> {{selectedIcon}}</span>
        <button type="button" class="modal-action modal-close waves-effect waves-green btn-flat" v-on:click.prevent=
        "onModalCancel">Cancel</button>
        <button type="button" class="modal-action modal-close waves-effect waves-green btn-flat" v-on:click.prevent=
        "onModalSelect">Select</button>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        mounted(){
            $(this.$refs.familyDropdownBtn).dropdown({
                inDuration: 300,
                outDuration: 225,
                constrainWidth: false, // Does not change width of dropdown to that of the activator
                hover: false, // Activate on hover
                gutter: 0, // Spacing from edge
                belowOrigin: false, // Displays dropdown below the button
                alignment: 'left', // Displays dropdown with edge aligned to the left of button
                stopPropagation: false // Stops event propagation
            });
        },
        data: function() {
            return {
                filterBy: '',
                sortBy: '',
                selectedFamily: 'all',
                cardSize: 120,
                search: '',
                icons: {
                    fontAwesome: [

                    ],
                    materialIcons:[
                        '3d_rotation',
                        'accessibility',
                        'accessible',
                        'account_balance',
                        'account_balance_wallet',
                        'account_box',
                        'account_circle',
                        'add_shopping_cart',
                        'alarm',
                        'alarm_add',
                        'alarm_off',
                        'alarm_on',
                        'all_out',
                        'android',
                        'announcement',
                        'aspect_ratio',
                        'assessment',
                        'assignment',
                        'assignment_ind',
                        'assignment_late',
                        'assignment_return',
                        'assignment_returned',
                        'assignment_turned_in',
                        'autorenew',
                        'backup',
                        'book',
                        'bookmark',
                        'bookmark_border',
                        'bug_report',
                        'build',
                        'cached',
                        'camera_enhance',
                        'card_giftcard',
                        'card_membership',
                        'card_travel',
                        'change_history',
                        'check_circle',
                        'chrome_reader_mode',
                        'class',
                        'code',
                        'compare_arrows',
                        'copyright',
                        'credit_card',
                        'dashboard',
                        'date_range',
                        'delete',
                        'delete_forever',
                        'description',
                        'dns',
                        'done',
                        'done_all',
                        'donut_large',
                        'donut_small',
                        'eject',
                        'euro_symbol',
                        'event',
                        'event_seat',
                        'exit_to_app',
                        'explore',
                        'extension',
                        'face',
                        'favorite',
                        'favorite_border',
                        'feedback',
                        'find_in_page',
                        'find_replace',
                        'fingerprint',
                        'flight_land',
                        'flight_takeoff',
                        'flip_to_back',
                        'flip_to_front',
                        'g_translate',
                        'gavel',
                        'get_app',
                        'gif',
                        'grade',
                        'group_work',
                        'help',
                        'help_outline',
                        'highlight_off',
                        'history',
                        'home',
                        'hourglass_empty',
                        'hourglass_full',
                        'http',
                        'https',
                        'important_devices',
                        'info',
                        'info_outline',
                        'input',
                        'invert_colors',
                        'label',
                        'label_outline',
                        'language',
                        'launch',
                        'lightbulb_outline',
                        'line_style',
                        'line_weight',
                        'list',
                        'lock',
                        'lock_open',
                        'lock_outline',
                        'loyalty',
                        'markunread_mailbox',
                        'motorcycle',
                        'note_add',
                        'offline_pin',
                        'opacity',
                        'open_in_browser',
                        'open_in_new',
                        'open_with',
                        'pageview',
                        'pan_tool',
                        'payment',
                        'perm_camera_mic',
                        'perm_contact_calendar',
                        'perm_data_setting',
                        'perm_device_information',
                        'perm_identity',
                        'perm_media',
                        'perm_phone_msg',
                        'perm_scan_wifi',
                        'pets',
                        'picture_in_picture',
                        'picture_in_picture_alt',
                        'play_for_work',
                        'polymer',
                        'power_settings_new',
                        'pregnant_woman',
                        'print',
                        'query_builder',
                        'question_answer',
                        'receipt',
                        'record_voice_over',
                        'redeem',
                        'remove_shopping_cart',
                        'reorder',
                        'report_problem',
                        'restore',
                        'restore_page',
                        'room',
                        'rounded_corner',
                        'rowing',
                        'schedule',
                        'search',
                        'settings',
                        'settings_applications',
                        'settings_backup_restore',
                        'settings_bluetooth',
                        'settings_brightness',
                        'settings_cell',
                        'settings_ethernet'
                    ]
                }
            }
        },
        watch: {
            cardSize: function (newCardSize) {
                this.updateIsotopeLayout('masonry')
            },
            search: function (newSearch) {
                this.updateIsotopeLayout('masonry')
            }
        },
        computed: {
            selectedIcon(){
                return $perAdminApp.getNodeFromViewOrNull('/state/iconbrowser/selected')
            },
            families(){
                var iconFamilies = [ 'all']
                for (var property in this.icons) {
                    if (this.icons.hasOwnProperty(property)) {
                        iconFamilies.push(property)
                    }
                }
                return iconFamilies
            },
            list(){
                var selectedFamily = this.camelize(this.selectedFamily)
                if(selectedFamily === 'all'){
                    console.log('selected all families')
                    var icons = []
                    for (var property in this.icons) {
                        if (this.icons.hasOwnProperty(property)) {
                            icons = icons.concat(this.icons[property])
                        }
                    }
                    console.log('icons: ', icons)
                    return icons
                } else {
                    console.log('selected family: ', selectedFamily)
                    return this.icons[selectedFamily]
                }
            }
        },
        methods: {
            searchFilter(icon) {
                if(this.search.length === 0) return true
                return (icon.indexOf(this.search) >= 0)
            },
            selectFamily(family){
                $perAdminApp.getNodeFromView('/state/iconbrowser').family = family
            },
            isSelected(icon) {
                return this.selectedIcon === icon 
            },
            selectIcon(icon) {
                console.log('selectIcon: ', icon)
                $perAdminApp.getNodeFromView('/state/iconbrowser').selected = icon
            },
            cardIconSize: function(cardSize){
                return Math.floor(cardSize/3)
            },
            updateIsotopeLayout: function(layout){
                this.$nextTick(function () {
                    this.$refs.isotope.layout(layout)
                })
            },
            getFamily(){
                return
            },
            getIsotopeOptions: function() {
                return {
                    layoutMode: 'masonry',
                    itemSelector: '.card',
                    stamp: '.stamp',
                    masonry: {
                        gutter: 15
                    }
                    ,
                    getSortData: {
                        icon: function(icon){
                            return icon.toLowerCase()    
                        },
                        // family: function(icon){
                        //     return Date.parse(itemElem.created)
                        // }
                    },
                    getFilterData:{
                        fontAwesome: icon => this.getFamily(icon) === 'font awesome',
                        materialDesign: icon => this.getFamily(icon) === 'material icons'
                    }
                }
            },
            onSort(sortType){
                this.sortBy = sortType
                this.$refs.isotope.sort(sortType)
            },
            onFilter(filterType){
                this.filterBy = filterType
                this.$refs.isotope.filter(filterType)
            },
            camelize(str) {
                return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function(letter, index) {
                    return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
                }).replace(/\s+/g, '');
            },
            onModalSelect(){
                $('#iconBrowserModal').modal('close')
            },
            onModalCancel(){
                // set selected icon to original icon
                $perAdminApp.getNodeFromView('/state/iconbrowser').selected = $perAdminApp.getNodeFromView('/state/iconbrowser/original')
                $('#iconBrowserModal').modal('close')
            }
        }
    }
</script>
