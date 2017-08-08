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
    <div class="row">
        <div class="col s12">
            <input v-model="querystring">
            <div class="right">
                <button title="go" v-on:click="executeQuery" class="btn">go</button>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col s12">
            <table>
               <thead>
                  <tr>
                      <th>Resource</th>
                      <th>Path</th>
                      <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="result in results.data">
                    <td>{{result.name}}</td>
                    <td>{{result.path}}</td>
                    <td><a v-bind:href="'/bin/browser.html'+result.path" target="composum">view</a></td>
                  </tr>
              </tbody>
          </table>
          <ul class="pagination">
              <li class="waves-effect"><a href="" v-on:click.stop.prevent="loadPage(-1)"><i class="material-icons">chevron_left</i></a></li>
              <li>Page {{page + 1 }}
              <li class="waves-effect"><a href="" v-on:click.stop.prevent="loadPage(1)"><i class="material-icons">chevron_right</i></a></li>
          </ul>
       </div>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {

                if(!this.results) this.results = {
                                                     pages: 0,
                                                     more: false,
                                                     data: []
                                                 }

                return {
                    querystring: 'select * from nt:base',
                    page: 0,
                    results: this.results

            }
        },
        methods: {
            executeQuery: function() {
                this.page = 0
                this.more = false
                this.query()
            },
            query: function() {
                var resObj = this.results
                axios.get('/bin/search?q='+this.querystring+'&page='+this.page).then(function(result) {
                    resObj.data = result.data.data
                    resObj.pages = result.data.pages
                    resObj.more = result.data.more
                })
            },
            loadPage: function(increment) {
                this.page = this.page + increment
                this.query()
            }
        }
    }
</script>
