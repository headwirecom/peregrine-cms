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
    <div class="wrapper">
      <template v-if="!schema.preview">
        <div tabindex="-1" ref="quilltoolbar">
            <select tabindex="-1" class="ql-header">
                <option value="1"></option>
                <option value="2"></option>
                <option value="3"></option>
                <option value="4"></option>
                <option value="5"></option>
                <option selected></option>
            </select>
            <button tabindex="-1" class="ql-bold"></button>
            <button tabindex="-1" class="ql-italic"></button>
            <button tabindex="-1" class="ql-underline"></button>
            <button tabindex="-1" class="ql-link"></button>
            <button tabindex="-1" class="ql-list" value="ordered"></button>
            <button tabindex="-1" class="ql-list" value="bullet"></button>
            <button tabindex="-1" class="ql-script" value="sub"></button>
            <button tabindex="-1" class="ql-script" value="super"></button>
            <button tabindex="-1" class="ql-indent" value="-1"></button>
            <button tabindex="-1" class="ql-indent" value="+1"></button>
            <button tabindex="-1" class="ql-code-block"></button>
            <button tabindex="-1" class="ql-clean"></button>
        </div>
        <div ref="quilleditor"></div>
      </template>
      <p v-else>{{value}}</p>
    </div>
</template>

<script>
    export default {
        mixins: [ VueFormGenerator.abstractField ],
        mounted() {
            if(!this.schema.preview) this.initialize()
        },
        beforeDestroy() {
            this.quill = null
        },
        watch: {
            value(newVal, oldVal) {
                if(newVal != this.$refs.quilleditor.children[0].innerHTML) {
                    this.$refs.quilleditor.children[0].innerHTML = this.value
                }
            }
        },
        methods: {
            initialize() {
                this.$refs.quilleditor.innerHTML = this.value ? this.value : ''
                this.quill = new Quill(this.$refs.quilleditor, {
                    theme: 'snow',
                    modules: {
                        toolbar: this.$refs.quilltoolbar
                    }            
                })
                const toolbar = this.quill.getModule('toolbar')
                toolbar.addHandler('link', (value) => { 
                    value ? this.showPageBrowser() : this.quill.format('link', false)
                })
                this.quill.on('text-change', (delta, oldDelta, source) => {
                    this.value = this.$refs.quilleditor.children[0].innerHTML
                } )
            },
            showPageBrowser() {
              $perAdminApp.pageBrowser(
                '/content/sites',
                true, // with Link tab?
                (newValue) => { 
                  console.log('newValue: ', newValue)
                  this.quill.format('link', newValue) 
                }
              )
            }
        }
    }
</script>

<style>
    .ql-container {
        background-color: white;
    }
</style>
