<template>
<div class="peregrine-content-view">
  <div class="tabs-wrapper horizontal">
    <div class="handles-wrapper">
        <button class="handle" :class="{active: selected === 1}" v-on:click="select(1)">Fields</button>
        <button class="handle" :class="{active: selected === 2}" v-on:click="select(2)">Form</button>
        <button class="handle" :class="{active: selected === 3}" v-on:click="select(3)">Code View</button>
        <div class="handle" style="border: none; background-color: transparent">a sample form to test how this could look like &nbsp; <a href="#">edit</a></div>
    </div>
    <div :class="`content active-tab-index-1`" v-if="selected === 1">
        <ul class="collection">
            <li v-for="field in fields" v-bind:key="field" class="collection-item" draggable="true">
                <admin-components-draghandle/>
                <admin-components-action 
                    v-bind:model="{
                            target: '',
                            command: 'selectField',
                            tooltipTitle: `select`
                        }">
                    <i class="material-icons">folder_open</i>
                    {{field}}
                </admin-components-action>
            </li>
            <li class="collection-item">
                <admin-components-action
                    v-bind:model="{
                        target: '',
                        command: 'addField',
                        tooltipTitle: `${$i18n('add field')}`
                    }">
                        <i class="material-icons">add_circle</i> {{$i18n('add field')}}
                </admin-components-action>
            </li>
        </ul>
    </div>
    <div :class="`content active-tab-index-2`" v-if="selected === 2">
        <ul class="collection">
            <li class="collection-item">
                <admin-components-action
                    v-bind:model="{
                        target: '',
                        command: 'addField',
                        tooltipTitle: `${$i18n('add field')}`
                    }">
                    Categorization
                    <ul class="collection">
                        <li class="collection-item">
                            Category: Hello
                        </li>
                    </ul>
                </admin-components-action>
            </li>
        </ul>
    </div>
    <div :class="`content active-tab-index-3`" v-if="selected === 3">
        <div><codemirror v-model="schemaAsFile"></codemirror></div>
    </div>
  </div>
</div>
</template>

<script>
export default {
    props: ['model'],
    data() {
        return {
            selected: 1,
            fields: [
                '#/properties/number', 
                '#/properties/street_name', 
                '#/properties/street_type'
            ],
            schema: {
                "type": "object",
                "properties": {
                    "number": { "type": "number" },
                    "street_name": { "type": "string" },
                    "street_type": { "type": "string",
                        "enum": ["Street", "Avenue", "Boulevard"]
                    }
                }
            }
        }
    },
    computed: {
        schemaAsFile() {
            return JSON.stringify(this.schema, true, 2)
        }
    },
    methods: {
        select(index) {
            this.selected = index;
        },
        selectField() {

        },
        addField(me, command) {
            const name = "field-"+me.fields.length;
            me.fields.push(`#/properties/${name}`);
            me.schema.properties[name] = {
                "type": "string"
            };            
        }
    }
}
</script>

<style>
</style>