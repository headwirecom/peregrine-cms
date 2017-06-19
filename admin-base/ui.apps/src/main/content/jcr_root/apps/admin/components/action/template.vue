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
        <a 
            v-if                    = "!model.type" 
            v-bind:href             = "model.target +'.html'"
            v-on:click.stop.prevent = "action" 
            v-bind:class            = "model.classes">
            {{model.title}}
            <slot></slot>
        </a>
        <a 
            v-if                    = "model.type === 'icon'" 
            v-bind:title            = "model.title" 
            v-bind:href             = "model.target" 
            v-on:click.stop.prevent = "action" 
            class                   = "btn-floating waves-effect waves-light" 
            v-bind:class            = "model.classes">
            <i class="material-icons" v-bind:class="isSelected ? 'actionSelected' : ''">
                {{model.icon ? model.icon : model.title}}
                <slot></slot>
            </i>
        </a>
    </span>
</template>

<script>
export default {
    props: ['model' ],
    computed: {
        isSelected() {
            if(!this.model.stateFrom) return false
            let currentState = $perAdminApp.getNodeFromViewOrNull(this.model.stateFrom)
            if(!currentState) {
                if(this.model.stateFromDefault) {
                    return true
                }
            } else if(currentState === this.model.target) {
                return true
            }
            return false
        }
    },
    methods: {
        action: function(e) {
            $perAdminApp.action(this, this.model.command, this.model.target)
        }
    }
}
</script>

<style>
    .actionSelected {
        background-color: white !important;
        color: #37474f !important;
    }
</style>
