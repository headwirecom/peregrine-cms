<template>
    <div v-bind:id="name" class="carousel slide" data-ride="carousel" v-bind:data-per-path="model.path" style="background-color: silver;">
        <ol class="carousel-indicators">
            <li v-for="(item, key) in model.children" v-bind:data-target="'#'+item.name" v-bind:data-slide-to="key" v-bind:class="key === 0 ? 'active': ''"></li>
        </ol>
        <div class="carousel-inner" role="listbox">
            <div v-for="(item, key) in model.children" class="carousel-item" v-bind:class="key === 0 ? 'active': ''">
                <img class="d-block img-fluid" v-bind:src="item.imagePath" v-bind:alt="item.alt">
                <div v-if="item.heading || item.text" class="carousel-caption d-none d-md-block">
                    <h3 v-if="item.heading">{{item.heading}}</h3>
                    <p v-if="item.text" v-html="item.text"></p>
                </div>
            </div>
        </div>
        <a class="carousel-control-prev" v-bind:href="'#'+name" role="button" data-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="sr-only">Previous</span>
        </a>
        <a class="carousel-control-next" v-bind:href="'#'+name" role="button" data-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="sr-only">Next</span>
        </a>
    </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            name() {
                return this.model.path.split('/').slice(1).join('-').slice(4)
            }
        },
        methods: {
            augmentEditorSchema(form) {
                form.fields[0].buttons[0].onclick = function(model) {
                    if(!model.children) {
                        window.parent.$perAdminApp.getApp().$set(model, 'children', [])
                    }
                    window.parent.$perAdminApp.getApp().$set(model.children, model.children.length, { name: 'child'+model.children.length})
                }
                form.fields[0].buttons[1].onclick = function(model) {
                    window.parent.$perAdminApp.getApp().$set(model, 'children', model.children.slice(1))
                    model.selection = {}
                }
                form.fields[0].values = function(model, schema) {
                    if(!model.children) {
                        window.parent.$perAdminApp.getApp().$set(model, 'children', [])
                    }
                    return model.children
                }
                form.fields[1].visible = function(model) { return model.selection}
                form.fields[2].visible = function(model) { return model.selection}
                form.fields[3].visible = function(model) { return model.selection}
                form.fields[4].visible = function(model) { return model.selection}
                form.fields[5].visible = function(model) { return model.selection}
                return form
            },
            beforeSave(data) {
                delete data.selection
                while(data.children.length > 0) {
                    let name = data.children[0].name
                    if(data.children[0].path) {
                        name = data.children[0].path.split('/').pop()
                    }
                    delete data.children[0].path
                    delete data.children[0].component
                    data.children[0]['sling:resourceType'] = 'example/components/carouselItem'
                    data[name] = data.children[0]
                    data.children = data.children.slice(1)
                }
                return data
            }
        }
    }
</script>
