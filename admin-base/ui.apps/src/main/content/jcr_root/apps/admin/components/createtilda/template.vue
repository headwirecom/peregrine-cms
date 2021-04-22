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
    <span>
        <span class="createtildabtn">
            <admin-components-action
                v-if="model.mode === 'button'"
                v-bind:model="{
                    target: editPath,
                    title: getBtnText,
                    command: 'selectPath',
                    classes: btnClasses
                }"></admin-components-action>
        </span>
        
        <admin-components-materializemodal
            ref="createtildamodal"
            v-on:complete="$emit('complete',$event)"
            v-bind:modalTitle="getBtnText" >

            <p>Members typically use their ~ page to describe themselves or their work.            
            Your personal ~ homepage is one step away </p>
            <template slot="footer">
                <admin-components-confirmdialog
                    v-on:confirm-dialog="confirmDialog"
                    submitText="submit" />
            </template>
        </admin-components-materializemodal>
        
        <div v-if="model.mode === 'modal' && !hasTilda">
            <div class="row">
                <div class="col s12">
                    <h1>{{$i18n('Profile')}}</h1>
                    <button
                        v-if="!hasTilda"
                        v-on:click="openTildaModal" 
                        title="Create ~homepage" 
                        class="btn">
                        Create ~homepage
                    </button>
                </div>
            </div>
        </div>
    </span>
</template>

<script>
    export default {
        props: ['model'],
        data:
            function() {
                return {
                    btnText: "~homepage",
                    editPath: "/content/admin/pages/homepage.html",
                    btnClasses: "btn"
            }
        },
        computed: {
            getBtnText: function(){
                return this.hasTilda ? `Edit ${this.btnText}` : `Create ${this.btnText}` 
            },
            hasTilda: function(){
                return $perAdminApp.getView().state.userPreferences.tildaPage === "true"
            }
        },
        methods: {
            confirmDialog($event){
                if ($event == 'confirm') {
                    console.log("submit new ~ page")
                }
                this.$refs.createtildamodal.close()
            },
            openTildaModal(){
                this.$refs.createtildamodal.open()
            }
        },
        mounted() {
            if (this.model.mode === "modal" && !this.hasTilda) {
                this.openTildaModal()
            }
        }
    }
</script>

