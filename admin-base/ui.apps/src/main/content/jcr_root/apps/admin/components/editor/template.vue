<template>
    <div>
        <p>editor</p>
        <vue-form-generator v-if="this.$root.$data.state.editor" :schema="this.$root.$data.state.editor.dialog" :model="formdata" :options="formOptions"></vue-form-generator>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() { return {

            formdata:{
              id: 1,
              name: "John Doe",
              password: "J0hnD03!x4",
              skills: ["Javascript", "VueJS"],
              email: "john.doe@gmail.com",
              status: true
            },

            schema: {},

            formOptions: {
              validateAfterLoad: true,
              validateAfterChanged: true
            }
          }
      },
      mounted: function() {
        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
      }
    }
</script>