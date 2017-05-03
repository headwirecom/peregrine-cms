<template>
    <span>
        <input id="fileupload" type="file" placeholder="upload file" class="form-control">
        <button v-on:click="uploadFile" class="btn">upload</button>
    </span>
</template>

<script>
export default {
    props: ['model'],
    data: function() {

            return {
            formModel: { file: '' },
            schema: { fields: [{
                       type: "input",
                       inputType: "file",
                       label: "File",
                       model: 'file',
                       required: false,
                       placeholder: "upload file"
                       },{
                        type: "submit",
                        buttonText: "upload",
                        onSubmit: this.uploadFile,
                        model: "upload"
                        }]},
            formOptions: {
                          validateAfterLoad: true,
                          validateAfterChanged: true
                        }
                   }

    }, methods: {
        uploadFile: function() {
            $perAdminApp.stateAction('uploadFiles', { path: $perAdminApp.getView().state.tools.assets, files: this.$el.children[0].files })
        }
    }
}

</script>
