const getTenantMixin = {
  methods: {
    getTenant() {
      return (
        $perAdminApp.getView().state.tenant || { name: 'No site selected' }
      );
    },
    getTenantBasePath() {
      return `/content/${this.getTenant().name}`
    }
  },
};

export default getTenantMixin;
