<template>
    <div class="container">
        <input type="text" v-model="input">
        <button class="btn" v-on:click.stop="search()">search</button>
        <div class="row">
            <div class="col s2" v-for="item in results"><a href="#" v-on:click.stop="select(item)" class="hoverable"><img v-bind:src="item.previewURL"></a></div>
        </div>
    </div>
</template>

<script>

    export default {
        props: ['model'],
        computed: {
            results() {
                var node = $perAdminApp.getNodeFromViewOrNull('/admin/images')
                return node;
            }
        },
        methods: {
            search() {
                var API_KEY = '5575459-c51347c999199b9273f4544d4';
                var URL = "https://pixabay.com/api/?key="+API_KEY+"&q="+encodeURIComponent(this.input);
                $.getJSON(URL, function(data){
                    var node = $perAdminApp.getNodeFromView('/admin')
                    if (parseInt(data.totalHits) > 0) {
                        Vue.set(node, 'images', data.hits)
                    }
                    else {
                        console.log('No hits');
                        Vue.set(node, 'images', [])
                    }
                });
            },
            select(item) {
                var name = item.previewURL.split('/').pop()
                $perAdminApp.stateAction('fetchExternalAsset', { url: item.webformatURL, path: $perAdminApp.getNodeFromView('/state/tools/assets'), name: name})
            }
        }
    }

</script>
