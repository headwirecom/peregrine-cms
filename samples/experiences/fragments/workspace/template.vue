<template>
  <div v-bind:data-per-path="model.path">
    <div v-if="experiences.view === 'layout'">
      <iframe v-bind:src="layoutView"></iframe>
    </div>
    <div v-if="experiences.view === 'component'">
      <ul>
        <li v-for="item in componentList">
          <hr>
          <div class="row">
            <div class="col m12 s12">{{item.path}} {{listExperiences(item)}}</div>
            <div class="col m6 s12">
              <experiences-components-reference v-bind:model="{ ref: item.path }"></experiences-components-reference>
            </div>
            <div class="col m6 s12">{{modelFor(item).path}}
              <vue-form-generator v-bind:model="modelFor(item)"
              v-bind:schema="schemaFor(item)" v-bind:options="{
                                                                validateAfterLoad: true,
                                                                validateAfterChanged: true,
                                                                focusFirstField: true
                                                            }"></vue-form-generator>
            </div>
          </div>
        </li>
      </ul>
    </div>
    <div v-if="editing">
      <admin-components-editor v-bind:model="{ component: 'admin-components-editor' }"></admin-components-editor>
    </div>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        data() {
          return { experiences: $perAdminApp.getNodeFromView('/state/experiences') }
        },
        methods: {
            schemaFor(item) {
                return {
                    fields: [
                        { type: 'input', model: 'text' }
                    ]
                }
            },
            modelFor(item) {
                const experiences = $perAdminApp.getNodeFromView('/state/currentExperiences')
                if(experiences) {
                    if(item.experiences && item.experiences.children) {
                        for(let i = 0; i < item.experiences.children.length; i++) {
                            const expNode = item.experiences.children[i]
                            if(expNode.experiences.join(',') === experiences.join(',')) {
                                return expNode
                            }
                        }
                    }
                }
                return item
            },
            findComponent(node, component, ret) {
                if(node.component && node.component === component) ret.push(node)
                if(node.children) {
                    for(let i = 0; i < node.children.length; i++) {
                        const child = node.children[i]
                        this.findComponent(child, component, ret)
                    }
                }
            },
            componentPathToName(name) {
                const segments = name.split('/')
                return segments.slice(2).join('-')
            },
            listExperiences(item) {
                let ret = []
                if(item.experiences && item.experiences.children) {
                    for(let i = 0; i < item.experiences.children.length; i++) {
                        const exp = item.experiences.children[i].experiences
                        if(exp) {
                            for(let j = 0; j < exp.length; j++) {
                                ret.push(exp[j])
                            }
                        }
                    }
                }
                return ret
            },
            selectComponent(item) {
                $perAdminApp.stateAction('editComponent', item.path)
            }
        },
        computed: {
            layoutView() {
                const experiences = this.experiences.currentExperience
                return $perAdminApp.getNodeFromView('/pageView/path') + '.html' + '#' + experiences
            },
            componentList() {
                const component = this.componentPathToName(this.experiences.currentComponent)
                const page = $perAdminApp.findNodeFromPath($perAdminApp.getView().pageView.page, '/jcr:content/components')
                let ret = []
                this.findComponent(page, component, ret)
                return ret
            },
            editing() {
                return $perAdminApp.getNodeFromView('/state/editor')
            }
        }
    }
</script>

