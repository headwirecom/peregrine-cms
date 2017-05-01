<template>
    <div class="jumbotron" v-bind:data-per-path="model.path">
        <h1 class="display-3">{{model.title}}</h1>
        <p class="lead">{{model.text}}</p>
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>
