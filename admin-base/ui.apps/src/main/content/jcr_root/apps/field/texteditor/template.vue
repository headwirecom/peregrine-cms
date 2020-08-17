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
        :show-always-active="false"
        :responsive="false"/>
    <p class="text-editor inline-edit"
       :class="['text-editor', 'inline-edit', {'inline-editing': editing}]"
       ref="textEditor"
       v-html="value"
       contenteditable="true"
       @focusin="onFocusIn"
       @focusout="onFocusOut"
       @input="onInput"
       @click="pingToolbar"
       @dblclick="onDblClick"
       @keydown="pingToolbar"
       @keyup="pingToolbar">
    </p>
  </div>
</template>

<script>
import {set} from '../../../../../js/utils'

export default {
    mixins: [VueFormGenerator.abstractField],
    data() {
      return {
        doc: document,
        editing: false
      }
    },
    computed: {
      view() {
        return $perAdminApp.getView()
      }
    },
    methods: {
      onFocusIn(event) {
        set(this.view, '/state/inline/rich', true)
        set(this.view, '/state/inline/doc', this.doc)
        this.editing = true
      },
      onFocusOut() {
        set(this.view, '/state/inline/rich', true)
        set(this.view, '/state/inline/doc', null)
        this.editing = false
      },
      onInput(event) {
        const domProps = this._vnode.children[2].data.domProps
        const content = event.target.innerHTML
        if (domProps) domProps.innerHTML = content
        this.value = content
        this.textEditorWriteToModel()
      },
      onDblClick(event) {
        if (event.target.tagName === 'IMG') {
          $perAdminApp.action(this, 'editImage', event.target)
        }
      },
      textEditorWriteToModel(vm=this) {
        vm.model.text = vm.$refs.textEditor.innerHTML
      },
      pingToolbar() {
        $perAdminApp.action(this, 'pingRichToolbar')
      }
    }
  }
</script>
