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
    <div class="container">
        <input type="text" v-model="input">
        <button class="btn" v-on:click.stop="search()">search</button>
        <div class="row">
            <div class="col s2" v-for="item in results"><a href="#" v-on:click.stop="select(item)" class="hoverable"><img v-bind:src="item.previewURL"></a></div>
        </div>
    </div>
</template>

<script>

    export default {
        props: ['model'],
        computed: {
            results() {
                var node = $perAdminApp.getNodeFromViewOrNull('/admin/images')
                return node;
            }
        },
        methods: {
            search() {
                var API_KEY = '5575459-c51347c999199b9273f4544d4';
                var URL = "https://pixabay.com/api/?key="+API_KEY+"&q="+encodeURIComponent(this.input);
                $.getJSON(URL, function(data){
                    var node = $perAdminApp.getNodeFromView('/admin')
                    if (parseInt(data.totalHits) > 0) {
                        Vue.set(node, 'images', data.hits)
                    }
                    else {
                        console.log('No hits');
                        Vue.set(node, 'images', [])
                    }
                });
            },
            select(item) {
                var name = item.previewURL.split('/').pop()
                $perAdminApp.stateAction('fetchExternalAsset', { url: item.webformatURL, path: $perAdminApp.getNodeFromView('/state/tools/assets'), name: name})
            }
        }
    }

</script>
