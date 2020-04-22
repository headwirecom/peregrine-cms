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
    <div>
        <div v-if="!schema.preview" class="wrapper wysiwygeditor">
            <trumbowyg :config="config" v-model="value" ref="editor"></trumbowyg>
        </div>
        <p v-else v-html="value"></p>
        <admin-components-pathbrowser
            v-if="isOpen"
            :isOpen="isOpen && browserType === 'asset'"
            :browserRoot="browserRoot"
            :browserType="browserType"
            :withLinkTab="withLinkTab"
            :altText="altText" :setAltText="setAltText"
            :currentPath="currentPath" :setCurrentPath="setCurrentPath"
            :selectedPath="selectedPath" :setSelectedPath="setSelectedPath"
            :onCancel="onCancel"
            :onSelect="onSelect">
        </admin-components-pathbrowser>
        <admin-components-pathbrowser
            v-if="isOpen && browserType === 'page'"
            :isOpen="isOpen"
            :browserRoot="browserRoot"
            :browserType="browserType"
            :withLinkTab="withLinkTab"
            :newWindow="newWindow" :toggleNewWindow="toggleNewWindow"
            :linkTitle="linkTitle" :setLinkTitle="setLinkTitle"
            :currentPath="currentPath" :setCurrentPath="setCurrentPath"
            :selectedPath="selectedPath" :setSelectedPath="setSelectedPath"
            :onCancel="onCancel"
            :onSelect="onSelect">
        </admin-components-pathbrowser>
    </div>
</template>

<script>
    export default {
        mixins: [ VueFormGenerator.abstractField ],
        data () {
            const basePath = '/content/'+$perAdminApp.getView().state.tenant.name; 

            return {
                browserRoot: `${basePath}/assets`,
                browserType: 'asset',
                currentPath: `${basePath}/assets`,
                selectedPath: null,
                altText: null,
                linkTitle: '',
                withLinkTab: true,
                newWindow: false,
                isOpen: false,
                default: {
                    config: {
                        svgPath: '/content/admin/assets/images/trumbowyg-icons.svg',
                        resetCss: true,
                        btnsDef: {
                            formattingWithCode: {
                                dropdown: ['p', 'quote', 'preformatted', 'h1', 'h2', 'h3', 'h4'],
                                ico: 'p', // Apply formatting icon
                                hasIcon: true
                            }
                        },
                        btns: [
                            'viewHTML',
                            'undo',
                            'redo',
                            'formattingWithCode',
                            'strong',
                            'em',
                            'superscript',
                            'subscript',
                            'link',
                            'insertImage',
                            'justifyLeft',
                            'justifyCenter',
                            'justifyRight',
                            'justifyFull',
                            'unorderedList',
                            'orderedList',
                            'removeformat'
                        ]
                    }
                }
            }
        },
        beforeCreate() {
            var self = this;
            $.extend(true, $.trumbowyg, {
                plugins: {
                    modalOverride: {
                        init: function(trumbowyg) {
                            trumbowyg.openModalInsert = function(title, fields, cmd) {
                                //Setup state of pathbrowser and open pathbrowser
                                let isImage = fields.hasOwnProperty('alt');
                                let url = fields.url.value;

                                self.browserType = isImage ? 'asset' : 'page';
                                self.browserRoot = isImage ? `${self.getBasePath()}/assets` : `${self.getBasePath()}/pages`;
                                //Internal Link
                                if( url && url.match(/^(https?:)?\/\//)) {
                                    self.currentPath = self.browserRoot;
                                    self.selectedPath = url;
                                }
                                else if(url && url.startsWith('/')) {
                                    const parts = url.split('/');
                                    parts.pop();
                                    self.currentPath = parts.join('/');
                                    self.selectedPath = isImage ? url : url.split('.')[0];
                                }
                                else {
                                    self.currentPath = self.browserRoot;
                                    self.selectedPath = null;
                                }

                                self.browse();
                                //Setup pathbrowser select event to call trumbowyg cmd callback
                                self.onSelect = function() {
                                    trumbowyg.restoreRange();
                                    if(isImage) {
                                        cmd({
                                            url: self.selectedPath,
                                            alt: self.altText
                                        })
                                    }
                                    else {
                                        let url = self.selectedPath;
                                        if(!url.match(/^(https?:)?\/\//)) url += '.html';
                                        cmd({
                                            text: fields.text.value,
                                            title: self.linkTitle,
                                            target: self.newWindow ? "_blank" : "_self",
                                            url: url,
                                        })
                                    }
                                    trumbowyg.syncCode(),
                                    trumbowyg.$c.trigger("tbwchange"),
                                    self.isOpen = false;
                                }
                                self.onCancel = function() {
                                    trumbowyg.restoreRange();
                                    self.isOpen = false;
                                }
                            }
                        }
                    }
                }
            })
        },
        computed: {
            isValidBtns() {
                let btns = this.schema.config.btns
                return !btns || this.isArrayAndNotEmpty(btns)
            },
            isValidBtnsDef() {
                let btnsDef = this.schema.config.btnsDef
                return !btnsDef || this.isObjectAndNotEmpty(btnsDef)
            },
            config() {
                let cfg = this.default.config
                if (this.schema.config) {
                    let btns = this.schema.config.btns
                    if (btns && this.isValidBtns) {
                        cfg.btns = btns
                        let btnsDef = this.schema.config.btnsDef
                        if (btnsDef && this.isValidBtnsDef) {
                            cfg.btnsDef = btnsDef
                        }
                    }
                    let plugins = this.schema.config.plugins;
                    if (plugins) {
                        cfg.plugins = plugins;
                    }
                }
                return cfg;
            }
        },
        methods: {
            getBasePath() {
                return `/content/${$perAdminApp.getView().state.tenant.name}`
            },
            isArrayAndNotEmpty(p) {
                return Array.isArray(p) && p.length > 0
            },
            isObjectAndNotEmpty(p) {
                return typeof p === 'object' && Object.entries(p).length > 0
            },
            toggleNewWindow() {
                this.newWindow = !this.newWindow;
            },
            setAltText(e) {
                this.altText = e.target.value;
            },
            setLinkTitle(e) {
                this.linkTitle = e.target.value;
            },
            setCurrentPath(path){
                this.currentPath = path
            },
            setSelectedPath(path){
                this.selectedPath = path
            },
            isImage(path) {
                return /\.(jpg|png|gif)$/.test(path);
            },
            isValidPath(path, root){
                return path && path !== root && path.includes(root)
            },
            browse() {
                $perAdminApp.getApi().populateNodesForBrowser(this.currentPath, 'pathBrowser')
                    .then( () => {
                        this.isOpen = true
                    }).catch( (err) => {
                        $perAdminApp.getApi().populateNodesForBrowser('/content', 'pathBrowser')
                    })
            }
        }
    }
</script>
