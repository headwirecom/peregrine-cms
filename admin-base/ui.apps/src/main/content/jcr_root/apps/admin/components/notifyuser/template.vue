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
    <transition name="fade">
      <div v-if="isVisible" class="modal-container">
        <div class="modal bottom-sheet">
            <div class="modal-content">
                <h4>{{title}}</h4>
                <p>{{message}}</p>
            </div>
            <div class="modal-footer">
                <a 
                    v-on:click="onOk"
                    href="#!" 
                    class="modal-action modal-close waves-effect waves-light btn-flat">
                    ok
                </a>
            </div>
        </div>
        <div v-on:click="onOk" class="modal-overlay"></div>
      </div>
    </transition>
</div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            notify() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification')
            },
            title() {
                let notification = this.notify
                if(notification) {
                    return notification.title
                }
                return ''
            },
            message() {
                let notification = this.notify
                if(notification) {
                    return notification.message
                }
                return ''
            },
            isVisible() {
                let notification = this.notify
                if(notification) {
                    return notification.isVisible
                }
                return false
            },
            onOk() {
                let notification = this.notify
                if(notification) {
                    return notification.onOk
                }
                return false
            }
        }
    }
</script>
