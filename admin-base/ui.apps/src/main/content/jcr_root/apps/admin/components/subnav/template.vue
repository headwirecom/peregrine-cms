<template>
    <div class="nav-content" style=" height: 48px;">
        <ul style="padding-top: 5px;" v-bind:class="classes">
            <template v-for="child in model.children">
                <li v-bind:is="child.component" v-bind:model="child"></li>
            </template>
        </ul>
    </div>
</template>

<script>
export default {
    props: ['model'],
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        }
    }
}
</script>

<style>
    .navcenter {
        text-align: center;
    }
    .navright {
        text-align: right;
    }
</style>
