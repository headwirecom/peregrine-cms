const stateActionMixin = {
  methods: {
    stateAction(...args) {
      return $perAdminApp.stateAction(...args);
    },
  },
};

export default stateActionMixin;
