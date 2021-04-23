<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
  <div class="wrap">
    <label>{{ schema.title }} </label>
    <ul v-if="!schema.preview"
        class="collapsible"
        :class="schema.multifield ? 'multifield' : 'singlefield'"
        ref="collapsible">
      <li v-for="(item, index) in value"
          class="collection-item"
          :class="getItemClass(item, index)"
          :key="`collection-item-${index}`"
          ref="item"
          draggable="false"> {{ item._opDelete }}
        <div
            ref="header"
            class="collapsible-header"
            draggable="true"
            @dragstart="onDragStart(item, index, $event)"
            @dragover.prevent="onDragOver($event, index)"
            @dragenter.prevent="onDragEnter"
            @dragleave.prevent="onDragLeave($event, index)"
            @drop.prevent="onDrop($event, index, item)"
            @click.stop.prevent="onSetActiveItem(index)">
          <i class="material-icons">drag_handle</i>
          <span v-if="schema.multifield">{{ itemName(item, index) }}</span>
          <input
              v-else
              ref="input"
              v-model="value[index]">
          <i class="material-icons delete-icon" @click="onRemoveItem(item, index)">delete</i>
        </div>
        <transition
            @enter="enter"
            @leave="leave"
            :css="false">
          <div v-if="schema.multifield && activeItem === index" class="collapsible-body">
            <vue-form-generator
                :schema="schema"
                :model="prepModel(item, schema)"></vue-form-generator>
          </div>
        </transition>
      </li>
      <button type="button" class="btn-flat btn-add-item" @click="onAddItem">
        <i class="material-icons">add</i>
      </button>
    </ul>
    <ul v-else class="collection">
      <template v-for="(item,i) in value">

        <ul v-if="typeof item === 'object'" class="collection z-depth-1" :key="item.name">
          <vue-form-generator
              v-if="schema.multifield"
              class="collection-item"
              :schema="schema"
              :model="prepModel(item, schema)"></vue-form-generator>
        </ul>

        <li v-else class="collection-item" :key="item+i">{{ item }}</li>

      </template>
    </ul>
  </div>
</template>

<script>
export default {
  mixins: [VueFormGenerator.abstractField],
  beforeMount() {
    if (!this.value) this.value = []
  },
  data() {
    return {
      activeItem: null
    }
  },
  computed: {
    itemModel() {
      const model = {}
      this.schema.fields.forEach((item, index) => {
        model[item.model] = ''
      })
      model.name = 'n' + Date.now()
      return model
    }
  },
  methods: {
    getSchemaForIndex(schema, index) {
      const newSchema = JSON.parse(JSON.stringify(schema))
      newSchema.fields[0].model = '' + index
      return newSchema
    },
    getItemClass(item, index) {
      if (!this.schema.multifield) return
      if (this.activeItem === index) {
        return 'active'
      }
      if (item._opDelete) {
        return 'deleted'
      }
      return false
    },
    itemName(item, index) {
      if (this.schema.fieldLabel) {
        const len = this.schema.fieldLabel.length
        for (let i = 0; i < len; i++) {
          let label = this.schema.fieldLabel[i]
          let childItem = this.value[index]
          if (childItem[label]) {
            return childItem[label]
          }
        }
      }
      return parseInt(index) + 1
    },
    prepModel(model, schema) {
      for (let i = 0; i < schema.fields.length; i++) {
        const field = schema.fields[i].model
        if (!model[field]) {
          Vue.set(model, field, '')
        }
      }
      return model
    },

    onAddItem(e) {
      if (!this.schema.multifield) {
        var newChild = ''
        if (!this.value || this.value === '') {
          Vue.set(this, 'value', new Array())
        }
      } else {
        var newChild = {name: 'n' + Date.now()}
        this.prepModel(newChild, this.schema)
        newChild['sling:resourceType'] = this.schema.resourceType
      }
      if (!this.value) {
        this.value = []
      }
      this.value.push(newChild)
      // Vue.set(this.value, this.value.length -1, newChild)
      this.onSetActiveItem(this.value.length - 1)
      this.$forceUpdate()
    },
    onRemoveItem(item, index) {
      this.value = this.value.filter((item, i) => i !== index)
      if (this.schema.multifield) {
        if (item.hasOwnProperty('path')) {
          let _deleted = $perAdminApp.getNodeFromViewWithDefault('/state/tools/_deleted', {})
          let copy = JSON.parse(JSON.stringify(item))
          copy._opDelete = true
          if (!_deleted[this.schema.model]) _deleted[this.schema.model] = []
          _deleted[this.schema.model].push(copy)
        }
        this.activeItem = null
      }
    },
    onSetActiveItem(index) {
      if (!this.schema.multifield) {
        this.$nextTick(() => {
          this.$nextTick(() => {
            if (this.$refs.input[index]) {
              this.$refs.input[index].focus()
            }
          })
        })
      } else {
        if (index === this.activeItem) {
          $(this.$refs.collapsible).collapsible('close', this.activeItem)
          this.activeItem = null
        } else {
          this.$nextTick(function () {
            if (this.activeItem !== null) {
              $(this.$refs.collapsible).collapsible('close', this.activeItem)
            }
            $(this.$refs.collapsible).collapsible('open', index)
            this.activeItem = index
            // focus first field of expanded item
            this.$nextTick(() => {
              let firstField = this.$refs.collapsible.querySelector('li.active input')
              if (firstField) firstField.focus()
            })
          })
        }
      }
    },
    onDragStart(item, index, ev) {
      const $item = this.$refs.item[index]
      $item.classList.add('dragging')
      ev.dataTransfer.setData('text', index)
    },
    onDragOver(ev, index) {
      const $item = this.$refs.item[index]
      const center = $item.offsetHeight / 2
      $item.classList.toggle('drop-after', ev.offsetY >= center)
      $item.classList.toggle('drop-before', ev.offsetY < center)
    },
    onDragEnter(ev) {
    },
    onDragLeave(ev, index) {
      const $item = this.$refs.item[index]
      $item.classList.remove('drop-after', 'drop-before')
    },
    onDrop(ev, index, item) {
      const oldIndex = parseInt(ev.dataTransfer.getData('text'))
      const $item = this.$refs.item[index]
      if ($item.classList.contains('drop-after')) {
        index += 1
      }
      $item.classList.remove('drop-after', 'drop-before')
      this.$refs.item.forEach((item) => item.classList.remove('dragging'))
      this.onReorder(oldIndex, index)
      this.$nextTick(() => {
        this.onSetActiveItem(index)
      })
    },
    /**
     * https://stackoverflow.com/a/5306832/4622620
     */
    onReorder(oldIndex, newIndex) {
      if (newIndex >= this.length) {
        var k = newIndex - this.length
        while ((k--) + 1) {
          this.value.push(undefined)
        }
      }

      this.value.splice(newIndex, 0, this.value.splice(oldIndex, 1)[0])
      this.$forceUpdate()
    },
    // animations with Velocity.js
    enter: function (el, done) {
      window.Materialize.Vel(el, 'slideDown', {duration: 250})
    },
    leave: function (el, done) {
      window.Materialize.Vel(el, 'slideUp', {duration: 250}, {complete: done})
    }
  }
}
</script>

<style scoped>
.wrap .collapsible .collection-item.dragging {
  transition: opacity .25s linear;
  opacity: .3;
}

.wrap .collapsible .collection-item.drop-before,
.wrap .collapsible .collection-item.drop-after {
  transition: border .1s linear;
}

.wrap .collapsible .collection-item.drop-before {
  border-top: 3px solid var(--pcms-orange);
}

.wrap .collapsible .collection-item.drop-after {
  border-bottom: 3px solid var(--pcms-orange);
}
</style>
