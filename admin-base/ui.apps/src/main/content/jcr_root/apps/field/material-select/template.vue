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
    <multiselect 
      v-if="!schema.preview"
      v-model="value" 
      v-bind="schema.selectOptions"
      :options="schema.values">
    </multiselect>
    <template v-else>
      <ol v-if="schema.selectOptions.multiple" class="preview-items">
        <li v-for="item in value" class="preview-item">
          <label>Name:</label> {{item.name}} <br>
          <label>Value:</label> {{item.value}}
        </li>
      </ol>
      <p v-else class="preview-item">
        <label>Name:</label> {{value.name}} <br>
        <label>Value:</label> {{value.value}}
      </p>
    </template>
  </div>
</template>

<script>
    export default {
      // v-bind="obj" is same as ...obj (object destructuring)
      props: ['model'], 
      mixins: [ VueFormGenerator.abstractField ]
    }
</script>
