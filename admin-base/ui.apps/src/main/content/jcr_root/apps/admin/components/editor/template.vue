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
    <div class="editor-panel" ref="editorPanel">
        <div class="editor-panel-content">
            <template v-if="schema !== undefined && dataModel !== undefined">
                <span v-if="title" class="panel-title">{{title}}</span>
                <div v-if="!hasSchema">this component does not have a dialog defined</div>
            <vue-form-generator
                :key="dataModel.path"
                ref="formGenerator"
                v-bind:schema="schema"
                v-bind:model="dataModel"
                v-bind:options="formOptions"/>
            </template>
        </div>
        <div class="editor-panel-buttons">
            <button v-if="!isRootComponent" class="waves-effect waves-light btn btn-raised"
                    v-bind:title="$i18n('deleteComponent')" v-on:click.stop.prevent="onDelete">
                <i class="material-icons">delete</i>
            </button>
            <button class="waves-effect waves-light btn btn-raised" v-bind:title="$i18n('cancel')"
                    v-on:click.stop.prevent="onCancel">
                <i class="material-icons">close</i>
            </button>
            <template>
              <button v-if="hasSchema" class="waves-effect waves-light btn btn-raised"
                      v-bind:title="$i18n('save')" v-on:click.stop.prevent="onOk">
                <i class="material-icons">check</i>
              </button>
              <button v-else class="btn btn-raised disabled" style="opacity: 0">
                <i class="material-icons">check</i>
              </button>
            </template>
        </div>
    </div>
</template>

<script>
import {set} from '../../../../../../js/utils'

export default {
      props: ['model'],
        updated: function() {
            let stateTools = $perAdminApp.getNodeFromViewWithDefault("/state/tools", {});
            stateTools._deleted = {}; // reset to empty?
            if(this.schema && this.schema.hasOwnProperty('groups')) {
                this.hideGroups()
            }
        },
      data() {
        return {
          isTouch: false,
          formOptions: {
            validateAfterLoad: true,
            validateAfterChanged: true,
            focusFirstField: true
          },
          focus: {
            loop: null,
            timeout: null,
            interval: 100,
            delay: 700,
            inView: 0
          }
        }
      },
      computed: {
          view() {
              return $perAdminApp.getView()
          },
        schema: function() {
            var view = $perAdminApp.getView()
            var component = view.state.editor.component
            var schema = view.admin.componentDefinitions[component].model
            return schema
        },
        dataModel: function() {
            var view = $perAdminApp.getView()
            var path = view.state.editor.path
            var model = $perAdminApp.findNodeFromPath(view.pageView.page, path)
            return model
        },
        hasSchema: function() {
            if(this.schema) return true
            return false
        },
        isRootComponent: function() {
            return $perAdminApp.getView().state.editor.path == '/jcr:content'
        },
          title: function() {
              var view = $perAdminApp.getView()
              var componentName = view.state.editor.component.split('-').join('/')
              const components = view.admin.components.data
              for(let i = 0; i < components.length; i++) {
                  const component = components[i]
                  if(component.path.endsWith(componentName) && component.group !== '.hidden') {
                      return component.title
                  }
              }
          }
      },
        watch: {
          'view.state.inline.model'(val) {
              if (!val) return
              this.focusFieldByModel(val)
          }
        },
        mounted(){
            this.isTouch = 'ontouchstart' in window || navigator.maxTouchPoints
            if(this.schema && this.schema.hasOwnProperty('groups')) {
                this.hideGroups()
            }
        },
      methods: {
        onOk(e) {
            let data = JSON.parse(JSON.stringify(this.dataModel));
            let _deleted = $perAdminApp.getNodeFromViewWithDefault("/state/tools/_deleted", {});

            //Merge _deleted child items back into the object that we need to save.
            //Loop through the model for this object/page/asset and find objects that have children
            for ( const key in data) {
                //If data[key] or deleted[key] is an array of objects
                if (( data && Array.isArray(data[key]) && data[key].length && typeof data[key][0] === 'object') ||
                    ( _deleted && Array.isArray(_deleted[key]) && _deleted[key].length && typeof _deleted[key][0] === 'object') ) {

                    let node = data[key];

                    //Use an object to easily merge back in deleted nodes
                    let targetNode = {}
                    //Insert deleted children
                    if(_deleted) {
                        for ( const j in _deleted[key]) {
                            const deleted = _deleted[key][j]
                            targetNode[deleted.name] = deleted;
                        }
                    }
                    //Insert children
                    for ( const i in node ) {
                        const child = node[i]
                        targetNode[child.name] = child;
                    }
                    data[key] = Object.values(targetNode);
                }
            }

            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.stateAction('savePageEdit', { data: data, path: view.state.editor.path } ).then( () => {
              $perAdminApp.action(this, 'unselect')
              $perAdminApp.getNodeFromView("/state/tools")._deleted = {}
            })
        },

        onCancel(e) {
            var view = $perAdminApp.getView()
            $perAdminApp.action(this, 'onEditorExitFullscreen')
            $perAdminApp.action(this, 'unselect')
            $perAdminApp.stateAction('cancelPageEdit', { pagePath: view.pageView.path, path: view.state.editor.path } ).then( () => {
                $perAdminApp.getNodeFromView("/state/tools")._deleted = {}
            })
        },

          onDelete(e) {
              const vm = this;
              var view = $perAdminApp.getView()
              $perAdminApp.askUser('Delete Component?', 'Are you sure you want to delete the component?', {
                  yesText: 'Yes',
                  noText: 'No',
                  yes() {
                      $perAdminApp.action(vm, 'onEditorExitFullscreen')
                      $perAdminApp.stateAction('deletePageNode', { pagePath: view.pageView.path, path: view.state.editor.path } ).then( () => {
                          $perAdminApp.action(vm, 'unselect')
                          $perAdminApp.getNodeFromView("/state/tools")._deleted = {}
                      })
                  },
                  no() {
                  }
              })
          },

        hideGroups() {
            const $groups = $('.vue-form-generator fieldset');
            $groups.each( function(i) {
                const $group = $(this);
                const $title = $group.find('legend');
                $title.click(function(e){
                    $group.find('div').toggle();
                    $group.toggleClass('active');
                })
                if(i !== 0) {
                    $group.find('div').hide();
                    $group.removeClass('active');
                }
                if(i === 0) $group.addClass('active');
                $group.addClass('vfg-group');
            })
        },

        getFieldAndIndexByModel(schema, model) {

          const fields = []
          if(schema.fields) {
            Array.prototype.push.apply(fields, schema.fields)
          }

          if(schema.groups) {
            schema.groups.forEach( (group) => { 
              if(group.fields) {
                Array.prototype.push.apply(fields, group.fields)
              }
            } )
          }


          const formGenerator = this.$refs.formGenerator
          let field
          let index = -1
          fields.some((f) => {
            if (f.visible) {
              if (typeof f.visible === 'string') {
                if (exprEval.Parser.evaluate(f.visible, formGenerator) === true) {
                  index++
                }
              } else if (formGenerator.fieldVisible(f) === true){
                index++
              }
            } else {
              index++
            }
            if (f.model === model) {
              return field = f
            }
          })
          return {field, index}
        },

        focusFieldByModel(model) {
          if (!model) return

          model = model.split('.')
          model.reverse()
          const {field, index} = this.getFieldAndIndexByModel(this.schema, model.pop())
          if (!field) return
          if (['input', 'texteditor', 'material-textarea'].indexOf(field.type) >= 0) {
            const $el = this.$refs.formGenerator.$children[index].$el
            this.openFieldGroup($el)
            this.$nextTick(() => {
              $el.scrollIntoView()
              set(this.view, '/state/inline/rich', this.isRichEditor(field))
            })
          } else if (field.type === 'collection') {
            this.focusCollectionField(model, field, index)
          } else {
            console.warn('Unsupported field type: ', field.type)
          }

          set(this.view, '/state/inline/model', null)
        },

        focusCollectionField(model, field, index) {
          const fieldCollection = this.$refs.formGenerator.$children[index].$children[0]
          this.openFieldGroup(fieldCollection.$el)
          this.$nextTick(() => {
            fieldCollection.activeItem = parseInt(model.pop())
            this.$nextTick(() => {
              const formGen = fieldCollection.$children[0]
              const fieldAndIndex = this.getFieldAndIndexByModel(field, model.pop())
              set(this.view, '/state/inline/rich', this.isRichEditor(fieldAndIndex.field))
              this.clearFocusStuff()
              this.focus.loop = setInterval(() => {
                formGen.$children[fieldAndIndex.index].$el.scrollIntoView()
              }, this.focus.interval)
              this.focus.timeout = setTimeout(() => {
                this.clearFocusStuff()
              }, this.focus.delay)
            })
          })
        },

        clearFocusStuff() {
          this.focus.inView = 0
          clearInterval(this.focus.loop)
          clearTimeout(this.focus.timeout)
        },

        isRichEditor(field) {
          return ['texteditor'].indexOf(field.type) >= 0
        },

        openFieldGroup(el) {
          console.log('openFieldGroup -> el:', el)
          let group = el.parentNode

          while (group.tagName !== 'FIELDSET') {
            group = group.parentNode
          }

          if (!group.classList.contains('active')) {
            group.querySelector('legend').click()
          }
        },
      }
//      ,
//      beforeMount: function() {
//        if(!perAdminView.state.editor) this.$set(perAdminView.state, 'editor', { })
//      }
    }
</script>

