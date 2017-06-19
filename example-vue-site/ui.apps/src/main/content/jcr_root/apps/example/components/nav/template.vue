<!--
  #%L
  example site - UI Apps
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
    <nav class="navbar navbar-toggleable-md navbar-light bg-faded" v-bind:data-per-path="model.path">
        <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <a class="navbar-brand" v-bind:href="siteRoot">{{model.brand}}</a>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item" v-for="navItem in model.navigation">
                    <a class="nav-link" v-bind:class="isActive(navItem)" v-bind:href="navItem.path +'.html'">{{navItem.title}}</a>
                </li>
            </ul>
        </div>
    </nav>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentPagePath: function() {
                return this.$root.$data.page.pagePath
            },
            siteRoot: function() {
                return this.$root.$data.page.siteRoot+'.html'
            }
        },
        methods: {
            isActive: function(item) {
                return item.path === this.$root.$data.page.pagePath ? 'active': ''
            }
        }
    }
</script>
