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
<div class="col s12 m6 l4 icon-action">
    <div class="card blue-grey darken-3">
        <div class="card-content white-text action" @click="onCardContentClick">
            <span class="card-title">{{$exp(model, 'title')}}</span>
            <p>{{$exp(model,'description')}}</p>
        </div>
        <div class="card-action">
            <admin-components-action v-if="internal(model.action, model.target)"
                v-bind:model="actionModel">
            </admin-components-action>
            <a v-else v-bind:href="model.action" v-bind:target="model.target">{{$i18n('explore')}}</a>
        </div>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data() {
          return {
              actionModel: {
                  target: this.model.action,
                  command: 'selectPath',
                  title: this.$i18n('explore')
              }
          }
        },
        methods: {
            internal(action, target) {
                return !action.startsWith('http') && (target === undefined || target === null)
            },
            onCardContentClick() {
                if (this.internal(this.model.action, this.model.target)) {
                    $perAdminApp.action(this, 'selectPath', this.model.action)
                } else {
                    window.open(this.model.action, this.model.target).focus()
                }
            }
        }
    }
</script>

<style>
.card .card-content p {
    height: 100px;
}
    .card-content.action:hover {
        background-color: rgba(255, 255, 255, 0.05);
        cursor: pointer;
    }
</style>

