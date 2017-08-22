<template>
  <div v-bind:data-per-path="model.path">{{text}}</div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            experience() {
                const experiences = window['$peregrineApp'] ? $peregrineApp.getExperiences() : $perAdminApp.getExperiences()
                if(this.model.experiences) {
                    for (let i = 0; i < this.model.experiences.children.length; i++) {
                        const experienceNode = this.model.experiences.children[i]
                        for(let j = 0; j < experienceNode.experiences.length; j++) {
                            if(experiences.indexOf(experienceNode.experiences[j]) >= 0) {
                                return experienceNode
                            }
                        }
                    }
                }
                return undefined

            },
            text() {
                const experience = this.experience
                if(experience) {
                    if(experience.text) return experience.text
                }
                return this.model.text
            }
        }
    }
</script>

