<template>
<div>
    <div v-bind:class="`modal bottom-sheet ${isVisible ? 'visible' : ''}`">
        <div class="modal-content">
            <h4>{{title}}</h4>
            <p>{{message}}</p>
        </div>
        <div class="modal-footer">
            <a 
                v-on:click="onOk"
                href="#!" 
                class="modal-action modal-close waves-effect waves-green btn-flat">
                ok
            </a>
        </div>
    </div>
    <div v-on:click="onOk" v-bind:class="`modal-overlay ${isVisible ? 'visible' : ''}`"></div>
</div>
</template>

<script>
    export default {
        props: ['model'],
        computed: {
            notify() {
                return $perAdminApp.getNodeFromViewOrNull('/state/notification')
            },
            title() {
                let notification = this.notify
                if(notification) {
                    return notification.title
                }
                return ''
            },
            message() {
                let notification = this.notify
                if(notification) {
                    return notification.message
                }
                return ''
            },
            isVisible() {
                let notification = this.notify
                if(notification) {
                    return notification.isVisible
                }
                return false
            },
            onOk() {
                let notification = this.notify
                if(notification) {
                    return notification.onOk
                }
                return false
            }
        }
    }
</script>
