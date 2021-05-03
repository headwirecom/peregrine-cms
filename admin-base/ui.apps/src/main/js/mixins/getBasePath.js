export default {
    methods: {
        getBasePath() {
            const view = $perAdminApp.getView()
            let tenant = {name: 'example'}
            if (view.state.tenant) {
                tenant = view.state.tenant
            }
            return `/content/${tenant.name}`
        },
    }
}