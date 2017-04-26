<template>
<div class="col m4">
    <div class="card indigo darken-1">
        <div class="card-content white-text">
            <span class="card-title">{{model.title}}</span>
            <p>
                {{model.description}}
            </p>
            <div class="card-action">
    <admin-components-action v-bind:model="{ target: model.action, command: 'selectPath', title: 'explore' }"></admin-components-action>
            </div>
        </div>
    </div>
</div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>

<style>
.card .card-content p {
    height: 100px;
}
</style>

