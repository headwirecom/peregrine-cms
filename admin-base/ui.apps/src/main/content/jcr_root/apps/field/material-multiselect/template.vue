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
      :show-labels="false"
			@remove="removeOption">
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
			}
		},

		methods: {
			removeOption: function(removedOption, id) {
				if( "jcr:primaryType"  in removedOption ) {
					let _deleted = $perAdminApp.getNodeFromView("/state/tools/object/_deleted");
					let copy = JSON.parse(JSON.stringify(removedOption));
					copy._opDelete = true;
					if(!_deleted[this.schema.model]) _deleted[this.schema.model] = [];
					_deleted[this.schema.model].push(copy)
				}
			}
		}
	};
</script>
