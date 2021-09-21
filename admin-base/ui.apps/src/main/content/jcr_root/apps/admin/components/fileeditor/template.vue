<template>
  <div class="peregrine-content-view file-editor" @click="focusEditor">
    <template v-if="codemirror.ready">
      <ul class="menu-bar">
        <li class="menu-action" @click="autoFormatSelection">
          <icon icon="code" />Format selection
        </li>
        <li class="menu-action" @click="execCommand('undo')">
          <icon icon="undo" /> Undo
        </li>
        <li class="menu-action" @click="execCommand('redo')">
          <icon icon="redo" /> Redo
        </li>
        <li v-if="lastSave" class="last-save">
          <icon icon="save" /> {{ vLastSave }}
        </li>
      </ul>
      <codemirror
        ref="cm"
        v-model="content.client"
        :options="codemirror.options"
        @keydown.native="handleHotkeys"
      >
      </codemirror>
      <div class="actions-wrapper">
        <div class="actions">
          <a
            class="btn-floating btn-large waves-effect waves-light save-btn"
            :title="$i18n('save')"
            @click="save"
          >
            <icon icon="save" />
          </a>
          <a
            class="btn-floating btn-large waves-effect waves-light save-and-exit-btn"
            :title="$i18n('save & exit')"
            @click="saveAndExit"
          >
            <icon icon="save" />
            <icon icon="cancel" class="sub-icon" />
          </a>
          <a
            class="btn-floating btn-large waves-effect waves-light exit-btn"
            :title="$i18n('exit (without saving)!')"
            @click="exit"
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
import { stateAction, toast, view } from '../../../../../../js/mixins';
import { asyncLoadJsScript, get, isMac } from '../../../../../../js/utils';
import Icon from '../icon/template.vue';
import Spinner from '../spinner/template.vue';

export default {
  name: 'FileEditor',
  components: { Icon, Spinner },
  mixins: [toast, view, stateAction],
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
      lastSave: null,
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
      if (this.extension === '.js') {
        return `javascript`;
      } else if (this.extension === '.json') {
        return {name: `javascript`, json: true};
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
    vLastSave() {
      const DD = `${this.lastSave.getDate()}`.padStart(2, '0');
      const MM = `${this.lastSave.getMonth() + 1}`.padStart(2, '0');
      const YYYY = `${this.lastSave.getFullYear()}`.padStart(2, '0');
      const hh = `${this.lastSave.getHours()}`.padStart(2, '0');
      const mm = `${this.lastSave.getMinutes()}`.padStart(2, '0');
      const ss = `${this.lastSave.getSeconds()}`.padStart(2, '0');

      return `${DD}.${MM}.${YYYY} ${hh}:${mm}:${ss}`;
    },
  },
  created() {
    this.loadSyntaxHighlighting().then(() => {
      this.codemirror.options.mode = this.mode;

      if (this.path) {
        asyncLoadJsScript(`${CODEMIRROR_PATH}/formatting/formatting.js`)
          .then(this.loadFileContent)
          .then(() => {
            this.codemirror.ready = true;
          })
          .catch(() => {
            this.codemirror.ready = true;
          });
      } else {
        this.toast(`File not found!`, 'error');
      }
    });
  },
  methods: {
    loadSyntaxHighlighting() {
      const { mode } = this;
      const { Promise } = window;

      if (typeof mode === 'string') {
        return asyncLoadJsScript(`${CODEMIRROR_PATH}/mode/${mode}/${mode}.js`);
      } else if (typeof mode === 'object') {
        return asyncLoadJsScript(`${CODEMIRROR_PATH}/mode/${mode.name}/${mode.name}.js`);
      } else {
        return Promise.resolve();
      }
    },

    loadFileContent() {
      const options = Object.assign({ url: this.path }, axiosPlainTextOptions);

      return axios(options)
        .then(({ data }) => {
          this.content.server = data;
          this.content.client = data;
        })
        .catch((e) => {
          console.error(e);
          this.toast(`File not found!`, 'error');
        });
    },

    save() {
      const { path, content, extension } = this;

      this.stateAction('saveFile', { path, content: content.client, extension })
        .then(() => {
          this.lastSave = new Date();
          this.content.server = this.content.client;
          this.loneToast('Saved file', 'success');
        })
        .catch(() => this.toast('Save failed!', 'error'));
    },

    saveAndExit() {
      const { path, content, extension } = this;

      this.stateAction('saveFile', { path, content: content.client, extension })
        .then(() => this.loadExplorer(this.getParentPath()))
        .catch(() => this.toast('Save failed!', 'error'));
    },

    exit() {
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

    focusEditor({ target }) {
      if (target.classList.contains('file-editor')) {
        const editor = this.$refs.cm.editor;
        editor.execCommand('goDocEnd');
        editor.focus();
      }
    },

    execCommand(command) {
      this.$refs.cm.editor.execCommand(command);
    },

    autoFormatSelection() {
      const editor = this.$refs.cm.editor;
      const range = {
        from: editor.getCursor(true),
        to: editor.getCursor(false),
      };

      editor.autoFormatRange(range.from, range.to);
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

    handleHotkeys(event) {
      const commandOrControl = isMac() ? event.metaKey : event.ctrlKey;

      if (commandOrControl) {
        if (event.altKey) {
          if (event.key === 'l') {
            event.preventDefault();
            this.autoFormatSelection();
          }
        } else if (event.key === 's') {
          event.preventDefault();
          this.save();
        }
      }
    },
  },
};

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

const CODEMIRROR_PATH = `/etc/felibs/admin/dependencies/codemirror`;
</script>

<style scoped>
.file-editor {
  width: calc(100% - 340px) !important;
  margin: 3rem 1% 1% 1%;
  border: 1px solid #607d8b;
  padding: 0;
  border-radius: 3px;
  max-height: calc(100% - 1% - 35px - 3rem);
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
  transition: bottom 300ms linear, background-color 350ms linear !important;
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
  transition: background-color 350ms linear, border 350ms linear !important;
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
  transition: bottom 300ms linear, background-color 350ms linear !important;
}

.exit-btn:hover {
  background-color: #ff9800;
  border-color: #ff9800;
}

.exit-btn .icon {
  color: var(--pcms-blue-grey-darken-3);
}

.menu-bar {
  width: calc(100% - 340px - 2%);
  background-color: #eceff1;
  list-style-type: none;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin: 0;
  border: 1px solid #607d8b;
  position: fixed;
  top: 122px;
  left: 1%;
  z-index: 5;
}

.menu-bar > li {
  display: flex;
  justify-content: center;
  align-items: center;
}

.menu-bar .menu-action {
  border-right: 1px solid #607d8b;
  padding: 0.25rem 0.5rem;
  color: #616567;
  cursor: pointer;
  transition: background-color 350ms linear;
  user-select: none;
}

.menu-bar .menu-action:hover {
  background-color: #cfd8dc;
}

.menu-bar .menu-action:active {
  background-color: #fff;
}

.menu-bar > li .icon {
  margin-right: 0.25rem;
}

.menu-bar .last-save {
  margin-left: auto;
  padding-right: 0.5rem;
  color: var(--pcms-gray);
  font-size: 85%;
}

.menu-bar .last-save .icon {
  font-size: 18px;
}
</style>
