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
    <div class="asset-preview">
        <template v-if="currentObject">
            <ul class="asset-info">
                <li>
                    <span class="asset-name">created:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li>
                    <span class="asset-name">modified:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li>
                    <span class="asset-name">source:</span>
                    <span class="asset-value">{{ currentObject.show }}</span>
                </li>
            </ul>
            <img v-if="isImage(currentObject.show)" v-bind:src="currentObject.show"/>
            <iframe v-else v-bind:src="currentObject.show"></iframe>

            <div>
                <button class="btn" v-on:click.stop.prevent="renameAsset()">rename</button>
                <button class="btn" v-on:click.stop.prevent="moveAsset()">move</button>
                <button class="btn" v-on:click.stop.prevent="deleteAsset()">delete</button>
            </div>

        </template>
        <template v-else>
            <div class="no-asset-selected">
                <span>no asset selected</span>
                <i class="material-icons">info</i>
            </div>
        </template>
    </div>

</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function () {
                return $perAdminApp.getNodeFromView("/state/tools/asset")
            },
            asset: function() {
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject.show)
            },

        },
        methods: {
            isImage: function(path) {
                const node = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, path)
                if(!node) return false
                const mime = node.mimeType
                return ['image/png','image/jpeg','image/jpg','image/gif','timage/tiff', 'image/svg+xml'].indexOf(mime) >= 0
            },
            renameAsset() {
                let newName = prompt('new name for '+this.asset.name)
                if(newName) {
                    $perAdminApp.stateAction('renameAsset', { path: this.asset.path, name: newName})
                    $perAdminApp.getNodeFromView('/state/tools').asset = null
                }
            },
            deleteAsset() {
                $perAdminApp.stateAction('deleteAsset', this.asset.path)
            },
            moveAsset() {
                let path = this.asset.path
                $perAdminApp.pathBrowser(
                    '/content/assets',
                    (newValue) => {
                        $perAdminApp.stateAction('moveAsset', { path: path, to: newValue, type: 'child'})
                        $perAdminApp.getNodeFromView('/state/tools').assets = newValue
                        $perAdminApp.getNodeFromView('/state/tools').asset = null
                    }
                )
            }
        }
    }
</script>
