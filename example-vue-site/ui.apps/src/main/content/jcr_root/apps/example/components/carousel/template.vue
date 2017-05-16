<template>
    <div v-bind:id="name" class="carousel slide" data-ride="carousel" v-bind:data-per-path="model.path" style="background-color: silver;">
        <ol class="carousel-indicators">
            <li v-bind:data-target="'#'+name" data-slide-to="0" class="active"></li>
            <li v-bind:data-target="'#'+name" data-slide-to="1"></li>
            <li v-bind:data-target="'#'+name" data-slide-to="2"></li>
        </ol>
        <div class="carousel-inner" role="listbox">
            <div class="carousel-item active">
                <img class="d-block img-fluid" src="/etc/felibs/admin/peregrine-logo.png" alt="First slide">
                <div class="carousel-caption d-none d-md-block">
                    <h3>...</h3>
                    <p>...</p>
                </div>
            </div>
            <div class="carousel-item">
                <img class="d-block img-fluid" src="/etc/felibs/admin/peregrine-logo.png" alt="Second slide">
                <div class="carousel-caption d-none d-md-block">
                    <h3>...</h3>
                    <p>...</p>
                </div>
            </div>
            <div class="carousel-item">
                <img class="d-block img-fluid" src="/etc/felibs/admin/peregrine-logo.png" alt="Third slide">
                <div class="carousel-caption d-none d-md-block">
                    <h3>...</h3>
                    <p>...</p>
                </div>
            </div>
        </div>
        <a class="carousel-control-prev" v-bind:href="'#'+name" role="button" data-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="sr-only">Previous</span>
        </a>
        <a class="carousel-control-next" v-bind:href="'#'+name" role="button" data-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="sr-only">Next</span>
        </a>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            name() {
                return this.model.path.split('/').slice(1).join('-').slice(4)
            }
        }
    }
</script>
