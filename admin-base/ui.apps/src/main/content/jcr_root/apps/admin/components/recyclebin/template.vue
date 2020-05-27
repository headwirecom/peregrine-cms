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

  Contributed by Cris Rockwell University of Michigan
  -->
<template>
<div>
    <div class="row">
        <div class="col s12">
            <h1><b>{{getTenant().title}}</b>{{$i18n(' Recycle Bin')}}</h1>
            <table>
               <thead>
                  <tr>
                      <th>Path</th>
                      <th>Deleted on</th>
                      <th>by</th>
                      <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="result in results.data" v-bind:key="`${result.recyclebin}`">
                    <td>{{result.path}}</td>
                    <td>{{result.date_deleted}}</td>
                    <td>{{result.deleted_by}}</td>
                    <td>
                        <admin-components-action
                            v-bind:model="{
                                target: result,
                                command: 'restoreRecyclable',
                                tooltipTitle: `${$i18n('restore version from')} ${result.date_deleted}`
                            }">
                            <i class="material-icons">restore_page</i>
                        </admin-components-action>
                        <admin-components-action
                                v-bind:model="{
                                target: result,
                                command: 'deleteRecyclable',
                                tooltipTitle: `${$i18n('delete forever')} ${result.recyclebin}`
                            }">
                            <i class="material-icons">delete_forever</i>
                        </admin-components-action>
                    </td>
                  </tr>
              </tbody>
          </table>
          <ul class="pagination">
              <li class="waves-effect" v-bind:class="{'disabled': !hasPrevious }"><a href="" v-bind:disabled="!hasPrevious" v-on:click.stop.prevent="loadPage(-1)"><i class="material-icons">chevron_left</i></a></li>
              <li>Page {{page + 1 }}
              <li class="waves-effect" v-bind:class="{'disabled': !hasNext }"><a href="" v-bind:disabled="!hasNext" v-on:click.stop.prevent="loadPage(1)"><i class="material-icons">chevron_right</i></a></li>
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
                    page: 0,
            }
        },
        computed: {
            results() {
                return $perAdminApp.getNodeFromViewOrNull('/admin/recyclebin')
            },
            hasPrevious(){
                return this.page > 0
            },
            hasNext(){
                return this.results.more
            }
        },
        methods: {
            loadPage: function(increment) {
                if( (this.hasNext && increment > 0) || (this.hasPrevious && increment < 0) ) {
                    this.page = this.page + increment
                }
            },
            getTenant() {
              return $perAdminApp.getView().state.tenant || {name: 'No site selected'}
            },
            restoreRecyclable(me, target) {
               const heading = `${me.$i18n('Restore')} "${target.path}" ${me.$i18n('from')} ${target.date_deleted}`
               $perAdminApp.askUser(heading, me.$i18n('Are you sure you want to restore this?'), {
                    yes() {
                        $perAdminApp.stateAction('recycleItem', {
                           recyclebinItemPath: target.recyclebin
                        })
                    }
                })
            },
            deleteRecyclable(me, target) {
                const heading = `${me.$i18n('Delete')} "${target.path}" ${me.$i18n('from')} ${target.date_deleted}`
                $perAdminApp.askUser(heading, me.$i18n('Delete this item forever?'), {
                    yes() {
                        $perAdminApp.stateAction('deleteRecyclable', target.recyclebin )
                    }
                })

            }
        },
        watch: {
            page: function(val){
                $perAdminApp.getApi().populateRecyclebin(val)
            }
        }
    }
</script>

<style>
    h1 {
        font-size: 2.5em;
    }
    tbody tr {
        border-bottom: 1px dashed lightgray;
    }
</style>