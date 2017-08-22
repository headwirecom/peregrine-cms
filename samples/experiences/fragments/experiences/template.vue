<template>
  <div>
    <component v-if="experience" v-bind:is="experience.component" v-bind:model="experience"></component>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            experience() {
                const experiences = $peregrineApp.getExperiences()
                let defaultChild = undefined
                for(let i = 0; i < this.model.children.length; i++) {
                    const child = this.model.children[i]
                    const childExperiences = child.experiences
                    for(let j = 0; j < childExperiences.length; j++) {
                        if(experiences.indexOf(childExperiences[j]) >= 0) {
                            return child
                        }
                        if(childExperiences[j].endsWith(':*')) {
                            defaultChild = child
                        }
                    }
                }
                return defaultChild
            }
        }
    }
</script>

