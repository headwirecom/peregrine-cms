<template>
    <div class="asset-preview">
        <template v-if="currentObject">
            <ul class="asset-info">
                <li>
                    <span class="asset-name">page:</span>
                    <span class="asset-value">{{currentObject}}</span>
                </li>
                <li>
                    <span class="asset-name">created:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li>
                    <span class="asset-name">modified:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li><pre>{{JSON.stringify(page, true, 2)}}</pre></li>
            </ul>
            <button class="btn">rename</button>
            <button class="btn">move</button>
            <button class="btn">delete</button>
            <!--
            <ul class="asset-info">
                <li>
                    <span class="asset-name">created:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li>
                    <span class="asset-name">modified:</span>
                    <span class="asset-value">April 1st, 2017</span>
                </li>
                <li>
                    <span class="asset-name">source:</span>
                    <span class="asset-value">{{ currentObject.show }}</span>
                </li>
            </ul>
            <img v-if="isImage(currentObject.show)" v-bind:src="currentObject.show"/>
            <iframe v-else v-bind:src="currentObject.show"></iframe>
            -->
        </template>
        <template v-else>
            <div class="no-asset-selected">
                <span>no page selected</span>
                <i class="material-icons">info</i>
            </div>
        </template>
    </div>

</template>

<script>
    export default {
        props: ['model'],
        computed: {
            currentObject: function () {
                return $perAdminApp.getNodeFromViewOrNull("/state/tools/page")
            },
            page: function() {
                return $perAdminApp.findNodeFromPath(this.$root.$data.admin.nodes, this.currentObject)
            }

        }
    }
</script>
