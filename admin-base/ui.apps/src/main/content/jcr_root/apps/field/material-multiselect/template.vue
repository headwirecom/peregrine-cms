<template>

	<div v-if="!schema.preview">
    <vue-multiselect 
      v-if="!schema.preview"
      v-model="value" 
			:multiple="true"
      :track-by="trackBy"
      :label="label"
      :deselectLabel="deselectLabel"
      :options="schema.values"
      :searchable="false"
			:taggable="true" 
      :clear-on-select="false"
      :close-on-select="false"
      :placeholder="placeholder"
      :allow-empty="allowEmpty"
      :show-labels="false">
    </vue-multiselect>
	</div>

	<ul v-else class="collection">
		<div v-for="item in value" class="collection-item">{{item.name || item}}</div>
	</ul>

</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ], 

		computed: {
			placeholder () {
				if(this.schema.selectOptions && this.schema.selectOptions.placeholder){
					return this.schema.selectOptions.placeholder
				}
				return 'Nothing selected'
			},
			allowEmpty () {
				return this.schema.required ? false : true
			},
			trackBy () {
				if(this.schema.selectOptions && this.schema.selectOptions.value){
					return this.schema.selectOptions.value
				}
				return 'value'
			},
			label () {
				if(this.schema.selectOptions && this.schema.selectOptions.name){
					return this.schema.selectOptions.name
				}
				return 'name'
			},
			deselectLabel () {
				if(this.schema.selectOptions && this.schema.selectOptions.deselectLabel){
					return this.schema.selectOptions.deselectLabel
				}
				return ''
			},
			modelFromValue: {
				get () {
					// will catch falsy, null or undefined
					if(this.value && this.value != null){
						// if model is a string, convert to object with name and value
						if(typeof this.value === 'string'){ 
							return this.schema.values.filter(item => item.value === this.value)[0]     
						} else {
							return this.value
						}
					} else {
						return ''
					}
				},
				set (newValue) {
					if(newValue && newValue != null){
						this.value = newValue[this.trackBy]
					} else {
						this.value = ''
					}
				}
			}
		},
		methods: {
			customLabel(label) {
				return typeof label === 'object' ? label.name: label
			}
		}
	};
</script>
