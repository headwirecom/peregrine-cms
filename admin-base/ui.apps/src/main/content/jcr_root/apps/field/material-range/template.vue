<template>
  <div v-if="!schema.preview" class="range-field" :class="{'is-empty': !value}">
    <button class="range-btn" @click="onRangeBtnClick">
      <admin-components-icon :icon="icon" :lib="IconLib.MATERIAL_ICONS"/>
      <span v-if="!value" class="strike"></span>
    </button>
    <input
        ref="range"
        type="range"
        class="range"
        v-model="value"
        :id="getFieldID(schema)"
        :class="[schema.fieldClasses, {hidden: !value}]"
        :disabled="schema.disabled || schema.preview"
        :alt="schema.alt"
        :max="schema.max"
        :min="schema.min"
        :name="schema.inputName"
        :required="schema.required"
        :step="schema.step"/>
    <div v-if="!value" class="empty-range">
      <div class="rail" @click="value = 0"></div>
    </div>
    <input
        type="text"
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
        @keypress="onRangeValueKeyPress"
        @paste="onRangeValuePaste"/>
    <!--div class="range-numbers">
      <div class="min">{{ min }}</div>
      <div class="max">{{ max }}</div>
    </div-->
  </div>
  <div v-else>{{ value }}</div>
</template>

<script>
import {IconLib, Toast} from '../../../../../js/constants'

export default {
  mixins: [VueFormGenerator.abstractField],
  data() {
    return {
      IconLib,
      oldValue: null,
      delay: 10,
      timeout: null,
      toast: {
        numeric: null,
        min: null,
        max: null
      }
    }
  },
  computed: {
    min() {
      return this.schema.min || 0
    },
    max() {
      return this.schema.max || 100
    },
    icon() {
      return 'linear_scale'
    }
  },
  watch: {
    value(val) {
      this.model[this.schema.model] = val
    }
  },
  created() {
    Object.keys(this.toast).forEach((key) => {
      this.$watch(`toast.${key}`, (val, old) => {
        if (old) {
          old.remove()
        }
      })
    })
  },
  methods: {
    onRangeBtnClick() {
      if (this.value) {
        this.oldValue = this.value
        this.value = null
      } else {
        this.value = this.oldValue ? this.oldValue : this.min
      }
    },
    onRangeValueKeyPress(event) {
      const pos = event.target.selectionStart
      const allowNegative = this.min < 0 && event.key === '-' && pos === 0
      let num = Number(event.key)

      if ((!allowNegative && isNaN(num)) || event.key === null || event.key === ' ') {
        event.preventDefault()
        this.toast.numeric = $perAdminApp.toast('Only numeric values allowed', Toast.Level.INFO)
      } else {
        if (allowNegative) {
          num = event.key
        }

        let value = event.target.value
        const valueArr = value.split('')

        valueArr.splice(pos, 0, num)
        value = valueArr.join('')

        if (value < this.min) {
          event.preventDefault()
          this.toast.min = $perAdminApp.toast(`Number too low (>= ${this.min})`, Toast.Level.INFO)
        } else if (value > this.max) {
          event.preventDefault()
          this.toast.max = $perAdminApp.toast(`Number too high (<= ${this.max})`, Toast.Level.INFO)
        }
      }
    },
    onRangeValuePaste(event) {
      const pos = event.target.selectionStart
      const num = Number(event.clipboardData.getData('text'))

      if (isNaN(num)) {
        event.preventDefault()
        this.toast.numeric = $perAdminApp.toast('Only numeric values allowed', Toast.Level.INFO)
      }

      let value = event.target.value
      const valueArr = value.split('')

      valueArr.splice(pos, 0, num)
      value = valueArr.join('')

      if (value < this.min) {
        event.preventDefault()
        this.toast.min = $perAdminApp.toast(`Number too low (>= ${this.min})`, Toast.Level.INFO)
      } else if (value > this.max) {
        event.preventDefault()
        this.toast.max = $perAdminApp.toast(`Number too high (<= ${this.max})`, Toast.Level.INFO)
      }
    }
  }
}
</script>
