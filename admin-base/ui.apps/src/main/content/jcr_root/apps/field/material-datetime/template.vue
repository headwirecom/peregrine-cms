<template>
		<div>
			<datetime v-if="!schema.preview && !schema.readonly" v-model="sanitizedValue" type="datetime" :placeholder="$i18n('selectDatetime')"></datetime>
			<div v-else>{{preview}}</div>
		</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		computed: {
			preview() {
				if(!this.value) return ''
				return new Date(this.value).toLocaleString()
			},
			sanitizedValue: {
				get () {
      		return this.value ? this.value : ''
				},
				set (newValue) {
					this.value = newValue
				}
			}
		}
	}
</script>
