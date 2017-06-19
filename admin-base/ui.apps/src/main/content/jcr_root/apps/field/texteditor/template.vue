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
    <div class="wrapper" style="display: flex; flex-direction: column; min-height: 200px;">
        <div ref="quilleditor" style="width: 100%"></div>
    </div>
</template>

<script>
    export default {
        mixins: [ VueFormGenerator.abstractField ],
        mounted() {
            this.initialize()
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
                this.$refs.quilleditor.innerHTML = this.value
                this.quill = new Quill(this.$refs.quilleditor, {
                    theme: 'snow',
                    modules: {
                        toolbar: [
                            [{ header: [1, 2, 3, 4, 5, false] }],
                            ['bold', 'italic', 'underline'],
                            [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                            [{ 'script': 'sub'}, { 'script': 'super' }],
                            [{ 'indent': '-1'}, { 'indent': '+1' }],
                            ['code-block'],
                            ['clean']
                        ]
                    }                })
                this.quill.on('text-change', (delta, oldDelta, source) => {
                    this.value = this.$refs.quilleditor.children[0].innerHTML
                } )
            }
        }
    }
</script>

<style>
    .ql-container {
        background-color: white;
    }
</style>
