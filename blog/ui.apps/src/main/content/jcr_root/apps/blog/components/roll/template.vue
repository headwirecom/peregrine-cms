<template>
  <div v-bind:data-per-path="model.path">
    <div v-bind:data-per-path="model.path">
      <article v-if="postView">
        <h2>{{currentPost.title}}</h2>{{currentPost.date}} by {{currentPost.author}}
        <div
        v-html="currentPost.lead"></div>
    <div v-html="currentPost.text"></div>
    </article>
    <article v-else="" v-for="post in app.posts['children']">
      <h2>{{post.title}}</h2>
      <div v-html="post.lead"></div>
      <a v-bind:href="'index.html/post//'+post['name']"
      v-on:click.stop.prevent="select(post)">more</a>
    </article>
  </div>
  </div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            app() {
                return $peregrineApp.getView().app
            },
            postView() {
                const app = this.app
                if(app && app.state && app.state.view) {
                    return app.state.view
                }
                return undefined
            },
            currentPost() {
                const posts = this.app.posts['children']
                for(let i = 0; i < posts.length; i++) {
                    if(posts[i]['name'] === this.postView) {
                        return posts[i]
                    }
                }
                return {}
            }
        },
        methods: {
            select(post) {
                alert('post '+post)
            }
        }
    }
</script>

