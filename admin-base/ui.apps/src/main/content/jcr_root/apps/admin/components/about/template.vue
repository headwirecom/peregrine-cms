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
    <div id="aboutPeregrine" class="modal">
        <div class="modal-content">
            <p class="left-align">
                <img style="margin: 10px; width: 100px; max-width: 100px;" src="/etc/felibs/admin/peregrine-logo.svg" align="right"/>
                <img style="width: 0px; height: 0px;" src="https://www.headwire.com/t/peregrine.png"/>
                <span v-html="$i18n('aboutp1')"></span>
            </p>
            <p class="left-align browser-default">
                <span v-html="$i18n('aboutp2')"></span>
            </p>
            <p class="left-align browser-default">
                <span v-html="$i18n('aboutp3')"></span>
            </p>
            <p class="left-align browser-default">
                <span v-html="$i18n('aboutp4')"></span>
            </p>
        </div>
        <div class="modal-footer">
            <button 
                type="button"
                class="modal-action modal-close waves-effect waves-light btn-flat"
                v-bind:title="$i18n('close')">
                {{$i18n('close')}}
            </button>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>