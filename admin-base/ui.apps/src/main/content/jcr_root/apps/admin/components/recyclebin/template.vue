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
            <table>
               <thead>
                  <tr>
                      <th>Resource</th>
                      <th>Path</th>
                      <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="result in results.data" v-bind:key="result.path">
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

                return {
                    querystring: ``,
                    page: 0
            }
        },
        computed: {
            results() {
                return $perAdminApp.getNodeFromViewOrNull('/admin/recyclebin')
            }
        },
        methods: {
            loadPage: function(increment) {
                this.page = this.page + increment
                // this.query()
            }
        }
    }
</script>
