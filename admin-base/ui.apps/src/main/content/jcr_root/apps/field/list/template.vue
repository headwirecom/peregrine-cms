<template>
    <div class="list-field">
        <input
            ref="field"
            class="form-control"
            type="text"
            style="width: calc(100% - 60px); display: inline; padding-right: 0px"
            :disabled="disabled"
            :maxlength="schema.max"
            :placeholder="schema.placeholder"
            :readonly="schema.readonly" >
        <button v-on:click.stop.prevent="add" style="float: right; height: 46px; width: 54px;"><i class="material-icons">insert_drive_file</i></button>
        <div v-for="item in value" style="max-width: 350px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
            <a href="#" v-on:click.stop.prevent="remove(item)">D</a> {{item}}
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        mixins: [ VueFormGenerator.abstractField ],
        methods: {
            remove(item) {
                let values = this.value

                for(let i = 0; i < values.length; i++) {
                    if(values[i] === item) {
                        values.splice(i, 1)
                        break;
                    }
                }
                this.$forceUpdate()
            },
            add() {
                this.$set(this.value, this.value.length, this.$refs.field.value)
                this.$forceUpdate()
            }
        }
    }
</script>
