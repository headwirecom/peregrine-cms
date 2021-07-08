<template>
  <div class="peregrine-content-view file-editor">
    <template v-if="codemirror.ready">
      <codemirror v-model="content.client" :options="codemirror.options" />
      <div class="actions-wrapper">
        <div class="actions">
          <a
            class="btn-floating btn-large waves-effect waves-light save-btn"
            :title="$i18n('save')"
            @click="onSave"
          >
            <icon icon="save" />
          </a>
          <a
            class="btn-floating btn-large waves-effect waves-light save-and-exit-btn"
            :title="$i18n('save & exit')"
            @click="onSaveAndExit"
          >
            <icon icon="save" />
            <icon icon="cancel" class="sub-icon" />
          </a>
          <a
            class="btn-floating btn-large waves-effect waves-light exit-btn"
            :title="$i18n('exit (without saving)!')"
            @click="onExit"
          >
            <icon icon="cancel" />
          </a>
        </div>
      </div>
    </template>
    <spinner
      v-if="!codemirror.ready"
      width="100"
      height="100"
      position="center"
    />
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
import Spinner from '../spinner/template.vue';

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
  components: { Icon, Spinner },
  mixins: [toast, view, api, stateAction, getTenant],
  props: ['model'],
  data() {
    return {
      content: {
        server: '',
        client: '',
      },
      codemirror: {
        ready: false,
        options: {
          viewportMargin: Infinity,
          lineNumbers: true,
          lineWrapping: true,
          indentWithTabs: false,
          tabSize: 4,
        },
      },
      save: {
        timestamp: null,
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
      } else if (this.extension === '.css') {
        return 'css';
      } else {
        return null;
      }
    },
    hasChanges() {
      return this.content.server !== this.content.client;
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
          this.content.server = data;
          this.content.client = data;
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

      this.stateAction('saveFile', { path, content: content.client, extension })
        .then(() => {
          this.save.timestamp = new Date();
          this.content.server = this.content.client;
          this.toast('Saved file', 'success');
        })
        .catch(() => this.toast('Save failed!', 'error'));
    },

    onSaveAndExit() {
      const { path, content, extension } = this;

      this.stateAction('saveFile', { path, content: content.client, extension })
        .then(() => this.loadExplorer(this.getParentPath()))
        .catch(() => this.toast('Save failed!', 'error'));
    },

    onExit() {
      const me = this;

      if (this.hasChanges) {
        $perAdminApp.askUser(
          'You have unsaved changes',
          'Are you sure you want to exit wihtout saving?',
          {
            yesText: 'Yes',
            noText: 'No',
            yes() {
              return me.loadExplorer(me.getParentPath());
            },
            no() {
              return;
            },
          }
        );
      } else {
        return me.loadExplorer(me.getParentPath());
      }
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

    getParentPath() {
      return this.path
        .split('/')
        .slice(0, -1)
        .join('/');
    },
  },
};
</script>

<style scoped>
.vue-codemirror-wrap {
  width: 98%;
  margin: 1%;
  border: 1px solid #607d8b;
  padding: 3px;
  border-radius: 3px;
  max-height: calc(100% - 1% - 45px);
  overflow: auto;
}

.actions-wrapper {
  position: fixed;
  bottom: 3rem;
  right: 25rem;
  width: 72px;
  height: 72px;
  z-index: 5;
}

.actions-wrapper:hover {
  height: 200px;
}

.actions-wrapper:hover .save-and-exit-btn {
  bottom: 4.5rem;
}

.actions {
  position: relative;
  width: 100%;
  height: 100%;
}

.actions:hover .exit-btn {
  bottom: 8.5rem;
}

.btn-floating {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
}

.save-btn {
  bottom: 0.5rem;
  right: 0.5rem;
  border: 1px solid var(--pcms-blue-grey-darken-3);
  z-index: 6;
}

.save-btn:hover {
  border: 1px solid #ff9800;
}

.save-and-exit-btn {
  bottom: 0.5rem;
  right: 0.5rem;
  border: 1px solid var(--pcms-blue-grey-darken-3);
  background-color: #fff;
  display: flex;
  transition: bottom 300ms linear !important;
}

.save-and-exit-btn .icon {
  color: var(--pcms-blue-grey-darken-3);
}

.save-and-exit-btn .icon.sub-icon {
  position: absolute;
  font-size: 1rem;
  top: 54%;
  left: 54%;
  width: 15px;
  height: 15px;
  padding: 0;
  margin: 0;
  border: 1px solid #fff;
  border-radius: 50%;
  background-color: #fff;
}

.save-and-exit-btn:hover {
  background-color: #ff9800;
  border-color: #ff9800;
}

.save-and-exit-btn:hover .icon.sub-icon {
  background-color: #ff9800;
  border-color: #ff9800;
}

.exit-btn {
  bottom: 0.5rem;
  right: 0.5rem;
  border: 1px solid var(--pcms-blue-grey-darken-3);
  background-color: #fff;
  display: flex;
  transition: bottom 300ms linear !important;
}

.exit-btn:hover {
  background-color: #ff9800;
  border-color: #ff9800;
}

.exit-btn .icon {
  color: var(--pcms-blue-grey-darken-3);
}
</style>
