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
    <richtoolbar
      class="on-right-panel"
      :show-always-active="false"
      :responsive="false"
      :editorContent="value"
      @ping="key = key === 'foo'? 'bar' : 'foo'"
    />
    <p class="text-editor inline-edit"
       :class="['text-editor', 'inline-edit', {'inline-editing': editing}]"
       ref="textEditor"
       v-html="value"
       :contenteditable="!(schema && schema.readonly)"
       :readonly="(schema && schema.readonly)"
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
import Richtoolbar from '../../admin/components/richtoolbar/template.vue'

const allowedStylesMap = {
  // bold, italic, etc handled by html tags
  "text-align": true,
  "font-size": true
};
const allowedStylesElementsMap = {
  IMG: true,
}
function removeUnwantedStyles(htmlText) {
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = htmlText

  tempDiv.querySelectorAll('[style]').forEach((span) => {
    if (allowedStylesElementsMap[span.nodeName]) return;
    const propertiesToRemove = []
    for (let i = 0; i < span.style.length; i++) {
      const property = span.style.item(i);
      if (!allowedStylesMap[property]) {
        propertiesToRemove.push(property);
      }
    }
    // must be done in later step, otherwise length changes
    for (let i = 0; i < propertiesToRemove.length; i++) {
      span.style.removeProperty(propertiesToRemove[i]);
    }
  })

  return tempDiv.innerHTML
}

export default {
  components: {Richtoolbar},
  mixins: [VueFormGenerator.abstractField],
  data() {
    return {
      doc: document,
      editing: false,
      key: 0,
    }
  },
  computed: {
    view() {
      return $perAdminApp.getView()
    },
  },

  mounted() {
    set(this.view, '/state/inline/rich', true)
  },

  methods: {
    onFocusIn(event) {
      set(this.view, '/state/inline/rich', true)
      set(this.view, '/state/inline/doc', this.doc)
      this.editing = true
      this.pingToolbar()
    },
    onFocusOut() {
      set(this.view, '/state/inline/rich', true)
      set(this.view, '/state/inline/doc', null)
      this.editing = false
      this.pingToolbar()
    },
    onInput(event) {
      const domProps = this._vnode.children[2].data.domProps
      const content = event.target.innerHTML;
      if (domProps) domProps.innerHTML = content
      this.value = content
      this.textEditorWriteToModel()
      this.pingToolbar()
    },
    onDblClick(event) {
      if (event.target.tagName === 'IMG') {
        $perAdminApp.action(this, 'editImage', event.target)
      }
    },
    textEditorWriteToModel(vm = this) {
      vm.model.text = removeUnwantedStyles(vm.$refs.textEditor.innerHTML);
    },
    pingToolbar() {
      this.key = this.key === 'foo' ? 'bar' : 'foo'
      $perAdminApp.action(this, 'pingRichToolbar')
    },
  },
  watch: {
    value() {
      if (!this.value) return
      const textCheckDiv = document.createElement('div')
      textCheckDiv.innerHTML = this.value
      if (!textCheckDiv.textContent.trim()) this.value = '';
    }
  }
}
</script>
