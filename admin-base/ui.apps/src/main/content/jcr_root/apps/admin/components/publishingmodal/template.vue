<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2020 The Regents of the University of Michigan
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
    <admin-components-materializemodal
        ref="materializemodal"
        v-on:complete="$emit('complete',$event)"
        v-bind:modalTitle="modalTitle" >
        <p>Content</p>
        <template slot="footer">
            <admin-components-confirmdialog
                v-on:confirm-dialog="confirmDialog"
                submitText="submit" />
        </template>
    </admin-components-materializemodal>
</template>

<script>
export default {
    props: [
        'isOpen',
        'path',
        'modalTitle',
    ],
    data(){
        return {

        }
    },
    computed: {
        pt: function() {
            var node = this.path
            return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, node)
        }
    },
    methods: {
        open(){
            this.$refs.materializemodal.open()
        },
        close(){
            this.$refs.materializemodal.close()
        },
        confirmDialog($event){
            if($event === "confirm") {
                console.log($event)
            } else {
                this.close()
            }
        }
    },
    mounted() {
        this.$refs.materializemodal.open()
    }
}
</script>