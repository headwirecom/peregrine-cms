<template>
  <div class="peregrine-content-view">
    <codemirror v-model="content" />
  </div>
</template>

<script>
import { SUFFIX_PARAM_SEPARATOR } from '../../../../../../js/constants';
import { get } from '../../../../../../js/utils';
import { toast, view, api, stateAction } from '../../../../../../js/mixins';

const axiosPlainTextOptions = {
  headers: {
    'Content-Type': 'text/plain',
  },
  responseType: 'text',
  transformResponse: [
    (data) => {
      return data;
    },
  ],
};

export default {
  mixins: [toast, view, api, stateAction],
  props: ['model'],
  data() {
    return {
      content: '',
    };
  },
  computed: {
    path() {
      return get(this.view, '/state/tools/objectdefinitioneditor', null);
    },
  },
  created() {
    if (this.path) {
      this.loadFileContent();
    } else {
      this.sendFileNotFoundToast();
    }
  },
  methods: {
    loadFileContent() {
      const options = Object.assign({ url: this.path }, axiosPlainTextOptions);

      axios(options)
        .then(({ data }) => {
          this.content = data;
        })
        .catch((e) => {
          console.error(e);
          this.sendFileNotFoundToast();
        });
    },

    sendFileNotFoundToast() {
      this.toast(`File not found!`, 'error');
    },

    onSave() {
      const { path, content } = this;

      this.stateAction('saveObjectDefinitionFile', {
        path,
        content,
        format: '.json',
      });
    },

    selectPathInNav(me, target) {
      if (target.path === me.path) {
        return $perAdminApp.stateAction('selectToolsNodesPath', {
          selected: target.path,
          path: '/state/tools/objectdefinitioneditor',
        });
      } else {
        return $perAdminApp.loadContent(
          `/content/admin/pages/object-definitions.html/path${SUFFIX_PARAM_SEPARATOR}${
            target.path
          }`
        );
      }
    },
  },
};
</script>

<style scoped>
.save-btn {
  margin-left: auto;
}
</style>
