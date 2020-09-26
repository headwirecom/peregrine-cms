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
    <div ref="wrapper" v-bind:class="['adobestockimagewizard', 'container']">
      <div class="search-bar">
        <button v-if="viewing" v-on:click.prevent.stop="deSelect()" class="back-to-grid btn-flat">
            <i class="material-icons">grid_on</i><span> back to results </span>
        </button>
        <form v-on:submit.prevent="search()" :class="['image-search', {'previewing': viewing}]">
          <input type="text" v-model="state.input" v-bind:placeholder="$i18n('Search for an image asset')" tabindex="1" autofocus/>
          <button class="" type="submit" v-bind:title="$i18n('search')" class="image-search-submit"><i class="material-icons">search</i></button>
        </form>
        <button v-if="!viewing" @click="isShowing ^= true" class="filter-button"> FILTERS </button>
      </div>
      <div>
        <hr>
      </div>
      <!-- Filters clear filters link -->
      <div v-if="!viewing" v-show="isShowing">
         <span v-if="state.input || state.orientation != 'all' || state.price !='all' || state.offensive != 0 || state.isolatedImagesOnly !=0"> Active filters : </span>
           <span v-if="state.input">
             <span>Keyword :
               <div class="chip">
                 <span> {{state.input}} </span>
                 <i @click="resetInputText" class="close material-icons"> close </i>
               </div>
             </span>
           </span><span></span>
           <span v-if="state.orientation != 'all'">
             <span>Orientation :
               <div class="chip">
                 <span v-if="state.orientation == 'vertical'"> Vertical </span>
                 <span v-if="state.orientation == 'horizontal'"> Horizontal </span>
                 <span v-if="state.orientation == 'square'"> Square </span>
                 <i @click="resetOrientation" class="close material-icons" > close </i>
               </div>
             </span>
           </span>
           <span v-if="state.price != 'all'">
             <span>Price :
               <div class="chip">
                 <span v-if="state.price == 'false' "> Standard </span>
                 <span v-if="state.price == 'true' "> Premium </span>
                 <i @click="resetPrice" class="close material-icons"> close </i>
               </div>
             </span>
           </span>
           <span v-if="state.offensive != 0">
             <span>Offensive :
               <div class="chip">
                 <span> Yes </span>
                 <i @click="resetOffensive" class="close material-icons"> close </i>
               </div>
             </span>
           </span>
           <span v-if="state.isolatedImagesOnly != 0">
             <span>Isolated images :
               <div class="chip">
                 <span> Yes </span>
                   <i @click="resetIsolatedImages" class="close material-icons"> close </i>
               </div>
             </span>
           </span>
           <a href="#" @click="clearFilters" class="clear-all-link"> Clear All </a>
      </div>
      <!-- Filters -->
      <div v-show="isShowing" v-if="!viewing">
        <hr>
        <form v-on:submit.prevent="search()">
          <div class="row">
            <div class="col s3">
              <label><b> Orientation </b></label>
              <select class="browser-default" v-model="state.orientation">
                <option value="vertical"> Vertical </option>
                <option value="horizontal"> Horizontal </option>
                <option value="square"> Square </option>
                <option value="all"> All </option>
              </select>
            </div>
            <div class="col s3">
              <label><b> Price </b></label>
              <select class="browser-default" v-model="state.price">
                <option value=true > Premium </option>
                <option value=false > Standard </option>
                <option value="all" > All </option>
              </select>
            </div>
              <div class="col s3">
                <label><b> Offensive </b></label>
                <select class="browser-default" v-model="state.offensive">
                  <option value="1"> Yes </option>
                  <option value="0"> No </option>
                </select>
              </div>
              <div class="col s3">
                <label><b> Isolated Images Only </b></label>
                <select class="browser-default" v-model="state.isolatedImagesOnly">
                  <option value="1"> Yes </option>
                  <option value="0"> No </option>
                </select>
              </div>
          </div>
          <button class="btn btn-sm percms-btn-pager btn-outline-primary" type="submit" v-bind:title="$i18n('search')" style="float: right;"> Apply Filters </button>
          <a href="#" @click="isShowing ^= true" class="cancel-link"> Cancel </a>
        </form>
        <br><br>
        <hr>
      </div>
      <div v-if="!state.results" class="center">
        <span>{{ $i18n('Search for an image from Adobe Stock and add it directly to your project') }}!</span>
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
              <img v-bind:src="viewing.thumbnail_url">
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
            <span class="resolution">{{viewing.thumbnail_width}} x {{viewing.thumbnail_height}}</span>
          </div>
        </div>

        <!-- Image Results Grid -->
        <div v-else v-on:scroll="handleScroll" class="image-results">
          <div
            v-for="(item, i) in state.results"
            v-on:click.stop="select(i)"
            v-bind:style="{backgroundImage: `url('${item.thumbnail_url}')`}"
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
                </div>
                <div class="gap-patch">
                  <div class="circle">
                </div>
              </div>
              <div class="circle-clipper right">
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
                numPages: null,
                orientation: 'all',
                price: 'all',
                offensive: 0,
                isolatedImagesOnly: 0
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
                loading: false,
                isShowing:false
            }
        },

        mounted() {
            this.containerWidth = this.$refs.wrapper.offsetWidth
            this.columns = Math.floor(this.containerWidth / 160)
            this.itemsPerPage = this.columns * 5
        },

        methods: {

            requestImages() {
                // Get the API key from Adobe Stock site. https://stock.adobe.com/
                const API_KEY = 'ENTER-YOUR-KEY-HERE';
                const URL = `https://stock.adobe.io/Rest/Media/1/Search/Files?locale=en_US&search_parameters[words]=`+
                            `${ encodeURIComponent(this.state.input) }`+
                            `&search_parameters[filters][orientation]=${ encodeURIComponent(this.state.orientation) }`+
                            `&search_parameters[offset]=${ encodeURIComponent(this.state.currentPage) }`+
                            `&search_parameters[limit]=${ encodeURIComponent(this.state.numPages) }`+
                            `&search_parameters[filters][orientation]=${ encodeURIComponent(this.state.orientation) }`+
                            `&search_parameters[filters][premium]=${ encodeURIComponent(this.state.price) }`+
                            `&search_parameters[filters][offensive:2]=${ encodeURIComponent(this.state.offensive) }`+
                            `&search_parameters[filters][isolated:on]=${ encodeURIComponent(this.state.isolatedImagesOnly) }`
                fetch(URL, {
                    method: 'POST',
                    headers: {'x-api-key' : API_KEY, 'x-product': 'myTestApp1.0'}
                })
                .then((response) => {
                    return response.json();
                })
                .then((data) => {
                    this.state.results = this.state.results.concat(data.files);
                    this.state.totalHits = data.nb_results;
                    // TODO: Fix the hardcoding of 32 limit it is by default result limit of Adobe Stock API.
                    // this.state.numPages = Math.ceil(this.state.totalHits/this.itemsPerPage);
                    this.state.numPages = 32;
                    this.loading = false;

                });
            },
            
            search() {
                this.viewing = null;
                this.endOfResults = false;
                this.state.currentPage = 0;
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
                if (this.state.currentPage < this.state.totalHits) {
                    this.state.currentPage = this.state.currentPage + 32;
                    this.loading = true;
                    this.requestImages();
                }
                else {
                    this.endOfResults = true;
                }
            },

            clearFilters() {
                this.state.orientation = 'all';
                this.state.price = 'all';
                this.state.offensive = 0;
                this.state.isolatedImagesOnly = 0;
                this.state.input = '';

            },
            
            resetOrientation() {
                this.state.orientation = 'all';
            },

            resetPrice() {
                this.state.price = 'all';
            },

            resetOffensive() {
                this.state.offensive = 0;
            },

            resetIsolatedImages() {
                this.state.isolatedImagesOnly = 0;
            },
            
            resetInputText() {
                this.state.input = '';
            },

            select(index) {
                this.uploading = null
                this.viewing = this.state.results[index]
                this.viewing.index = index
                this.viewing.name = this.viewing.thumbnail_url.split('/').pop()
                console.log(this.viewing.name);
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
                    url: item.thumbnail_url, 
                    path: $perAdminApp.getNodeFromView('/state/tools/assets'), 
                    name: name,
                    config: { onUploadProgress: ev => this.uploadProgress(Math.floor((ev.loaded * 100) / ev.total)) },
                    error: () => this.uploading = null
                })
            }
        }
    }

</script>
