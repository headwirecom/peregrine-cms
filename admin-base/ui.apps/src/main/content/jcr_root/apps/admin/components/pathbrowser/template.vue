<template>
<div>
    <div id="pathBrowserModal" class="modal">
        <div class="modal-content">
            <div class="row">
                <div class="col s12">
                    <ul class="tabs">
                        <li class="tab col s2"><a href="#" v-bind:class="isSelected('browse') ? 'active' : ''" v-on:click.stop.prevent="selectBrowse">Browse</a></li>
                        <li class="tab col s2"><a href="#" v-bind:class="isSelected('search') ? 'active' : ''" v-on:click.stop.prevent="selectSearch">Search</a></li>
                    </ul>
                </div>
                <div v-if="isSelected('browse')" class="col s12">
                    <ul class="collection with-header">
                        <li class="collection-header">{{path}}</li>
                        <li class="collection-item" v-for="item in nodes.children" v-if="display(item)">
                            <a href="" v-if="isFile(item)">{{item.name}}</a>
                            <a href="" v-if="isFolder(item)">{{item.name}}</a>
                        </li>
                    </ul>
                </div>
                <div v-if="isSelected('search')" class="col s12">
                    searchui
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <a 
                href="#!"
                class="modal-action modal-close waves-effect waves-green btn-flat">
                ok
            </a>
        </div>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data: function() {
            return {
                selected: 'browse',
                path: '/content/assets'
            }
        },
        computed: {
            nodes() {
                let view = $perAdminApp.getView()
                let nodes = view.admin.nodes
                if(nodes) {
                    return $perAdminApp.findNodeFromPath(nodes, this.path)
                }
                return {}
            }
        },
        methods: {
            display(item) {
                return item.name !== 'jcr:content'
            },
            isFile(item) {
                return true
            },
            isFolder(item) {
                return false
            },
            isSelected(name) {
                return name === this.selected
            },
            selectBrowse(ev) {
                this.selected = 'browse'
            },
            selectSearch(ev) {
                this.selected = 'search'
            }
        }
    }
</script>
