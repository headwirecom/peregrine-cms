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
        <div class="card">
          <icon icon="info-circle" :lib="IconLib.FONT_AWESOME" />
          Supported file extensions:
          <ul>
            <li
              v-for="extension in supportedExtensions"
              :key="extension"
            >
              {{ extension }}
            </li>
          </ul>
        </div>
        <vue-form-generator
          ref="nameTab"
          :model="formmodel"
          :schema="schemas.name"
          :options="formOptions"
        >
        </vue-form-generator>
      </tab-content>
    </form-wizard>
  </div>
</template>

<script>
import { nameAvailable } from '../../../../../../js/mixins';
import Icon from '../icon/template.vue';
import { IconLib } from '../../../../../../js/constants';

export default {
  components: { Icon },
  mixins: [nameAvailable],
  props: ['model'],
  data() {
    return {
      IconLib,
      supportedExtensions: ['.json'],
      formmodel: {
        path: $perAdminApp.getNodeFromView(this.model.dataFrom),
        format: '',
        name: '',
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
              validator: [this.nameAvailable, this.isSupportedExtension],
            },
          ],
        },
      },
    };
  },
  methods: {
    isSupportedExtension(value) {
      if (!value || value.length === 0 || value.indexOf('.') <= -1) {
        return ['file extension missing'];
      }

      const ext = `.${value.split('.').pop()}`;

      if (!this.supportedExtensions.includes(ext)) {
        return ['file extension not supported'];
      }

      return [];
    },

    beforeChooseNameTabChange: function() {
      return this.$refs.nameTab.validate();
    },

    onComplete: function() {
      const { name, path } = this.formmodel;

      $perAdminApp.stateAction('createObjectDefinitionFile', {
        parent: path,
        name: name,
        data: this.formmodel,
        returnTo: this.model.returnTo,
      });
    },
  },
};
</script>

<style scoped>
.card {
  padding: 1rem;
  background-color: #f6f6f6;
}
</style>
