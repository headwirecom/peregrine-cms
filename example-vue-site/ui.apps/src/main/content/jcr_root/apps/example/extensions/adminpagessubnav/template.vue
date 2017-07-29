<template>
    <div> my extension </div>
</template>

<script>
    if($perAdminApp) $perAdminApp.registerExtension('admin.pages.subnav', 'example-extensions-adminpagessubnav')
    export default {
        props: ['model']
    }
</script>
