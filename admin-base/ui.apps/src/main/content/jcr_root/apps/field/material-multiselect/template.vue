<template>

	<div v-if="!schema.preview">
    <vue-multiselect 
      v-if="!schema.preview"
      v-model="value" 
			v-bind="schema.selectOptions"
      :options="schema.values"
			:multiple="true"
			:clearOnSelect="true"
			@remove="removeOption">
    </vue-multiselect>
	</div>

	<ul v-else class="collection">
		<div v-for="item in value" class="collection-item" v-bind:key="item.path">{{item.name || item}}</div>
	</ul>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ], 
		methods: {
			removeOption: function(removedOption, id) {
				if( removedOption.hasOwnProperty('path') ) {
					let _deleted = $perAdminApp.getNodeFromViewWithDefault("/state/tools/_deleted", {});
					let copy = JSON.parse(JSON.stringify(removedOption));
					copy._opDelete = true;
					if(!_deleted[this.schema.model]) _deleted[this.schema.model] = [];
					_deleted[this.schema.model].push(copy)
				}
			}
		}
	};
</script>
