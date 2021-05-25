<template>
  <div class="peregrine-content-view">
    <div class="tabs-wrapper horizontal">
      <div class="handles-wrapper">
        <button
          v-for="handle in tabs.handles"
          :key="`handle-${handle.id}`"
          :disabled="handle.disabled"
          class="handle"
          :class="{ active: selected === handle.id }"
          v-on:click="selected = handle.name"
        >
          {{ handle.label }}
        </button>
        <button
          type="button"
          class="btn btn-raised waves-effect waves-light right"
          @click="onSave"
        >
          Save
        </button>
      </div>
      <!-- div :class="`content active-tab-index-1`" v-if="selected === 'fields'"> <ul class="collection"> <li v-for="field in fields" v-bind:key="field" class="collection-item" draggable="true" > <admin-components-draghandle /> <admin-components-action v-bind:model="{ target: '', command: 'selectField', tooltipTitle: `select`, }" > <i class="material-icons">folder_open</i> {{ field }} </admin-components-action> </li> <li class="collection-item"> <admin-components-action v-bind:model="{ target: '', command: 'addField', tooltipTitle: `${$i18n('add field')}`, }" > <i class="material-icons">add_circle</i> {{ $i18n('add field') }} </admin-components-action> </li> </ul> </div> <div :class="`content active-tab-index-2`" v-if="selected === 'form'"> <ul class="collection"> <li class="collection-item"> <admin-components-action v-bind:model="{ target: '', command: 'addField', tooltipTitle: `${$i18n('add field')}`, }" > Categorization <ul class="collection"> <li class="collection-item"> Category: Hello </li> </ul> </admin-components-action> </li> </ul> </div -->
      <div :class="`content active-tab-index-3`" v-if="selected === 'code'">
        <div>
          <codemirror v-model="content" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { get } from '../../../../../../js/utils';
import { toast, view, api } from '../../../../../../js/mixins';

export default {
  mixins: [toast, view, api],
  props: ['model'],
  data() {
    return {
      tabs: {
        handles: [
          { id: 'fields', label: 'Fields', disabled: true },
          { id: 'form', label: 'Form', disabled: true },
          { id: 'code', label: 'Code View' },
        ],
      },
      selected: 'code',
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
      axios
        .get(this.path)
        .then(({ data }) => (this.content = JSON.stringify(data, null, 2)))
        .catch((e) => {
          console.error(e);
          this.sendFileNotFoundToast();
        });
    } else {
      this.sendFileNotFoundToast();
    }
  },
  methods: {
    getContent(...args) {
      return $perAdminApp.getContent(...args);
    },
    sendFileNotFoundToast() {
      this.toast(`File not found!`, 'error');
    },
    onSave() {
      //todo: save func.
    }
  },
};
</script>
