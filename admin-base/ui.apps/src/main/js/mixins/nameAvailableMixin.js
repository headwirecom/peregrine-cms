const nameAvailableMixin = {
  methods: {
    nameAvailable(value) {
      if (!value || value.length === 0) {
        return ['name is required'];
      } else {
        const folder = $perAdminApp.findNodeFromPath(
          $perAdminApp.getView().admin.nodes,
          this.formmodel.path
        );
        
        for (let i = 0; i < folder.children.length; i++) {
          if (folder.children[i].name === value) {
            return ['name aready in use'];
          }
        }
        
        return [];
      }
    },
  },
};

export default nameAvailableMixin;
