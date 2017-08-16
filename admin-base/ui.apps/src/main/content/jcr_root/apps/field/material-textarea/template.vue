<template>
	<div class="wrapper">
		<div v-if="!schema.preview" class="input-field">
			<textarea 
				ref="textarea"
				class="form-control materialize-textarea" 
				v-model="value" 
				v-bind:style="{minHeight: `${schema.rows}em`, maxHeight: `${schema.rows}em`}"
				:id="getFieldID(schema)" 
				:disabled="disabled" 
				:maxlength="schema.max" 
				:minlength="schema.min" 
				:placeholder="schema.placeholder" 
				:readonly="schema.readonly" 
				:name="schema.inputName">
			</textarea>
		</div>
		<p v-else>{{value}}</p>
	</div>
</template>

<script>	
	export default {
		mixins: [ VueFormGenerator.abstractField ],
		mounted() {
			$(this.$refs.textarea).trigger('autoresize');
		},
		updated() {
			if( document.activeElement != this.$refs.textarea ){
				$(this.$refs.textarea).trigger('autoresize');
			}
		}
	}
</script>