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
        <div class="search-bar">
            <button v-if="viewing" v-on:click.prevent.stop="deSelect()" class="back-to-grid btn-flat">
                <i class="material-icons">grid_on</i><span>back to results</span>
            </button>
            <form v-on:submit.prevent="search()" :class="['image-search', {'previewing': viewing}]">
                <input type="text" v-model="state.input" v-bind:placeholder="$i18n('searchImageAsset')" tabindex="1" autofocus/>
                <button class="" type="submit" v-bind:title="$i18n('search')" class="image-search-submit"><i class="material-icons">search</i></button>
            </form>
        </div>
        <div v-if="!state.results" class="center">
            <span>{{ $i18n('pixabaySearchHint') }}!</span>
        </div>

        <div v-if="state.results" class="search-content">
            <span v-if="state.results.length < 1 && !loading" class="no-results">No images found for '{{ state.input }}'</span>

            <!-- Image Preview --> 
            <div v-else-if="viewing" class="image-preview">
                <div class="image-row">
                    <button v-on:click.prevent.stop="select(viewing.index - 1)" :class="['btn-flat','btn-large',{'disabled': viewing.index == 0}]">
                        <i class="material-icons">keyboard_arrow_left</i>
                    </button>
                    <div class="image-container">
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
                <div class="image-preview-details">
                    <span class="resolution">{{viewing.webformatWidth}} x {{viewing.webformatHeight}}</span>
                    <div class="chipcontainer"><div v-for="tag in tags" class="chip">{{tag}}</div></div>
                </div>
            </div>

            <!-- Image Results Grid --> 
            <div v-else v-on:scroll="handleScroll" class="image-results">
                <div
                    v-for="(item,i) in state.results"
                    v-on:click.stop="select(i)"
                    v-bind:style="{backgroundImage: `url('${item.previewURL}')`}" 
                    class="image-item hoverable">
                </div>
                <div class="image-results-status">
                    <div v-if="endOfResults" class="col s12">
                        <span>end of results</span>
                    </div>
                    <div v-else class="preloader-wrapper small active">
                        <div class="spinner-layer spinner-green-only">
                        <div class="circle-clipper left">
                            <div class="circle"></div>
                        </div><div class="gap-patch">
                            <div class="circle"></div>
                        </div><div class="circle-clipper right">
                            <div class="circle"></div>
                        </div>
                        </div>
                    </div>
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
                scrollPos: null,
                currentPage: null,
                input: null,
                numPages: null
            }
        },

        data() {
            return {
                state: $perAdminApp.getNodeFromViewOrNull('/state/imageSearch'),
                viewing: null,
                uploading: null,
                containerWidth: null,
                columns: null,
                itemsPerPage: null,
                endOfResults: false,
                loading: false
            }
        },

        computed: {
            tags() { return this.viewing.tags.split(', ') }
        },

        mounted() {
            this.containerWidth = this.$refs.wrapper.offsetWidth
            this.columns = Math.floor(this.containerWidth / 160)
            this.itemsPerPage = this.columns * 5
        },

        methods: {

            requestImages() {
                const API_KEY = '5575459-c51347c999199b9273f4544d4';
                const URL = `https://pixabay.com/api/?key=${API_KEY}`+
                            `&page=${ this.state.currentPage }`+
                            `&per_page=${ this.itemsPerPage }`+
                            `&q=${ encodeURIComponent(this.state.input) }`
                $.getJSON( URL, data => {
                    this.state.results = this.state.results.concat(data.hits);
                    this.state.totalHits = data.totalHits;
                    this.state.numPages = Math.ceil(data.totalHits/this.itemsPerPage);
                    this.loading = false;
                })
            },

            search() {
                this.viewing = null;
                this.endOfResults = false;
                this.state.currentPage = 1;
                this.state.results = [];
                this.loading = true;
                this.requestImages();
            },

            handleScroll(e) {
                if (!this.loading) { 
                    const scrollPos = e.target.scrollTop + e.target.offsetHeight;
                    const fullHeight    = e.target.scrollHeight;
                    const distanceLeft = fullHeight - scrollPos;
                    //Load the next page when we near the bottom of the last
                    if( distanceLeft < e.target.offsetHeight * 0.25   ) {
                        this.loadNextPage();
                    }
                }
            },

            loadNextPage() {
                if (this.state.currentPage < this.state.numPages) {
                    this.state.currentPage++;
                    this.loading = true;
                    this.requestImages();
                }
                else {
                    this.endOfResults = true;
                }

            },

            select(index) {
                this.uploading = null
                this.viewing = this.state.results[index]
                this.viewing.index = index
                this.viewing.name = this.viewing.previewURL.split('/').pop()
                if (index >= this.state.results.length - 1) this.loadNextPage();
            },
            deSelect() {
                this.viewing = null
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
