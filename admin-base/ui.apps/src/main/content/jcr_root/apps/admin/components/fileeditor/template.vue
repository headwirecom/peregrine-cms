<template>
  <div class="peregrine-content-view">
    <codemirror v-model="content" />
  </div>
</template>

<script>
import { SUFFIX_PARAM_SEPARATOR } from '../../../../../../js/constants';
import { api, stateAction, toast, view } from '../../../../../../js/mixins';
import { get } from '../../../../../../js/utils';

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
      return get(this.view, '/state/tools/file', null);
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
        return this.loadFileEditor(target.path);
      } else {
        return this.loadExplorer(target.path);
      }
    },

    loadFileEditor(path) {
      return $perAdminApp.stateAction('selectToolsNodesPath', {
        selected: path,
        path: '/state/tools/file',
      });
    },

    loadExplorer(path) {
      $perAdminApp.stateAction('unselectFile');
      return $perAdminApp.loadContent(
        `/content/admin/pages/object-definitions.html/path${SUFFIX_PARAM_SEPARATOR}${path}`
      );
    },
  },
};
</script>

<style scoped>
.save-btn {
  margin-left: auto;
}
</style>
