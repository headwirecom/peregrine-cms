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
                    <h2>{{userName}} {{$i18n('profile')}}</h2>
                    <button
                        v-if="!hasTilda"
                        v-on:click="openTildaModal" 
                        title="Create your ~page" 
                        class="btn">
                        {{$i18n('Create your')}} ~{{$i18n('page')}}
                    </button>
                </div>
            </div>
        </div>
    </span>
</template>

<script>
    import {createDebouncer} from '../../../../../../js/utils'
    const uriAvailableDebouncer = createDebouncer()
    export default {
        props: ['model'],
        data:
            function() {
                return {
                    btnText: "~page",
                    editPath: "/admin/pages/homepage.html",
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
                                label: "~Page URI",
                                model: "tildaPageUri",
                                required: true,                           
                                onChanged: (model, newVal, oldVal, field) => {
                                    this.formmodel.tildaPageUri = $perAdminApp.normalizeString(newVal);
                                },
                                validator: [this.validPageName, this.nameAvailable]
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
            },
            userName: function(){
                return $perAdminApp.getView().state.user
            }
        },
        methods: {
            confirmDialog($event){
                const errors = []
                if ($event == 'confirm'){                    
                    errors.push(this.nameAvailable(this.formmodel.tildaPageUri))
                    console.log(errors)
                    console.log(this.formmodel)
                    console.log(this.$refs.tildaForm)
                }
                if (!errors || errors.length == 0){
                    // submit async to create ~page
                    // if promise resolves then close
                    // if there was an error do not close
                    this.$refs.createtildamodal.close()
                }
            },
            openTildaModal(){
                this.$refs.createtildamodal.open()
            },
            validPageName: function(event){
                let value = event
                if (event && event instanceof Object && event.data){
                    value = event.data
                }
                const errors = []
                if (!value || value.length === 0){
                    errors.push(this.$i18n('Page URI is required. '))
                } 
                if (value.length < 3) {
                    errors.push(this.$i18n('Must be at least 3 characters long. '))
                }
                if (value.length > 40) {
                    errors.push(this.$i18n('Must not be longer than 40 characters. '))
                }
                if (value.match(/[^0-9a-z-]/)){
                    errors.push(this.$i18n('May only contain lowercase, numbers, and dashes. '))
                }
                if (!value.match(/[a-z]/)){
                    errors.push(this.$i18n('Must contain at least one letter. '))
                }
                return errors;
            },
            nameAvailable: function(value){
                // check whether name is available by checking with user-uri service
                // user-uri is available if no other ~<name> already exists
                console.log(value)
                let errors = this.validPageName(value)
                if (errors && errors.length == 0){
                    errors = []
                    errors = uriAvailableDebouncer.call(() => $perAdminApp.getApi().checkUserPageAvailability(value), 375)
                        .then(res => {
                            const errorsBE = []
                            if (res.nameValid) {
                                if (!res.nameAvailable) {
                                    errorsBE.push(this.$i18n('This uri is not available.'))
                                }
                            } else {
                                errorsBE.push(this.$i18n('This uri is invalid. '))
                            }
                            return errorsBE
                        })
                         //nameValid   
                        .catch(() => [])
                } else {
                    errors.push(this.$i18n('This name is not valid. '))
                }
                return errors
            }

        },
        mounted() {
            if (this.model.mode === "modal" && !this.hasTilda) {
                this.openTildaModal()
            }
        }
    }
</script>

