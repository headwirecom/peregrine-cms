<template>
    <div class="pathbrowser input-field">
        <input
            class="form-control"
            style="width: calc(100% - 60px); display: inline; padding-right: 0px"
            type="text"
            v-model="value"
            :disabled="disabled"
            :maxlength="schema.max"
            :placeholder="schema.placeholder"
            :readonly="schema.readonly" >
        <button v-on:click.stop.prevent="browse" style="float: right; height: 46px; width: 54px;"><i class="material-icons">insert_drive_file</i></button>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        mixins: [ VueFormGenerator.abstractField ],
        methods: {
            browse() {
                $perAdminApp.pathBrowser(
                    this.schema.browserRoot,
                    (newValue) => { this.value = newValue }
                )
                return $perAdminApp.getNodeFromViewOrNull('/state/pathbrowser/methods').onShow()
            }
        }
    }
</script>

<style>
    .pathbrowser {
        width: 100%;
    }
</style>
