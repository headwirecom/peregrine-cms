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
    <div v-bind:class="['sourceimagewizard', {'initial-search':  !state.results}]">
        <div class="container">
            <form v-on:submit.prevent="search()" class="container">
                <input type="text" v-model="state.input" placeholder="Search for an image asset" autofocus/>
                <button class="" type="submit" title="search"><i class="material-icons">search</i></button>
            </form>
        </div>

        <template v-if="state.results">
            <span v-if="state.results.length < 1" class="no-results">No images found for '{{ state.input }}'</span>

            <div v-else-if="viewing">
                <div class="container image-preview">
                    <button v-on:click.prevent.stop="select('prev')">
                        <i class="material-icons">keyboard_arrow_left</i>
                    </button>
                    <img v-bind:src="state.results[viewing.index].webformatURL || null">
                    <button v-on:click.prevent.stop="select('next')">
                        <i class="material-icons">keyboard_arrow_right</i>
                    </button>
                </div>
                <button v-on:click.prevent.stop="addImage(state.results[viewing.index])">
                    <i class="material-icons">check</i>
                </button>
                <button v-on:click.prevent.stop="deSelect()">
                    <i class="material-icons">clear</i>
                </button>
            </div>

            <div v-else class="container image-results">
                <div
                    v-for="(item,i) in state.results"
                    v-on:click.stop="select(i)"
                    v-bind:style="{backgroundImage: `url('${item.previewURL}')`}" 
                    class="image-item hoverable">
                </div>
                <div v-if="numPages > 0" class="image-pagination">
                    <span>Displaying page {{state.currentPage}} of {{numPages}}</span>
                    <ul class="pagination">
                        <li class="waves-effect"><a href="#!"><i class="material-icons">chevron_left</i></a></li>
                        <li v-for="(page,i) in numPages" v-on:click.stop="selectPage(i + 1)"><a href="#!">{{ i + 1}}</a></li>
                        <li class="waves-effect"><a href="#!"><i class="material-icons">chevron_right</i></a></li>
                    </ul>
                </div>
            </div>

        </template>
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
            }
        },

        data() {
            return {
                state: $perAdminApp.getNodeFromViewOrNull('/state/imageSearch'),
                viewing: null
            }
        },

        computed: {
            numPages() {return Math.ceil(this.state.totalHits / 20)},
        },

        methods: {

            search() {
                if (this.state.currentPage == null) this.state.currentPage = 1;
                const API_KEY = '5575459-c51347c999199b9273f4544d4';
                const URL = `https://pixabay.com/api/?key=${API_KEY}&page=${ this.state.currentPage }&q=${ encodeURIComponent(this.state.input) }`
                $.getJSON( URL, data => {
                    this.state.results = data.hits;
                    this.state.totalHits = data.totalHits;
                    this.viewing = null;
                })
            },

            select(item) {
                if (index === 'next') this.viewing.index += 1
                else if (index === 'prev') this.viewing.index -=1
                else this.viewing = item
            },
            deSelect() {
                this.viewing = null
            },
            selectPage(index) {
                this.state.currentPage = index;
                this.search();
            },

            uploadProgress(percent) {
                this.progress = percent;
            },

            addImage(item) {
                var name = item.previewURL.split('/').pop()
                $perAdminApp.stateAction('fetchExternalAsset', { 
                    url: item.webformatURL, 
                    path: $perAdminApp.getNodeFromView('/state/tools/assets'), 
                    name: name,
                    config: { onUploadProgress: ev => this.uploadProgress(Math.floor((ev.loaded * 100) / ev.total)) }
                })
            }
        }
    }

</script>
