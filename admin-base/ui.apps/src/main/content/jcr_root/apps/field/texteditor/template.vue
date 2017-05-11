<template>
    <div class="wrapper" style="display: flex; flex-direction: column; min-height: 200px;">
        <div ref="quilleditor" style="width: 100%"></div>
    </div>
</template>

<script>
    export default {
        mixins: [ VueFormGenerator.abstractField ],
        mounted() {
            this.initialize()
        },
        methods: {
            initialize() {
                this.$refs.quilleditor.innerHTML = this.value
                this.quill = new Quill(this.$refs.quilleditor, {theme: 'snow'})
                this.quill.on('text-change', (delta, oldDelta, source) => {
                    this.value = this.$refs.quilleditor.children[0].innerHTML
                } )
            }
        }
    }
</script>

<style>
    .ql-container {
        background-color: white;
    }
</style>
