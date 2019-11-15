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
  <span class="__multiselect">
    <vue-multiselect
        :value="mode"
        deselect-label
        track-by="title"
        label="title"
        placeholder="Select one"
        :options="devices"
        :searchable="false"
        :allow-empty="false"
        @select="onSelect"
    >
      <template slot="option" slot-scope="props">
        <i class="material-icons" :title="props.option.title" :alt="props.option.title">{{props.option.icon}}</i>
      </template>
      <template slot="singleLabel" slot-scope="props">
        <i class="material-icons btn-floating" :title="props.option.title" :alt="props.option.title">{{props.option.icon}}</i>
      </template>
      <template slot="caret" slot-scope="props"><span></span></template>
    </vue-multiselect>
  </span>
</template>

<script>
  export default {
    props: ['model'],
    computed: {
      title() {
        const stateFrom = $perAdminApp.getNodeFromViewOrNull(this.model.children[0].stateFrom);
        return stateFrom? stateFrom : 'desktop';
      },
      mode() {
        const me = this;
        return this.model.children.find((child) => {
          return child.title === me.title;
        });
      },
      devices() {
        const me = this;
        return this.model.children.filter((child) => {
          return child.title !== me.title;
        });
      }
    },
    methods: {
      onSelect(item) {
        $perAdminApp.action(this, item.command, item.target);
      }
    }
  };
</script>

<style>

  .__multiselect .multiselect__tags {
    padding: 0;
    background: transparent;
    border: inherit;
  }

  .__multiselect .material-icons {
    line-height: 40px;
    height: 40px;
    padding-left: 8px;
    margin-top: -1px;
    margin-bottom: -4px;
  }

  .__multiselect .multiselect__option {
    padding: 0;
  }

  .__multiselect .multiselect--active .btn-floating {
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }

  .__multiselect .multiselect__content-wrapper {
    overflow: hidden;
  }

</style>