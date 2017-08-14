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
    <form 
      id="file_upload" 
      method="post" 
      action="" 
      enctype="multipart/form-data"
      v-bind:class="`file-upload ${isDragging || uploadProgress ? 'dragging-file' : ''}`"
      v-on:submit.prevent    ="uploadFile"
      v-on:drag.prevent      ="stopPropagation"
      v-on:dragstart.prevent ="stopPropagation"
      v-on:dragover.prevent  ="setDragState"
      v-on:dragenter.prevent ="setDragState"
      v-on:dragleave.prevent ="unSetDragState"
      v-on:dragend.prevent   ="unSetDragState"
      v-on:drop.prevent      ="onDropFile">

      <div v-if="isDragging || uploadProgress" class="file-upload-inner">
        <i class="material-icons">file_download</i>
        <span class="file-upload-text">Drag &amp; Drop files anywhere</span>
        <progress class="file-upload-progress" v-bind:value="uploadProgress" max="100"></progress>
      </div>

    </form>
</template>

<script>
export default {
  props: ['model'],
  data() {
    return {
      isDragging: false,
      uploadProgress: 0,
      formModel: { file: '' },
      schema: {
        fields: [
          {
            type: "input",
            inputType: "file",
            label: "File",
            model: 'file',
            required: false,
            placeholder: "upload file"
          },
          {
            type: "submit",
            buttonText: "upload",
            onSubmit: this.uploadFile,
            model: "upload"
          }
        ]
      },
      formOptions: {
        validateAfterLoad: true,
        validateAfterChanged: true
      }
    }
  },
  methods: {
    setDragState(ev){
      ev.stopPropagation()
      this.isDragging = true
    },
    unSetDragState(ev){
      ev.stopPropagation()
      this.isDragging = false
    },
    onDropFile(ev){
      ev.stopPropagation()
      this.isDragging = false
      this.uploadFile(ev.dataTransfer.files)
    },
    stopPropagation(ev){
      ev.stopPropagation()
    },
    uploadFile(files) {
      $perAdminApp.stateAction('uploadFiles', {
        path: $perAdminApp.getView().state.tools.assets,
        files: files,
        cb: this.setUploadProgress
      })
    },
    setUploadProgress(percentCompleted){
      this.uploadProgress = percentCompleted
      if(percentCompleted === 100){
        $perAdminApp.notifyUser(
          'Success',
          'File uploaded successfully.',
          () => { this.uploadProgress = 0 }
        )
      }
    }
  }
}

</script>
