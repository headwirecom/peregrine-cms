const toastMixin = {
  methods: {
    toast(...args) {
      $perAdminApp.toast(...args);
    },
  },
};

export default toastMixin;
