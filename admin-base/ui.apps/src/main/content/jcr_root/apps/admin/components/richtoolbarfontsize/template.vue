<template>
  <div class="font-size-wrapper btn-group">
    <button class="subtract btn" @click="onSubtract" title="Decrease font size" :disabled="effectiveDisabled">
      <icon icon="remove" :lib="iconLib" />
    </button>

    <div class="inputWrapper" @focusin="onFocusIn" @focusout="onFocusOut">
      <input
        v-bind:value="inputValue"
        @input="onInput"
        @keyup="onInputKeyUp"
        ref="inputRef"
        type="number"
        max="250"
        min="1"
        :disabled="effectiveDisabled"
      />

      <ul>
        <template v-for="sizePreset in fontSizeSelections">
          <li :key="sizePreset" @click="onListSelect(sizePreset)">
            {{ sizePreset }}
          </li>
        </template>
      </ul>
    </div>

    <button class="add btn" @click="onAdd" title="Increase font size" :disabled="effectiveDisabled">
      <icon icon="add" :lib="iconLib" />
    </button>
  </div>
</template>

<script>
import { IconLib } from "../../../../../../js/constants";
import Icon from "../icon/template.vue";

export default {
  name: "FontSizeSelector",
  components: { Icon },
  props: {
    exec: { type: Function },
    iconLib: {
      type: String,
      default: IconLib.MATERIAL_ICONS
    },
    isRangeInEditor: { type: Function },
    getDefaultFontSize: {
      type: Function
    },
    isNodeInEditor: {
      type: Function
    },
    getSelection: {
      type: Function
    },
    getEditorSelection: {
      type: Function
    },
    disabled: {
      type: Boolean,
      default: false
    },
  },

  data() {
    return {
      fontSizeSelections: [ 8, 9, 10, 11, 12, 14, 18, 24, 30, 36, 48, 60, 72, 96 ],
      selectionRange: null,
      inputValue: "",
      inlineListenerInterval: null,
      focused: false,
    };
  },

  computed: {
    effectiveDisabled() {
      return this.disabled && !this.focused
    },
    forInline() {
      if (this.$refs.inputRef.closest('.richtoolbar.on-sub-nav')) {
        return true
      }
      return false
    }
  },

  mounted() {
    document.addEventListener("selectionchange", this.onSelectionChange);

    // this is bad, but it doesn't work with on load/DOMContentLoaded or readystate, the listener just doesn't get triggered for the sub-nav input
    // because it loads before the iframe.
    // this is the most reliable thing I could come up with to circumvent this BS
    document.querySelector("iframe#editview")?.contentDocument.addEventListener("selectionchange", this.onSelectionChange);
    this.inlineListenerInterval = setInterval(() => {
      // Must queryselect each time, it seems the iframe rerenders sometimes causing event listeners to be lost
      document.querySelector("iframe#editview")?.contentDocument.removeEventListener("selectionchange", this.onSelectionChange);
      document.querySelector("iframe#editview")?.contentDocument.addEventListener("selectionchange", this.onSelectionChange);
    }, 1000)
  },
  onBeforeUnmount() {
    document.removeEventListener("selectionchange", this.onSelectionChange);
    document.querySelector("iframe#editview").contentDocument.removeEventListener("selectionchange", this.onSelectionChange);
    clearInterval(this.inlineListenerInterval)
  },

  methods: {

    // sets fontsize of input to that of selected text
    onSelectionChange(event) {
      // skip if no selection is present or input is being focused
      const iframeDoc = document.querySelector("iframe#editview")?.contentDocument
      const currSelection = event.currentTarget.getSelection()
      if (currSelection.rangeCount <= 0) return
      if (event.currentTarget.isEqualNode(iframeDoc)) clearInterval(this.inlineListenerInterval)
      if (document.activeElement.isEqualNode(this.$refs.inputRef)) return;

      const currRange = currSelection.getRangeAt(0);

      if (!this.isRangeInEditor(currRange)) return

      const htmlEl = currRange.startContainer.closest
        ? currRange.startContainer
        : currRange.startContainer.parentElement;

      // checking inline style
      if (htmlEl && this.isNodeInEditor(htmlEl)) {
        const fontSizeParent = htmlEl.closest('[style*="font-size"]');
        const check = this.isNodeInEditor(fontSizeParent) ? fontSizeParent : htmlEl;
        const nr = parseInt(check?.style?.fontSize);
        if (!isNaN(nr) && nr > 0) {
          this.inputValue = nr;
          return;
        }
      }

      // getting style from preview computedStyle
      const defaultFontSize = parseInt(this.getDefaultFontSize());
      if (defaultFontSize && !isNaN(defaultFontSize)) {
        this.inputValue = defaultFontSize;
      }
    },

    applyFontSize() {
      if (!this.inputValue) {
        console.warn("tried to set falsy fontsize");
        return;
      }
      this.exec("updateFontSize", this.inputValue);
    },
    onAdd() {
      let value = this.inputValue;
      value = Number(value);
      if (!value || isNaN(value)) return;
      value += 1;
      this.inputValue = value;
      this.applyFontSize();
    },
    onSubtract() {
      let value = this.inputValue;
      value = Number(value);
      if (!value || isNaN(value)) return;
      value -= 1;
      this.inputValue = value;
      this.applyFontSize();
    },

    onInput(e) {
      let value = e.target.value;
      value = value.replace(/[^\d]/g, "");
      value = Number(value);
      if (!value || isNaN(value)) return;
      this.inputValue = value;
    },

    onInputKeyUp(e) {
      if (e.key === "Enter" || e.key === "Tab") {
        this.$refs.inputRef.parentElement.blur();
        this.$refs.inputRef.blur();
      }
    },

    onListSelect(sizePreset) {
      this.inputValue = sizePreset;
      this.applyFontSize();
      this.$nextTick(() => {
        this.$refs.inputRef.parentElement.blur();
        this.$refs.inputRef.blur();
      });
    },

    // save/load selection between manual inputs
    onFocusOut(e) {
      this.focused = false
      this.exec("restoreSelection");
      if (this.selectionRange) {
        const selection = this.selectionRange.startContainer.ownerDocument.getSelection()
        selection.removeAllRanges();
        selection.addRange(this.selectionRange);
      }
      this.$nextTick(() => {
        this.applyFontSize();
      });
    },
    onFocusIn(e) {
      this.focused = true
      this.exec("saveSelection");
      const range = this.getEditorSelection()
      if (range && this.isRangeInEditor(range)) {
        this.selectionRange = range;
      }
    }
  }
};
</script>

<style scoped>
.font-size-wrapper {
  display: flex;
}

.inputWrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  background-color: white;
  color: black;
  border-bottom: 1px solid var(--pcms-blue-grey);
}

.inputWrapper input {
  padding: 0;
  margin: 0;
  border: none;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  text-align: center;
}

/* Hide the browser's number-input spinner arrows visually
   while keeping keyboard arrow-key increment/decrement intact */
.inputWrapper input::-webkit-outer-spin-button,
.inputWrapper input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.inputWrapper input[type="number"] {
  -moz-appearance: textfield;
}

.inputWrapper ul {
  position: absolute;
  display: flex;
  flex-direction: column;
  z-index: 1;
  top: 100%;
  left: 0;
  width: 100%;
  visibility: collapse;
  background-color: white;
  border: 1px solid var(--pcms-gray);

  transition: visibility 0.3s;
}

.inputWrapper ul,
.inputWrapper ul li {
  padding: 2px;
  margin: 0;
  list-style: none;
}
.inputWrapper ul li {
  cursor: pointer;
}

.inputWrapper:focus-within ul {
  visibility: visible;
}
</style>
