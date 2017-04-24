<template>
    <div>
        <p>editor</p>
        {{this.$root.$data.state.editor}}
        <vue-form-generator :schema="schema" :model="formdata" :options="formOptions"></vue-form-generator>
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

            schema:{ fields:[{
               type: "input",
               inputType: "text",
               label: "ID (disabled text field)",
               model: "id",
               readonly: true,
               disabled: true
            },{
               type: "input",
               inputType: "text",
               label: "Name",
               model: "name",
               placeholder: "Your name",
               featured: true,
               required: true
            }]},

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