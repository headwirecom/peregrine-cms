<template>
    <div class="nav-content" style=" height: 48px;">
        <ul style="text-align: center; padding-top: 5px;">
            <template v-for="child in model.children">
                <li v-bind:is="child.component" v-bind:model="child"></li>
            </template>
        </ul>
    </div>
</template>

<script>
export default {
    props: ['model']
}
</script>
