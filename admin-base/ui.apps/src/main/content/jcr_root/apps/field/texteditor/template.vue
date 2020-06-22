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
  <div class="text-editor-wrapper">
    <admin-components-richtoolbar
        :showViewportBtn="false"
        :showPreviewBtn="false"/>
    <p class="text-editor"
       v-html="value"
       contenteditable="true"
       @focusin="onFocusIn"
       @focusout="onFocusOut"
       @input="onInput">
    </p>
  </div>
</template>

<script>
  import {set} from '../../../../../js/utils'

  export default {
    mixins: [VueFormGenerator.abstractField],
    data() {
      return {
        doc: document
      }
    },
    computed: {
      view() {
        return $perAdminApp.getView()
      }
    },
    methods: {
      onFocusIn() {
        set(this.view, '/state/inline/doc', this.doc)
      },
      onFocusOut() {
        set(this.view, '/state/inline/doc', null)
      },
      onInput(event) {
        if (this._vnode.data.domProps) this._vnode.data.domProps.innerHTML = event.target.innerHTML
        this.model.text = event.target.innerHTML
      },
      pingToolbar() {
        set(this.view, '/state/inline/ping', true)
      }
    }
  }
</script>
