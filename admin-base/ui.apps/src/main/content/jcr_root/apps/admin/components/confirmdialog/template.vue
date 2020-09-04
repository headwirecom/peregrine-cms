<template>
    <div class="confirm-dialog">
        <button class="modal-action waves-effect waves-light btn-flat" v-on:click="cancelEdit">
            {{$i18n(cancelText)}}
        </button>
        <button class="modal-action waves-effect waves-light btn-flat"
                v-if="submitText"
                v-on:click="confirmEdit">
            {{$i18n(submitText)}}
        </button>
    </div>
</template>

<script>

export default {
  name: 'ConfirmDialog',
    props: {
        cancelText: {type: String, default: "cancel"},
        submitText: String
    },
    methods: {
        cancelEdit(){
            this.$emit('confirm-dialog', 'cancel')
        },
        confirmEdit(){
            this.$emit('confirm-dialog', 'confirm')
        }
    }
}
</script>