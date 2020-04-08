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
    <div class="nav-content sub-nav" :class="classes">
        <div v-if="isEditPage" class="page-tree">
            <!-- Dropdown Trigger -->
            <a class='dropdown-button btn' href='#' data-activates='dropdown1'>pages</a>

            <!-- Dropdown Structure -->
            <ul id='dropdown1' class='dropdown-content'>
                <li><a href="#!">one</a></li>
                <li><a href="#!">two</a></li>
                <li class="divider"></li>
                <li><a href="#!">three</a></li>
                <li><a href="#!"><i class="material-icons">view_module</i>four</a></li>
                <li><a href="#!"><i class="material-icons">cloud</i>five</a></li>
            </ul>
        </div>
        <template v-for="child in model.children">
            <div v-bind:is="child.component" v-bind:model="child"></div>
        </template>
        <span v-if="isEditPage" class="center-keeper"></span>
    </div>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        },
        isEditPage() {
            return this.model.classes && this.model.classes.indexOf('navcenter') >= 0
        },
    },
    mounted() {
        $('.dropdown-button').dropdown({
                inDuration: 300,
                outDuration: 225,
                constrainWidth: false, // Does not change width of dropdown to that of the activator
                hover: false, // Activate on hover
                gutter: 0, // Spacing from edge
                belowOrigin: false, // Displays dropdown below the button
                alignment: 'left', // Displays dropdown with edge aligned to the left of button
                stopPropagation: false // Stops event propagation
            }
        );
    },
    methods: {
        isEditor: function() {
            return this.$root.$data.adminPage.title === "editor"
        },
        getPath: function(){
            if( this.$root.$data.pageView){
                if( this.$root.$data.pageView.path ){
                    return this.$root.$data.pageView.path;
                } else {
                    return "";
                }
            } else {
                return "";
            }
        },
        getDownloadPath(){
            return this.getPath().split('/').reverse()[0];
        }
    }
}
</script>

