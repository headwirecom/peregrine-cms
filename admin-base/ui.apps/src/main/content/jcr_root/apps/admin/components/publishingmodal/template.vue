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

        <table>
            <thead>
            <tr>
                <th>Path</th>
                <th>Status</th>
                <th>Action</th>
            </tr>
            </thead>

            <tbody v-if="references">
            <tr>
                <td>{{references.sourcePath}}</td>
                <td>{{printStatus(references)}}</td>
                <td class="switch">
                    <label> <input type="checkbox" v-model="references.activated"> <span class="lever"></span> </label>
                </td>
            </tr>
            <tr v-for="ref in references.references" v-bind:key="ref.path">
                <td>{{ref.path}}</td>
                <td>{{printStatus(ref)}}</td>
                <td class="switch">
                    <label> <input type="checkbox" v-model="ref.activated"> <span class="lever"></span> </label>
                </td>
            </tr>
            </tbody>
        </table>


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
        pt() {
            var node = this.path
            return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, node)
        },
        references(){            
            return $perAdminApp.getView().state.references
        },
    },
    methods: {
        open(){
            this.$refs.materializemodal.open()
        },
        close(){
            this.$refs.materializemodal.close()
        },
        printStatus(ref){
            if (ref.is_stale) {
                return "Published (Stale)"
            } else if (ref.activated === true){
                return "Published"                
            } else if(ref.activated === false) {
                return "Unpublished"
            } else {
                return "No Status"
            }
        },
        confirmDialog($event){
            if($event === "confirm") {
                console.log($event)
                // get path for items set to publish
                const deep = false;
                const deactivate = false;
                const referencesToRepl = []
                if (this.references.references !== undefined){
                    this.references.references.forEach(ref => {
                        if (ref.activated){
                            referencesToRepl.push(ref.path)
                        }
                    });
                }
                const target = {
                    path: this.path,
                    references: referencesToRepl
                }
                console.log(target)
                $perAdminApp.stateAction('publish', target)

            } else {
                this.close()
            }
        }
    },
    mounted() {
        $perAdminApp.getApi().populateReferences(this.path);
        this.$refs.materializemodal.open()
    }
}
</script>