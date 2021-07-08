const toastMixin = {
  data() {
    return {
      __loneToast: null,
    };
  },
  methods: {
    toast(...args) {
      return $perAdminApp.toast(...args);
    },
    loneToast(...args) {
      if (this.__loneToast) {
        this.__loneToast.remove();
      }

      this.__loneToast = this.toast(...args);
    },
  },
};

export default toastMixin;
