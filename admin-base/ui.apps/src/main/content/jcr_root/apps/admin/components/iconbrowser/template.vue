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
        <div class="iconbrowser-options">                  
            <button  
                type="button" 
                ref="familyDropdownBtn" 
                class="dropdown-button" 
                data-activates='familyDropdown'>
                {{selectedFamily}}
            </button>
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
        </div>
        <div class="iconbrowser-filter">
            <input placeholder="search"  type="text" v-model="search">
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
                                :class="isSelected(icon) ? 'item-icon selected' : 'item-icon'"
                                v-bind:style="`width: ${cardSize}px; height: ${cardSize}px`"
                                v-on:click.stop.prevent="selectIcon(icon)"
                                v-bind:title="icon">
                                <div class="item-content">
                                    <i
                                        v-bind:class="getIconClass(icon)"
                                        v-bind:style="`font-size: ${cardIconSize(cardSize)}px`">{{getIconText(icon)}}</i>
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
        <span class="selected-path">
            <i v-if="selectedIcon" v-bind:class="getIconClass(selectedIcon)">{{getIconText(selectedIcon)}}</i> {{selectedIcon}}
        </span>
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
                        'fa-adjust',
                        'fa-adn',
                        'fa-align-center',
                        'fa-align-justify',
                        'fa-align-left',
                        'fa-align-right',
                        'fa-ambulance',
                        'fa-anchor',
                        'fa-android',
                        'fa-angle-double-down',
                        'fa-angle-double-left',
                        'fa-angle-double-right',
                        'fa-angle-double-up',
                        'fa-angle-down',
                        'fa-angle-left',
                        'fa-angle-right',
                        'fa-angle-up',
                        'fa-apple',
                        'fa-archive',
                        'fa-arrow-circle-down',
                        'fa-arrow-circle-left',
                        'fa-arrow-circle-o-down',
                        'fa-arrow-circle-o-left',
                        'fa-arrow-circle-o-right',
                        'fa-arrow-circle-o-up',
                        'fa-arrow-circle-right',
                        'fa-arrow-circle-up',
                        'fa-arrow-down',
                        'fa-arrow-left',
                        'fa-arrow-right',
                        'fa-arrow-up',
                        'fa-asterisk',
                        'fa-backward',
                        'fa-ban',
                        'fa-bar-chart-o',
                        'fa-barcode',
                        'fa-beer',
                        'fa-bell',
                        'fa-bell-o',
                        'fa-bitbucket',
                        'fa-bitbucket-square',
                        'fa-bitcoin',
                        'fa-bold',
                        'fa-bolt',
                        'fa-book',
                        'fa-bookmark',
                        'fa-bookmark-o',
                        'fa-briefcase',
                        'fa-btc',
                        'fa-bug',
                        'fa-building',
                        'fa-bullhorn',
                        'fa-bullseye',
                        'fa-calendar',
                        'fa-calendar-o',
                        'fa-camera',
                        'fa-camera-retro',
                        'fa-caret-down',
                        'fa-caret-left',
                        'fa-caret-right',
                        'fa-caret-square-o-down',
                        'fa-caret-square-o-left',
                        'fa-caret-square-o-right',
                        'fa-caret-square-o-up',
                        'fa-caret-up',
                        'fa-certificate',
                        'fa-chain',
                        'fa-chain-broken',
                        'fa-check',
                        'fa-check-circle',
                        'fa-check-circle-o',
                        'fa-check-square',
                        'fa-check-square-o',
                        'fa-chevron-circle-down',
                        'fa-chevron-circle-left',
                        'fa-chevron-circle-right',
                        'fa-chevron-circle-up',
                        'fa-chevron-down',
                        'fa-chevron-left',
                        'fa-chevron-right',
                        'fa-chevron-up',
                        'fa-circle',
                        'fa-circle-o',
                        'fa-clipboard',
                        'fa-clock-o',
                        'fa-cloud',
                        'fa-cloud-download',
                        'fa-cloud-upload',
                        'fa-cny',
                        'fa-code',
                        'fa-code-fork',
                        'fa-coffee',
                        'fa-cog',
                        'fa-cogs',
                        'fa-columns',
                        'fa-comment',
                        'fa-comment-o',
                        'fa-comments',
                        'fa-comments-o',
                        'fa-compass',
                        'fa-copy',
                        'fa-credit-card',
                        'fa-crop',
                        'fa-crosshairs',
                        'fa-css3',
                        'fa-cut',
                        'fa-cutlery',
                        'fa-dashboard',
                        'fa-dedent',
                        'fa-desktop',
                        'fa-dollar',
                        'fa-dot-circle-o',
                        'fa-download',
                        'fa-dribbble',
                        'fa-dropbox',
                        'fa-edit',
                        'fa-eject',
                        'fa-envelope',
                        'fa-envelope-o',
                        'fa-eraser',
                        'fa-eur',
                        'fa-euro',
                        'fa-exchange',
                        'fa-exclamation',
                        'fa-exclamation-circle',
                        'fa-exclamation-triangle',
                        'fa-external-link',
                        'fa-external-link-square',
                        'fa-eye',
                        'fa-eye-slash',
                        'fa-facebook',
                        'fa-facebook-square',
                        'fa-fast-backward',
                        'fa-fast-forward',
                        'fa-female',
                        'fa-fighter-jet',
                        'fa-file',
                        'fa-file-o',
                        'fa-file-text',
                        'fa-file-text-o',
                        'fa-files-o',
                        'fa-film',
                        'fa-filter',
                        'fa-fire',
                        'fa-fire-extinguisher',
                        'fa-flag',
                        'fa-flag-checkered',
                        'fa-flag-o',
                        'fa-flash',
                        'fa-flask',
                        'fa-flickr',
                        'fa-floppy-o',
                        'fa-folder',
                        'fa-folder-o',
                        'fa-folder-open',
                        'fa-folder-open-o',
                        'fa-font',
                        'fa-forward',
                        'fa-foursquare',
                        'fa-frown-o',
                        'fa-gamepad',
                        'fa-gavel',
                        'fa-gbp',
                        'fa-gear',
                        'fa-gears',
                        'fa-gift',
                        'fa-github',
                        'fa-github-alt',
                        'fa-github-square',
                        'fa-gittip',
                        'fa-glass',
                        'fa-globe',
                        'fa-google-plus',
                        'fa-google-plus-square',
                        'fa-group',
                        'fa-h-square',
                        'fa-hand-o-down',
                        'fa-hand-o-left',
                        'fa-hand-o-right',
                        'fa-hand-o-up',
                        'fa-headphones',
                        'fa-heart',
                        'fa-heart-o',
                        'fa-home',
                        'fa-html5',
                        'fa-inbox',
                        'fa-indent',
                        'fa-info',
                        'fa-info-circle',
                        'fa-inr',
                        'fa-instagram',
                        'fa-italic',
                        'fa-jpy',
                        'fa-key',
                        'fa-keyboard-o',
                        'fa-krw',
                        'fa-laptop',
                        'fa-leaf',
                        'fa-legal',
                        'fa-lemon-o',
                        'fa-level-down',
                        'fa-level-up',
                        'fa-lightbulb-o',
                        'fa-link',
                        'fa-linkedin',
                        'fa-linkedin-square',
                        'fa-linux',
                        'fa-list',
                        'fa-list-alt',
                        'fa-list-ol',
                        'fa-list-ul',
                        'fa-location-arrow',
                        'fa-lock',
                        'fa-long-arrow-down',
                        'fa-long-arrow-left',
                        'fa-long-arrow-right',
                        'fa-long-arrow-up',
                        'fa-magic',
                        'fa-magnet',
                        'fa-mail-forward',
                        'fa-mail-reply',
                        'fa-mail-reply-all',
                        'fa-male',
                        'fa-map-marker',
                        'fa-maxcdn',
                        'fa-medkit',
                        'fa-meh-o',
                        'fa-microphone',
                        'fa-microphone-slash',
                        'fa-minus',
                        'fa-minus-circle',
                        'fa-minus-square',
                        'fa-minus-square-o',
                        'fa-mobile',
                        'fa-mobile-phone',
                        'fa-money',
                        'fa-moon-o',
                        'fa-music',
                        'fa-outdent',
                        'fa-pagelines',
                        'fa-paperclip',
                        'fa-paste',
                        'fa-pause',
                        'fa-pencil',
                        'fa-pencil-square',
                        'fa-pencil-square-o',
                        'fa-phone',
                        'fa-phone-square',
                        'fa-picture-o',
                        'fa-pinterest',
                        'fa-pinterest-square',
                        'fa-plane',
                        'fa-play',
                        'fa-play-circle',
                        'fa-play-circle-o',
                        'fa-plus',
                        'fa-plus-circle',
                        'fa-plus-square',
                        'fa-power-off',
                        'fa-print',
                        'fa-puzzle-piece',
                        'fa-qrcode',
                        'fa-question',
                        'fa-question-circle',
                        'fa-quote-left',
                        'fa-quote-right',
                        'fa-random',
                        'fa-refresh',
                        'fa-renren',
                        'fa-reorder',
                        'fa-repeat',
                        'fa-reply',
                        'fa-reply-all',
                        'fa-retweet',
                        'fa-rmb',
                        'fa-road',
                        'fa-rocket',
                        'fa-rotate-left',
                        'fa-rotate-right',
                        'fa-rouble',
                        'fa-rss',
                        'fa-rss-square',
                        'fa-rub',
                        'fa-ruble',
                        'fa-rupee',
                        'fa-save',
                        'fa-scissors',
                        'fa-search',
                        'fa-search-minus',
                        'fa-search-plus',
                        'fa-share',
                        'fa-share-square',
                        'fa-share-square-o',
                        'fa-shield',
                        'fa-shopping-cart',
                        'fa-sign-in',
                        'fa-sign-out',
                        'fa-signal',
                        'fa-sitemap',
                        'fa-skype',
                        'fa-smile-o',
                        'fa-sort',
                        'fa-sort-alpha-asc',
                        'fa-sort-alpha-desc',
                        'fa-sort-amount-asc',
                        'fa-sort-amount-desc',
                        'fa-sort-asc',
                        'fa-sort-desc',
                        'fa-sort-down',
                        'fa-sort-numeric-asc',
                        'fa-sort-numeric-desc',
                        'fa-sort-up',
                        'fa-spinner',
                        'fa-square',
                        'fa-square-o',
                        'fa-stack-exchange',
                        'fa-stack-overflow',
                        'fa-star',
                        'fa-star-half',
                        'fa-star-half-empty',
                        'fa-star-half-full',
                        'fa-star-half-o',
                        'fa-star-o',
                        'fa-step-backward',
                        'fa-step-forward',
                        'fa-stethoscope',
                        'fa-stop',
                        'fa-strikethrough',
                        'fa-subscript',
                        'fa-suitcase',
                        'fa-sun-o',
                        'fa-superscript',
                        'fa-table',
                        'fa-tablet',
                        'fa-tachometer',
                        'fa-tag',
                        'fa-tags',
                        'fa-tasks',
                        'fa-terminal',
                        'fa-text-height',
                        'fa-text-width',
                        'fa-th',
                        'fa-th-large',
                        'fa-th-list',
                        'fa-thumb-tack',
                        'fa-thumbs-down',
                        'fa-thumbs-o-down',
                        'fa-thumbs-o-up',
                        'fa-thumbs-up',
                        'fa-ticket',
                        'fa-times',
                        'fa-times-circle',
                        'fa-times-circle-o',
                        'fa-tint',
                        'fa-toggle-down',
                        'fa-toggle-left',
                        'fa-toggle-right',
                        'fa-toggle-up',
                        'fa-trash-o',
                        'fa-trello',
                        'fa-trophy',
                        'fa-truck',
                        'fa-try',
                        'fa-tumblr',
                        'fa-tumblr-square',
                        'fa-turkish-lira',
                        'fa-twitter',
                        'fa-twitter-square',
                        'fa-umbrella',
                        'fa-underline',
                        'fa-undo',
                        'fa-unlink',
                        'fa-unlock',
                        'fa-unsorted',
                        'fa-upload',
                        'fa-usd',
                        'fa-user',
                        'fa-user-md',
                        'fa-video-camera',
                        'fa-vimeo-square',
                        'fa-vk',
                        'fa-volume-down',
                        'fa-volume-off',
                        'fa-volume-up',
                        'fa-warning',
                        'fa-weibo',
                        'fa-wheelchair',
                        'fa-windows',
                        'fa-won',
                        'fa-wrench',
                        'fa-xing',
                        'fa-xing-square',
                        'fa-yen',
                        'fa-youtube',
                        'fa-youtube-play',
                        'fa-youtube-square'
                    ],
                    material:[
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
                return $perAdminApp.getNodeFromViewOrNull('/state/iconbrowser/families')
            },
            list(){
                var selectedFamily = this.selectedFamily
                if(selectedFamily === 'all'){
                    console.log('selected all families')
                    var icons = []
                    for (var property in this.icons) {
                        if (this.icons.hasOwnProperty(property)) {
                            icons = icons.concat(this.icons[property])
                        }
                    }
                    // console.log('icons: ', icons)
                    return icons
                } else {
                    console.log('selected family: ', selectedFamily)
                    return this.icons[selectedFamily]
                }
            }
        },
        methods: {
            iconBrowserModalOpen(that, options) {
                $('#iconBrowserModal').modal('open', options)
                Vue.nextTick(function () {
                    that.$refs.isotope.layout('masonry')
                })
            },
            searchFilter(icon) {
                if(this.search.length === 0) return true
                return (icon.indexOf(this.search) >= 0)
            },
            selectFamily(family){
                this.selectedFamily = this.camelize(family)
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
            getIconText(icon){
                if(this.getIconClass(icon) === 'material-icons'){
                    return icon
                }
                return ''
                
            },
            getIconClass(icon) {
                var iconClass
                switch(true) {
                    case (icon.includes('_')):
                        iconClass = 'material-icons'
                        break
                    case (icon.includes('fa')):
                        iconClass = 'fa ' + icon
                        break
                    default:
                        iconClass = 'material-icons'
                }
                return iconClass
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
