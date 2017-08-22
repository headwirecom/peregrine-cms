<template>
  <div v-bind:data-per-path="model.path">
    <button class="btn" v-on:click="onLayout">layout</button>
    <select v-model="experiences.currentComponent" style="display: inline; width: 200px; color: black;">
      <option value="" disabled>Choose a component</option>
      <option v-for="component in components" v-bind:value="component.path">{{component.name}}</option>
    </select>
    <select v-model="experiences.currentExperience" style="display: inline; width: 200px; color: black;">
      <option value="">none</option>
      <option v-for="item in experienceList" v-bind:value="item">{{item}}</option>
    </select>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        data() {
            const state = $perAdminApp.getNodeFromView('/state')
            if(!state.experiences) {
              state.experiences = {
                  view: 'layout',
                  currentComponent: undefined,
                  currentExperience: undefined
              }
            }
            return { experiences: state.experiences };
        },
        beforeMount() {
          this.$watch('experiences.currentComponent', function(newVal) {
              if(newVal) {
                  this.experiences.view = 'component'
              }
          })

            this.$watch('experiences.currentExperience', function(newVal) {
                if(newVal) {
                    Vue.set($perAdminApp.getNodeFromView('/state'), 'currentExperiences', [newVal])
                }
            })
        },
        unmount() {

        },
        computed: {
          components() {
              let ret = []
              const components = $perAdminApp.getNodeFromView('/admin/components/data')
              for(let i = 0; i < components.length; i++) {
                  if(components[i].path.startsWith('/apps/experiences')) {
                      ret.push(components[i])
                  }
              }
              return ret
          },
          experienceList() {
            return ['lang:de', 'lang:en', 'device:desktop', 'device:mobile']
          }
        },
        methods: {
            onLayout() {
                this.experiences.view = 'layout'
                this.experiences.currentComponent = undefined
            }
        }
    }
</script>

