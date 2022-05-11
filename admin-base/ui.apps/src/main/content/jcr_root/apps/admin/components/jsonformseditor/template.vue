<template>
  <div class="peregrine-content-view jsonforms-editor">
    <json-forms
        v-bind:data="data"
        v-bind:renderers="renderers"
        v-bind:schema="schema"
        v-bind:uischema="uischema"
        @change="onChange"
    />
  </div>
</template>

<script>
import { JsonForms, JsonFormsChangeEvent } from "@jsonforms/vue2";
import {
  defaultStyles,
  mergeStyles,
  vanillaRenderers
} from "@jsonforms/vue2-vanilla";

// mergeStyles combines all classes from both styles definitions
const myStyles = mergeStyles(defaultStyles, { control: { label: "mylabel" } });

const renderers = [
  ...vanillaRenderers
  // here you can add custom renderers
];

const schema = {
  properties: {
    name: {
      type: "string",
      minLength: 1,
      description: "The task's name"
    },
    description: {
      title: "Long Description",
      type: "string"
    },
    done: {
      type: "boolean"
    },
    dueDate: {
      type: "string",
      format: "date",
      description: "The task's due date"
    },
    rating: {
      type: "integer",
      maximum: 5
    },
    recurrence: {
      type: "string",
      enum: ["Never", "Daily", "Weekly", "Monthly"]
    },
    recurrenceInterval: {
      type: "integer",
      description: "Amount of days until recurrence"
    }
  }
};

const uischema = {
  type: "HorizontalLayout",
  elements: [
    {
      type: "VerticalLayout",
      elements: [
        {
          type: "Control",
          scope: "#/properties/name"
        },
        {
          type: "Control",
          scope: "#/properties/description",
          options: {
            multi: true
          }
        },
        {
          type: "Control",
          scope: "#/properties/done"
        }
      ]
    },
    {
      type: "VerticalLayout",
      elements: [
        {
          type: "Control",
          scope: "#/properties/dueDate"
        },
        {
          type: "Control",
          scope: "#/properties/rating"
        },
        {
          type: "Control",
          scope: "#/properties/recurrence"
        },
        {
          type: "Control",
          scope: "#/properties/recurrenceInterval"
        }
      ]
    }
  ]
};

export default {
  name: "App",
  components: {
    JsonForms
  },
  data() {
    return {
      // freeze renderers for performance gains
      renderers: Object.freeze(renderers),
      data: {
        name: "Send email to Adrian",
        description: "Confirm if you have passed the subject\nHereby ...",
        done: true,
        recurrence: "Daily",
        rating: 3
      },
      schema,
      uischema
    };
  },
  methods: {
    onChange(event) {
      this.data = event.data;
    }
  },
  provide() {
    return {
      styles: myStyles
    };
  }
};
</script>

<style scoped>

</style>
