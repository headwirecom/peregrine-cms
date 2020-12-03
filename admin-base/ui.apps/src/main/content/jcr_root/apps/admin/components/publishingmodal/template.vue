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
            <tbody v-if="references">
                <tr>
                    <th>Publish Item</th>
                    <th>Action</th>
                </tr>
                <tr>
                    <td>{{references.sourcePath}} <span>({{printStatus(references)}})</span></td>
                    <td class="switch">
                        <label> <input type="checkbox" v-model="references.publish" disabled> <span class="lever publishingaction"></span> </label>
                    </td>
                    <td class="printaction">{{printAction(references)}}</td>
                </tr>
            </tbody>
            <tbody v-if="references && references.references && references.references.length > 0">
                <tr>
                    <th>Uses</th>
                </tr>
                <tr v-for="ref in references.references" v-bind:key="ref.path">
                    <td>{{ref.path}} <span>({{printStatus(ref)}})</span></td>
                    <td class="switch">
                        <label> <input type="checkbox" v-model="ref.publish"> <span class="lever publishingaction"></span> </label>
                    </td>
                    <td class="printaction">{{printAction(ref)}}</td>              
                </tr>
            </tbody>
            <tbody v-if="referencedBy && referencedBy.length > 0">
                <tr>
                    <th>Used by</th>
                </tr>
                <tr v-for="refed in referencedBy" v-bind:key="refed.path">
                    <td>{{refed.path}} <span>({{printStatus(refed)}})</span></td>
                    <td class="switch">
                        <label> <input type="checkbox" v-model="refed.publish"> <span class="lever publishingaction"></span> </label>
                    </td>
                    <td class="printaction">{{printAction(refed)}}</td>
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
import ReferenceUtil from '../../../../../../js/mixins/ReferenceUtil'

export default {
    props: [
        'isOpen',
        'path',
        'modalTitle',
        
    ],
    data(){
        return {
            'referencedBy':[]
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
    mixins: [ReferenceUtil],
    methods: {
        open(){
            this.$refs.materializemodal.open()
        },
        close(){
            this.$refs.materializemodal.close()
        },
        printAction(ref){
            if (ref.is_stale && ref.publish) {
                return "Publish (Stale)"
            } else if (ref.activated && ref.publish){
                return "Publish (Again)"
            } else if(!ref.activated && ref.publish) {
                return "Publish (New)"
            } else if (!ref.publish){
                return "None"
            }
        },
        confirmDialog($event){
            if($event === "confirm") {
                // get path for items set to publish
                const deep = false;
                const deactivate = false;
                const referencesToRepl = []
                if (this.references.references !== undefined){
                    this.references.references.forEach(ref => {
                        if (ref.publish){
                            referencesToRepl.push(ref.path)
                        }
                    });
                }
                if (this.referencedBy !== undefined){
                    this.references.references.forEach(ref => {
                        if (ref.publish){
                            referencesToRepl.push(ref.path)
                        }
                    });
                }
                const target = {
                    path: this.path,
                    references: referencesToRepl
                }                
                $perAdminApp.stateAction('publish', target)
            } 
            this.close()
        },
        initializePublishActionFlag(reference){
            // if the publish is not defined
            if(reference['publish'] === undefined) {
                if(reference.activated && !reference.is_stale) {
                    // If the reference is already published and the lastModified date is before the publish date (i.e. not stale)
                    // Then there is no need to re-publish this reference and the publish action flag should be false
                    // It is set as a reactive property, which the user may override by toggling the switch if they choose.
                    Vue.set(reference, 'publish', false)
                } else {
                // otherwise set the reference's publish action flag defaults to true
                    Vue.set(reference, 'publish', true)
                }
            }
        }
    },
    mounted() {
        const me = this
        $perAdminApp.getApi().populateReferences(this.path)
            .then(function(){
                Vue.set(me.references, 'publish', true)
                if(me.references.references != undefined){
                    me.references.references.forEach((ref)=> {
                        me.initializePublishActionFlag(ref)
                    })
                }
            })        
        $perAdminApp.getApi().populateReferencedBy(this.path)
            .then(function(){
                me.referencedBy = me.trimReferences($perAdminApp.getView().state.referencedBy.referencedBy)
                if(me.referencedBy){
                    me.referencedBy.forEach((ref)=> {
                        me.initializePublishActionFlag(ref)
                    })
                }
            })
            .then(()=>this.$refs.materializemodal.open())
        
    }
}
</script>

<style>
    span.lever.publishingaction {margin-left: 3px;}
    .printaction { width: 150px;}
</style>