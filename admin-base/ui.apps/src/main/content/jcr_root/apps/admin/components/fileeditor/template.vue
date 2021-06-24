<template>
  <div class="peregrine-content-view">
    <codemirror
      v-if="codemirror.ready"
      v-model="content"
      :options="codemirror.options"
    />
    <a
      class="btn-floating btn-large waves-effect waves-light save-btn"
      :title="$i18n('save')"
      @click="onSave"
    >
      <icon icon="save" />
    </a>
  </div>
</template>

<script>
import { SUFFIX_PARAM_SEPARATOR } from '../../../../../../js/constants';
import {
  api,
  getTenant,
  stateAction,
  toast,
  view,
} from '../../../../../../js/mixins';
import { asyncLoadJsScript, get } from '../../../../../../js/utils';
import Icon from '../icon/template.vue';

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
  name: 'FileEditor',
  components: { Icon },
  mixins: [toast, view, api, stateAction, getTenant],
  props: ['model'],
  data() {
    return {
      content: '',
      codemirror: {
        ready: false,
        options: {
          lineNumbers: true,
          lineWrapping: true,
          indentWithTabs: false,
          tabSize: 4,
        },
      },
    };
  },
  computed: {
    path() {
      return get(this.view, '/state/tools/file', null);
    },
    filename() {
      return this.path.split('/').pop();
    },
    extension() {
      return `.${this.filename.split('.').pop()}`;
    },
    mode() {
      if (['.js', '.json'].includes(this.extension)) {
        return `javascript`;
      } else if (this.extension === '.xml') {
        return `xml`;
      } else {
        return null;
      }
    },
  },
  created() {
    this.loadSyntaxHighlighting().then(() => {
      this.codemirror.options.mode = this.mode;

      if (this.path) {
        this.loadFileContent();
      } else {
        this.toast(`File not found!`, 'error');
      }
    });
  },
  methods: {
    loadSyntaxHighlighting() {
      const { mode } = this;

      if (mode) {
        return asyncLoadJsScript(
          `/etc/felibs/admin/dependencies/codemirror/mode/${mode}/${mode}.js`
        );
      } else {
        return window.Promise.resolve();
      }
    },

    loadFileContent() {
      const options = Object.assign({ url: this.path }, axiosPlainTextOptions);

      axios(options)
        .then(({ data }) => {
          this.content = data;
          this.codemirror.ready = true;
        })
        .catch((e) => {
          console.error(e);
          this.codemirror.ready = true;
          this.toast(`File not found!`, 'error');
        });
    },

    onSave() {
      const { path, content, extension } = this;

      this.stateAction('saveFile', { path, content, extension });
    },

    selectPathInNav(me, { path }) {
      if (path === me.path) {
        return this.loadFileEditor(path);
      } else {
        return this.loadExplorer(path);
      }
    },

    loadFileEditor(path) {
      return $perAdminApp.stateAction('selectToolsNodesPath', {
        selected: path,
        path: '/state/tools/file',
      });
    },

    loadExplorer(path) {
      const page = path.split('/')[3];

      $perAdminApp.stateAction('unselectFile');

      return $perAdminApp.loadContent(
        `/content/admin/pages/${page}.html/path${SUFFIX_PARAM_SEPARATOR}${path}`
      );
    },
  },
};
</script>

<style scoped>
.save-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  z-index: 5;
}
</style>
