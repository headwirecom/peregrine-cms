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
  <div ref="modal" :id="id" class="modal materialize-modal">
    <div v-if="!!$slots.default" class="modal-content">
      <slot></slot>
    </div>
    <div v-if="!!$slots.footer" class="modal-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      dismissible: {
        type: Boolean,
        default: true
      },
      opacity: {
        type: Number,
        default: .5
      },
      inDuration: {
        type: Number,
        default: 300
      },
      outDuration: {
        type: Number,
        default: 200
      },
      startingTop: {
        type: String,
        default: '4%'
      },
      endingTop: {
        type: String,
        default: '10%'
      }
    },
    data() {
      return {
        $modal: null
      }
    },
    computed: {
      id() {
        return `materializemodal-${this._uid}`
      }
    },
    mounted() {
      const vm = this
      this.$modal = $(vm.$refs.modal)
      //this.$modal.appendTo('body')
      $(this.$modal).modal({
        dismissible: vm.dismissible,
        opacity: vm.opacity,
        inDuration: vm.inDuration,
        outDuration: vm.outDuration,
        startingTop: vm.startingTop,
        endingTop: vm.endingTop,
        ready(modal, trigger) {
          vm.$emit('ready', modal, trigger)
        },
        complete() {
          vm.$emit('complete')
        }
      })
    },
    methods: {
      open() {
        this.$modal.modal('open')
      },
      close() {
        this.$modal.modal('close')
      }
    }
  }
</script>