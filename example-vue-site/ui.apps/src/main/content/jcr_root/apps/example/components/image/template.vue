<template>
    <div class="card" v-bind:data-per-path="model.path">
        <img class="card-img-top" v-bind:src="model.imagePath" v-bind:alt="model.caption">
        <div class="card-block">
            <h4 class="card-block">{{model.title}}</h4>
            <p class="card-text">{{model.caption}}</p>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

<style>
    img .card-img-top {
        width: 100%;
    }
</style>
