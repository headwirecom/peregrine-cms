<template>
    <div style="height: 90%; ">
        <p>components</p>
        <div class="collection" style="height: 100%; overflow: auto;">
            <a draggable="true" class="collection-item" v-for="cmp in this.$root.$data.admin.components.data">{{cmp.path.split('/')[2]}} {{cmp.name}}</a>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>