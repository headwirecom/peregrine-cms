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
    <span>
      <a v-bind:title="$i18n('fileUpload')" href="#!" class="btn-floating waves-effect waves-light">
        <label style="cursor: inherit;">
          <input type="file" ref="file_upload" style="display:none" v-on:change="addFiles">
          <i class="material-icons">file_upload</i>
        </label>
      </a>
    </span>
</template>

<script>
export default {
  props: ['model'],
  methods: {
    addFiles (ev) {
      this.uploadFile(ev.target.files)
    },
    uploadFile(files) {
      $perAdminApp.stateAction('uploadFiles', {
        path: $perAdminApp.getView().state.tools.assets,
        files: files,
        cb: this.fileUploadComplete
      })
    },
    fileUploadComplete(percentCompleted){
      return
    }
  }
}

</script>
