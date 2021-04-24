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
            Your personal ~ homepage is a few steps away. Fill out a few details. </p>
            
            <vue-form-generator
                :model="formmodel"
                :schema="nameSchema"
                :options="formOptions"
                ref="tildaForm">
            </vue-form-generator>

            <template slot="footer">
                <admin-components-confirmdialog
                    v-on:confirm-dialog="confirmDialog"
                    submitText="submit" />
            </template>
        </admin-components-materializemodal>
        
        <div v-if="model.mode === 'modal' && !hasTilda">
            <div class="row">
                <div class="col s12">
                    <h2>{{$i18n('Create Profile')}}</h2>
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
                    btnClasses: "btn",
                    formmodel: {
                        tildaPageUri: '',
                        firstName: '',
                        lastName: '',
                        pronouns: '',
                        org: '',
                        title: '',
                    },
                    formOptions: {
                        validationErrorClass: "has-error",
                        validationSuccessClass: "has-success",
                        validateAfterChanged: true,
                        focusFirstField: true
                    },
                    nameChanged: false,
                    nameSchema: {
                        fields: [
                            {
                                type: "input",
                                inputType: "text",
                                label: "First Name",
                                model: "firstName",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.firstName = newVal;
                                }
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Last Name",
                                model: "lastName",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.lastName = newVal;
                                }
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "~page URI",
                                model: "tildaPageUri",
                                required: true,
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.tildaPageUri = $perAdminApp.normalizeString(newVal);
                                },
                                validator: [this.nameAvailable, this.validPageName]
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Pronouns",
                                model: "pronouns",
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.pronouns = newVal;
                                }
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Organization",
                                model: "org",
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.org = newVal;
                                },
                            },
                            {
                                type: "input",
                                inputType: "text",
                                label: "Title/Position",
                                model: "title",
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.title = newVal;
                                },
                            },
                        ]
                    }                    
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
                    console.log(this.formmodel)
                }
                this.$refs.createtildamodal.close()
            },
            openTildaModal(){
                this.$refs.createtildamodal.open()
            },
            validPageName: function(event) {
                let value = event
                if (event && event instanceof Object && event.data) {
                    value = event.data
                }
                if(!value || value.length === 0) {
                    return [this.$i18n('Name is required.')]
                }
                let regExMatch = /[^0-9a-zA-Z_-]/
                let errorMsg = 'Page names may only contain letters, numbers, underscores, and dashes'
                if (this.uNodeType === "Asset") {
                    regExMatch = /[^0-9a-z.A-Z_-]/
                    errorMsg = 'Assets names may only contain letters, numbers, underscores, and dashes'
                }

                if (value.match(regExMatch)) {
                    return [this.$i18n(errorMsg)]
                }
                return [];
            },
            nameAvailable: function(value) {
                // TODO 
                // name is available by checking with user-uri service
                // user-uri is available if no other ~<name> already exists
                return []
            }

        },
        mounted() {
            if (this.model.mode === "modal" && !this.hasTilda) {
                this.openTildaModal()
            }
        }
    }
</script>

