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
    <div id="promptUserModal" class="modal bottom-sheet" ref="modal">
        <div class="modal-content">
            <h4>{{title}}</h4>
            <p>{{message}}</p>
            <input
                v-model="value"
                type="text">
        </div>
        <div class="modal-footer">
            <button 
                type="button"
                class="modal-action modal-close waves-effect waves-light btn-flat"
                v-on:click="cancel()"
                title="cancel">
                {{noText}}
            </button>
            <button 
                type="button"
                class="modal-action modal-close waves-effect waves-light btn-flat"
                v-on:click="ok()"
                title="ok">
                {{yesText}}
            </button>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {
            return {
                value: null
            }
        },
        computed: {
            title() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/title')
            },
            message() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/message')
            },
            yesText() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/yesText')
            },
            noText() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/noText')
            }
        },
        methods: {
            cancel() {
                $('#promptUserModal').modal('getInstance').options.takeAction = false;
                this.value = null;
            },
            ok() {
                $('#promptUserModal').modal('getInstance').options.takeAction = true;
                $('#promptUserModal').modal('getInstance').options.value = this.value;
                this.value = null;
            }
        }

    }
</script>
