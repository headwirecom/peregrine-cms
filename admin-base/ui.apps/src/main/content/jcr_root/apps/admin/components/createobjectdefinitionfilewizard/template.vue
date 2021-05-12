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
      <tab-content title="Select a supported file-format">
        <ul class="collection">
          <li
            v-for="(format, i) in supportedFormats"
            :key="`format-${i}`"
            class="collection-item"
            :class="{
              'grey lighten-2': formmodel.format === format.ext,
              disabled: format.disabled,
            }"
            @click.stop.prevent="formmodel.format = format.ext"
          >
            {{ format.label }}
            <small class="text-muted">&nbsp;&nbsp;({{ format.ext }})</small>
          </li>
        </ul>
      </tab-content>
      <tab-content
        title="choose name"
        :before-change="beforeChooseNameTabChange"
      >
        <vue-form-generator
          :model="formmodel"
          :schema="schemas.name"
          :options="formOptions"
          ref="nameTab"
        >
        </vue-form-generator>
      </tab-content>
    </form-wizard>
  </div>
</template>

<script>
import { isValidObjectName, nameAvailable } from '../../../../../../js/mixins';

export default {
  mixins: [isValidObjectName, nameAvailable],
  props: ['model'],
  data() {
    return {
      supportedFormats: [
        { label: 'JSON', ext: '.json' },
        { label: 'Text', ext: '.txt', disabled: true },
      ],
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
              validator: [this.nameAvailable, this.validObjectName],
            },
          ],
        },
      },
    };
  },
  created() {
    this.formmodel.format = this.supportedFormats[0].ext;
  },
  methods: {
    onComplete: function() {
      const { name, path, format } = this.formmodel;
      $perAdminApp.stateAction('createObjectDefinitionFile', {
        parent: path,
        name: name + format,
        format,
        data: this.formmodel,
        returnTo: this.model.returnTo,
      });
    },

    beforeChooseNameTabChange: function() {
      return this.$refs.nameTab.validate();
    },
  },
};
</script>
