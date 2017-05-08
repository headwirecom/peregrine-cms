<template>
<p v-html="text" v-bind:data-per-path="model.path"></p>
</template>

<script>
export default {
    props: [ 'model'],
    computed: {
        text: function() {
            if(this.model.text !== '') {
                return this.model.text
            }
            if(window.parent && window.parent.$perAdminApp) {
                return 'empty'
            }
        }
    }
//    mounted: function() {
//        if(window.parent && window.parent.$perAdminView) {
//            var box = this.$el.getBoundingClientRect()
//            if(box.width === 0 || box.height === 0) {
//                var div = document.createElement('div')
//                div.setAttribute('data-per-path', this.model.path)
//                var text = document.createTextNode('empty')
//                div.appendChild(text)
//                this.$el.appendChild(div)
//            }
//        }
//    }
}
</script>
