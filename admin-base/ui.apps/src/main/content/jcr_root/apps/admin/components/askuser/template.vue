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
    <dialog id="askUserModal" ref="modal">
        <div class="modal-content">
            <h4>{{title}}</h4>
            <p>{{message}}</p>
        </div>
        <div class="modal-footer">
            <button
                type="button"
                class="btn-flat"
                v-on:click="noFn()"
                title="cancel">
                {{noText}}
            </button>
            <button
                type="button"
                class="btn-flat"
                v-on:click="yesFn()"
                title="ok">
                {{yesText}}
            </button>
        </div>
    </dialog>
</template>

<script>
    export default {
        props: ['model'],
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
            },
            yesFn() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/yesFn')
            },
            noFn() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification/noFn')
            },
        },
    }
</script>

<style scoped>
dialog[open] {
  display: block;
  border: none;
  width: 100%;
  top: auto;
  bottom: 0%;
  right: 0;
  left: 0;
  margin: 0;
  width: 100%;
  opacity: 1;
  max-height: 45%;
  border-radius: 0;
  will-change: bottom, opacity;
  animation: slide-up 0.3s ease-in-out;
  max-width: none;
}

@keyframes slide-up {
  from {
    bottom: -100%;
    opacity: 0;
  }
  to {
    bottom: 0%;
    opacity: 1;
  }
}

dialog .modal-content {
  padding: 24px;
  padding-top: 54px;
}
dialog .modal-footer {
  padding: 4px 6px;
  height: 56px;
  width: 100%;
  text-align: right;
}

</style>
