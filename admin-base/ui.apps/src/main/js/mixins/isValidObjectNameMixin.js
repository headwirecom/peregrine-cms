const isValidObjectNameMixin = {
  methods: {
    validObjectName(value) {
      if (!value || value.length === 0) {
        return ['name is required'];
      }
      if (value.match(/[^0-9a-zA-Z-]/)) {
        return [
          'object names may only contain letters, numbers, underscores, and dashes. Google dislikes underscores in URLs',
        ];
      }
      return [];
    },
  },
};

export default isValidObjectNameMixin;
