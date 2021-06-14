const viewMixin = {
  computed: {
    view() {
      return $perAdminApp.getView();
    },
  },
};

export default viewMixin;
