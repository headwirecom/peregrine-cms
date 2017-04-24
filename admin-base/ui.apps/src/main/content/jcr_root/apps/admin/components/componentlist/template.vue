<template>
    <div>
        <p>components</p>
        <div class="card blue-grey darken-1" draggable="true">
            <div class="card-content white-text">
                <span class="card-title">text</span>
                <p>text component - allows you to author rich text areas</p>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        props: ['model']
    }
</script>