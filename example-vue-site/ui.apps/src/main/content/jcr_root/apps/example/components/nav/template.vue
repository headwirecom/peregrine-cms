<template>
    <nav class="navbar navbar-toggleable-md navbar-light bg-faded" v-bind:data-per-path="model.path">
        <button class="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <a class="navbar-brand" v-bind:href="siteRoot">{{model.brand}}</a>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item" v-for="navItem in model.navigation">
                    <a class="nav-link" v-bind:class="isActive(navItem)" v-bind:href="navItem.path +'.html'">{{navItem.title}}</a>
                </li>
            </ul>
        </div>
    </nav>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentPagePath: function() {
                return this.$root.$data.page.pagePath
            },
            siteRoot: function() {
                return this.$root.$data.page.siteRoot+'.html'
            }
        },
        methods: {
            isActive: function(item) {
                return item.path === this.$root.$data.page.pagePath ? 'active': ''
            }
        }
    }
</script>
