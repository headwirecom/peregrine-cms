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
    <div v-if="currentObject">
        <button class="btn-flat" v-on:click.stop.prevent="onView">view</button>
        <button class="btn-flat" v-on:click.stop.prevent="onEdit">edit</button>

        <div v-if="!edit">
            <pre>{{currentObject.data}}</pre>
        </div>
        <form v-else>

            <vue-form-generator
                    v-bind:schema  = "schema"
                    v-bind:model   = "currentObject.data"
                    v-bind:options = "formOptions">
            </vue-form-generator>
            <button class="btn-flat" v-on:click.stop.prevent="onOk">ok</button>
            <button class="btn-flat" v-on:click.stop.prevent="onCancel">cancel</button>
        </form>

    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function () {
                return $perAdminApp.getNodeFromView("/state/tools/object")
            },
            schema: function () {
                let resourceType = this.currentObject.data['sling:resourceType']
                resourceType = resourceType.split('/').join('-')
                return $perAdminApp.getNodeFromView('/admin/componentDefinitions/' + resourceType)
            }
        }
        ,
        data: function() {
            return {
                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true
                },
                edit: true

            }
        },
        methods: {
            onView: function() {
                this.edit = false
            },
            onEdit: function() {
                this.edit = true
            },
            onOk: function() {
                // should store the current node
                $perAdminApp.stateAction('saveObjectEdit', { data: this.currentObject.data, path: this.currentObject.show } )
            },
            onCancel: function() {
                $perAdminApp.stateAction('selectObject', { selected: this.currentObject.show } )
            }
        }
    }
</script>
