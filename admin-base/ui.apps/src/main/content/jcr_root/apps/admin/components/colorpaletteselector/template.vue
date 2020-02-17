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
  <div>
    <link rel="stylesheet"
          href="/etc/felibs/themecleanflex/css/colors.css"
          type="text/css"/>
    <link rel="stylesheet"
          :href="`/content/sites/themecleanflex/css/pallets/${value}.css`"
          type="text/css"/>
    <label>Select Color Palette</label>
    <div class="wrapper">
      <div class="__multiselect">
        <vue-multiselect
            :value="value"
            :options="palettes"
            :searchable="false"
            :allow-empty="false"
            deselect-label=""
            @select="onSelect"/>
      </div>
      <div class="palette-preview">
        <div v-for="colorVar in colorVars"
          :style="{'background-color': `var(--${colorVar})`}"
          :title="colorVar"/>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    props: ['model'],
    data() {
      return {
        value: 'default.light',
        palettes: [
          'default.light',
          'default.dark',
          'lime.light',
          'lime.dark',
          'headwire'
        ],
        colorVars: [
          'bg-primary-color',
          'bg-secondary-color',
          'text-primary-color',
          'text-secondary-color',
          'border-primary-color'
        ]
      }
    },
    methods: {
      onSelect(palette) {
        this.value = palette
      }
    }
  }
</script>

<style scoped>
  .wrapper {
    display: flex;
    flex-direction: row;
    align-items: stretch;
    width: 100%;
  }

  .wrapper > div {
    flex: 1;
  }

  .palette-preview {
    display: flex;
    flex-direction: row;
    align-items: stretch;
    border: 1px solid #000000;
    border-left-width: 0;
    min-height: 100px;
    max-width: 300px;
  }

  .palette-preview > div {
    border-left: 1px solid #000000;
    flex: 1;
  }
</style>
