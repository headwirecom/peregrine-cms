<template>
    <div> my extension </div>
</template>

<script>
    if(typeof $perAdminApp != 'undefined') {
        $perAdminApp.registerExtension('admin.pages.subnav', 'example-extensions-adminpagessubnav')
    }
    export default {
        props: ['model']
    }
</script>
