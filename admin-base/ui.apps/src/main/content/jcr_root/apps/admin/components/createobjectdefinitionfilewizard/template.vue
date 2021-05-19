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
  <div class="container">
    <form-wizard
      :title="'Create File'"
      :subtitle="''"
      @on-complete="onComplete"
      color="#37474f"
    >
      <tab-content
        title="choose name"
        :before-change="beforeChooseNameTabChange"
      >
        <vue-form-generator
          ref="chooseNameVfg"
          :model="formmodel"
          :schema="schemas.name"
          :options="formOptions"
        >
        </vue-form-generator>
      </tab-content>

      <tab-content title="Write content">
        <div>
          <div class="template-load-buttons">
            <h5>Insert template</h5>
            <button
              v-for="(template, i) in templates"
              :key="`template-${i}`"
              type="button"
              class="btn waves-effect waves-light"
              @click="insertTemplate(template)"
            >
              {{ template.name }}
            </button>
          </div>
          <div class="codemirror-wrapper">
            <span v-if="showInitialInfo" class="initial-info">
              Click or choose a template to start editing
            </span>
            <codemirror
              v-model="formmodel.content"
              ref="coremirror"
              @click.native="onCodemirrorClick"
            >
              Click to edit
            </codemirror>
          </div>
        </div>
      </tab-content>
      <tab-content title="verify">
        <pre v-html="JSON.stringify(formmodel, null, 2)"></pre>
      </tab-content>
    </form-wizard>
  </div>
</template>

<script>
import { nameAvailable } from '../../../../../../js/mixins';
import { IconLib } from '../../../../../../js/constants';
import Icon from '../icon/template.vue';
import * as templates from './templates';

export default {
  name: 'CreateObjectDefinitionFileWizard',
  components: { Icon },
  mixins: [nameAvailable('.json')],
  props: ['model'],
  data() {
    return {
      IconLib,
      showInitialInfo: true,
      formmodel: {
        path: $perAdminApp.getNodeFromView(this.model.dataFrom),
        name: '',
        content: '{}',
      },
      formOptions: {
        validationErrorClass: 'has-error',
        validationSuccessClass: 'has-success',
        validateAfterChanged: true,
        focusFirstField: true,
      },
      schemas: {
        name: {
          fields: [
            {
              type: 'input',
              inputType: 'text',
              label: 'Filename',
              model: 'name',
              required: true,
              placeholder: 'my-file-name.json',
              validator: [this.nameAvailable],
            },
          ],
        },
      },
      templates: [
        { name: 'schema', content: templates.schema },
        { name: 'ui-schema', content: templates.uiSchema },
      ],
    };
  },
  methods: {
    beforeChooseNameTabChange() {
      return this.$refs.chooseNameVfg.validate();
    },

    onComplete() {
      this.formmodel.name += '.json';
      const { name, path } = this.formmodel;

      $perAdminApp.stateAction('createObjectDefinitionFile', {
        parent: path,
        name: name,
        data: this.formmodel,
        returnTo: this.model.returnTo,
      });
    },

    onCodemirrorClick() {
      this.showInitialInfo = false;
    },

    insertTemplate(template) {
      this.showInitialInfo = false;
      this.formmodel.content = template.content;
    },
  },
};
</script>

<style scoped>
.codemirror-wrapper {
  position: relative;
}

.initial-info {
  position: absolute;
  top: 1rem;
  left: 4rem;
  z-index: 999;
  color: silver;
}

.template-load-buttons {
  margin-bottom: 1rem;
}

.template-load-buttons .btn {
  margin-right: 0.25rem;
}
</style>
