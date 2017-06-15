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
        beforeDestroy() {
            this.quill = null
        },
        watch: {
            value(newVal, oldVal) {
                if(newVal != this.$refs.quilleditor.children[0].innerHTML) {
                    this.$refs.quilleditor.children[0].innerHTML = this.value
                }
            }
        },
        methods: {
            initialize() {
                this.$refs.quilleditor.innerHTML = this.value
                this.quill = new Quill(this.$refs.quilleditor, {
                    theme: 'snow',
                    modules: {
                        toolbar: [
                            [{ header: [1, 2, 3, 4, 5, false] }],
                            ['bold', 'italic', 'underline'],
                            [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                            [{ 'script': 'sub'}, { 'script': 'super' }],
                            [{ 'indent': '-1'}, { 'indent': '+1' }],
                            ['code-block'],
                            ['clean']
                        ]
                    }                })
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
