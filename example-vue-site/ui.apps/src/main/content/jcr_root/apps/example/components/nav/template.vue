<template>
    <nav class="navbar navbar-toggleable-md navbar-light bg-faded" v-bind:data-per-path="model.path">
        <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <a class="navbar-brand" href="#">{{model.brand}}</a>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item active">
                    <a class="nav-link" href="#">intro</a>
                </li>
            </ul>
        </div>
    </nav>
</template>

<script>
    export default {
        props: ['model']
    }
</script>
