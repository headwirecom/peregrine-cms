<template>
<div class="row">
    <admin-components-iconaction v-for="action in fromSource" v-bind:model="action"></admin-components-iconaction>
</div>
</template>

<script>
    export default {
        props: ['model']
        ,
        computed: {
            fromSource: function() {
                var segments = this.model.source.split('/').slice(1)
                var ret = this.$root.$data
                for(var i = 0; i < segments.length; i++) {
                    ret = ret[segments[i]]
                }
                return ret
            }
        }
    }
</script>
