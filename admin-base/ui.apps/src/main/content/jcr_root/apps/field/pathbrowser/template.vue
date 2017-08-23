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
    <div class="wrap">
      <template v-if="!schema.preview">
        <input
          :id="getFieldID(schema)"
          type="text"
          :value="value"
          :disabled="disabled"
          :maxlength="schema.max"
          :placeholder="schema.placeholder"
          :readonly="schema.readonly"
          @input="value = $event.target.value" />
        <button v-on:click.stop.prevent="browse" class="btn-flat">
          <i class="material-icons">insert_drive_file</i>
        </button>
      </template>
      <p v-else>{{value}}</p>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        mixins: [ VueFormGenerator.abstractField ],
        methods: {
            setPathBrowserValue(){
                this.value = $perAdminApp.getNodeFromView('/state/pathbrowser/selected')
            }, 
            browse() {
                let type = this.schema.browserType
                if(!type) console.warn('pathbrowser type was not defined in schema. Root is set to "/content".')
                let root
                switch(type) {
                    case ('assets'):
                        root = '/content/assets'
                        break
                    case ('sites'):
                        root = '/content/sites'
                        break;
                    default:
                        root = '/content'
                }
                let currentPath = root
                let selectedPath = this.value
                if(selectedPath) {
                    // change currentPath to slected items parent
                    currentPath = selectedPath.substr(0, selectedPath.lastIndexOf('/'))
                }
                const initModalState = {
                    root: root,
                    type: type,
                    current: currentPath,
                    selected: selectedPath,
                    withLinkTab: false
                }
                let options = this.schema.browserOptions
                if(!options) {
                    console.warn('No options specified. Modal will open with defaults.')
                    options = {}
                }
                options.complete = this.setPathBrowserValue 
                // set pathbrowser modal initial state
                $perAdminApp.pathBrowser(initModalState, options)
                /* modal options:
                {
                    dismissible: true, // Modal can be dismissed by clicking outside of the modal
                    opacity: .5, // Opacity of modal background
                    inDuration: 300, // Transition in duration
                    outDuration: 200, // Transition out duration
                    startingTop: '4%', // Starting top style attribute
                    endingTop: '10%', // Ending top style attribute
                    ready: function(modal, trigger) {}, // Callback for Modal open. Modal and trigger parameters available.
                    complete: function(){} // Callback for Modal close
                }
                */
            }
        }
    }
</script>
