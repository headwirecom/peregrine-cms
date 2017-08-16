<template>
	<div class="wrap">
		<ul v-if="!schema.preview" :class="schema.inline ? 'radio-list inline' : 'radio-list'" :disabled="disabled" :id="getFieldID(schema)">
		  <li v-for="item in items" :class="isItemChecked(item) ? 'checked' : ''">
		    <input 
		    	class="form-control"
		    	:id="getItemName(item)"
		    	type="radio" 
		    	:class="schema.withGap ? 'with-gap' : ''"
		    	:disabled="disabled" 
		    	:name="id" 
		    	@click="onSelection(item)" 
		    	:value="getItemValue(item)" 
		    	:checked="isItemChecked(item)"/>
		    <label :for="getItemName(item)">{{ getItemName(item) }}</label>
		  </li>
		</ul>
		<template v-else>
			<p v-for="item in items" v-if="isItemChecked(item)">{{ getItemName(item) }}</p>
		</template>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ], 

		computed: {
			items() {
				let values = this.schema.values;
				if (typeof(values) == "function") {
					return values.apply(this, [this.model, this.schema]);
				} else {
					return values;
				}
			},
			id(){
				return this.schema.model;
			}
		},

		methods: {
			getItemValue(item) {
				if (_.isObject(item)){
					if (typeof this.schema["radiosOptions"] !== "undefined" && typeof this.schema["radiosOptions"]["value"] !== "undefined") {
						return item[this.schema.radiosOptions.value];
					} else {
						if (typeof item["value"] !== "undefined") {
							return item.value;
						} else {
							throw "`value` is not defined. If you want to use another key name, add a `value` property under `radiosOptions` in the schema. https://icebob.gitbooks.io/vueformgenerator/content/fields/radios.html#radios-field-with-object-values";
						}
					}
				} else {
					return item;
				}
			},
			getItemName(item) {
				if (_.isObject(item)){
					if (typeof this.schema["radiosOptions"] !== "undefined" && typeof this.schema["radiosOptions"]["name"] !== "undefined") {
						return item[this.schema.radiosOptions.name];
					} else {
						if (typeof item["name"] !== "undefined") {
							return item.name;
						} else {
							throw "`name` is not defined. If you want to use another key name, add a `name` property under `radiosOptions` in the schema. https://icebob.gitbooks.io/vueformgenerator/content/fields/radios.html#radios-field-with-object-values";
						}
					}
				} else {
					return item;
				}
			},
			onSelection(item) {
				this.value = this.getItemValue(item);
			},
			isItemChecked(item) {
				let currentValue = this.getItemValue(item);
				return (currentValue === this.value);
			},
		}
	};
</script>
