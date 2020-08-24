<template>
  <div v-if="!schema.preview" class="range-field" :class="{'is-empty': !value}">
    <input
        v-if="value"
        ref="range"
        type="range"
        class="range"
        v-model="value"
        :id="getFieldID(schema)"
        :class="[schema.fieldClasses]"
        :disabled="schema.disabled || schema.preview"
        :alt="schema.alt"
        :max="schema.max"
        :min="schema.min"
        :name="schema.inputName"
        :required="schema.required"
        :step="schema.step"/>
    <div v-else class="empty-range">
      <div class="rail"></div>
    </div>
    <input
        type="number"
        class="range-value"
        v-model="value"
        :id="`${getFieldID(schema)}-input`"
        :disabled="schema.disabled || schema.preview"
        :alt="schema.alt"
        :max="schema.max"
        :min="schema.min"
        :step="schema.step"
        :name="`${getFieldID(schema)}-input`"
        :required="schema.required"
        placeholder="null"
        @keypress="onKeyPress"/>
    <div class="range-numbers">
      <div class="min">{{ min }}</div>
      <div class="max">{{ max }}</div>
    </div>
  </div>
  <div v-else>{{ value }}</div>
</template>

<script>
export default {
  mixins: [VueFormGenerator.abstractField],
  data() {
    return {
      delay: 10,
      timeout: null
    }
  },
  computed: {
    min() {
      return this.schema.min || 0
    },
    max() {
      return this.schema.max || 100
    }
  },
  methods: {
    onKeyPress(event) {
      const oldVal = event.target.value

      if (!oldVal) return

      if (oldVal >= this.max) {
        event.preventDefault()
      }
    }
  }
}
</script>
