<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
    <div class="nav-content sub-nav" :class="classes">
        <div v-if="isEditPage" class="page-tree">
            <admin-components-materializedropdown
                :below-origin="true"
                :items="pageTree.items">
                <template>
                    {{ currentPage }}<span class="caret-down"></span>
                </template>
                <template slot="header">
                    <admin-components-treeview/>
                </template>
            </admin-components-materializedropdown>
        </div>
        <template v-for="child in model.children">
            <div v-bind:is="child.component" v-bind:model="child" v-bind:key="child.path"></div>
        </template>
        <span v-if="isEditPage" class="center-keeper"></span>
    </div>
</template>

<script>
export default {
    props: ['model'],
    data() {
      return {
          pageTree: {
              items: []
          }
      }
    },
    computed: {
        classes() {
            if(this.model.classes) {
                return this.model.classes
            }
            return 'navright'
        },
        isEditPage() {
            return this.model.classes && this.model.classes.indexOf('navcenter') >= 0
        },
        nodes() {
            return $perAdminApp.getView().admin.nodes
        },
        currentPage() {
            return this.getPath().split('/').pop() || 'loading...'
        }
    },
    watch: {
      nodes(newVal) {
          this.populatePageTree(newVal)
      }
    },
    methods: {
        isEditor: function() {
            return this.$root.$data.adminPage.title === "editor"
        },
        getPath: function(){
            if( this.$root.$data.pageView){
                if( this.$root.$data.pageView.path ){
                    return this.$root.$data.pageView.path;
                } else {
                    return "";
                }
            } else {
                return "";
            }
        },
        getDownloadPath(){
            return this.getPath().split('/').reverse()[0];
        },
        populatePageTree(nodes) {
            if (!this.isEditPage) return

            const tenant = $perAdminApp.getView().state.tenant
            const pageRootNode = $perAdminApp.findNodeFromPath(nodes, tenant.roots.pages)

            if (pageRootNode && pageRootNode.children) {
                pageRootNode.children.forEach((child) => this.crawl(child))
            }
        },
        crawl(node) {
            if (this.pageTree.items.filter((item) => item.path === node.path).length <= 0) {
                const shortName = node.path.split('/');
                shortName.splice(0, 4)
                this.pageTree.items.push({
                    label: shortName.join('/'),
                    icon: 'description',
                    path: node.path,
                    click: () => $perAdminApp.stateAction('editPage', node.path)
                })
            }

            if (node.children) {
                node.children.forEach((child) => this.crawl(child))
            }
        }
    }
}
</script>

