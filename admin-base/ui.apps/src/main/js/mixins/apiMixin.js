const apiMixin = {
  computed: {
    api() {
      return $perAdminApp.getApi();
    },
  },
};

export default apiMixin;
