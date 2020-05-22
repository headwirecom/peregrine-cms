<template>
    <div class="peregrine-content-view">
        <p>
            this should be a dialog editor for the dialog.json files. similar to the page editor, 
            drag and drop form elements from a list to the right into a visual list of elements. 
            Once we select a form element the right hand panel shows the properties for the elements.
        </p>
        <p>
            The ability to preview forms would be nice here as well. 
        </p>
        <p>
            The objects defined in /samples/example-vue-site/ui.apps/src/main/content/jcr_root/apps/example/objects
            should provide a good list of all available form fields.<br>
            We also should allow for grouping of fields (one level).
        </p>
    </div>    
</template>

<script>
export default {
    props: ['model']
}
</script>

<style>

</style>