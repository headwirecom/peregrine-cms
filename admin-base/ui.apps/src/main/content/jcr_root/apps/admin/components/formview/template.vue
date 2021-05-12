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
          v-on:click="select(handle.id)"
        >
          {{ handle.label }}
        </button>
      </div>
      <!-- div :class="`content active-tab-index-1`" v-if="selected === Tab.FIELDS"> <ul class="collection"> <li v-for="field in fields" v-bind:key="field" class="collection-item" draggable="true" > <admin-components-draghandle /> <admin-components-action v-bind:model="{ target: '', command: 'selectField', tooltipTitle: `select`, }" > <i class="material-icons">folder_open</i> {{ field }} </admin-components-action> </li> <li class="collection-item"> <admin-components-action v-bind:model="{ target: '', command: 'addField', tooltipTitle: `${$i18n('add field')}`, }" > <i class="material-icons">add_circle</i> {{ $i18n('add field') }} </admin-components-action> </li> </ul> </div> <div :class="`content active-tab-index-2`" v-if="selected === Tab.FORM"> <ul class="collection"> <li class="collection-item"> <admin-components-action v-bind:model="{ target: '', command: 'addField', tooltipTitle: `${$i18n('add field')}`, }" > Categorization <ul class="collection"> <li class="collection-item"> Category: Hello </li> </ul> </admin-components-action> </li> </ul> </div -->
      <div
        :class="`content active-tab-index-3`"
        v-if="selected === Tab.CODE_VIEW"
      >
        <div><codemirror v-model="schemaAsFile"></codemirror></div>
      </div>
    </div>
  </div>
</template>

<script>
const Tab = {
  FIELDS: 1,
  FORM: 2,
  CODE_VIEW: 3,
};

export default {
  props: ['model'],
  data() {
    return {
      Tab,
      tabs: {
        handles: [
          { id: Tab.FIELDS, label: 'Fields', disabled: true },
          { id: Tab.FORM, label: 'Form', disabled: true },
          { id: Tab.CODE_VIEW, label: 'Code View' },
        ],
      },
      selected: Tab.CODE_VIEW,
      fields: [
        '#/properties/number',
        '#/properties/street_name',
        '#/properties/street_type',
      ],
      schema: {
        type: 'object',
        properties: {
          number: { type: 'number' },
          street_name: { type: 'string' },
          street_type: {
            type: 'string',
            enum: ['Street', 'Avenue', 'Boulevard'],
          },
        },
      },
    };
  },
  computed: {
    schemaAsFile() {
      return JSON.stringify(this.schema, true, 2);
    },
  },
  methods: {
    select(index) {
      this.selected = index;
    },
    selectField() {},
    addField(me, command) {
      const name = 'field-' + me.fields.length;
      me.fields.push(`#/properties/${name}`);
      me.schema.properties[name] = {
        type: 'string',
      };
    },
  },
};
</script>

<style></style>
