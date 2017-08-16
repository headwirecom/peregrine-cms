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
    <div ref="wrapper" v-bind:class="['sourceimagewizard', 'container', {'initial-search':  !state.results}]">
        <form v-on:submit.prevent="search()" class="image-search">
            <input type="text" v-model="state.input" placeholder="Search for an image asset" tabindex="1" autofocus/>
            <button class="" type="submit" title="search"><i class="material-icons">search</i></button>
        </form>

        <div v-if="state.results" class="search-content">
            <span v-if="state.results.length < 1" class="no-results">No images found for '{{ state.input }}'</span>

            <!-- Image Preview --> 
            <div v-else-if="viewing" class="image-preview">
                <button v-on:click.prevent.stop="deSelect()" class="back-to-grid btn-flat btn-large">
                    <i class="material-icons">grid_on</i> back to image results
                </button>
                <div class="image-row">
                    <button v-on:click.prevent.stop="select(viewing.index - 1)" :class="['btn-flat','btn-large',{'disabled': viewing.index == 0}]">
                        <i class="material-icons">keyboard_arrow_left</i>
                    </button>
                    <div class="image-container" :style="{width: `${viewing.width}px`}">
                        <img v-bind:src="viewing.webformatURL">
                        <!-- Image rename form -->
                        <div v-if="uploading" class="progress">
                            <div class="determinate" :style="{width: `${uploading}%`}"></div>
                        </div>                   
                        <form v-else v-on:submit.prevent="addImage(state.results[viewing.index], viewing.name)" class="image-rename-form">
                            <input type="text" v-model="viewing.name" autofocus/>
                            <button type="submit"  class="btn waves-effect waves-light">
                                <i class="material-icons">save</i>
                            </button>
                        </form>
                    </div>
                    <button v-on:click.prevent.stop="select(viewing.index + 1)" :class="['btn-flat','btn-large',{'disabled': viewing.index == state.results.length - 1}]">
                        <i class="material-icons">keyboard_arrow_right</i>
                    </button>
                </div>
            </div>

                <!-- Pagination -->
                <div v-if="state.numPages > 0" class="image-pagination">
                    <span>Displaying page {{state.currentPage}} of {{state.numPages}}</span>
                    <ul class="pagination">
                        <li class="waves-effect"><a href="#!" v-on:click.stop="selectPage(currentPage - 1)"><i class="material-icons">chevron_left</i></a></li>
                        <li v-for="(page,i) in state.numPages" :class="[{'active': state.currentPage == i+1}]"><a href="#!" v-on:click.stop="selectPage(i + 1)">{{ i + 1}}</a></li>
                        <li class="waves-effect"><a href="#!" v-on:click.stop="selectPage(currentPage + 1)"><i class="material-icons">chevron_right</i></a></li>
                    </ul>
                </div>
            <!-- Image Results Grid --> 
            <div v-else class="image-results">
                <div
                    v-for="(item,i) in state.results"
                    v-on:click.stop="select(i)"
                    v-bind:style="{backgroundImage: `url('${item.previewURL}')`}" 
                    class="image-item hoverable">
                </div>
                <!-- Pagination -->
                <div v-if="state.numPages > 0" class="image-pagination">
                    <span></span>
                    <ul class="pagination">
                        <li class="waves-effect"><a href="#!" v-on:click.stop="selectPage(currentPage - 1)"><i class="material-icons">chevron_left</i></a></li>
                        <li v-for="(page,i) in state.numPages" :class="[{'active': state.currentPage == i+1}]"><a href="#!" v-on:click.stop="selectPage(i + 1)">{{ i + 1}}</a></li>
                        <li class="waves-effect"><a href="#!" v-on:click.stop="selectPage(currentPage + 1)"><i class="material-icons">chevron_right</i></a></li>
                    </ul>
                </div>
            </div>

        </div>
    </div>
</template>

<script>

    export default {
        props: ['model'],

        beforeCreate() {
            let perState = $perAdminApp.getNodeFromViewOrNull('/state'); 
            perState.imageSearch = perState.imageSearch || {
                results: null,
                totalHits: null,
                currentPage: null,
                input: null,
                numPages: null
            }
        },

        data() {
            return {
                state: $perAdminApp.getNodeFromViewOrNull('/state/imageSearch'),
                viewing: null,
                containerWidth: null,
                uploading: null,
                containerWidth: null,
                columns: null,
                itemsPerPage: null
            }
        },

        mounted() {
            this.containerWidth = this.$refs.wrapper.offsetWidth
            this.columns = Math.floor(this.containerWidth / 160)
            this.itemsPerPage = this.columns * 5
        },

        methods: {

            search() {
                if (this.state.currentPage == null) this.state.currentPage = 1;
                const API_KEY = '5575459-c51347c999199b9273f4544d4';
                const URL = `https://pixabay.com/api/?key=${API_KEY}`+
                            `&page=${ this.state.currentPage }`+
                            `&per_page=${ this.itemsPerPage }`+
                            `&q=${ encodeURIComponent(this.state.input) }`
                $.getJSON( URL, data => {
                    this.state.results = data.hits;
                    this.state.totalHits = data.totalHits;
                    this.state.numPages = Math.ceil(data.totalHits/this.itemsPerPage);
                    this.viewing = null;
                })
            },

            select(index) {
                this.uploading = null
                this.viewing = this.state.results[index]
                this.viewing.index = index
                this.viewing.name = this.viewing.previewURL.split('/').pop()
            },
            deSelect() {
                this.viewing = null
            },
            selectPage(index) {
                this.state.currentPage = index;
                this.search();
            },

            uploadProgress(percent) {
                this.uploading = percent;
            },

            addImage(item, name) {
                this.uploading = 0;
                $perAdminApp.stateAction('fetchExternalAsset', { 
                    url: item.webformatURL, 
                    path: $perAdminApp.getNodeFromView('/state/tools/assets'), 
                    name: name,
                    config: { onUploadProgress: ev => this.uploadProgress(Math.floor((ev.loaded * 100) / ev.total)) },
                    error: () => this.uploading = null
                })
            }
        }
    }

</script>
